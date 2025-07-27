//package com.example.distributedsystemsapp.backend.src;
//
//import java.io.*;
//import java.net.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static java.awt.geom.Point2D.distance;
//
//public class WorkerHandler extends Thread {
//    private final Socket socket;
//    private final String workerId;
//
//    public WorkerHandler(Socket socket, String workerId) {
//        this.socket = socket;
//        this.workerId = workerId;
//    }
//
//    @Override
//    public void run() {
//        System.out.println("Handling new client...");
//        System.out.println("[DEBUG] WorkerHandler thread started for ID: " + workerId);
//
//        try (
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
//        ) {
//            out.flush();
//
//            // When connected by Master, immediately send HELLO
//            Shared.HelloData helloData = new Shared.HelloData(workerId);
//            Shared.Request helloRequest = new Shared.Request(Shared.RequestType.HELLO, helloData);
//
//            synchronized (out) {
//                out.writeObject(helloRequest);
//                out.flush();
//            }
//
//            while (true) {
//                Object input;
//                try {
//                    input = in.readObject();
//                } catch (EOFException e) {
//                    System.out.println("[DEBUG] Connection closed by remote peer.");
//                    break;
//                }
//
//                System.out.println("[DEBUG] Worker received request: " + input);
//
//                Object finalInput = input;
//                new Thread(() -> {
//                    try {
//                        handleSingleRequest(finalInput, out);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleSingleRequest(Object input, ObjectOutputStream out) throws IOException, ClassNotFoundException {
//
//        Shared.SystemConfig config;
//        try {
//            config = Shared.loadSystemConfig("backend/src/inputs/system_config.json");
//        } catch (IOException e) {
//            System.out.println("[WorkerHandler] Failed to load system config: " + e.getMessage());
//            return;
//        }
//
//        if (!(input instanceof Shared.Request)) {
//                    out.writeObject("Invalid request type.");
//                    out.flush();
//                    return;
//                }
//
//                Shared.Request req = (Shared.Request) input;
//                Shared.RequestType type = req.getType();
//                Object payload = req.getPayload();
//                //Manager Queries
//                switch (type) {
//                    case ADD_STORE:
//                        Shared.Store store = (Shared.Store) payload;
//                        synchronized (Worker.storeMap) {
//                            Worker.storeMap.put(store.storeName.toLowerCase(), store);
//                        }
//                        System.out.println("[Worker] The store " + store.storeName +
//                                " was added successfully, its price category is : " + store.priceCategory);
//                        synchronized (out) {
//                            out.writeObject("Store added: " + store.storeName +
//                                    " (price category: " + store.priceCategory + ")");
//                            out.flush();
//                        }
//                        break;
//
//                    case QUERY_SALES_BY_CATEGORY:
//                        Map<String, Integer> catResult = new HashMap<>();
//                        int catTotal = 0;
//                        synchronized (Worker.storeMap) {
//                            for (Object obj : Worker.storeMap.values()) {
//                                Shared.Store s = (Shared.Store) obj;
//                                int storeTotal = 0;
//                                for (Shared.Product p : s.products) {
//                                    storeTotal += p.getTotalSold();
//                                }
//                                if (storeTotal > 0) {
//                                    catResult.put(s.storeName.toLowerCase(), storeTotal);
//                                    catTotal += storeTotal;
//                                }
//                            }
//                        }
//                        catResult.put("total", catTotal);
//
//                        try (Socket reducerSocket = new Socket(config.reducerHost, config.reducerPort);
//                             ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())) {
//                            reducerOut.writeObject(catResult);
//                            reducerOut.flush();
//                        }
//
//                        break;
//
//
//                    case QUERY_SALES_BY_PRODUCT:
//                        Shared.ProductCategoryQuery pq = (Shared.ProductCategoryQuery) payload;
//                        Map<String, Integer> prodResult = new HashMap<>();
//                        int prodTotal = 0;
//                        synchronized (Worker.storeMap) {
//                            for (Object obj : Worker.storeMap.values()) {
//                                Shared.Store s = (Shared.Store) obj;
//                                int storeTotal = 0;
//                                for (Shared.Product p : s.products) {
//                                    if (p.productType.equalsIgnoreCase(pq.productType)) {
//                                        storeTotal += p.getTotalSold();
//                                    }
//                                }
//                                if (storeTotal > 0) {
//                                    prodResult.put(s.storeName.toLowerCase(), storeTotal);
//                                    prodTotal += storeTotal;
//                                }
//                            }
//                        }
//                        prodResult.put("total", prodTotal);
//
//                        // ΕΔΩ στέλνεις στο Reducer, όχι στον Master:
//                        try (Socket reducerSocket = new Socket(config.reducerHost, config.reducerPort);
//                             ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream())) {
//                            reducerOut.writeObject(prodResult);
//                            reducerOut.flush();
//                        }
//                        break;
//
//
//                    case ADD_PRODUCT:
//                        Shared.AddProductData ap = (Shared.AddProductData) payload;
//                        Shared.Store addStore = (Shared.Store) Worker.storeMap.get(ap.storeName.toLowerCase());
//                        if (addStore == null) {
//                            synchronized (out) {
//                                out.writeObject("Store not found.");
//                                out.flush();
//                            }
//                            return;
//                        } else {
//                            if (ap.product.availableAmount < 0 || ap.product.price <= 0) {
//                                synchronized (out) {
//                                    out.writeObject("Error: Invalid amount or price.");
//                                    out.flush();
//                                }
//                                return;
//                            }
//                            boolean exists = false;
//                            for (Shared.Product p : addStore.products) {
//                                if (p.productName.equalsIgnoreCase(ap.product.productName)) {
//                                    exists = true;
//                                    break;
//                                }
//                            }
//                            if (exists) {
//                                synchronized (out) {
//                                    out.writeObject("Product already exists.");
//                                    out.flush();
//                                }
//                            } else {
//                                addStore.products.add(ap.product);
//                                addStore.recalculatePriceCategory();
//                                System.out.println("[Worker] After the product addition, the new price category of " +
//                                        addStore.storeName + " is: " + addStore.priceCategory);
//                                synchronized (out) {
//                                    out.writeObject("Product added to store: " + ap.product.productName +
//                                            ". New price category: " + addStore.priceCategory);
//                                    out.flush();
//                                }
//                            }
//                        }
//                        break;
//
//                    case REMOVE_PRODUCT:
//                        Shared.RemoveProductData rp = (Shared.RemoveProductData) payload;
//                        Shared.Store rmStore = (Shared.Store) Worker.storeMap.get(rp.storeName.toLowerCase());
//                        if (rmStore == null) {
//                            synchronized (out) {
//                                out.writeObject("Store not found.");
//                                out.flush();
//                            }
//                            return;
//                        } else {
//                            boolean found = false;
//                            for (Shared.Product p : rmStore.products) {
//                                if (p.productName.equalsIgnoreCase(rp.productName)) {
//                                    p.visible = false;
//                                    rmStore.recalculatePriceCategory();
//                                    found = true;
//                                    break;
//                                }
//                            }
//                            if (found) {
//                                System.out.println("[Worker] After the product removal, the new price category of " +
//                                        rmStore.storeName + " is: " + rmStore.priceCategory);
//                                synchronized (out) {
//                                    out.writeObject("Product '" + rp.productName + "' hidden from customers." +
//                                            " New price category: " + rmStore.priceCategory);
//                                    out.flush();
//                                }
//
//                            } else {
//                                synchronized (out) {
//                                    out.writeObject("Product not found.");
//                                    out.flush();
//                                }
//                                return;
//                            }
//                        }
//                        break;
//
//                    case UPDATE_STOCK:
//                        Shared.StockUpdateData sd = (Shared.StockUpdateData) payload;
//                        Shared.Store stStore = Worker.storeMap.values().stream()
//                                .filter(s -> s.storeName.equalsIgnoreCase(sd.storeName))
//                                .findFirst().orElse(null);
//
//                        if (stStore == null) {
//                            synchronized (out) {
//                                out.writeObject("Store not found.");
//                                out.flush();
//                            }
//                            return;
//                        }
//
//
//                        Shared.Product foundProduct = stStore.products.stream()
//                                .filter(p -> p.productName.equalsIgnoreCase(sd.productName))
//                                .findFirst().orElse(null);
//                        if (foundProduct == null) {
//                            synchronized (out) {
//                                out.writeObject("Product not found.");
//                                out.flush();
//                            }
//                            return;
//                        }
//
//                        String response;
//                        synchronized (foundProduct) {
//                            if (sd.delta == 0) {
//                                response = "No change in stock.";
//                            } else if (sd.delta < 0 && foundProduct.availableAmount + sd.delta < 0) {
//                                response = "Insufficient stock. Available: " + foundProduct.availableAmount;
//                            } else {
//                                foundProduct.availableAmount += sd.delta;
//                                foundProduct.notifyAll(); // notify waiting clients
//                                response = "Stock updated. New amount: " + foundProduct.availableAmount;
//                            }
//                        }
//                        synchronized (out) {
//                            out.writeObject(response);
//                            out.flush();
//                        }
//                        break;
//
//                    //Client Queries
//                    case SEARCH_STORES:
//                        Shared.SearchFilters filters = (Shared.SearchFilters) payload;
//                        List<Shared.Store> results = new ArrayList<>();
//
//                        synchronized (Worker.storeMap) {
//                            for (Shared.Store s : Worker.storeMap.values()) {
//                                double dist = Shared.distance(filters.clientLat, filters.clientLon, s.latitude, s.longitude);
//
//                                boolean matchesCategory = filters.categories.contains(s.foodCategory);
//                                boolean matchesStars = s.stars >= filters.minStars;
//
//                                // Convert the store's enum priceCategory into a string representation
//                                //and compare it to the filter provided by the client.
//                                String expected = switch (s.priceCategory) {
//                                    case DOLLAR -> "$";
//                                    case DOUBLE_DOLLAR -> "$$";
//                                    case TRIPLE_DOLLAR -> "$$$";
//                                };
//                                boolean matchesPrice = expected.equals(filters.priceLevel);
//
//                                if (dist <= 5 && matchesCategory && matchesStars && matchesPrice) {
//                                    results.add(s);
//                                }
//                                System.out.println("[Worker] Checking store: " + s.storeName + ", dist=" + dist + ", matchesCategory=" + matchesCategory + ", matchesStars=" + matchesStars + ", matchesPrice=" + matchesPrice);
//
//                            }
//                        }
//                        synchronized (out) {
//                            out.writeObject(results);
//                            out.flush();
//                        }
//                        break;
//                    case RATE_STORE:
//                        Shared.RatingData ratingData = (Shared.RatingData) payload;
//                        Shared.Store targetStore = Worker.storeMap.get(ratingData.storeName.toLowerCase());
//
//                        if (targetStore == null) {
//                            synchronized (out) {
//                                out.writeObject("Store not found.");
//                                out.flush();
//                            }
//                            return;
//                        }
//
//                        synchronized (targetStore) {
//                            double newStars = (targetStore.stars * targetStore.noOfVotes + ratingData.stars) / (targetStore.noOfVotes + 1);
//                            targetStore.noOfVotes++;
//                            targetStore.stars = newStars;
//                            synchronized (out) {
//                                out.writeObject("Rating submitted successfully. New rating: " + newStars);
//                                out.flush();
//                            }
//                        }
//                        break;
//
//                    case BUY_FROM_SEARCH:
//                        Shared.BuyData buySearch = (Shared.BuyData) payload;
//                        targetStore = Worker.storeMap.get(buySearch.storeName.toLowerCase());
//
//                        if (targetStore == null) {
//                            synchronized (out) {
//                                out.writeObject("Store not found.");
//                                out.flush();
//                            }
//                            return;
//                        }
//
//                        Shared.Product prod = null;
//                        for (Shared.Product p : targetStore.products) {
//                            if (p.productName.equalsIgnoreCase(buySearch.productName)) {
//                                prod = p;
//                                break;
//                            }
//                        }
//
//                        if (prod == null) {
//                            synchronized (out) {
//                                out.writeObject("Product not found.");
//                                out.flush();
//                            }
//                            return;
//                        }
//
//                        synchronized (prod) {
//                            //Send error message only once
//                            if (prod.availableAmount < buySearch.amount) {
//                                synchronized (out) {
//                                    out.writeObject("Waiting for stock...");
//                                    out.flush();
//                                }
//                                System.out.println("[Worker] Not enough stock for " + buySearch.productName + ". Client is waiting...");
//                            }
//                            while (prod.availableAmount < buySearch.amount) {
//                                try {
//                                    prod.wait();
//                                } catch (InterruptedException e) {
//                                    synchronized (out) {
//                                        out.writeObject("Interrupted while waiting.");
//                                        out.flush();
//                                    }
//                                    return;
//                                }
//                            }
//
//                            //Sufficient stock
//                            prod.availableAmount -= buySearch.amount;
//                            prod.sell(buySearch.amount);
//                            double price = buySearch.amount * prod.price;
//                            synchronized (out) {
//                                out.writeObject("Client sale: Paid €" + price);
//                                out.flush();
//                            }
//                        }
//                        break;
//
//                    // Used in active replication to ensure replica workers stay updated.
//                    // This request contains the latest state of a store and overwrites the local copy.
//                    // Τhe Manager never sees this request.
//                    case SYNC_STORE_STATE:
//                        Shared.StoreSyncPayload syncData = (Shared.StoreSyncPayload) payload;
//                        System.out.println("[DEBUG] SYNC_STORE_STATE received for store: " + syncData.storeName);
//                        System.out.println("[DEBUG] Current storeMap keys before sync: " + Worker.storeMap.keySet());
//                        synchronized (Worker.storeMap) {
//                            Worker.storeMap.put(syncData.storeName, syncData.updatedStore);
//                        }
//                        synchronized (out) {
//                            out.writeObject("Store state synced: " + syncData.storeName);
//                            out.flush();
//                        }
//                        System.out.println("[Worker] Store '" + syncData.storeName + "' synchronized from Master.");
//                        break;
//
//                    // The Master requests the current state of a specific store (by name).
//                    // Used in active replication to fetch the store from the primary worker,
//                    // so that the updated state can be pushed to the replica.
//                    case GET_STORE_STATE:
//                        String requestedStore = (String) payload;
//                        Shared.Store storeToSend = Worker.storeMap.get(requestedStore.toLowerCase());
//
//                        if (storeToSend != null) {
//                            synchronized (out) {
//                                out.writeObject(storeToSend);
//                                out.flush();
//                            }
//                        } else {
//                            synchronized (out) {
//                                out.writeObject("Store not found.");
//                                out.flush();
//                            }
//                        }
//                        break;
//
//                    //The Master wants to check beforehand if a store exists
//                    case CHECK_STORE_EXISTS:
//                        String storeToCheck = (String) payload;
//                        boolean exists;
//                        synchronized (Worker.storeMap) {
//                            exists = Worker.storeMap.keySet().stream()
//                                    .anyMatch(s -> s.equalsIgnoreCase(storeToCheck));
//                        }
//                        synchronized (out) {
//                            out.writeObject(exists ? "yes" : "no");
//                            out.flush();
//                        }
//                        break;
//
//                    default:
//                        System.out.println("[Worker] Unknown request type received: " + type);
//                        synchronized (out) {
//                            out.writeObject("Unknown request type.");
//                            out.flush();
//                        }
//                }
//    }
//}