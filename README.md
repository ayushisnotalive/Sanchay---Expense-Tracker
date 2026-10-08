# Sanchay (संचय) - Enterprise Personal Finance & Wealth Suite

Institutional-grade personal finance, wealth tracking, and offline-first budgeting ecosystem designed for Google Play Store release and high-throughput enterprise scalability.

---

## 🏛️ Project Directory Architecture

The repository is strictly partitioned into two independent, purpose-built modules with **no redundant backend code**:

```
Sanchay/
├── backend/                  # ☕ Pure Java 21 Spring Boot 3.3.4 Enterprise Backend
│   ├── src/main/java/        # 100% Java REST API, JPA Repositories, Services, Security
│   ├── src/main/resources/   # application.yml, HikariCP, JPA, Cache configurations
│   ├── src/test/java/        # JUnit 5 integration and unit tests (100% pass)
│   ├── build.gradle.kts      # Applies 'java' plugin only (Java 21 toolchain)
│   ├── Dockerfile            # Containerized multi-stage production deployment
│   └── docker-compose.yml    # Full-stack orchestrator with PostgreSQL 16
│
├── app/                      # 📱 Pure Kotlin Android Client & Jetpack Compose UI
│   ├── src/main/java/        # 100% Kotlin Compose Screens, MVVM ViewModels, Room DB
│   ├── src/main/res/         # Material 3 Themes, Vector Drawables, Shortcuts
│   ├── build.gradle.kts      # Android Gradle Plugin, KSP, Compose Compiler, ProGuard/R8
│   └── proguard-rules.pro    # Optimized R8 shrinking & obfuscation rules
│
├── distribution/             # 🚀 Play Store Release Assets
│   └── playstore/            # Store listing copy, Privacy Policy, and Launch Checklist
│
├── gradle/                   # Shared Gradle 9.7.1 wrapper and version catalog
├── settings.gradle.kts       # Multi-project Gradle build configuration (:app, :backend)
└── .gitignore                # Enterprise git exclusions (keystores, caches, tooling)
```

> **Design Principle**:
> - **Backend (`backend/`)**: Strictly **Java 21 Spring Boot 3**. Zero Kotlin dependencies or Kotlin source files.
> - **Client (`app/`)**: Strictly **Kotlin Jetpack Compose Android**. Zero Java source files.

---

## ☕ 1. Java Spring Boot Backend (`backend/`)

### Key Capabilities
- **Spring Boot 3.3.4 on Java 21** with Project Loom Virtual Threads (`spring.threads.virtual.enabled: true`).
- **Stateless JWT Security**: Custom `JwtAuthenticationFilter`, BCrypt password hashing, and role-based access control (`ROLE_USER`, `ROLE_ADMIN`).
- **REST API Endpoints**:
  - `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
  - `POST /api/v1/sync/push`, `GET /api/v1/sync/pull` (Bidirectional delta sync)
  - `GET /api/v1/backup/export`, `POST /api/v1/backup/import` (Full JSON data portability)
  - `GET /api/v1/expenses`, `/api/v1/wallets`, `/api/v1/savings`, `/api/v1/debts`, `/api/v1/investments`, `/api/v1/subscriptions`
  - `POST /api/v1/ai/advice` (AI advisor fallback)
- **High-Performance Caching**: In-memory Caffeine Cache (`maximumSize=1000`, `expireAfterWrite=10m`).
- **Database Flexibility**: In-memory H2 for rapid local dev; PostgreSQL 16 with HikariCP for production.
- **Interactive Documentation**: SpringDoc OpenAPI 3 / Swagger UI at `/swagger-ui/index.html`.

### Running Backend Locally
```bash
# Run Spring Boot backend on http://localhost:8080
./gradlew :backend:bootRun

# Run automated test suite
./gradlew :backend:test
```

---

## 📱 2. Kotlin Jetpack Compose Android Client (`app/`)

### Key Capabilities
- **100% Kotlin & Jetpack Compose**: Material 3 theming, edge-to-edge support, dark mode, dynamic typography.
- **Offline-First Architecture**: Android Jetpack Room with automatic SQLite database migrations and initial seed callback.
- **Complete Suite of 14+ Financial Screens**:
  - Daily Expenses & Categorized Budgets
  - Multi-Currency Wallets & Live Conversion
  - Bill Splitting with Settlement Matrix
  - Debt Tracker (Borrow & Lend with Payoff Ledger)
  - Investment Portfolio Tracker
  - Recurring Subscriptions with Notification Scheduling
  - OCR Receipt Scanning via Google ML Kit
  - Interactive Financial Visualizations via Vico Charts
  - Gemini AI Financial Advisor & On-Device Fallback
  - JSON & Excel (.xlsx) Backup & Export
- **Smart Push Notifications**: Periodic `SmartNotificationWorker` alerting users when monthly spend reaches 80% and 100% of budget.
- **Launcher Quick Shortcuts**: "Add Expense", "AI Advisor", and "Search" dynamic shortcuts.

### Building Client Artifacts
```bash
# Debug APK for testing
./gradlew :app:assembleDebug

# Production Google Play App Bundle (AAB - 23MB)
./gradlew :app:bundleRelease

# Production Universal Signed APK (APK - 30MB)
./gradlew :app:assembleRelease
```

---

## 🛠️ Verification & Test Suite

Run all verification tasks across both modules:
```bash
# Backend unit & integration tests
./gradlew :backend:test

# Compile & bundle verification
./gradlew :app:bundleRelease :backend:assemble
```
