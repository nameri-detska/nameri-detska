# nameri-detska frontend

Purely vibe-coded with [OpenCode](https://opencode.ai). Find kindergartens and nurseries near you in Sofia, Bulgaria —
sorted by distance, with an interactive map.

## Motivation

As a parent in Sofia, I received an email that my child was not accepted at first classification. I started searching
for a private SRZI-registered nursery and found the process needlessly complex — the official ISODZ system is hard to
navigate, the list of registered private nurseries is buried in a PDF, and there is no way to sort facilities by
proximity.

This project is a quick demonstration of a better experience: search by address or location, see all facilities on a
map, and sort them by how close they are to you. The hope is that ISODZ adopts these improvements and this site can be
shut down for good.

## Tech Stack

- **Framework:** Next.js 16 (App Router)
- **Language:** TypeScript
- **Styling:** Tailwind CSS v4
- **Data Fetching:** TanStack React Query
- **Maps:** MapLibre GL (OpenStreetMap tiles)
- **Icons:** Lucide React
- **Theming:** next-themes (dark/light mode)
- **Geocoding:** Nominatim (OpenStreetMap)
- **Backend:** Java/Kotlin API (separate repository)

## Getting Started

### Prerequisites

- Node.js 26+
- A running instance of the [nameri-detska backend](https://github.com/nameri-detska/backend) (or set
  `NEXT_PUBLIC_BACKEND_URL` to a deployed backend)

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The app runs at [http://localhost:3000](http://localhost:3000).

### Production Build

```bash
npm run build
npm start
```

### Docker

```bash
docker build -t nameri-detska-frontend .
docker run -p 3000:3000 -e NEXT_PUBLIC_BACKEND_URL=https://your-backend.example.com nameri-detska-frontend
```

## Environment Variables

| Variable                  | Default                 | Description                   |
|---------------------------|-------------------------|-------------------------------|
| `NEXT_PUBLIC_BACKEND_URL` | `http://localhost:8080` | URL of the backend API server |

## Project Structure

```
├── app/
│   ├── layout.tsx          # Root layout (fonts, providers, navbar)
│   ├── page.tsx            # Landing page
│   ├── globals.css         # Global styles, CSS variables, MapLibre overrides
│   ├── karta/page.tsx      # Map page with search
│   └── about/page.tsx      # About the project (motivation, data sources)
├── components/
│   ├── landing-page.tsx    # Geolocation & navigation logic
│   ├── landing-hero.tsx    # Hero section with location search
│   ├── map-page.tsx        # Main search + map + list layout
│   ├── facility-map.tsx    # MapLibre GL map with custom SVG pins
│   ├── facility-list.tsx   # Sortable, filterable facility list
│   ├── theme-provider.tsx  # next-themes wrapper
│   ├── theme-switcher.tsx  # Light/dark toggle
│   ├── react-query-provider.tsx
│   └── navbar/             # Desktop & mobile nav components
├── hooks/
│   └── use-facilities.ts   # React Query hooks for facility data
├── lib/
│   ├── api-client.ts       # Backend API client
│   ├── geocode.ts          # Nominatim address geocoding
│   └── utils.ts            # CSS class merging, formatting helpers
├── types/
│   └── facility.ts         # TypeScript interfaces
└── public/
    └── logo.svg
```

## Data Sources

- **Municipal kindergartens & nurseries:** ISODZ public REST API (`kg.sofia.bg`)
- **Private SRZI-registered nurseries:** PDF published on ISODZ (as of 08.06.2026)
- **Private MON-registered kindergartens:** Registry of the Ministry of Education and Science
- **Address geocoding:** Nominatim (user addresses), Google Maps Geocoding API (facility addresses, pre-computed)
- **Distance calculation:** Haversine formula (straight-line)

## License

MIT
