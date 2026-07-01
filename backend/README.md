# Nameri Detska — Backend

REST API backend for **[nameri-detska.com](https://nameri-detska.com)**, a web application that helps Bulgarian parents find children's facilities — kindergartens, nurseries, and combined facilities. Built with **Quarkus**, compiled to a **native binary** via GraalVM/Mandrel, and deployed on **Google Cloud Run**.

## Tech Stack

|              |                                       |
| ------------ | ------------------------------------- |
| Language     | Java 25                               |
| Framework    | Quarkus 3.36.1                        |
| Database     | PostgreSQL (JDBC)                     |
| Build Tool   | Maven                                 |
| Container    | Docker + UBI9 minimal image           |
| CI/CD        | GitHub Actions → Google Cloud Run     |
| Code Gen     | Lombok                                |
| Formatting   | Spotless + Eclipse formatter          |

## Project Structure

```
src/main/java/com/nameri/detska/
├── KidFacility.java                 # Core entity
├── KidFacilityType.java             # Enum: KINDERGARTEN, KINDERGARTEN_WITH_NURSERY, NURSERY
├── KidFacilityOwnershipType.java    # Enum: MUNICIPAL, PRIVATE_SRZI, PRIVATE_MON
├── KidFacilityRepository.java       # Raw JDBC data access
│── KidFacilityService.java          # Business logic layer
│── KidFacilityResource.java         # JAX-RS REST endpoint
```

## API

### `POST /api/facilities`

Returns all kid facilities. Response is cached for 5 minutes.

**Response** `200 OK`

```json
[
  {
    "id": "uuid",
    "name": "ДГ Слънце",
    "kidFacilityType": "KINDERGARTEN",
    "kidFacilityOwnershipType": "MUNICIPAL",
    "address": "ул. Примерна 12, София",
    "latitude": 42.6977,
    "longitude": 23.3219
  }
]
```

| Enum value                    | Bulgarian equivalent  |
| ----------------------------- |-----------------------|
| `KINDERGARTEN`                | Детска градина        |
| `KINDERGARTEN_WITH_NURSERY`   | Детска градина с ясла |
| `NURSERY`                     | Ясла                  |
| `MUNICIPAL`                   | Общинска              |
| `PRIVATE_SRZI`                | Частна ясла (СРЗИ)    |
| `PRIVATE_MON`                 | Частна градина (МОН)  |

## Getting Started

### Prerequisites

- JDK 25 ([Eclipse Temurin](https://adoptium.net/) recommended)
- Maven 3.9+
- PostgreSQL (with a database and table — see [Database Setup](#database-setup))
- Docker (optional, for containerized build/run)

### Database Setup

Create the `kid_facility` table in your PostgreSQL database:

```sql
CREATE TABLE kid_facility (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    kid_facility_type TEXT NOT NULL,
    kid_facility_ownership_type TEXT NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);
```

Configure the connection via environment variables:

```powershell
$env:QUARKUS_DATASOURCE_JDBC_URL = "jdbc:postgresql://localhost:5432/yourdb"
$env:QUARKUS_DATASOURCE_USERNAME = "youruser"
$env:QUARKUS_DATASOURCE_PASSWORD = "yourpassword"
```

### Run in Dev Mode

```bash
mvn quarkus:dev
```

Hot reload enabled. Debug logging is active in the `%dev` profile. API available at `http://localhost:8080`.

### Build

```bash
# JVM build
mvn package

# Native image build (requires GraalVM/Mandrel)
mvn package -Pnative

# Native image build inside a container (no local GraalVM needed)
mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25
```

### Docker

```bash
# Build the native image and package it
docker build -t nameri-detska-backend .

# Run
docker run -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host:5432/db \
  -e QUARKUS_DATASOURCE_USERNAME=user \
  -e QUARKUS_DATASOURCE_PASSWORD=pass \
  nameri-detska-backend
```

### Format Code

```bash
mvn spotless:apply
```

Database credentials, CORS origins for staging, and other environment-specific config must be provided via environment variables at runtime. See [Quarkus configuration reference](https://quarkus.io/guides/config-reference).

## Contributing

1. Fork the repository
2. Run `mvn spotless:apply` before committing to ensure code formatting matches the project style
3. Open a pull request against `main`

Import ordering and formatting rules are defined in `.formatter/nameri-detska-formatter.xml` and `.formatter/nameri-detska.importorder`.

## License

MIT
