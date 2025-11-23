# BigB Planer - API Dokumentation

## Übersicht

Die REST API ist unter folgendem Base Path erreichbar:
```
http://localhost:8080/api
```

## API Endpunkte

### 1. Health Check
**Endpoint:** `GET /api/planer/health`

Prüft, ob der API-Service verfügbar ist.

**Beispiel-Aufruf:**
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

### 2. Schedule generieren
**Endpoint:** `POST /api/planer/generate`

Generiert einen optimierten Spielplan basierend auf den eingegebenen Spielernamen und der Anzahl der Runden.

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

**Beispiel-Aufruf:**
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

**Mögliche Fehler:**
- **400 Bad Request:** 
  - Weniger als 4 Spieler
  - 0 oder negative Anzahl von Runden
  - Leere Spielerliste
  - Doppelte Spielernamen

---

### 3. Paarungsstatistiken
**Endpoint:** `GET /api/planer/statistics`

Gibt Statistiken über die Spielerpaarungen zurück.

**Beispiel-Aufruf:**
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

### 4. Alle Paarungen abrufen
**Endpoint:** `GET /api/planer/pairings`

Gibt eine Liste aller Spielerpaarungen sortiert nach Häufigkeit zurück.

**Beispiel-Aufruf:**
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

## Swagger UI - Interaktive API Dokumentation

Nach dem Start des Servers ist die interaktive Swagger UI unter folgendem Link erreichbar:

```
http://localhost:8080/swagger-ui
```

Hier kannst du:
- Alle verfügbaren Endpunkte sehen
- Request/Response Schemas einsehen
- Endpunkte direkt aus dem Browser testen

### OpenAPI Spezifikation

Die vollständige OpenAPI Spezifikation ist unter folgendem Link erreichbar:

```
http://localhost:8080/openapi
```

---

## Konfiguration

Die API ist in der `application.properties` wie folgt konfiguriert:

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

## Server starten

```bash
./gradlew.bat quarkusDev
```

Der Server startet dann auf `http://localhost:8080` im Development Mode mit Hot-Reload.

---

## Validierungsregeln

Beim Generieren eines Schedules werden folgende Regeln überprüft:

1. **Mindestens 4 Spieler erforderlich**
2. **Mindestens 1 Runde erforderlich**
3. **Keine doppelten Spielernamen**
4. **Keine leere Spielerliste**

Bei Verletzung dieser Regeln wird ein **400 Bad Request** mit einer entsprechenden Fehlermeldung zurückgegeben.

---

## Beispiel-Workflow

1. **Server starten:**
   ```bash
   ./gradlew.bat quarkusDev
   ```

2. **Health Check:**
   ```bash
   curl http://localhost:8080/api/planer/health
   ```

3. **Schedule generieren:**
   ```bash
   curl -X POST http://localhost:8080/api/planer/generate \
     -H "Content-Type: application/json" \
     -d '{"playerNames":["A","B","C","D","E","F","G","H","I","J"],"numberOfRounds":5,"playersPerRound":4}'
   ```

4. **Statistiken abrufen:**
   ```bash
   curl http://localhost:8080/api/planer/statistics
   ```

5. **Swagger UI öffnen:**
   ```
   http://localhost:8080/swagger-ui
   ```

