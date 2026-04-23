# SmartCampusAPI

A RESTful API built with JAX-RS (Jersey) for managing rooms and sensors across a university smart campus. The system allows facilities managers to register rooms, deploy sensors, log sensor readings, and retrieve historical data through a clean, resource-based HTTP interface.

---

## Tech Stack

- Java 11+
- JAX-RS via Jersey 2.41
- Grizzly HTTP embedded server
- Jackson for JSON
- Maven for build management
- In-memory storage using ConcurrentHashMap

---

## How to Build and Run

### Prerequisites
- JDK 11 or above installed
- Maven installed (or use the NetBeans bundled Maven)

### Steps

1. Clone the repository:
```
git clone https://github.com/ravindu212/SmartCampusAPI.git
```

2. Navigate into the project folder:
```
cd SmartCampusAPI/SmartCampusAPI
```

3. Build the project:
```
mvn clean install
```

4. Run the server:
```
mvn exec:java
```

5. The API will start at:
```
http://localhost:8080/api/v1/
```

Press ENTER in the terminal to stop the server.

---

## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── resource/
│   ├── DiscoveryResource.java
│   ├── RoomResource.java
│   ├── SensorResource.java
│   └── SensorReadingResource.java
├── store/
│   └── DataStore.java
└── exception/
    ├── RoomNotEmptyException.java
    ├── RoomNotEmptyExceptionMapper.java
    ├── LinkedResourceNotFoundException.java
    ├── LinkedResourceNotFoundExceptionMapper.java
    ├── SensorUnavailableException.java
    ├── SensorUnavailableExceptionMapper.java
    ├── GlobalExceptionMapper.java
    └── ApiLoggingFilter.java
```

---

## Sample curl Commands

### 1. Get API discovery info
```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Register a sensor in a room
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```

### 4. Post a sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":25.3}'
```

### 5. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

---

## API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/ | Discovery endpoint |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{roomId} | Get a specific room |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | List all sensors (supports ?type= filter) |
| POST | /api/v1/sensors | Register a new sensor |
| GET | /api/v1/sensors/{sensorId} | Get a specific sensor |
| GET | /api/v1/sensors/{sensorId}/readings | Get all readings for a sensor |
| POST | /api/v1/sensors/{sensorId}/readings | Add a new reading |

---

## Report

### Part 1 — Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS resource class and how it affects in-memory data management.**

JAX-RS creates a new resource class instance for every incoming request by default. This means you cannot store shared data as instance fields inside resource classes since they would be lost after each request. To handle this, I used a singleton DataStore class accessed via getInstance(), which holds all data in ConcurrentHashMaps shared across all requests. ConcurrentHashMap is used instead of a regular HashMap to safely handle multiple simultaneous requests without race conditions.

**Q: Why is HATEOAS considered advanced RESTful design and how does it help client developers?**

HATEOAS means including links to related resources or actions inside API responses. This makes the API self-describing — clients can discover what to do next without reading external documentation. It also means the server can change URLs without breaking clients since clients follow links rather than hardcoding paths. It reduces guesswork for developers and makes the API easier to explore and integrate with.

---

### Part 2 — Room Management

**Q: What are the implications of returning only IDs versus full room objects in a list response?**

Returning only IDs keeps responses lightweight but forces clients to make extra requests to get details, which causes the N+1 problem. Returning full objects gives the client everything at once but increases payload size. For this project I returned full objects since the dataset is small and clients typically need room details immediately rather than fetching them one by one.

**Q: Is the DELETE operation idempotent in your implementation?**

Yes. The first DELETE on a room removes it and returns 204. A second identical DELETE returns 404 since the room is already gone. The server state is the same after both — the room does not exist. The response code changes but the outcome does not, which is the correct definition of idempotency for DELETE.

---

### Part 3 — Sensor Operations & Linking

**Q: What happens if a client sends data in a format different from what @Consumes(APPLICATION_JSON) declares?**

JAX-RS automatically rejects the request before it reaches the method and returns an HTTP 415 Unsupported Media Type response. No custom logic is needed for this — the runtime handles it entirely, which prevents unexpected data formats from reaching the application layer.

**Q: Why is @QueryParam better than putting the filter in the URL path for filtering?**

Path parameters are meant to identify a specific resource. Query parameters are meant for optional filters on a collection. Using /sensors?type=CO2 clearly means "filter all sensors by type" while /sensors/type/CO2 implies type and CO2 are resources themselves, which is misleading. Query parameters also let the same endpoint handle both the unfiltered and filtered cases cleanly without needing separate methods.

---

### Part 4 — Deep Nesting with Sub-Resources

**Q: What are the architectural benefits of the Sub-Resource Locator pattern?**

It separates concerns by delegating nested path handling to a dedicated class. In this project SensorResource hands off /sensors/{id}/readings to SensorReadingResource instead of handling everything itself. This keeps each class focused on one responsibility and makes the code easier to maintain. If reading logic needs to change, only SensorReadingResource is touched. Putting all nested logic in one controller would make it large and hard to manage as the API grows.

---

### Part 5 — Error Handling, Exception Mapping & Logging

**Q: Why is HTTP 422 more semantically accurate than 404 when a referenced roomId does not exist?**

404 means the requested URL was not found. In this case the URL /api/v1/sensors exists and works fine — the problem is inside the request body where the roomId references something that does not exist. 422 Unprocessable Entity is designed for exactly this situation where the request is syntactically correct but semantically invalid. Using 404 would mislead the client into thinking the endpoint itself was not found.

**Q: What are the cybersecurity risks of exposing Java stack traces to API consumers?**

Stack traces reveal class names, package names, library versions, and file paths. Attackers can use this to identify frameworks in use, look up known vulnerabilities for those versions, and craft more targeted attacks. They can also reveal internal logic depending on where the error occurred. A global exception mapper that returns only a generic error message prevents any of this information from leaking outside the server.

**Q: Why use JAX-RS filters for logging instead of manually adding Logger.info() to every method?**

Adding logging manually to every method is repetitive and easy to forget. Filters intercept every request and response automatically without touching the resource classes at all. This keeps resource classes clean and focused on business logic, ensures consistent logging across the whole API, and makes it easy to change the log format in one place rather than updating every method individually.
