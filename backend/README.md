# Намери Детска — Backend

REST API backend за **[nameri-detska.com](https://nameri-detska.com)**, уеб приложение, което помага на българските родители да намират детски заведения — градини, ясли и обединени заведения. Изграден с **Quarkus**, компилиран до **native binary** чрез GraalVM/Mandrel и деплойнат на **Google Cloud Run**.

## Технологичен Стак

|              |                                       |
| ------------ | ------------------------------------- |
| Език         | Java 25                               |
| Framework    | Quarkus 3.36.1                        |
| База данни   | PostgreSQL (JDBC)                     |
| Build Tool   | Maven                                 |
| Контейнер    | Docker + UBI9 minimal image           |
| CI/CD        | GitHub Actions → Google Cloud Run     |
| Code Gen     | Lombok                                |
| Форматиране  | Spotless + Eclipse formatter          |

## Структура на Проекта

```
src/main/java/com/nameri/detska/
├── KidFacility.java                 # Основна entity
├── KidFacilityType.java             # Enum: KINDERGARTEN, KINDERGARTEN_WITH_NURSERY, NURSERY
├── KidFacilityOwnershipType.java    # Enum: MUNICIPAL, PRIVATE_SRZI, PRIVATE_MON
├── KidFacilityRepository.java       # Raw JDBC достъп до данни
│── KidFacilityService.java          # Бизнес логика
│── KidFacilityResource.java         # JAX-RS REST endpoint
```

## API

### `POST /api/facilities`

Връща всички детски заведения. Отговорът се кешира за 5 минути.

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

| Enum стойност                | Български еквивалент   |
| ---------------------------- |-----------------------|
| `KINDERGARTEN`               | Детска градина        |
| `KINDERGARTEN_WITH_NURSERY`  | Детска градина с ясла |
| `NURSERY`                    | Ясла                  |
| `MUNICIPAL`                  | Общинска              |
| `PRIVATE_SRZI`               | Частна ясла (СРЗИ)    |
| `PRIVATE_MON`                | Частна градина (МОН)  |

## Първи Стъпки

### Предварителни Изисквания

- JDK 25 ([Eclipse Temurin](https://adoptium.net/) препоръчително)
- Maven 3.9+
- PostgreSQL (с база данни и таблица — виж [Настройка на База Данни](#настройка-на-база-данни))
- Docker (опционално, за контейнеризиран build/run)

### Настройка на База Данни

Създайте таблица `kid_facility` във вашата PostgreSQL база данни:

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

Конфигурирайте връзката чрез environment променливи:

```powershell
$env:QUARKUS_DATASOURCE_JDBC_URL = "jdbc:postgresql://localhost:5432/yourdb"
$env:QUARKUS_DATASOURCE_USERNAME = "youruser"
$env:QUARKUS_DATASOURCE_PASSWORD = "yourpassword"
```

### Стартиране в Dev Режим

```bash
mvn quarkus:dev
```

Hot reload е активиран. Дебъг логването е активно в `%dev` профила. API е достъпно на `http://localhost:8080`.

### Build

```bash
# JVM build
mvn package

# Native image build (изисква GraalVM/Mandrel)
mvn package -Pnative

# Native image build в контейнер (не е нужен локален GraalVM)
mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25
```

### Docker

```bash
# Изграждане на native image и пакетиране
docker build -t nameri-detska-backend .

# Стартиране
docker run -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host:5432/db \
  -e QUARKUS_DATASOURCE_USERNAME=user \
  -e QUARKUS_DATASOURCE_PASSWORD=pass \
  nameri-detska-backend
```

### Форматиране на Кода

```bash
mvn spotless:apply
```

База данни идентификационни данни, CORS origins за staging и друга специфична за средата конфигурация трябва да се предоставят чрез environment променливи по време на изпълнение. Виж [Quarkus configuration reference](https://quarkus.io/guides/config-reference).

## Принос

1. Форкнете хранилището
2. Стартирайте `mvn spotless:apply` преди къмитване, за да сте сигурни, че форматирането на кода отговаря на стила на проекта
3. Отворете pull request към `main`

Правилата за подредба на импорти и форматиране са дефинирани в `.formatter/nameri-detska-formatter.xml` и `.formatter/nameri-detska.importorder`.

## Лиценз

MIT
