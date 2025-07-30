
#  Android Client App – Food Delivery

This is the **Android application** for the distributed food delivery system, developed for the _Distributed Systems_ course at AUEB (2024–2025). It serves as the frontend interface for end-users (customers).

---

##  Features

- Connects to backend Master server using **TCP Sockets**
- Allows customers to:
  - View stores within 5 km
  - Filter by:
    - Food Category (e.g., sushi, pizza)
    - Star Rating (1–5)
    - Price Range: `$`, `$$`, `$$$`
  - View detailed store info
  - Browse products
  - Submit orders
  - Rate stores

---

##  Communication Protocol

- The app connects via TCP Socket to the Master.
- All communication is **asynchronous**:
  - Requests (e.g., `search()`, `buy()`) sent in background thread.
  - Responses parsed and shown when received.
- Open socket remains active during session.

---

##  Technologies

- Android SDK
- Java / Kotlin (depending on implementation)
- Threads (for background socket communication)
- TCP Sockets only (no HTTP, no Firebase)

---

##  UI Screens

- Home / Filters screen
- Store detail screen
- Product cart
- Order confirmation

---

##  Notes

- All store data is sent in JSON format.
- Communication format must be consistent with backend.
- Threading is **essential** to keep the UI responsive.

---

