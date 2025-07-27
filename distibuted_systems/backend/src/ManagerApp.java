package com.example.distributedsystemsapp.backend.src;

import java.io.*;
import java.net.*;
import java.util.*;

public class ManagerApp {
    public static void main(String[] args) {
        Shared.SystemConfig config;
        try {
            config = Shared.loadSystemConfig("backend/src/inputs/system_config.json");
        } catch (IOException e) {
            System.out.println("[Manager] Failed to load system config: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);

        try (
                Socket socket = new Socket(config.masterHost, config.masterPort);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            System.out.println("[Manager] Successful connection with Master.");

            while (true) {
                System.out.println("\n[Manager Menu]");
                System.out.println("1. Add a Store");
                System.out.println("2. Query Sales by Store Category");
                System.out.println("3. Query Sales by Product Type");
                System.out.println("4. Update Product Availability");
                System.out.println("5. Add Product to Store");
                System.out.println("6. Hide Product from Store");
                System.out.println("7. Exit");
                System.out.print("Select an option: ");

                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid Choice.");
                    continue;
                }

                switch (choice) {
                    case 1 -> {
                        // Prefix για όλα τα αρχεία JSON
                        String basePath = "backend/src/stores/";

                        System.out.print("Enter JSON filename (only name, e.g., store1.json): ");
                        String filename = scanner.nextLine().trim();

                        String path = basePath + filename;

                        if (path.isEmpty()) {
                            System.out.println("[Error] Path cannot be empty.");
                            break;
                        }
                        try {
                            Shared.Store store = Shared.JSONLoader.loadStoreFromJson(path);
                            Shared.Request req = new Shared.Request(Shared.RequestType.ADD_STORE, store);

                            synchronized (out) {
                                out.writeObject(req);
                                out.flush();
                            }

                            Object response = in.readObject();
                            System.out.println("[Master Response] " + response);
                        } catch (Exception e) {
                            System.out.println("[Error] Failed to send request: " + e.getMessage());
                        }
                    }
                    case 2 -> {
                        System.out.print("Enter store category: ");
                        String category = scanner.nextLine().trim();

                        if (category.isEmpty()) {
                            System.out.println("[Error] Store category cannot be empty.");
                            break;
                        }

                        try {
                            Shared.FoodCategory.valueOf(category.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.out.println("[Error] Such store category does not exist.");
                            break;
                        }

                        Shared.Request req = new Shared.Request(
                                Shared.RequestType.QUERY_SALES_BY_CATEGORY,
                                new Shared.FoodCategoryQuery()//might need to alter this method
                        );

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }

                        Object response = in.readObject();
                        if (response instanceof Map<?, ?> map) {
                            if (map.size() <= 1 && map.containsKey("total")) {
                                System.out.println("[Manager] No sales recorded yet for this store category.");
                            }
                            else{
                                System.out.println("\n[Total Sales by Store Category]");
                                for (Map.Entry<?, ?> entry : map.entrySet()) {
                                    System.out.println(entry.getKey() + ": " + entry.getValue());
                                }
                            }
                        } else {
                            System.out.println("[Warning] Unexpected response type received: " + response.getClass().getName());
                        }
                    }
                    case 3 -> {
                        System.out.print("Enter product type (e.g., salad): ");
                        String type = scanner.nextLine().trim();

                        if (type.isEmpty()) {
                            System.out.println("[Error] Product type cannot be empty.");
                            break;
                        }

                        Shared.Request req = new Shared.Request(
                                Shared.RequestType.QUERY_SALES_BY_PRODUCT,
                                new Shared.ProductCategoryQuery(type)
                        );

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }

                        Object response = in.readObject();
                        if (response instanceof Map<?, ?> map) {
                            if (map.size() <= 1 && map.containsKey("total")) {
                                System.out.println("[Manager] No sales recorded yet for this product category.");
                            }
                            else {
                                System.out.println("\n[Total Sales by Product Type]");
                                for (Map.Entry<?, ?> entry : map.entrySet()) {
                                    System.out.println(entry.getKey() + ": " + entry.getValue());
                                }
                            }
                        }else {
                            System.out.println("[Warning] Unexpected response type received: " + response.getClass().getSimpleName());
                        }
                    }
                    case 4 -> {
                        System.out.print("Enter store name: ");
                        String storeName = scanner.nextLine().trim();
                        if (storeName.isEmpty()) {
                            System.out.println("[Error] Store name cannot be empty.");
                            break;
                        }

                        System.out.print("Enter product name: ");
                        String productName = scanner.nextLine().trim();
                        if (productName.isEmpty()) {
                            System.out.println("[Error] Product name cannot be empty.");
                            break;
                        }

                        System.out.print("Enter quantity to add (or remove): ");
                        int delta;
                        try {
                            delta = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("[Error] Invalid number for quantity.");
                            break;
                        }
                        Shared.StockUpdateData data = new Shared.StockUpdateData(storeName, productName, delta);
                        Shared.Request req = new Shared.Request(Shared.RequestType.UPDATE_STOCK, data);

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }
                        Object response = in.readObject();
                        System.out.println("[Response] " + response);
                    }
                    case 5 -> {
                        System.out.print("Enter store name: ");
                        String storeName = scanner.nextLine().trim();
                        if (storeName.isEmpty()) {
                            System.out.println("[Error] Store name cannot be empty.");
                            break;
                        }

                        System.out.print("Enter product name: ");
                        String name = scanner.nextLine().trim();
                        if (name.isEmpty()) {
                            System.out.println("[Error] Product name cannot be empty.");
                            break;
                        }

                        System.out.print("Enter product type: ");
                        String type = scanner.nextLine().trim();
                        if (type.isEmpty()) {
                            System.out.println("[Error] Product type cannot be empty.");
                            break;
                        }

                        System.out.print("Enter available quantity: ");
                        int quantity;
                        try {
                            quantity = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("[Error] Invalid number for quantity.");
                            break;
                        }

                        System.out.print("Enter price (€): ");
                        double price;
                        try {
                            price = Double.parseDouble(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("[Error] Invalid number for price.");
                            break;
                        }

                        Shared.Product product = new Shared.Product(name, type, quantity, price);
                        Shared.AddProductData addData = new Shared.AddProductData(storeName, product);
                        Shared.Request req = new Shared.Request(Shared.RequestType.ADD_PRODUCT, addData);

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }

                        Object response = in.readObject();
                        System.out.println("[Response] " + response);
                    }
                    case 6 -> {
                        System.out.print("Enter store name: ");
                        String storeName = scanner.nextLine().trim();
                        if (storeName.isEmpty()) {
                            System.out.println("[Error] Store name cannot be empty.");
                            break;
                        }

                        System.out.print("Enter product name to hide: ");
                        String productName = scanner.nextLine().trim();
                        if (productName.isEmpty()) {
                            System.out.println("[Error] Product name cannot be empty.");
                            break;
                        }

                        Shared.RemoveProductData removeData = new Shared.RemoveProductData(storeName, productName);
                        Shared.Request req = new Shared.Request(Shared.RequestType.REMOVE_PRODUCT, removeData);

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }

                        Object response = in.readObject();
                        System.out.println("[Response] " + response);
                    }
                    case 7 -> {
                        System.out.println("\n[Manager] Exit.");
                        return;
                    }
                    default -> System.out.println("Invalid Choice.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Manager] Connection Error: " + e.getMessage());
        }
    }
}