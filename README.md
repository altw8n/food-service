# DeliveryService — Микросервисная система доставки еды
# DeliveryService — Food Delivery Microservice System

---

## RU — Русский

### Описание

Учебный проект микросервисной системы доставки еды на базе Spring Boot. Реализует полный цикл обработки заказа: от создания до назначения курьера. Сервисы взаимодействуют синхронно через HTTP и асинхронно через Apache Kafka.

### Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                              │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP
                         ▼
┌────────────────────────────────────────┐
│           order-service :8080          │
│  - Создание заказов                    │
│  - Инициация оплаты                    │
│  - Обновление статуса заказа           │
└───┬──────────────┬───────────────┬─────┘
    │ HTTP         │ Kafka         │ Kafka
    │              │ orders.events │ delivery.events
    ▼              ▼               ▼
┌───────────┐  ┌──────────────────────────────────┐
│  payment  │  │        delivery-service :8082     │
│  service  │  │  - Назначение курьера             │
│  :8081    │  │  - Отправка события о назначении  │
└───────────┘  └──────────────────────────────────┘
```

### Сервисы

| Сервис            | Порт | Назначение                                         |
|-------------------|------|----------------------------------------------------|
| `order-service`   | 8080 | Управление заказами, оркестрация процесса          |
| `payment-service` | 8081 | Обработка платежей                                 |
| `delivery-service`| 8082 | Назначение курьера, отправка событий о доставке    |

### Стек технологий

- **Java 17**
- **Spring Boot 3.5**
- **Spring Data JPA** — работа с БД
- **Spring Kafka** — асинхронный обмен событиями
- **Spring Web / HttpExchange** — синхронное межсервисное взаимодействие
- **PostgreSQL 16** — хранение данных
- **Apache Kafka (Confluent 8.1)** — брокер сообщений
- **Lombok / MapStruct** — уменьшение бойлерплейта
- **Docker Compose** — локальная инфраструктура
- **Gradle (Kotlin DSL)** — сборка, многомодульный проект

### Структура проекта

```
deliveryService/
├── common-libs/          # Общие DTO и Kafka-события (OrderPaidEvent, DeliveryAssignedEvent и др.)
├── order-service/        # Сервис заказов
├── payment-service/      # Сервис платежей
├── delivery-service/     # Сервис доставки
├── docker-compose.yaml   # PostgreSQL + Kafka
└── settings.gradle.kts   # Многомодульная сборка
```

### Kafka-топики и события

| Топик             | Продюсер          | Консюмер            | Событие                 |
|-------------------|-------------------|---------------------|-------------------------|
| `orders.events`   | order-service     | delivery-service    | `OrderPaidEvent`        |
| `delivery.events` | delivery-service  | order-service       | `DeliveryAssignedEvent` |

### Полный цикл заказа

```
1. POST /api/orders          — создание заказа (статус: PENDING_PAYMENT)
2. POST /api/orders/{id}/pay — оплата заказа через payment-service
3.   order-service → Kafka (orders.events) → delivery-service
4.   delivery-service назначает случайного курьера и ETA
5.   delivery-service → Kafka (delivery.events) → order-service
6.   order-service обновляет заказ (статус: DELIVERY_ASSIGNED)
7. GET /api/orders/{id}      — получение итогового статуса заказа
```

### Статусы заказа

| Статус               | Описание                              |
|----------------------|---------------------------------------|
| `PENDING_PAYMENT`    | Заказ создан, ожидает оплаты          |
| `PAID`               | Оплата прошла успешно                 |
| `PAYMENT_FAILED`     | Оплата отклонена                      |
| `DELIVERY_ASSIGNED`  | Курьер назначен                       |
| `DELIVERED`          | Заказ доставлен                       |

### Методы оплаты

| Метод           | Результат           |
|-----------------|---------------------|
| `CARD`          | Всегда успешно      |
| `YANDEX_SPLIT`  | Всегда успешно      |
| `QR`            | Всегда отклоняется  |

### Быстрый старт

#### Требования

- JDK 17+
- Docker Desktop
- Gradle (или используй `./gradlew`)

#### 1. Запустить инфраструктуру

```bash
docker compose up -d
```

#### 2. Собрать проект

```bash
./gradlew build
```

#### 3. Запустить сервисы

Запускай каждый сервис отдельно из корня проекта (через IntelliJ IDEA или CLI):

```bash
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :delivery-service:bootRun
```

### REST API

#### Создать заказ

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": 1,
  "address": "ул. Пушкина, д. 10",
  "items": [
    { "itemId": 101, "quantity": 2, "name": "Бургер" },
    { "itemId": 102, "quantity": 1, "name": "Картошка фри" }
  ]
}
```

#### Оплатить заказ

```http
POST http://localhost:8080/api/orders/{id}/pay
Content-Type: application/json

{
  "paymentMethod": "CARD"
}
```

#### Получить заказ

```http
GET http://localhost:8080/api/orders/{id}
```

### Сброс данных (без перезапуска контейнеров)

**PostgreSQL:**
```bash
docker exec -it <postgres-container> psql -U postgres -d orders -c \
  "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres;"
```

**Kafka:**
```bash
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --delete --topic orders.events
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --delete --topic delivery.events
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --create --topic orders.events --partitions 1 --replication-factor 1
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --create --topic delivery.events --partitions 1 --replication-factor 1
```

---

## EN — English

### Overview

A study project implementing a microservice-based food delivery system built with Spring Boot. It covers the full order lifecycle: from creation to courier assignment. Services communicate synchronously over HTTP and asynchronously through Apache Kafka.

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                              │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP
                         ▼
┌────────────────────────────────────────┐
│           order-service :8080          │
│  - Create orders                       │
│  - Initiate payments                   │
│  - Update order status                 │
└───┬──────────────┬───────────────┬─────┘
    │ HTTP         │ Kafka         │ Kafka
    │              │ orders.events │ delivery.events
    ▼              ▼               ▼
┌───────────┐  ┌──────────────────────────────────┐
│  payment  │  │        delivery-service :8082     │
│  service  │  │  - Assign courier                 │
│  :8081    │  │  - Publish delivery event         │
└───────────┘  └──────────────────────────────────┘
```

### Services

| Service           | Port | Responsibility                                    |
|-------------------|------|---------------------------------------------------|
| `order-service`   | 8080 | Order management, process orchestration           |
| `payment-service` | 8081 | Payment processing                                |
| `delivery-service`| 8082 | Courier assignment, delivery event publishing     |

### Tech Stack

- **Java 17**
- **Spring Boot 3.5**
- **Spring Data JPA** — database access
- **Spring Kafka** — asynchronous event-driven communication
- **Spring Web / HttpExchange** — synchronous inter-service HTTP calls
- **PostgreSQL 16** — persistent storage
- **Apache Kafka (Confluent 8.1)** — message broker
- **Lombok / MapStruct** — boilerplate reduction
- **Docker Compose** — local infrastructure
- **Gradle (Kotlin DSL)** — build system, multi-module project

### Project Structure

```
deliveryService/
├── common-libs/          # Shared DTOs and Kafka events (OrderPaidEvent, DeliveryAssignedEvent, etc.)
├── order-service/        # Order service
├── payment-service/      # Payment service
├── delivery-service/     # Delivery service
├── docker-compose.yaml   # PostgreSQL + Kafka
└── settings.gradle.kts   # Multi-module build definition
```

### Kafka Topics & Events

| Topic             | Producer          | Consumer            | Event                   |
|-------------------|-------------------|---------------------|-------------------------|
| `orders.events`   | order-service     | delivery-service    | `OrderPaidEvent`        |
| `delivery.events` | delivery-service  | order-service       | `DeliveryAssignedEvent` |

### Order Lifecycle

```
1. POST /api/orders          — create order (status: PENDING_PAYMENT)
2. POST /api/orders/{id}/pay — pay via payment-service over HTTP
3.   order-service → Kafka (orders.events) → delivery-service
4.   delivery-service assigns a random courier and ETA
5.   delivery-service → Kafka (delivery.events) → order-service
6.   order-service updates the order (status: DELIVERY_ASSIGNED)
7. GET /api/orders/{id}      — fetch final order state
```

### Order Statuses

| Status               | Description                          |
|----------------------|--------------------------------------|
| `PENDING_PAYMENT`    | Order created, awaiting payment      |
| `PAID`               | Payment successful                   |
| `PAYMENT_FAILED`     | Payment declined                     |
| `DELIVERY_ASSIGNED`  | Courier assigned                     |
| `DELIVERED`          | Order delivered                      |

### Payment Methods

| Method          | Result           |
|-----------------|------------------|
| `CARD`          | Always succeeds  |
| `YANDEX_SPLIT`  | Always succeeds  |
| `QR`            | Always fails     |

### Quick Start

#### Prerequisites

- JDK 17+
- Docker Desktop
- Gradle (or use the `./gradlew` wrapper)

#### 1. Start infrastructure

```bash
docker compose up -d
```

#### 2. Build the project

```bash
./gradlew build
```

#### 3. Run services

Start each service separately from the project root (via IntelliJ IDEA or CLI):

```bash
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :delivery-service:bootRun
```

### REST API

#### Create Order

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": 1,
  "address": "123 Main St",
  "items": [
    { "itemId": 101, "quantity": 2, "name": "Burger" },
    { "itemId": 102, "quantity": 1, "name": "French Fries" }
  ]
}
```

#### Pay for Order

```http
POST http://localhost:8080/api/orders/{id}/pay
Content-Type: application/json

{
  "paymentMethod": "CARD"
}
```

#### Get Order

```http
GET http://localhost:8080/api/orders/{id}
```

### Resetting Data (without restarting containers)

**PostgreSQL:**
```bash
docker exec -it <postgres-container> psql -U postgres -d orders -c \
  "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres;"
```

**Kafka:**
```bash
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --delete --topic orders.events
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --delete --topic delivery.events
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --create --topic orders.events --partitions 1 --replication-factor 1
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --create --topic delivery.events --partitions 1 --replication-factor 1
```
