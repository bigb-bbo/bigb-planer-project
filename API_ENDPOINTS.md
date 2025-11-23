# BigB Planer - API Documentation

## Overview

The REST API is accessible under the following base path:
```
http://localhost:8080/api
```

## API Endpoints

### 1. Health Check
**Endpoint:** `GET /api/planer/health`

Checks if the API service is available.

**Example Call:**
```bash
curl -X GET http://localhost:8080/api/planer/health
```

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

---

### 2. Generate Schedule
**Endpoint:** `POST /api/planer/generate`

Generates an optimized game schedule based on the entered player names and the number of rounds.

**Request Body:**
```json
{
  "playerNames": [
    "Alice", "Bob", "Charlie", "David",
    "Eve", "Frank", "Grace", "Henry",
    "Iris", "Jack"
  ],
  "numberOfRounds": 5,
  "playersPerRound": 4
}
```

**Example Call:**
```bash
curl -X POST http://localhost:8080/api/planer/generate \
  -H "Content-Type: application/json" \
  -d '{
    "playerNames": ["Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry", "Iris", "Jack"],
    "numberOfRounds": 5,
    "playersPerRound": 4
  }'
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "numberOfRounds": 5,
  "players": [
    {
      "id": "uuid-1",
      "name": "Alice"
    },
    {
      "id": "uuid-2",
      "name": "Bob"
    }
  ],
  "rounds": [
    {
      "roundNo": 1,
      "roundDate": "2025-01-12",
      "selectedPlayers": [
        {"id": "uuid-1", "name": "Alice"},
        {"id": "uuid-2", "name": "Bob"},
        {"id": "uuid-3", "name": "Charlie"},
        {"id": "uuid-4", "name": "David"}
      ]
    }
  ],
  "createdAt": "2025-01-11T10:30:00"
}
```

**Possible Errors:**
- **400 Bad Request:** 
  - Fewer than 4 players
  - 0 or negative number of rounds
  - Empty player list
  - Duplicate player names

---

### 3. Pairing Statistics
**Endpoint:** `GET /api/planer/statistics`

Returns statistics about the player pairings.

**Example Call:**
```bash
curl -X GET http://localhost:8080/api/planer/statistics
```

**Response (200 OK):**
```json
{
  "totalUniquePairings": 42,
  "totalPairingRecords": 150,
  "maxFrequency": 5,
  "minFrequency": 1,
  "avgFrequency": 3.57
}
```

---

### 4. Retrieve All Pairings
**Endpoint:** `GET /api/planer/pairings`

Returns a list of all player pairings sorted by frequency.

**Example Call:**
```bash
curl -X GET http://localhost:8080/api/planer/pairings
```

**Response (200 OK):**
```json
[
  {
    "playerIds": ["uuid-1", "uuid-2", "uuid-3", "uuid-4"],
    "frequency": 5
  },
  {
    "playerIds": ["uuid-1", "uuid-2", "uuid-3", "uuid-5"],
    "frequency": 4
  }
]
```

---

## Swagger UI - Interactive API Documentation

After starting the server, the interactive Swagger UI is available at the following link:

```
http://localhost:8080/swagger-ui
```

Here you can:
- See all available endpoints
- View request/response schemas
- Test endpoints directly from the browser

---

## Configuration

The API is configured in the `application.properties` as follows:

```properties
# REST API Path
quarkus.resteasy-reactive.path=/api

# Swagger UI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui

# Server Port
quarkus.http.port=8080
```

---

## Starting the Server

```bash
./gradlew.bat quarkusDev
```

The server then starts at `http://localhost:8080` in development mode with hot-reload.

---

## Validation Rules

When generating a schedule, the following rules are checked:

1. **At least 4 players required**
2. **At least 1 round required**
3. **No duplicate player names**
4. **No empty player list**

If these rules are violated, a **400 Bad Request** is returned with an appropriate error message.

---

## Example Workflow

1. **Start Server:**
   ```bash
   ./gradlew.bat quarkusDev
   ```

2. **Health Check:**
   ```bash
   curl http://localhost:8080/api/planer/health
   ```

3. **Generate Schedule:**
   ```bash
   curl -X POST http://localhost:8080/api/planer/generate \
     -H "Content-Type: application/json" \
     -d '{"playerNames":["A","B","C","D","E","F","G","H","I","J"],"numberOfRounds":5,"playersPerRound":4}'
   ```

4. **Retrieve Statistics:**
   ```bash
   curl http://localhost:8080/api/planer/statistics
   ```

5. **Open Swagger UI:**
   ```
   http://localhost:8080/swagger-ui
   ```
