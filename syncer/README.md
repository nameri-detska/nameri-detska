# kindergarten-data-syncer

[![CI](https://github.com/nameri-detska/kindergarten-data-syncer/actions/workflows/ci-push.yaml/badge.svg)](https://github.com/nameri-detska/kindergarten-data-syncer/actions/workflows/ci-push.yaml)
![Java](https://img.shields.io/badge/Java-25-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.36.2-red)

Синхронизиращ pipeline за данни, който събира, геокодира и съхранява данни за детски заведения за платформата *
*[Намери Детска](https://nameri-detska.com)** — помага на родители в София да намират детски градини и ясли на карта.

## Как Работи

Syncer-ът изпълнява **ETL pipeline**, който:

1. **Извлича** — заявки към общински детски градини от [kg.sofia.bg](https://kg.sofia.bg) REST API, изтегля официалния
   СРЗИ PDF с лицензирани частни ясли и заявки към публичния регистър на Министерството на образованието (МОН) за частни
   детски градини в София
2. **Парсира** — използва Google Gemini AI за извличане на структурирани данни от СРЗИ PDF на български език
3. **Геокодира** — преобразува адреси в географска ширина/дължина чрез Google Maps, с транслитерация от кирилица на
   латиница за по-добри резултати
4. **Записва** — заменя цялата таблица `kid_facility` в PostgreSQL с актуални данни
5. **Коригира** — прилага ръчни корекции на координати от `overrides.txt` за адреси, които се геокодират неточно

Резултатът е чист, геокодиран набор от данни, готов за заявки от основното приложение.

## Технологичен Стак

| Категория          | Избор                                       |
|--------------------|---------------------------------------------|
| Runtime            | Java 25, Quarkus 3.36                       |
| База данни         | PostgreSQL (Hibernate + Panache)            |
| AI / PDF парсиране | Google Gemini (LangChain4j) + Apache PDFBox |
| Геокодиране        | Google Maps Geocoding API                   |
| Build              | Maven, uber-JAR                             |
| Контейнер          | Docker (SapMachine JRE Alpine)              |
| CI/CD              | GitHub Actions, Google Container Registry   |

## Първи Стъпки

### Предварителни Изисквания

- Java 25
- PostgreSQL база данни
- [Google Maps API ключ](https://developers.google.com/maps/documentation/geocoding/overview)
- [Google Gemini API ключ](https://aistudio.google.com/apikey)

### Build

```bash
./mvnw package
```

Генерира uber-JAR в `target/kindergarten-data-syncer-dev.jar`.

### Стартиране

Задайте необходимите environment променливи и стартирайте JAR файла:

```bash
export QUARKUS_LANGCHAIN4J_GEMINI_API_KEY="your-gemini-key"
export GOOGLE_MAPS_API_KEY="your-maps-key"
export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://localhost:5432/yourdb"
export QUARKUS_DATASOURCE_USERNAME="postgres"
export QUARKUS_DATASOURCE_PASSWORD="your-password"

java -jar target/kindergarten-data-syncer-dev.jar
```

Приложението изпълнява sync pipeline-а и приключва с код `0` при успех или `1` при грешка.

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

## Източници на Данни

| Източник                             | Тип      | URL                                                                                                                                 |
|--------------------------------------|----------|-------------------------------------------------------------------------------------------------------------------------------------|
| Общински детски градини              | REST API | `https://kg.sofia.bg/api/public/kg/type/kinderGarden/all?filterType=by_region&kgType=0&regionId=0`                                  |
| Лицензирани частни ясли (СРЗИ)       | PDF файл | `https://kg.sofia.bg/api/public/file/91f643b4bd6a4b179aaec3de09d028af` · [Източник](https://kg.sofia.bg/#/manual)                   |
| Частни детски градини (МОН регистър) | REST API | [`/public-register`](https://ri-api.mon.bg/data/get/public-register) + [`/institution`](https://ri-api.mon.bg/data/get/institution) |

API-то на МОН използва POST заявки:

`/public-register`:

```json
{
  "region": [
    22,
    23
  ],
  "instType": [
    2
  ],
  "financialSchoolType": [
    3
  ],
  "isRIActive": 1
}
```

`/institution` (`instid` и `procID` се вземат от отговора на `/public-register`):

```json
{
  "instid": "2200016",
  "procID": "9643"
}
```

## Конфигурация

Цялата конфигурация се задава чрез environment променливи (стандартна Quarkus конвенция). Ключови настройки:

| Променлива                           | Описание                          |
|--------------------------------------|-----------------------------------|
| `QUARKUS_DATASOURCE_JDBC_URL`        | PostgreSQL JDBC connection string |
| `QUARKUS_DATASOURCE_USERNAME`        | Потребителско име за база данни   |
| `QUARKUS_DATASOURCE_PASSWORD`        | Парола за база данни              |
| `GOOGLE_MAPS_API_KEY`                | Google Maps Geocoding API ключ    |
| `QUARKUS_LANGCHAIN4J_GEMINI_API_KEY` | Google Gemini API ключ            |

Gemini моделът по подразбиране е `gemini-2.5-flash-lite` и може да бъде сменен чрез
`quarkus.langchain4j.gemini.chat-model.model-id`.

## Ръчни Корекции

Някои адреси се геокодират неточно. Файлът `overrides.txt` (в корена на classpath) позволява ръчна корекция на
координати и адреси. Формат — разделен с `|`, по един запис на ред:

```
# Коментарите започват с #
coordinates|ключ от адрес|ширина,дължина
address|ключ от адрес|коригиран адрес
```

Пример:

```
# ДГ №67 Чучулига - сграда бл. 8
coordinates|гр. София,|42.67080097,23.27524424
address|гр. София,|гр. София, жк. Бъкстон бл. № 8
```

Редове без три полета разделени с `|` се игнорират. Прилагат се само стойности, които се различават от текущото
състояние в базата данни.

## Архитектура

```
KidFacilitiesSyncRunner (@QuarkusMain)
└── SyncService (оркестратор)
    ├── MunicipalIngestionService              → kg.sofia.bg REST API
    ├── PrivateSrziNurseryIngestionService     → СРЗИ PDF файл
    │   └── AIPrivateNurseryPdfParser          → Gemini AI извличане
    ├── PrivateMonKindergartenIngestionService → ri-api.mon.bg REST API
    ├── GoogleMapsGeocodingService             → Google Maps API
    ├── KidFacilityService                     → PostgreSQL (Panache)
    └── ManualFacilityOverrideService          → overrides.txt
```

## Схема на База Данни

Таблицата `kid_facility` се заменя изцяло при всяко изпълнение:

| Колона                        | Тип    | Описание                                               |
|-------------------------------|--------|--------------------------------------------------------|
| `id`                          | UUID   | Автоматично генериран първичен ключ                    |
| `name`                        | text   | Име на заведението                                     |
| `kid_facility_type`           | enum   | `KINDERGARTEN`, `KINDERGARTEN_WITH_NURSERY`, `NURSERY` |
| `kid_facility_ownership_type` | enum   | `MUNICIPAL`, `PRIVATE_SRZI`, `PRIVATE_MON`             |
| `address`                     | text   | Уличен адрес                                           |
| `latitude`                    | double | Геокодирана географска ширина                          |
| `longitude`                   | double | Геокодирана географска дължина                         |

## Лиценз

MIT

---

Част от проекта **[Намери Детска](https://nameri-detska.com)** — намерете детска градина или ясла близо до вас в София.
