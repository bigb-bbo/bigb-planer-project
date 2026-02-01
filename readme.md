BigB Planer

Project including 
- gradle build concepts 
- using Quarkus as web server
- an in-memory database
- define unit tests for all features
- try to reach a coverage of over 90%
- based on domain driven design concepts, for
- generating a planning sheet (usable for trainings, ...)
- use RESTful service to access the planning functionality
- use DTOs for data exchange together with mapper to map domain objects to and from DTOs
- OpenAPI/Swagger UI for interactive API documentation

## Architecture Migration: From OpenLiberty to Quarkus

The project was migrated from OpenLiberty to **Quarkus**. Quarkus is a modern Java framework that:
- Offers faster startup times
- Has lower memory usage
- Enables native image compilation
- Supports hot-reload in development mode

## Start the server

In WSL terminal use:
```bash
./gradlew quarkusDev (or simply "sh startQuarkusDev.sh" from any directory)
```

The server starts at `http://localhost:8080` with automatic hot-reload on code changes.

## API Access

- **Base URL:** `http://localhost:8080/api`
- **Health Check:** `http://localhost:8080/api/planer/health`
- **Swagger UI:** `http://localhost:8080/swagger-ui`
- **Simple UI for testing:** `http://localhost:8080/planer/index.html`

## Frontend — Spielernamen konfigurieren

Die einfache Planer‑UI lädt die initiale Liste der Spielernamen zur Laufzeit aus einer lokalen statischen Datei. Das bedeutet:

- Die Datei, die die Spielernamen enthält, liegt unter:
  `src/main/resources/META-INF/resources/planer/players.json`
  und wird im laufenden Server unter `/planer/players.json` ausgeliefert.

- Unterstützte Formate (beide werden erkannt):
  - Einfaches JSON‑Array von Strings:
    ```json
    ["Alice","Bob","Charlie","David","Eve","Frank","Grace","Henry","Iris","Jack"]
    ```
  - Objekt mit Feld `playerNames`:
    ```json
    { "playerNames": ["Alice","Bob","Charlie","David","Eve","Frank","Grace","Henry","Iris","Jack"] }
    ```

- Vorgehen zum Anpassen der Spielernamen:
  1. Öffne die Datei `src/main/resources/META-INF/resources/planer/players.json` in deinem Editor.
  2. Bearbeite die Liste nach Bedarf (du kannst auch andere Anzahlen verwenden).
  3. Speichere die Datei.
  4. Lade die UI im Browser neu (F5). Falls der Browser die Datei cached, mache ein Hard‑Reload (Ctrl+F5) oder leere den Cache.
     In der Quarkus Dev‑Umgebung sollte die Datei bei Dateispeicherung neu ausgeliefert werden; aus Browser‑Caches resultierende Diskrepanzen bitte durch Hard‑Reload beheben.

- Hinweise:
  - Die UI zeigt anschließend die geladenen Namen als Initialwerte in den Eingabefeldern an; du kannst sie dort noch manuell anpassen bevor du den Plan generierst.
  - Die Datei `players.json` ist die Quelle, die du versionierst und direkt änderst.
  - Wenn du das Verhalten in der Produktion änderst (z. B. statische Ressourcen in einem ZIP/JAR), achte darauf, dass `players.json` in der ausgelieferten Anwendung vorhanden ist.

## Download (XLS/CSV)

Die UI bietet jetzt eine Schaltfläche "Download Plan (XLS)", mit der du den zuletzt generierten Spielplan als XLS-kompatible CSV-Datei herunterladen kannst.

- Woher kommt die Datei?
  - Der Server liefert die zuletzt generierte Planung (nur, wenn zuvor über den `/api/planer/generate`-Endpoint ein Plan erzeugt wurde).
  - Der Download-Endpoint ist: `GET /api/planer/download`.

- Format:
  - Die Datei ist eine kommagetrennte CSV mit Header: `RoundNo,Date,Player1,Player2,Player3,Player4`.
  - Der Server setzt den Response-Header `Content-Disposition: attachment; filename=plan-<timestamp>.csv`.
  - Zusätzlich schreibt der Server die CSV in das lokale `tmp/`-Verzeichnis des Anwendungs-Arbeitsverzeichnisses (relativ zum Projekt-Root).
    - Dateiname-Schema: `tmp/plan-<timestamp>.csv` (Beispiel: `tmp/plan-1627891234567.csv`).
    - Der Server liefert den absoluten Serverpfad der gespeicherten Datei im Header `X-Server-File`.

- Wo landet die Datei im Client?
  - Die Datei wird vom Browser als Download behandelt und üblicherweise im Standard-Download-Ordner des Betriebssystems gespeichert.

- UI:
  - Im Test-UI (`/planer/index.html`) findest du nun neben den anderen Buttons einen Button "Download Plan (XLS)". Er ruft den Endpoint auf und startet den Browser-Download. Zusätzlich ist die Datei serverseitig unter `tmp/plan-<timestamp>.csv` abgelegt.

## Available Endpoints

1. **Health Check:**
   ```
   GET /api/planer/health
   ```

2. **Generate schedule:**
   ```
   POST /api/planer/generate
   ```
   Body:
   ```json
   {
     "playerNames": ["Alice", "Bob", "Charlie", "..."],
     "numberOfRounds": 5,
     "playersPerRound": 4
   }
   ```

3. **Retrieve statistics:**
   ```
   GET /api/planer/statistics
   ```

4. **Retrieve pairings:**
   ```
   GET /api/planer/pairings
   ```

5. **Download last generated plan (CSV/XLS compatible):**
   ```
   GET /api/planer/download
   ```

For detailed API documentation see `API_ENDPOINTS.md`

## Run tests

```bash
./gradlew.bat test
```

For specific tests:
```bash
./gradlew.bat test --tests "ScheduleGenerationServiceTest"
./gradlew.bat test --tests "PlanerResourceUnitTest"
```

## FIXED ISSUES:
- Migrated from OpenLiberty to Quarkus DONE
  - Faster startup times
  - Better development experience with hot-reload
  - Native image support
- OpenAPI/Swagger UI integrated DONE
  - Interactive API documentation under /swagger-ui
  - Automatically generated documentation based on annotations
- REST services configured correctly DONE
  - Base Path: /api ([](http://localhost:8080/swagger-ui/))
  - All endpoints documented and tested
- simple UI layer (HTML/JS) for testing DONE
  - Basic frontend to interact with the API ([](http://localhost:8080/planer/index.html))
  - Simple forms to submit data and view results

## TODOs:
- Improve UI layer (with React)
- Persistence (database instead of in-memory or at least save to json file)
- Extended validations to match or specific needs for player availability
- Performance optimizations for large player lists
