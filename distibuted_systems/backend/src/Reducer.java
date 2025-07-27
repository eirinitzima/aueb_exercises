package com.example.distributedsystemsapp.backend.src;

import java.io.*;
import java.net.*;
import java.util.*;

public class Reducer {
    private static final int EXPECTED_WORKERS = 3; // βάλε όσους Workers έχεις

    public static void main(String[] args) {

        Shared.SystemConfig config;
        try {
            config = Shared.loadSystemConfig("backend/src/inputs/system_config.json");
        } catch (IOException e) {
            System.out.println("[Reducer] Failed to load system config: " + e.getMessage());
            return;
        }

        int LISTEN_PORT = config.reducerPort;
        int MASTER_PORT = config.reducerToMasterPort;

        Map<String, Integer> finalResult = new HashMap<>();
        int totalSales = 0;
        int responsesReceived = 0;

        try (ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)) {
            System.out.println("[Reducer] Listening on port " + LISTEN_PORT);

            while (responsesReceived < EXPECTED_WORKERS) {
                Socket workerSocket = serverSocket.accept();
                System.out.println("[Reducer] Connected with Worker.");

                try (
                        ObjectInputStream in = new ObjectInputStream(workerSocket.getInputStream());
                ) {
                    Object data = in.readObject();
                    if (data instanceof Map<?, ?> map) {
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            String key = entry.getKey().toString();
                            int value = Integer.parseInt(entry.getValue().toString());

                            if (key.equals("total")) {
                                totalSales += value;
                            } else {
                                finalResult.merge(key, value, Integer::sum);
                            }
                        }
                        responsesReceived++;
                    }
                }
            }

            finalResult.put("total", totalSales);

            // Στείλε το τελικό αποτέλεσμα στον Master
            try (
                    Socket masterSocket = new Socket(config.masterHost, MASTER_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
            ) {
                out.writeObject(finalResult);
                out.flush();
                System.out.println("[Reducer] Sent final result to Master.");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
