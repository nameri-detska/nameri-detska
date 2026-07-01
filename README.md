# Nameri Detska (Намери Детска)

[![CI](https://github.com/nameri-detska/nameri-detska/actions/workflows/ci-push.yml/badge.svg)](https://github.com/nameri-detska/nameri-detska/actions/workflows/ci-push.yml)
![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)

Find kindergartens and nurseries near you in Sofia, Bulgaria — sorted by distance, with an interactive map.

Live at **[nameri-detska.com](https://nameri-detska.com)**

## Motivation

As a parent in Sofia, I received an email that my child was not accepted at first classification. Searching for alternatives was unnecessarily complex: the official ISODZ system is hard to navigate, the list of registered private nurseries is buried in a PDF, and there is no way to sort facilities by proximity. This project demonstrates a better experience. The hope is that ISODZ adopts these improvements so this site can be shut down for good.

## Architecture

```
┌──────────┬──────────────────────────────────────┐
│  syncer  │  ETL pipeline (one-shot)             │
│  Java 25 │  Ingests → Geocodes → Persists data  │
│ Quarkus  │  Sources: ISODZ API, SRZI PDF, MON   │
└────┬─────┴──────────────────┬───────────────────┘
     │                        │
     ▼                        ▼
┌─────────┐           ┌──────────────┐
│PostgreSQL│◄──────────│   backend    │
│         │  JDBC     │  Java 25     │
└─────────┘           │  Quarkus     │
                      │  GET /api/   │
                      │  facilities  │
                      └──────┬───────┘
                             │  JSON
                             ▼
                      ┌──────────────┐
                      │   frontend   │
                      │  Next.js 16  │
                      │  TypeScript  │
                      │  MapLibre GL │
                      └──────────────┘
```

- **syncer** — Collects facility data from three Bulgarian government sources, parses PDFs with Gemini AI, geocodes addresses via Google Maps, and writes to PostgreSQL.
- **backend** — Quarkus REST API serving geocoded facility data with 1-hour caching.
- **frontend** — Next.js web app with interactive map, location search, filtering by type/ownership, and distance-based sorting.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 16, React 19, TypeScript, Tailwind CSS v4, MapLibre GL, TanStack React Query |
| Backend | Java 25, Quarkus, PostgreSQL (JDBC), GraalVM native image |
| ETL | Java 25, Quarkus, Hibernate + Panache, LangChain4j Gemini, PDFBox, Google Maps API |
| CI/CD | GitHub Actions, Google Cloud Run, Artifact Registry |
| Build | Maven (Java), npm workspaces (frontend) |

## Getting Started

### Prerequisites

- **Node.js 26+** (frontend)
- **JDK 25** (backend & syncer)
- **PostgreSQL** (backend & syncer)
- **Maven 3.9+** (Java builds)
- **Google Maps API key** (syncer geocoding)
- **Google Gemini API key** (syncer PDF parsing)

### Running everything locally

1. **Set up PostgreSQL** — create a database and the `kid_facility` table (see [backend/README.md](backend/README.md#database-setup)).
2. **Populate data** — run the syncer to fetch and geocode facility data (see [syncer/README.md](syncer/README.md)).
3. **Start the backend** — `cd backend && mvn quarkus:dev` (runs on port 8080).
4. **Start the frontend** — `cd frontend && npm run dev` (runs on port 3000).

Open [http://localhost:3000](http://localhost:3000).

### Docker

Each component has its own Dockerfile. See the per-component READMEs for instructions.

## Project Structure

```
nameri-detska/
├── frontend/        # Next.js 16 web app
├── backend/         # Quarkus REST API
├── syncer/          # ETL data pipeline
├── .github/         # CI/CD workflows & Dependabot
├── .formatter/      # Java code style (shared)
├── package.json     # npm workspace root
└── pom.xml          # Maven parent POM
```

## Data Sources

| Source | Type |
|---|---|
| [ISODZ](https://kg.sofia.bg) municipal registry | REST API |
| Licensed private nurseries (SRZI) | PDF download |
| [MON public register](https://ri-api.mon.bg) of private kindergartens | REST API |
| Address geocoding | Nominatim (user), Google Maps (facilities) |

## Contributing

1. Fork the repository
2. Run `mvn spotless:apply` before committing Java changes to match the project's code style
3. Open a pull request against `main`

Code formatting rules are in `.formatter/`.

## License

MIT — see each component's README for details.
