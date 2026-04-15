# AetherFlow Server

Spring Boot backend for the AetherFlow project.

## Tech Stack

- Java 21
- Spring Boot 3
- MyBatis-Plus
- MySQL
- Sa-Token
- SpringDoc OpenAPI

## Local Development

1. Copy `src/main/resources/application-local.example.yml` to `src/main/resources/application-local.yml`.
2. Update the local database connection settings.
3. Initialize the database with `sql/aether-flow.sql`.
4. Start the application with your IDE or:

```bash
./mvnw spring-boot:run
```

## Documentation

- Development notes: `docs/`
- API docs after startup: `/swagger-ui.html`
