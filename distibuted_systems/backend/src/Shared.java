package com.example.distributedsystemsapp.backend.src;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.Gson;


public class Shared {

    // ===== ENUMS =====
    public enum PriceCategory {
        DOLLAR {
            @Override
            public String toString() {
                return "$";
            }
        },
        DOUBLE_DOLLAR {
            @Override
            public String toString() {
                return "$$";
            }
        },
        TRIPLE_DOLLAR {
            @Override
            public String toString() {
                return "$$$";
            }
        };
    }

    public enum FoodCategory {
        PIZZERIA, SUSHI, BURGER, SALAD, MEXICAN, OTHER
    }

    public enum RequestType {
        HELLO,//Used from a Worker when connected
        ADD_STORE,
        ADD_PRODUCT,
        REMOVE_PRODUCT,
        UPDATE_STOCK,
        QUERY_SALES_BY_CATEGORY,
        QUERY_SALES_BY_PRODUCT,
        SEARCH_STORES,// client requests
        BUY_FROM_SEARCH,
        RATE_STORE,
        SYNC_STORE_STATE, // Used by Master to send full updated Store to replica
        GET_STORE_STATE, // Used internally by the Master to fetch the full store object from a primary Worker, for synchronization purposes.
        CHECK_STORE_EXISTS//Used internally by Master to check if a store's name exists
        }

    // ===== CORE OBJECTS =====
    public static class Product implements Serializable {
        public final String productName;
        public final String productType;
        public int availableAmount;
        public final double price;
        private int totalSold = 0;
        public boolean visible = true;

        public Product(String name, String type, int amount, double price) {
            this.productName = name;
            this.productType = type;
            this.availableAmount = amount;
            this.price = price;
        }

        public synchronized void sell(int quantity) {
            this.totalSold += quantity;
        }

        public synchronized int getTotalSold() {
            return totalSold;
        }

        @Override
        public String toString() {
            return productName + " (" + productType + ") - " + price + "€, stock: " + availableAmount + ", sold: " + totalSold;
        }
    }

    public static class Store implements Serializable {
        public final String storeName;
        public final double latitude;
        public final double longitude;
        public final FoodCategory foodCategory;
        public double stars;
        public int noOfVotes;
        public String storeLogoPath;
        public List<Product> products;
        public PriceCategory priceCategory;

        public Store(String storeName, double latitude, double longitude,
                     FoodCategory foodCategory, int stars, int noOfVotes,
                     String storeLogoPath, List<Product> products) {
            this.storeName = storeName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.foodCategory = foodCategory;
            this.stars = stars;
            this.noOfVotes = noOfVotes;
            this.storeLogoPath = storeLogoPath;
            this.products = products;
            this.priceCategory = calculatePriceCategory();
        }

        private PriceCategory calculatePriceCategory() {
            if (products == null || products.isEmpty()) return PriceCategory.DOLLAR;
            double sum = 0;
            for (Product p : products) {
                if (p.visible) {
                    sum += p.price;
                }
            }
            double avg = sum / products.size();
            if (avg <= 5) return PriceCategory.DOLLAR;
            else if (avg <= 15) return PriceCategory.DOUBLE_DOLLAR;
            else return PriceCategory.TRIPLE_DOLLAR;
        }

        public void recalculatePriceCategory(){
            this.priceCategory = calculatePriceCategory();
        }
        @Override
        public String toString() {
            return storeName + " [" + foodCategory + ", " + priceCategory + ", ★" + stars + "]";
        }
    }
    //Used for synchronizing Store objects
    public static class StoreSyncPayload implements Serializable {
        public String storeName;
        public Store updatedStore;

        public StoreSyncPayload(String storeName, Store updatedStore) {
            this.storeName = storeName;
            this.updatedStore = updatedStore;
        }
    }

    public static class HelloData implements Serializable {
        public String workerAddress;

        public HelloData(String workerAddress) {
            this.workerAddress = workerAddress;
        }
    }

    public static class JSONLoader {
        public static Store loadStoreFromJson(String path) throws IOException {
            Gson gson = new Gson();
            Reader reader = new FileReader(path);
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            String storeName = json.get("StoreName").getAsString();
            double lat = json.get("Latitude").getAsDouble();
            double lon = json.get("Longitude").getAsDouble();
            String cat = json.get("FoodCategory").getAsString();
            int stars = json.get("Stars").getAsInt();
            int votes = json.get("NoOfVotes").getAsInt();
            String logo = json.get("StoreLogo").getAsString();

            JsonArray productArray = json.getAsJsonArray("Products");
            List<Product> products = new ArrayList<>();

            for (JsonElement el : productArray) {
                JsonObject obj = el.getAsJsonObject();
                String pname = obj.get("ProductName").getAsString();
                String ptype = obj.get("ProductType").getAsString();
                int amount = obj.get("Available Amount").getAsInt();
                double price = obj.get("Price").getAsDouble();
                products.add(new Product(pname, ptype, amount, price));
            }

            return new Store(storeName, lat, lon,
                    FoodCategory.valueOf(cat.toUpperCase()), stars, votes, logo, products);
        }
    }

    // ===== GENERIC REQUEST WRAPPER =====
    public static class Request implements Serializable {
        private RequestType type;
        private Object payload;

        public Request(RequestType type, Object payload) {
            this.type = type;
            this.payload = payload;
        }

        public RequestType getType() {
            return type;
        }

        public Object getPayload() {
            return payload;
        }
    }

    // ===== PAYLOAD STRUCTURES =====
    public static class BuyData implements Serializable {
        public String storeName;
        public String productName;
        public int amount;

        public BuyData(String storeName, String productName, int amount) {
            this.storeName = storeName;
            this.productName = productName;
            this.amount = amount;
        }
    }

    public static class StockUpdateData implements Serializable {
        public String storeName;
        public String productName;
        public int delta;

        public StockUpdateData(String storeName, String productName, int delta) {
            this.storeName = storeName;
            this.productName = productName;
            this.delta = delta;
        }
    }

    public static class RemoveProductData implements Serializable {
        public String storeName;
        public String productName;

        public RemoveProductData(String storeName, String productName) {
            this.storeName = storeName;
            this.productName = productName;
        }
    }

    public static class AddProductData implements Serializable {
        public String storeName;
        public Product product;

        public AddProductData(String storeName, Product product) {
            this.storeName = storeName;
            this.product = product;
        }
    }

    public static class ProductCategoryQuery implements Serializable {
        public String productType;

        public ProductCategoryQuery(String productType) {
            this.productType = productType.toLowerCase();
        }
    }

    public static class FoodCategoryQuery implements Serializable {
        // no fields needed, just a flag type
    }

    public static class SearchFilters implements Serializable {// client
        public double clientLat, clientLon;
        public List<FoodCategory> categories;
        public int minStars;
        public String priceLevel; // $, $$, $$$

        public SearchFilters(double lat, double lon, List<FoodCategory> categories, int minStars, String priceLevel) {
            this.clientLat = lat;
            this.clientLon = lon;
            this.categories = categories;
            this.minStars = minStars;
            this.priceLevel = priceLevel;
        }
    }

    public static class RatingData implements Serializable { // client
        public String storeName;
        public int stars; // 1–5

        public RatingData(String storeName, int stars) {
            this.storeName = storeName;
            this.stars = stars;
        }
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static class SystemConfig {
        public String masterHost;
        public String reducerHost;
        public int masterPort;
        public int reconnectPort;
        public int reducerPort;
        public int reducerToMasterPort;
        public List<WorkerInfo> workers;

        public static class WorkerInfo {
            public String host;
            public int port;
        }
    }

    public static SystemConfig loadSystemConfig(String path) throws IOException {
        Gson gson = new Gson();
        Reader reader = new FileReader(path);
        return gson.fromJson(reader, SystemConfig.class);
    }

}