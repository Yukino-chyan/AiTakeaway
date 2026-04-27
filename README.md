# AiTakeaway

An AI-assisted food delivery platform. Customers can browse merchants, order food through a conversational AI assistant, manage their cart and orders, and leave reviews. Merchants manage their menus and incoming orders via a separate web portal.

---

## Project Structure

```
AiTakeaway/
├── src/                    # Spring Boot backend
├── customer-uniapp/        # Customer mobile app (UniApp + Vue 3)
├── merchant-web/           # Merchant portal (Vue 3 + Element Plus)
├── sql/                    # Database schema and test data
└── pom.xml
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3.5, Java 17 |
| ORM | MyBatis-Plus 3.5.7 |
| Database | MySQL 5.7+ |
| Cache | Redis |
| Security | Spring Security + JWT (JJWT 0.12.3) |
| AI/LLM | LangChain4j 0.36.2 + DeepSeek API |
| API Docs | Knife4j (OpenAPI 3) |
| Customer App | UniApp + Vue 3, Pinia, luch-request |
| Merchant Web | Vue 3, Element Plus, Pinia, Axios |
| Build | Maven (backend), Vite (frontend) |

---

## Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 5.7+
- Redis
- Node.js 18+ (for frontends)
- DeepSeek API key

---

## Getting Started

### 1. Database Setup

```sql
CREATE DATABASE ai_takeaway CHARACTER SET utf8mb4;
```

Run the SQL scripts in order:

```bash
mysql -u root -p ai_takeaway < sql/schema.sql
mysql -u root -p ai_takeaway < sql/add_review_table.sql
mysql -u root -p ai_takeaway < sql/test_data.sql   # optional test data
```

### 2. Backend Configuration

Create `src/main/resources/secret.yml` with your credentials (this file is git-ignored):

```yaml
spring:
  datasource:
    username: your_mysql_username
    password: your_mysql_password

langchain4j:
  open-ai:
    chat-model:
      api-key: your_deepseek_api_key
```

Start the backend:

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`. API docs are available at `http://localhost:8080/doc.html`.

### 3. Merchant Web Portal

```bash
cd merchant-web
npm install
npm run dev
```

Runs on `http://localhost:3000`. API requests are proxied to `http://localhost:8080`.

### 4. Customer Mobile App

```bash
cd customer-uniapp
npm install
```

To run as H5 in browser:

```bash
npm run dev:h5
```

For WeChat Mini Program or other platforms, open the project in HBuilderX and select the target platform.

---

## Features

**Customer App**
- Browse merchants and menus
- AI-powered ordering assistant (conversational interface)
- Cart management (supports multiple merchants)
- Order placement, tracking, and history
- Order reviews and ratings

**Merchant Portal**
- Shop profile management
- Menu (dish) CRUD
- Incoming order management and status updates

---

## API Overview

The backend exposes REST endpoints under `/api/`:

| Prefix | Description |
|--------|-------------|
| `/api/auth` | Login and registration |
| `/api/merchants` | Merchant listing and details |
| `/api/dishes` | Menu items |
| `/api/cart` | Shopping cart |
| `/api/orders` | Order management |
| `/api/reviews` | Reviews and ratings |
| `/api/ai` | AI ordering assistant (chat) |

Full interactive documentation is available at `/doc.html` when the backend is running.

---

## Database Schema

Core tables: `user`, `merchant`, `dish`, `order`, `order_item`, `cart`, `review`.

All tables use soft deletes (`deleted` flag) and include `create_time` / `update_time` timestamps.

User roles: `CUSTOMER`, `MERCHANT`, `ADMIN`.

Order statuses: `0` pending, `1` confirmed, `2` delivering, `3` completed, `4` cancelled.

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `spring.datasource.url` | MySQL connection URL |
| `spring.datasource.username` | MySQL username |
| `spring.datasource.password` | MySQL password |
| `spring.data.redis.host` | Redis host (default: localhost) |
| `langchain4j.open-ai.chat-model.api-key` | DeepSeek API key |

Configure these in `secret.yml` or as environment variables. Never commit API keys to version control.
