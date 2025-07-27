package com.example.distributedsystemsapp.backend.src;
import com.example.distributedsystemsapp.backend.src.Shared;



import java.io.*;
import java.net.*;
import java.util.*;

public class ClientApp {
    public static void main(String[] args) {
        Shared.SystemConfig config;
        try {
            config = Shared.loadSystemConfig("backend/src/inputs/system_config.json");
        } catch (IOException e) {
            System.out.println("[Client] Failed to load system config: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);

        try (
                Socket socket = new Socket(config.masterHost, config.masterPort);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            System.out.println("[Client] Connected to Master.");

            while (true) {
                System.out.println("\n[Client Menu]");
                System.out.println("1. Search for stores");
                System.out.println("2. Buy a product");
                System.out.println("3. Rate a store");
                System.out.println("4. Exit");
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
                        System.out.print("Enter your latitude: ");
                        double lat;
                        try {
                            lat = Double.parseDouble(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid latitude.");
                            break;
                        }

                        System.out.print("Enter your longitude: ");
                        double lon;
                        try {
                            lon = Double.parseDouble(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid longitude.");
                            break;
                        }

                        System.out.print("Enter desired categories: ");
                        String[] categories = scanner.nextLine().split(",");
                        List<Shared.FoodCategory> catSet = new ArrayList<>();
                        for (String c : categories) {
                            try {
                                catSet.add(Shared.FoodCategory.valueOf(c.trim().toUpperCase()));
                            } catch (IllegalArgumentException ignored) {
                                System.out.println("[Warning] Skipping invalid category: " + c);
                            }
                        }

                        System.out.print("Minimum stars (1-5): ");
                        int stars;
                        try {
                            stars = Integer.parseInt(scanner.nextLine().trim());
                            if (stars < 1 || stars > 5) {
                                System.out.println("Invalid number of stars. Must be between 1 and 5.");
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number of stars.");
                            break;
                        }

                        System.out.print("Desired price level ($, $$, $$$): ");
                        String price = scanner.nextLine().trim();
                        if (price.isEmpty()) {
                            System.out.println("[Error] Price cannot be empty.");
                            break;
                        }

                        Shared.SearchFilters filters = new Shared.SearchFilters(lat, lon, catSet, stars, price);
                        Shared.Request request = new Shared.Request(Shared.RequestType.SEARCH_STORES, filters);

                        synchronized (out) {
                            out.writeObject(request);
                            out.flush();
                        }

                        Object resp = in.readObject();
                        if (resp instanceof List<?> list && !list.isEmpty()) {
                            System.out.println("\n[Search Results]");
                            list.forEach(System.out::println);
                        } else {
                            System.out.println("No stores found matching your filters.");
                        }
                    }
                    case 2 -> {
                        System.out.print("Enter store name: ");
                        String store = scanner.nextLine().trim();
                        if (store.isEmpty()) {
                            System.out.println("[Error] Store cannot be empty.");
                            break;
                        }

                        System.out.print("Enter product name: ");
                        String product = scanner.nextLine().trim();
                        if (product.isEmpty()) {
                            System.out.println("[Error] Product cannot be empty.");
                            break;
                        }

                        System.out.print("Enter quantity to buy: ");
                        int amount;
                        try {
                            amount = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount.");
                            continue;
                        }

                        Shared.BuyData buyData = new Shared.BuyData(store, product, amount);
                        Shared.Request req = new Shared.Request(Shared.RequestType.BUY_FROM_SEARCH, buyData);

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }

                        Object resp = in.readObject();
                        System.out.println("[Response] " + resp);
                    }
                    case 3 -> {
                        System.out.print("Enter store name: ");
                        String store = scanner.nextLine().trim();
                        if (store.isEmpty()) {
                            System.out.println("[Error] Store cannot be empty.");
                            break;
                        }

                        System.out.print("Enter your rating (1-5): ");
                        int stars;
                        try {
                            stars = Integer.parseInt(scanner.nextLine().trim());
                            if (stars < 1 || stars > 5) {
                                System.out.println("Invalid number of stars. Must be between 1 and 5.");
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number of stars.");
                            continue;
                        }

                        Shared.RatingData data = new Shared.RatingData(store, stars);
                        Shared.Request req = new Shared.Request(Shared.RequestType.RATE_STORE, data);

                        synchronized (out) {
                            out.writeObject(req);
                            out.flush();
                        }

                        Object resp = in.readObject();
                        System.out.println("[Response] " + resp);
                    }
                    case 4 -> {
                        System.out.println("[Client] Exiting.");
                        return;
                    }
                    default -> System.out.println("[Error] Invalid option.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Client] Connection error: " + e.getMessage());
        }
    }
}