# Backend System – Distributed Food Delivery

This is the **backend system** for a distributed online food delivery platform developed for the course _Distributed Systems_ at AUEB (2024–2025). The backend is written in **Java**, based on a **Master–Worker architecture**, with **TCP Socket** communication and **in-memory storage**.

---

- **Master**: Multithreaded Java TCP server coordinating all components.
- **Workers**: Store restaurants and products, handle distributed queries and orders.
- **Manager Console**: CLI tool to manage restaurants and run queries.
- **All communication**: Done via TCP sockets.

---

##  Technologies

- Java 8+
- Sockets (java.net.ServerSocket, java.net.Socket)
- Threads (`synchronized`, `wait`, `notify`)
- In-memory data structures only (no DB)

---

##  Master Node

### Responsibilities:

- Receive/store restaurant data from managers
- Assign stores to workers
- Handle filtered searches and order requests from clients
- Coordinate MapReduce tasks across workers

### MapReduce Use Cases:

- Filter stores (by distance, category, stars, price)
- Aggregate sales by:
- FoodCategory (e.g., pizzeria)
- ProductCategory (e.g., salad)

---

##  Worker Nodes

Each Worker:

- Stores data for assigned stores
- Responds to Master requests
- Handles concurrent updates
- Ensures synchronized access to data

All data is stored in memory.

---

## Manager Console App

Functions:

- Add / remove products
- Adjust stock
- View total sales per:
- Store category (e.g., "Pizza Fun": 100)
- Product category (e.g., "salad": 75)

Manager uploads stores via `.json` + logo path. Example store format:

```json
{
"StoreName": "Pizza Fun",
"Latitude": 37.99,
"Longitude": 23.73,
"FoodCategory": "pizzeria",
"Stars": 4,
"NoOfVotes": 23,
"StoreLogo": "images/pizzafun.png",
"Products": [
  { "ProductName": "Margherita", "ProductType": "pizza", "Available Amount": 100, "Price": 9.2 },
  ...
]
}

