# Намери Детска — Frontend

Изцяло vibe-coded с [OpenCode](https://opencode.ai). Намерете детски градини и ясли близо до вас в София — сортирани по разстояние, с интерактивна карта.

## Мотивация

Като родител в София получих имейл, че детето ми не е прието на първо класиране. Започнах да търся частна ясла, регистрирана в СРЗИ, и установих, че процесът е ненужно сложен — официалната система ИСОДЗ е трудна за навигация, списъкът с регистрирани частни ясли е заровен в PDF, а няма начин да сортирате заведенията по близост.

Този проект е бърза демонстрация на по-добро решение: търсене по адрес или локация, всички заведения на карта и сортиране по това колко са близо до вас. Надяваме се, че ИСОДЗ ще възприеме тези подобрения и този сайт ще може да бъде спрян за постоянно.

## Технологичен Стак

- **Framework:** Next.js 16 (App Router)
- **Language:** TypeScript
- **Styling:** Tailwind CSS v4
- **Data Fetching:** TanStack React Query
- **Maps:** MapLibre GL (OpenStreetMap тайлове)
- **Icons:** Lucide React
- **Theming:** next-themes (тъмен/светъл режим)
- **Geocoding:** Nominatim (OpenStreetMap)
- **Backend:** Java 25 / Quarkus (същото монорепо)

## Първи Стъпки

### Предварителни Изисквания

- Node.js 26+
- Работещ nameri-detska backend (виж [../backend/README.md](../backend/README.md)) или задайте `NEXT_PUBLIC_BACKEND_URL` към деплойнат backend

### Инсталация

```bash
npm install
```

### Разработка

```bash
npm run dev
```

Приложението работи на [http://localhost:3000](http://localhost:3000).

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

| Променлива                | Стойност по Подразбиране | Описание                           |
|---------------------------|-------------------------|------------------------------------|
| `NEXT_PUBLIC_BACKEND_URL` | `http://localhost:8080` | URL на backend API сървъра         |

## Структура на Проекта

```
├── app/
│   ├── layout.tsx          # Основен layout (шрифтове, провайдъри, навигация)
│   ├── page.tsx            # Начална страница
│   ├── globals.css         # Глобални стилове, CSS променливи, MapLibre предефинирания
│   ├── map/page.tsx        # Карта с търсене (основна)
│   ├── karta/page.tsx      # Пренасочване към /map
│   └── about/page.tsx      # За проекта (мотивация, източници на данни)
├── components/
│   ├── landing-page.tsx    # Геолокация и навигационна логика
│   ├── landing-hero.tsx    # Hero секция с търсене по локация
│   ├── map-page.tsx        # Основен компонент: търсене + карта + списък
│   ├── facility-map.tsx    # MapLibre GL карта с персонализирани SVG пинове
│   ├── facility-list.tsx   # Сортиращ и филтриращ списък със заведения
│   ├── theme-provider.tsx  # next-themes обвивка
│   ├── theme-switcher.tsx  # Превключване светъл/тъмен режим
│   ├── react-query-provider.tsx
│   └── navbar/             # Desktop & mobile навигационни компоненти
├── hooks/
│   └── use-facilities.ts   # React Query hooks за данни за заведения
├── lib/
│   ├── api-client.ts       # Backend API клиент
│   ├── geocode.ts          # Nominatim адресно геокодиране
│   └── utils.ts            # CSS class merging, форматиращи помощни функции
├── types/
│   └── facility.ts         # TypeScript интерфейси
└── public/
    └── logo.svg
```

## Лиценз

MIT
