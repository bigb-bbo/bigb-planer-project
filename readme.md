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

```bash
./gradlew.bat quarkusDev
```

The server starts at `http://localhost:8080` with automatic hot-reload on code changes.

## API Access

- **Base URL:** `http://localhost:8080/api`
- **Health Check:** `http://localhost:8080/api/planer/health`
- **Swagger UI:** `http://localhost:8080/swagger-ui`
- **Simple UI for testing:** `http://localhost:8080/planer/index.html`

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
