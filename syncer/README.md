# kindergarten-data-syncer

[![CI](https://github.com/nameri-detska/kindergarten-data-syncer/actions/workflows/ci-push.yaml/badge.svg)](https://github.com/nameri-detska/kindergarten-data-syncer/actions/workflows/ci-push.yaml)
![Java](https://img.shields.io/badge/Java-25-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.36.2-red)

Data synchronization pipeline that collects, geocodes, and stores childcare facility data for the **[Nameri
Detska](https://nameri-detska.com)** platform — helping parents in Sofia, Bulgaria find kindergartens and nurseries on
a map.

## How it works

The syncer runs an **ETL pipeline** that:

1. **Ingest** — fetches municipal kindergartens from the [kg.sofia.bg](https://kg.sofia.bg) REST API, downloads the
   official SRZI PDF listing licensed private nurseries, and queries the Ministry of Education (MON) public register
   for private kindergartens in Sofia
2. **Parse** — uses Google Gemini AI to extract structured data from the Bulgarian-language SRZI PDF
3. **Geocode** — resolves addresses to latitude/longitude via Google Maps, with Cyrillic-to-Latin transliteration for
   better results
4. **Persist** — replaces the entire `kid_facility` table in PostgreSQL with fresh data
5. **Override** — applies manual coordinate fixes from `overrides.txt` for addresses that geocode poorly

The result is a clean, geocoded dataset ready to be queried by the main application.

## Tech stack

| Category | Choice |
|---|---|
| Runtime | Java 25, Quarkus 3.36 |
| Database | PostgreSQL (Hibernate + Panache) |
| AI / PDF parsing | Google Gemini (LangChain4j) + Apache PDFBox |
| Geocoding | Google Maps Geocoding API |
| Build | Maven, uber-JAR |
| Container | Docker (SapMachine JRE Alpine) |
| CI/CD | GitHub Actions, Google Container Registry |

## Getting started

### Prerequisites

- Java 25
- PostgreSQL database
- [Google Maps API key](https://developers.google.com/maps/documentation/geocoding/overview)
- [Google Gemini API key](https://aistudio.google.com/apikey)

### Build

```bash
./mvnw package
```

Produces an uber-JAR at `target/kindergarten-data-syncer-dev.jar`.

### Run

Set the required environment variables and run the JAR:

```bash
export QUARKUS_LANGCHAIN4J_GEMINI_API_KEY="your-gemini-key"
export GOOGLE_MAPS_API_KEY="your-maps-key"
export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://localhost:5432/yourdb"
export QUARKUS_DATASOURCE_USERNAME="postgres"
export QUARKUS_DATASOURCE_PASSWORD="your-password"

java -jar target/kindergarten-data-syncer-dev.jar
```

The application runs the sync pipeline and exits with code `0` on success or `1` on failure.

### Docker

```bash
docker build -f src/main/docker/Dockerfile -t kindergarten-data-syncer .
docker run \
  -e QUARKUS_LANGCHAIN4J_GEMINI_API_KEY="..." \
  -e GOOGLE_MAPS_API_KEY="..." \
  -e QUARKUS_DATASOURCE_JDBC_URL="..." \
  -e QUARKUS_DATASOURCE_USERNAME="..." \
  -e QUARKUS_DATASOURCE_PASSWORD="..." \
  kindergarten-data-syncer
```

## Data sources

| Source | Type | URL |
|---|---|---|
| Municipal kindergartens | REST API | `https://kg.sofia.bg/api/public/kg/type/kinderGarden/all?filterType=by_region&kgType=0&regionId=0` |
| Licensed private nurseries (SRZI) | PDF download | `https://kg.sofia.bg/api/public/file/91f643b4bd6a4b179aaec3de09d028af` |
| Private kindergartens (MON register) | REST API | `https://ri-api.mon.bg/data/get/public-register` + `https://ri-api.mon.bg/data/get/institution` |

## Configuration

All configuration is driven by environment variables (standard Quarkus convention). Key properties:

| Variable | Description |
|---|---|
| `QUARKUS_DATASOURCE_JDBC_URL` | PostgreSQL JDBC connection string |
| `QUARKUS_DATASOURCE_USERNAME` | Database username |
| `QUARKUS_DATASOURCE_PASSWORD` | Database password |
| `GOOGLE_MAPS_API_KEY` | Google Maps Geocoding API key |
| `QUARKUS_LANGCHAIN4J_GEMINI_API_KEY` | Google Gemini API key |

The Gemini model defaults to `gemini-2.5-flash-lite` and can be overridden via
`quarkus.langchain4j.gemini.chat-model.model-id`.

## Manual overrides

Some addresses geocode imprecisely. The `overrides.txt` file (at the classpath root) allows manual correction of
coordinates and addresses. Format — pipe-delimited, one entry per line:

```
# Comments start with #
coordinates|address substring key|lat,lng
address|address substring key|corrected address
```

Example:
```
# ДГ №67 Чучулига - сграда бл. 8
coordinates|гр. София,|42.67080097,23.27524424
address|гр. София,|гр. София, ж.к. Бъкстон бл. № 8
```

Lines without three pipe-delimited fields are ignored. Only values that differ from the current DB state are
applied.

## Architecture

```
KidFacilitiesSyncRunner (@QuarkusMain)
└── SyncService (orchestrator)
    ├── MunicipalIngestionService              → kg.sofia.bg REST API
    ├── PrivateSrziNurseryIngestionService     → SRZI PDF download
    │   └── AIPrivateNurseryPdfParser          → Gemini AI extraction
    ├── PrivateMonKindergartenIngestionService → ri-api.mon.bg REST API
    ├── GoogleMapsGeocodingService             → Google Maps API
    ├── KidFacilityService                     → PostgreSQL (Panache)
    └── ManualFacilityOverrideService          → overrides.txt
```

## Database schema

The `kid_facility` table is fully replaced on each run:

| Column | Type | Description |
|---|---|---|
| `id` | UUID | Auto-generated primary key |
| `name` | text | Facility name |
| `kid_facility_type` | enum | `KINDERGARTEN`, `KINDERGARTEN_WITH_NURSERY`, `NURSERY` |
| `kid_facility_ownership_type` | enum | `MUNICIPAL`, `PRIVATE_SRZI`, `PRIVATE_MON` |
| `address` | text | Street address |
| `latitude` | double | Geocoded latitude |
| `longitude` | double | Geocoded longitude |

## License

MIT

---

Part of the **[Nameri Detska](https://nameri-detska.com)** project — find childcare near you in Sofia.
