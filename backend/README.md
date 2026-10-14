# Sanchay Enterprise Spring Boot 3 Backend

Institutional-grade Java 21 REST API backend for the Sanchay Personal Finance & Wealth suite.

---

## 🚀 Quick Start

### 1. Run locally with H2 in-memory DB (Development)
```bash
./gradlew :backend:bootRun
```
- Server starts on `http://localhost:8080`
- Interactive OpenAPI / Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- In-memory H2 Console: `http://localhost:8080/h2-console` (`jdbc:h2:mem:sanchaydb`, user: `sa`, password: empty)

### 2. Run with Docker Compose (PostgreSQL Production)
```bash
cd backend
docker compose up --build -d
```
- PostgreSQL 16 on port 5432
- Spring Boot 3 on port 8080 with Virtual Threads enabled

---

## 🔒 Security & Authentication

- **Stateless JWT**: Standard Bearer token authentication in HTTP header: `Authorization: Bearer <token>`
- **Registration**: `POST /api/v1/auth/register`
- **Login**: `POST /api/v1/auth/login`

---

## 📡 REST API Modules

| Module | Base Path | Description |
|--------|-----------|-------------|
| **Auth** | `/api/v1/auth` | User registration, login, and token issuance |
| **Sync Engine** | `/api/v1/sync` | `/push` and `/pull` delta synchronization |
| **Expenses** | `/api/v1/expenses` | CRUD & category analytics |
| **Wallets** | `/api/v1/wallets` | Multi-wallet balances and transfers |
| **Subscriptions**| `/api/v1/subscriptions` | Recurring subscriptions and alerts |
| **Savings Goals**| `/api/v1/savings` | Visual goal milestones and deposits |
| **Debts** | `/api/v1/debts` | Loans, debts, and payoff tracking |
| **Investments** | `/api/v1/investments` | Portfolio asset value & tracking |
| **Backup & Data** | `/api/v1/backup` | `/export` and `/import` full JSON backups |
| **AI Advisor** | `/api/v1/ai` | `/advice` proxy with multi-tier fallback |

---

## ⚡ Performance Features
- **Java 21 Project Loom**: Virtual threads for high concurrent throughput.
- **Caffeine Cache**: In-memory caching for analytical queries.
- **HikariCP**: Low-latency connection pool.
