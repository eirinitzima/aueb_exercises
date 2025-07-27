//package com.example.distributedsystemsapp.backend.src;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//
//public class Worker {
//
//    // In-memory store registry for this Worker
//    public static Map<String, Shared.Store> storeMap = new HashMap<>();
//
//    public static void main(String[] args) {
//        Shared.SystemConfig config;
//        try {
//            config = Shared.loadSystemConfig("backend/src/inputs/system_config.json");
//        } catch (IOException e) {
//            System.out.println("[Worker] Failed to load config: " + e.getMessage());
//            return;
//        }
//
//        int workerIndex;
//        try {
//            workerIndex = Integer.parseInt(System.getProperty("WORKER_INDEX"));
//        } catch (Exception e) {
//            System.out.println("[Worker] Set WORKER_INDEX using VM option: -DWORKER_INDEX=0 or 1");
//            return;
//        }
//
//        Shared.SystemConfig.WorkerInfo info = config.workers.get(workerIndex);
//        int port = info.port;
//        // Use port as unique Worker ID
//        String workerId = info.host + ":" + port;
//
//        //Connect to Master
//        new Thread(() -> connectToMaster(workerId, config.masterHost , config.reconnectPort)).start();
//
//
//        try (ServerSocket serverSocket = new ServerSocket(port)) {
//            System.out.println("Worker is listening on port " + port);
//
//            // Accept Manager or Client connections and delegate to WorkerHandler
//            while (true) {
//                Socket socket = serverSocket.accept();
//                System.out.println("New connection from " + socket.getInetAddress());
//
//                new WorkerHandler(socket, workerId).start();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Connects to Master to announce this Worker's ID using HELLO.
//    private static void connectToMaster(String workerId, String masterHost, int reconnectPort) {
//        try (
//                Socket socket = new Socket(masterHost, reconnectPort);
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
//        ) {
//            out.flush();
//            Shared.HelloData hello = new Shared.HelloData(workerId);
//            Shared.Request helloRequest = new Shared.Request(Shared.RequestType.HELLO, hello);
//
//            synchronized (out) {
//                out.writeObject(helloRequest);
//                out.flush();
//            }
//            socket.close();
//            System.out.println("[Worker] Sent HELLO to Master.");
//        } catch (IOException e) {
//            System.out.println("[Worker] Could not connect to Master: " + e.getMessage());
//        }
//    }
//
//}
