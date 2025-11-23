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

## Server starten

```bash
./gradlew.bat quarkusDev
```

Der Server startet auf `http://localhost:8080` mit automatischem Hot-Reload bei Codeänderungen.

## API Access

- **Base URL:** `http://localhost:8080/api`
- **Health Check:** `http://localhost:8080/api/planer/health`
- **Swagger UI:** `http://localhost:8080/swagger-ui`

## Verfügbare Endpunkte

1. **Health Check:**
   ```
   GET /api/planer/health
   ```

2. **Schedule generieren:**
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

3. **Statistiken abrufen:**
   ```
   GET /api/planer/statistics
   ```

4. **Paarungen abrufen:**
   ```
   GET /api/planer/pairings
   ```

Für detaillierte API-Dokumentation siehe `API_ENDPOINTS.md`

## Tests ausführen

```bash
./gradlew.bat test
```

Für spezifische Tests:
```bash
./gradlew.bat test --tests "ScheduleGenerationServiceTest"
./gradlew.bat test --tests "PlanerResourceUnitTest"
```

## FIXED ISSUES:
- Migriert von OpenLiberty zu Quarkus DONE
  - Schnellere Startup-Zeiten
  - Bessere Development Experience mit Hot-Reload
  - Native Image Support
- OpenAPI/Swagger UI eingebunden DONE
  - Interaktive API-Dokumentation unter /swagger-ui
  - Automatisch generierte Dokumentation basierend auf Annotations
- REST-Services korrekt konfiguriert DONE
  - Base Path: /api
  - Alle Endpunkte dokumentiert und getestet

## TODOs:
- UI-Layer erstellen (Frontend mit React/Vue/Angular)
- Persistierung (Datenbank statt In-Memory)
- Authentication & Authorization
- API-Rate Limiting
- Erweiterte Validierungen
- Performance-Optimierungen für große Spielerlisten
          RestAssured.port = 9980;
          RestAssured.basePath = "/bigb-planer-project/api";
      }
  
      @Test
      public void testPlanerResourceIsAccessible() {
          given()
              .when()
              .get("/planer")
              .then()
              .statusCode(200)
              .body("message", equalTo("Expected response message"));
      }
  }
