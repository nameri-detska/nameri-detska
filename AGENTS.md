# AGENTS.md

This file provides guidance for AI agents working on this repository.

## Project Overview

Намери Детска (nameri-detska) — a web app helping parents find kindergartens and nurseries near them in Sofia, Bulgaria.
Monorepo with three modules:

| Module      | Purpose           | Stack                                                            |
|-------------|-------------------|------------------------------------------------------------------|
| `frontend/` | End-user web app  | Next.js 16 (App Router), React 19, TypeScript 6, Tailwind CSS v4 |
| `backend/`  | REST API          | Java 25, Quarkus 3.37, PostgreSQL (raw JDBC), GraalVM native     |
| `syncer/`   | ETL data pipeline | Java 25, Quarkus 3.37, Hibernate/Panache, LangChain4j Gemini     |

Live: https://nameri-detska.com  | Deployed on Google Cloud Run via GitHub Actions.

## Build & Run Commands

### Frontend

```bash
cd frontend
npm install
npm run dev          # Dev server on localhost:3000
npm run build        # Production build (standalone output)
npm run lint         # ESLint (next lint)
```

### Backend

```bash
cd backend
./mvnw quarkus:dev   # Dev mode with hot reload on localhost:8080
./mvnw package       # Build JVM jar
./mvnw package -Pnative  # Build native binary (GraalVM)
```

### Syncer

```bash
cd syncer
./mvnw quarkus:dev   # Run sync pipeline locally
./mvnw package       # Build uber-JAR
./mvnw test          # Run tests
```

### Code Formatting (Java)

```bash
mvn spotless:apply   # Applies Eclipse formatter config from .formatter/
```

## Architecture

### Frontend (`frontend/`)

- **App Router** with `"use client"` on interactive components, server components for layout/prefetching
- **Path alias:** `@/*` → project root
- **Data fetching:** TanStack React Query v5 in `hooks/use-facilities.ts`; prefetch + `HydrationBoundary` in root layout
- **Styling:** Tailwind v4 utility classes + CSS custom properties for dark/light mode (via `next-themes`)
- **Maps:** MapLibre GL v5 with OpenStreetMap tiles
- **Utility:** `cn()` in `lib/utils.ts` = `clsx` + `tailwind-merge`
- **Module layout:** `app/` (routes), `components/` (shared), `hooks/`, `lib/`, `types/`

### Backend (`backend/`)

- **Layered:** `KidFacilityResource` (JAX-RS) → `KidFacilityService` (@CacheResult) → `KidFacilityRepository` (raw JDBC)
- **DI:** Quarkus ARC CDI, `@ApplicationScoped`, `@Inject` / Lombok `@RequiredArgsConstructor`
- **Caching:** Caffeine cache, 1-hour TTL on facility data
- **CORS:** Allows localhost:3000, nameri-detska.com, Cloud Run frontend URL

### Syncer (`syncer/`)

- **@QuarkusMain** entry point: `KidFacilitiesSyncRunner`
- **Interface-driven ingestion:** `KidFacilityIngestionService` interface with multiple implementations (municipal, SRZI
  PDF, MON API)
- **AI:** LangChain4j + Google Gemini for extracting nursery data from PDFs (pre-processed with PDFBox)
- **Geocoding:** Google Maps Geocoding API
- **Manual override:** `CoordinateOverridesService` for hardcoded corrections

## Code Conventions

### Java

- **Package:** `com.nameri.detska[.kindergarten.data.syncer.{feature}]`
- **Lombok:** `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`
- **Naming:** `XxxService`, `XxxRepository`, `XxxResource`, `XxxDto`; records for DTOs
- **Formatting:** 4-space indent, end-of-line braces, custom import order (`.formatter/`)
- **Tests:** JUnit 5 + Mockito, `Mockito.mock()` / `when()` pattern, constructor injection of mocks
- **CRITICAL:** Never adapt test expectations to match buggy production behavior. If a test fails because production
  code has a bug, fix the production code — do not change the test to work around the bug.

### TypeScript/React

- **Naming:** PascalCase for components/interfaces, camelCase for functions/variables, kebab-case for files
- **Styling:** Tailwind utility classes; `cn()` for conditional merging
- **Theming:** `--bg`, `--text`, etc. CSS variables defined in `globals.css`

## Environment

- `frontend/.env.development` — `NEXT_PUBLIC_BACKEND_URL=http://localhost:8080`
- `frontend/.env.production` — Points to Cloud Run backend
- Backend DB credentials and API keys (Google Maps, Gemini) via environment variables at runtime

## CI/CD

- Push to `main` triggers path-filtered builds via `.github/workflows/ci-push.yml`
- Only changed modules are rebuilt and deployed to Google Cloud Run
- Dependabot configured for weekly Maven + GitHub Actions updates
- Separate weekly workflow auto-updates npm dependencies
