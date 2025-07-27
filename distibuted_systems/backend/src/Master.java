package com.example.distributedsystemsapp.backend.src;

import java.io.*;
import java.net.*;
import java.util.*;

public class Master {
    private static  int PORT;
    private static final List<Socket> workerSockets = new ArrayList<>();
    private static final List<ObjectOutputStream> workerOuts = new ArrayList<>();
    private static final List<ObjectInputStream> workerIns = new ArrayList<>();
    //Maps address to worker index
    private static final Map<String, Integer> addressToWorkerIndex = new HashMap<>();
    //Keeps old address to worker mapping
    private static final Map<String, Integer> previousWorkerIndexMap = new HashMap<>();
    // Maps for the implementation of Active Replication
    private static final Map<String, Integer> primaryMap = new HashMap<>();
    private static final Map<String, Integer> replicaMap = new HashMap<>();
    private static Shared.SystemConfig config;



    public static void main(String[] args) {

        try {
            config = Shared.loadSystemConfig("backend/src/inputs/system_config.json");
        } catch (IOException e) {
            System.out.println("[Master] Failed to load config: " + e.getMessage());
            return;
        }

        PORT = config.masterPort;

        // Connect to Workers via the arguments
        for (Shared.SystemConfig.WorkerInfo w : config.workers) {
            try {
                Socket socket = new Socket(w.host, w.port);

                // Setup streams
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Register worker socket and streams
                workerSockets.add(socket);
                workerOuts.add(out);
                workerIns.add(in);

                // Create a unique ID based on IP:port
                String workerId = w.host + ":" + w.port;
                addressToWorkerIndex.put(workerId, workerSockets.size() - 1);

                // Read HELLO request from Worker
                Object hello = in.readObject();
                if (hello instanceof Shared.Request r && r.getType() == Shared.RequestType.HELLO) {
                    Shared.HelloData data = (Shared.HelloData) r.getPayload();
                    String announcedId = data.workerAddress;
                    addressToWorkerIndex.put(announcedId, workerSockets.size() - 1);
                    System.out.println("[Master] HELLO received from Worker at " + announcedId);
                } else {
                    System.out.println("[Master] Warning: Unexpected object from Worker during handshake.");
                }

                System.out.println("[Master] Connected with Worker: " + w.host + ":" + w.port);
            } catch (Exception e) {
                System.out.println("[Master] Connection error with Worker: " + w.host + ":" + w.port + " → " + e.getMessage());
            }
        }
        new Thread(() -> listenForWorkers(config.reconnectPort)).start(); // Πρέπει να του περάσεις το port

        // Now listen for Manager (Client) requests
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Master] Waiting for Manager connection on port " + PORT);

            while (true) {
                Socket managerSocket = serverSocket.accept();
                System.out.println("[Master] New connection with Manager: " + managerSocket.getInetAddress());
                new Thread(() -> handleManager(managerSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("[Master] Error: " + e.getMessage());
        }
    }

    private static void handleManager(Socket managerSocket) {
        try (
                ObjectOutputStream out = new ObjectOutputStream(managerSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(managerSocket.getInputStream())
        ) {
            System.out.println("[Master] Ready for requests...");

            while (true) {
                try {
                    Object request = in.readObject();

                    new Thread(() -> {
                        try {
                            handleSingleRequest(request, out);
                        } catch (Exception e) {
                            synchronized (out) {
                                try {
                                    out.writeObject("[Master] Internal error handling request: " + e.getMessage());
                                    out.flush();
                                } catch (IOException ex) {
                                    System.err.println("[Master] Failed to send error back to Manager: " + ex.getMessage());
                                }
                            }
                        }
                    }).start();
                } catch (EOFException e) {
                    System.out.println("[Master] Manager disconnected.");
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("[Master] Error in reading the query: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("[Master] Error with Manager's socket: " + e.getMessage());
        }
    }

    private static void handleSingleRequest(Object request, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        if (!(request instanceof Shared.Request sharedRequest)) {
            out.writeObject("[Master] Non valid request.");
            out.flush();
            return;
        }

        System.out.println("[Master] Received request: " + sharedRequest.getType());
        Object payload = sharedRequest.getPayload();
        Shared.RequestType type = sharedRequest.getType();

        if (sharedRequest.getType() == Shared.RequestType.HELLO) {
            Shared.HelloData helloData = (Shared.HelloData) sharedRequest.getPayload();
            String workerId = helloData.workerAddress;

            // Retrieve the current index of the reconnected worker
            int newWorkerIndex = addressToWorkerIndex.get(workerId);

            // Retrieve the previous index (if it exists) from earlier connection
            Integer oldWorkerIndex = previousWorkerIndexMap.get(workerId);

            System.out.println("[Master] HELLO received from Worker at " + workerId);
            System.out.println("[Master] Worker reconnected as index " + newWorkerIndex);
            System.out.println("[DEBUG] oldWorkerIndex = " + oldWorkerIndex);

            // Update primary/replica maps if the worker existed before
            if (oldWorkerIndex != null) {
                for (Map.Entry<String, Integer> entry : primaryMap.entrySet()) {
                    if (entry.getValue().equals(oldWorkerIndex)) {
                        primaryMap.put(entry.getKey(), newWorkerIndex);
                        System.out.println("[Master] Updated primary for store " + entry.getKey() + " to Worker #" + newWorkerIndex);
                    }
                }

                for (Map.Entry<String, Integer> entry : replicaMap.entrySet()) {
                    if (entry.getValue().equals(oldWorkerIndex)) {
                        replicaMap.put(entry.getKey(), newWorkerIndex);
                        System.out.println("[Master] Updated replica for store " + entry.getKey() + " to Worker #" + newWorkerIndex);
                    }
                }
            }

            // Re-send the store state to this reconnected Worker if it's now primary
            for (Map.Entry<String, Integer> entry : primaryMap.entrySet()) {
                if (entry.getValue() == newWorkerIndex) {
                    String storeName = entry.getKey();
                    System.out.println("[Master] Resending store " + storeName + " to Worker " + newWorkerIndex);

                    try {
                        Object storeObj = null;

                        // Try to fetch store data from replica
                        Integer replicaIndex = replicaMap.get(storeName);
                        try {
                            storeObj = sendRequestToWorker(replicaIndex,
                                    new Shared.Request(Shared.RequestType.GET_STORE_STATE, storeName));
                            System.out.println("[Master] Store " + storeName + " fetched from Replica Worker #" + replicaIndex);
                        } catch (Exception e) {
                            System.out.println("[Master] Replica fetch failed. Error: " + e.getMessage());
                        }

                        // If successful, sync store data to the reconnected primary Worker
                        if (storeObj instanceof Shared.Store store) {
                            Shared.StoreSyncPayload payloadSync = new Shared.StoreSyncPayload(storeName, store);
                            Shared.Request syncRequest = new Shared.Request(Shared.RequestType.SYNC_STORE_STATE, payloadSync);

                            sendRequestToWorker(newWorkerIndex, syncRequest);
                            System.out.println("[Master] Store " + storeName + " re-sent successfully.");
                        } else {
                            System.out.println("[Master] Failed to fetch store for " + storeName + ". Sync skipped.");
                        }

                    } catch (Exception e) {
                        System.out.println("[Master] Error handling store sync: " + e.getMessage());
                    }
                }
            }

            return;
        }

        //Chose worker based on the storeName, if it exists
        int workerIndex = 0;
        String storeName = null;

        System.out.println("[Master] Handling request type: " + type);

        // Determine storeName and workerIndex if request is related to a specific store.
        // Global queries (e.g., SEARCH_STORES) are handled separately below.
        switch (type) {
            case ADD_STORE -> storeName = ((Shared.Store) payload).storeName;
            case ADD_PRODUCT -> storeName = ((Shared.AddProductData) payload).storeName;
            case REMOVE_PRODUCT -> storeName = ((Shared.RemoveProductData) payload).storeName;
            case UPDATE_STOCK -> storeName = ((Shared.StockUpdateData) payload).storeName;

            //Global queries must be broadcasted to all workers
            case QUERY_SALES_BY_CATEGORY,QUERY_SALES_BY_PRODUCT,SEARCH_STORES, BUY_FROM_SEARCH, RATE_STORE -> {
                workerIndex = -1; //such index means all workers receive the query
            }
            default -> {
                System.out.println("[Master] Unknown or unsupported request type: " + type);
                out.writeObject("[Master] Error: Unknown request type.");
                out.flush();
                return;
            }
        }

        // Normalize storeName to lowercase for consistent routing and lookup
        if (storeName != null) {
            storeName = storeName.toLowerCase();
        }

        // If storeName is required, ensure it exists
        if (storeName != null && type != Shared.RequestType.ADD_STORE &&!checkStoreExistsGlobally(storeName)) {
            synchronized (out) {
                out.writeObject("Store not found.");
                out.flush();
            }
            return;
        }

        if (storeName != null) {
            System.out.println("[Master] Handling store: " + storeName);
            workerIndex = Math.abs(storeName.hashCode()) % workerOuts.size();
        }

        // Only print this if it's not a global broadcast (workerIndex = -1)
        if (workerIndex != -1) {
            int targetPort = workerSockets.get(workerIndex).getPort();
            System.out.println("[Master] Sending query to Worker #" + workerIndex + " (port: " + targetPort + ")");
        }

        // Global statistics query: send to all workers and aggregate their responses
        if (type == Shared.RequestType.QUERY_SALES_BY_CATEGORY || type == Shared.RequestType.QUERY_SALES_BY_PRODUCT) {
            Map<String, Integer> merged = broadcastAndAggregateIntegerMap(sharedRequest);
            out.writeObject(merged);
            out.flush();
            return;
        }

        if (type == Shared.RequestType.SEARCH_STORES || type == Shared.RequestType.BUY_FROM_SEARCH || type == Shared.RequestType.RATE_STORE) {
            List<Object> results = broadcastAndCollectResponses(sharedRequest);
            out.writeObject(results);
            out.flush();
            return;
        }

        // ACTIVE REPLICATION: ADD_STORE
        if (type == Shared.RequestType.ADD_STORE && storeName != null) {
            int primary = Math.abs(storeName.hashCode()) % workerOuts.size();
            int replica = (primary + 1) % workerOuts.size(); // next worker cyclically

            primaryMap.put(storeName, primary);
            replicaMap.put(storeName, replica);

            System.out.println("[Master] Replicating store '" + storeName +
                    "' → Primary: Worker #" + primary + ", Replica: Worker #" + replica);

            // Send to primary and get response
            System.out.println("[Master] Sending ADD_STORE to Primary Worker #" + primary);
            Object response = sendRequestToWorker(primary, sharedRequest);
            out.writeObject(response);
            out.flush();

            // Send to replica and discard its response safely
            try {
                Object discard = sendRequestToWorker(replica, sharedRequest);
                System.out.println("[Master] (Discarded) Replica responded to ADD_STORE: " + discard);
            } catch (Exception e) {
                System.out.println("[Master] Warning: Failed to read from replica → " + e.getMessage());
            }
            return;
        }
        if (storeName != null && primaryMap.containsKey(storeName) && isReplicatedType(type)) {

            Object workerResponse;
            try {
                workerResponse = tryPrimaryThenReplica(sharedRequest, storeName);
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("[Master] Both Primary and Replica failed for store: " + storeName);
                workerResponse = "[Master] Request failed. All replicas unavailable.";
            }
            // Always write to Manager (only once)
            out.writeObject(workerResponse);
            out.flush();

            // If the query changes the object's state synchronize the replica
            if (type == Shared.RequestType.UPDATE_STOCK ||
                    type == Shared.RequestType.ADD_PRODUCT ||
                    type == Shared.RequestType.REMOVE_PRODUCT) {
                try {
                    int primary = primaryMap.get(storeName);
                    int replica = replicaMap.get(storeName);

                    System.out.println("[Master] Requesting updated store state for synchronization...");

                    // Fetch store state from primary if its available or replica
                    Object storeResponse;
                    try {
                        storeResponse = sendRequestToWorker(primary, new Shared.Request(Shared.RequestType.GET_STORE_STATE, storeName));
                        System.out.println("[Master] Fetched store state from Primary.");
                    } catch (IOException e) {
                        System.out.println("[Master] Primary unavailable, trying Replica to fetch store state...");
                        storeResponse = sendRequestToWorker(replica, new Shared.Request(Shared.RequestType.GET_STORE_STATE, storeName));
                        System.out.println("[Master] Fetched store state from Replica.");
                    }

                    if (storeResponse instanceof Shared.Store updatedStore) {
                        System.out.println("[Master] Successfully fetched updated store state for store: " + storeName);

                        // Send updated store state to replica
                        Shared.StoreSyncPayload syncPayload = new Shared.StoreSyncPayload(storeName, updatedStore);
                        Shared.Request syncRequest = new Shared.Request(Shared.RequestType.SYNC_STORE_STATE, syncPayload);

                        System.out.println("[Master] Sending updated store state to Replica Worker #" + replica);
                        try {
                            sendRequestToWorker(replica, syncRequest);
                            System.out.println("[Master] Replica synchronized successfully for store: " + storeName);
                        } catch (Exception e) {
                            System.out.println("[Master] Warning: Failed to synchronize Replica for store: " + storeName + ". Error: " + e.getMessage());
                        }

                    } else {
                        System.out.println("[Master] Warning: Unexpected response during GET_STORE_STATE. Sync aborted.");
                    }
                } catch (Exception e) {
                    System.out.println("[Master] Error during synchronization after update: " + e.getMessage());
                }
            }
        }
    }
    //Helper method, used for Manager global requests
    private static Map<String, Integer> broadcastAndAggregateIntegerMap(Shared.Request request) throws IOException, ClassNotFoundException {
        // Send to all workers, but discard replica responses
        for (int i = 0; i < workerOuts.size(); i++) {
            if (isReplica(i)) {
                try {
                    sendRequestToWorker(i, request);
                    Object discard = workerIns.get(i).readObject(); // discard response
                    System.out.println("[Master] (Discarded) Replica responded to broadcast: " + discard);
                } catch (Exception e) {
                    System.out.println("[Master] Warning: Failed to discard from replica #" + i + ": " + e.getMessage());
                }
                continue;
            }

            // Send to primary
            sendRequestToWorker(i, request);
        }

        // Wait for final result from Reducer
        try (ServerSocket masterServerSocket = new ServerSocket(config.reducerToMasterPort)) {
            System.out.println("[Master] Waiting for final result from Reducer...");

            try (Socket reducerSocket = masterServerSocket.accept();
                 ObjectInputStream reducerIn = new ObjectInputStream(reducerSocket.getInputStream())) {

                Object response = reducerIn.readObject();

                if (response instanceof Map<?, ?> finalResult) {
                    Map<String, Integer> result = new HashMap<>();
                    for (Map.Entry<?, ?> entry : finalResult.entrySet()) {
                        result.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
                    }
                    System.out.println("[Master] Received final aggregated result from Reducer!");
                    return result;
                } else {
                    System.out.println("[Master] Unexpected response from Reducer.");
                    return Collections.emptyMap();
                }
            }
        }
    }

    //Helper method, used for client requests
    private static List<Object> broadcastAndCollectResponses(Shared.Request request) throws IOException, ClassNotFoundException {
        List<Object> combinedResults = new ArrayList<>();

        for (int i = 0; i < workerOuts.size(); i++) {
            // if it's a replica worker, ignore the response
            if (isReplica(i)) {
                try {
                    sendRequestToWorker(i, request);
                    Object discard = workerIns.get(i).readObject();
                    System.out.println("[Master] (Discarded) Replica responded to broadcast: " + discard);
                } catch (Exception e) {
                    System.out.println("[Master] Warning: Failed to read from replica #" + i + ": " + e.getMessage());
                }
                continue;
            }

            //if it's a primary worker, save the response
            Object response = sendRequestToWorker(i, request);
            if (response instanceof String s && s.equals("Store not found.")) continue;

            if (response instanceof Collection<?> coll) {
                combinedResults.addAll(coll);
            } else if (response != null) {
                combinedResults.add(response);
            }
        }

        return combinedResults;
    }


    private static boolean isReplica(int index) {
        return replicaMap.containsValue(index);
    }

    // Helper method to send a request to a Worker and get the response.
    private static Object sendRequestToWorker(int workerIndex, Shared.Request request) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = workerOuts.get(workerIndex);
        ObjectInputStream in = workerIns.get(workerIndex);

        System.out.println("[DEBUG] [Master] Sending request to Worker #" + workerIndex +
                " | Type: " + request.getType());

        try {
            synchronized (out) {
                out.writeObject(request);
                out.flush();
            }

            synchronized (in) {
                Object response = in.readObject();
                System.out.println("[DEBUG] [Master] Response from Worker #" + workerIndex + ": " + response);
                return response;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[ERROR] [Master] Communication failed with Worker #" + workerIndex + ": " + e.getMessage());
            throw e; // allow caller (e.g. tryPrimaryThenReplica) to fallback to replica
        }
    }
    //Helper Method, checks if a store exists
    private static boolean checkStoreExistsGlobally(String storeName) {
        try {
            for (int i = 0; i < workerOuts.size(); i++) {
                System.out.println("[Master] Checking existence of store '" + storeName + "' on Worker #" + i);
                Object response = sendRequestToWorker(i, new Shared.Request(Shared.RequestType.CHECK_STORE_EXISTS, storeName));

                if (response instanceof String s && s.equalsIgnoreCase("yes")) {
                    System.out.println("[Master] Store '" + storeName + "' found on Worker #" + i);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[Master] Error during store existence check: " + e.getMessage());
        }
        System.out.println("[Master] Store '" + storeName + "' not found on any Worker.");
        return false;
    }

    // Helper: determines if a request type should support failover to replica
    private static boolean isReplicatedType(Shared.RequestType type) {
        return switch (type) {
            case ADD_PRODUCT, REMOVE_PRODUCT, UPDATE_STOCK, RATE_STORE -> true;
            default -> false;
        };
    }
    //Helper method used for active replication
    private static Object tryPrimaryThenReplica(Shared.Request request, String storeName) throws IOException, ClassNotFoundException {
        int primary = primaryMap.getOrDefault(storeName, -1);
        int replica = replicaMap.getOrDefault(storeName, -1);

        if (primary == -1 || replica == -1) {
            throw new IOException("No replication mapping for store: " + storeName);
        }

        try {
            System.out.println("[Master] Sending request to Primary for store: " + storeName);
            Object response = sendRequestToWorker(primary, request);
            System.out.println("[Master] Response received from Primary for store: " + storeName);
            return response;
        } catch (IOException e) {
            System.out.println("[Master] Primary Worker failed for store " + storeName + ". Trying replica...");
            Object response = sendRequestToWorker(replica, request);
            System.out.println("[Master] Response received from Replica for store: " + storeName);
            return response;
        }
    }

    // Used by Master to accept all Worker reconnections
    private static void listenForWorkers(int port) {
        try (ServerSocket workerServer = new ServerSocket(port)) {
            System.out.println("[Master] Listening for Worker REconnections only on port " + port + "...");


            while (true) {
                Socket workerSocket = workerServer.accept();
                System.out.println("[Master] Incoming Worker reconnect attempt from " + workerSocket.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(workerSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(workerSocket.getInputStream());

                try {
                    Object hello = in.readObject();
                    if (hello instanceof Shared.Request r && r.getType() == Shared.RequestType.HELLO) {
                        Shared.HelloData data = (Shared.HelloData) r.getPayload();
                        String workerId = data.workerAddress;

                        Integer previousIndex = addressToWorkerIndex.get(workerId);

                        if (previousIndex == null) {
                            System.out.println("[Master] [Rejected] Worker " + workerId + " is not recognized. Ignoring connection.");
                            in.close();
                            out.close();
                            workerSocket.close();
                            continue;
                        }

                        int thisWorkerIndex = previousIndex;
                        previousWorkerIndexMap.put(workerId, previousIndex);

                        try {
                            workerSockets.get(thisWorkerIndex).close();
                            workerOuts.get(thisWorkerIndex).close();
                            workerIns.get(thisWorkerIndex).close();
                            System.out.println("[Master] [Reconnection] Closed old streams for Worker " + workerId);
                        } catch (Exception e) {
                            System.out.println("[Master] [Reconnection] Failed to close previous streams: " + e.getMessage());
                        }

                        workerSockets.set(thisWorkerIndex, workerSocket);
                        workerOuts.set(thisWorkerIndex, out);
                        workerIns.set(thisWorkerIndex, in);
                        System.out.println("[Master] [Reconnection] Worker " + workerId + " reconnected at index " + thisWorkerIndex);

                        //HELLO request to normal handler
                        handleSingleRequest(r, out);
                    } else {
                        System.out.println("[Master] [Error] Unexpected object during Worker reconnect handshake.");
                    }

                } catch (Exception e) {
                    System.out.println("[Master] [Error] Worker reconnect failed: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("[Master] [Fatal] Worker reconnection listener crashed: " + e.getMessage());
        }
    }
}