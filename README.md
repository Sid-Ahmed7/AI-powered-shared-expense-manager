# 🔥 Fendly — AI-powered shared expense manager

<div align="center">

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Zod](https://img.shields.io/badge/Zod-validation-3E67B1?style=for-the-badge&logo=zod&logoColor=white)
![i18next](https://img.shields.io/badge/i18next-FR%20%2F%20EN-26A69A?style=for-the-badge&logo=i18next&logoColor=white)
![Gemini](https://img.shields.io/badge/Google_Gemini-AI-8E75B2?style=for-the-badge&logo=google&logoColor=white)

</div>

<br/>

> Fendly is a full-stack web application for managing shared expenses in groups. It features AI-assisted expense parsing (text & receipt photo), automatic balance calculation, optimized repayment suggestions, and AI-generated payment reminders.

---

## 📚 Table of contents

- [Tech stack](#-tech-stack)
- [Environment variables](#-environment-variables)
- [Local development](#-local-development)
- [Production](#-production)
- [Database](#-database)
- [Security](#-security)
- [AI integration](#-ai-integration)
- [Tests](#-tests)
- [CI/CD](#-cicd)
- [Project structure](#-project-structure)

---

## 🛠 Tech stack

### Backend

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 25 |
| Framework | Spring Boot | 4.x |
| Database | PostgreSQL | 16+ |
| Migrations | Flyway | — |
| Security | Spring Security + JWT | — |
| Email | Spring Mail (Gmail SMTP) | — |
| AI | Google Gemini API | gemini-3.8-flash |
| Tests | JUnit 5 + Mockito + Testcontainers | — |
| Build | Maven (mvnw wrapper) | — |

### Frontend

| Layer | Technology | Version |
|---|---|---|
| UI framework | React | 19 |
| Language | TypeScript | 6 |
| Build tool | Vite | 8 |
| Styling | Tailwind CSS | 4 |
| Data fetching | TanStack Query (react-query) | 5 |
| Forms | react-hook-form + zod | — |
| Internationalization | i18next + react-i18next | FR / EN |
| Icons | Lucide React | — |
| Mobile (native) | Capacitor | — |

### Infrastructure

| Service | Technology |
|---|---|
| Containers | Docker + Docker Compose |
| Reverse proxy (prod) | Nginx (inside the frontend container) |
| CI/CD | GitHub Actions |
| Security scan | Trivy (filesystem: vulns, misconfigs, secrets) |

---

## 🔐 Environment variables

### 1. Root `.env` — Docker Compose / production

Create a `.env` file at the project root (next to `docker-compose.yml`).

```env
# ── PostgreSQL ────────────────────────────────────────────
POSTGRES_DB=pactole
POSTGRES_USER=pactole
POSTGRES_PASSWORD=change_me_strong_password

# ── JWT (generate with: openssl rand -base64 32) ─────────
PACTOLE_SECURITY_JWT_SECRET=your_base64_secret

# ── Google Gemini ─────────────────────────────────────────
PACTOLE_AI_API_KEY=your_gemini_api_key

# ── Email (Gmail SMTP with App Password) ──────────────────
SPRING_MAIL_USERNAME=your_gmail_address@gmail.com
SPRING_MAIL_PASSWORD=your_16_char_app_password

# ── CORS / URLs ───────────────────────────────────────────
PACTOLE_CORS_ORIGINS=https://yourdomain.com
PACTOLE_APP_URL=https://yourdomain.com
```

> ⚠️ **Never commit this file.** It is listed in `.gitignore`.

---

### 2. `backend/src/main/resources/application-dev.properties` — backend local dev

This file is **gitignored**. Copy `application-dev.properties.example` and fill in your values.

```properties
# ── DataSource ────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5432/pactole
spring.datasource.username=pactole
spring.datasource.password=change_me

# ── JWT (generate with: openssl rand -base64 32) ─────────
pactole.security.jwt.secret=your_base64_secret

# ── CORS ──────────────────────────────────────────────────
pactole.cors-origins=http://localhost:5173,http://localhost:3000

# ── App URL (used in password reset emails) ───────────────
pactole.app-url=http://localhost:5173

# ── Google AI Studio : https://aistudio.google.com/app/apikey
pactole.ai.api-key=CHANGE_ME

# ── Gmail SMTP ────────────────────────────────────────────
# Use a Gmail App Password, NOT your account password.
# Enable: Google Account → Security → 2-Step Verification → App passwords
spring.mail.username=your.address@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

> 💡 **How to get a Gmail App Password:** Google Account → Security → 2-Step Verification → App passwords. Generate one for "Mail". Use the 16-character code (no spaces).

---

### 3. `frontend/.env` — frontend local dev

```env
VITE_API_URL=http://localhost:8080
```

In production, Nginx proxies `/api` to the backend container, so this variable is only needed for local development (`npm run dev`).

---

## 💻 Local development

### Prerequisites

- Java 25 JDK
- Node.js 22+
- Docker Desktop (for the database)

### 1. Start the database

```bash
docker compose -f docker-compose.dev.yml up -d
```

This starts only PostgreSQL on port `5432`. Flyway runs automatically when the backend starts.

### 2. Start the backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API is available at `http://localhost:8080`.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app is available at `http://localhost:5173`.

---

## 🚀 Production

```bash
# Build and start all containers (backend, frontend/nginx, postgres)
docker compose up --build -d
```

The `docker-compose.yml` at the project root orchestrates three services:
- `db` — PostgreSQL
- `backend` — Spring Boot JAR
- `frontend` — Nginx serving the Vite build + reverse-proxying `/api` to the backend

Make sure your root `.env` is filled in before running.

---

## 🗄 Database

Migrations live in `backend/src/main/resources/db/migration/` and run automatically via Flyway on startup.

| File | Description |
|---|---|
| `V1__security_sessions.sql` | Initial schema: accounts, groups, members, expenses, refresh tokens |
| `V2__password_reset.sql` | Adds the `password_reset_tokens` table |

---

## 🛡 Security

| Feature | Detail |
|---|---|
| Access token | JWT, 10 min expiry, signed with HS512 |
| Refresh token | Stored in DB (hashed), sliding 30-day / absolute 90-day window |
| Refresh token rotation | Old token invalidated on every refresh |
| Password reset | Time-limited single-use token, emailed link |
| Rate limiting | Per-IP limits on global, auth, login, and join endpoints |
| Account lockout | After repeated failed login attempts |
| Input sanitisation | All user-supplied strings are sanitised before use |
| Prompt guard | AI endpoints reject inputs that attempt prompt injection |
| CORS | Restricted to `pactole.cors-origins` |
| CSRF | Disabled (stateless JWT API) |
| Passwords | BCrypt |

---

## 🤖 AI integration

Fendly uses the Google Gemini API, configurable via `pactole.ai.*` properties. AI endpoints are rate-limited independently (`pactole.ai.calls-per-hour`, `pactole.ai.calls-per-day`).

| Feature | Endpoint | Description |
|---|---|---|
| Natural language parsing | `POST /api/groups/{id}/expenses/parse` | Parses free text like _"Pizza €32 split between Alice and Bob"_ into an expense draft |
| Receipt scanning | `POST /api/groups/{id}/expenses/scan` | Sends a base64 image to Gemini Vision and extracts line items |
| Monthly recap | `POST /api/groups/{id}/recap` | Generates a markdown spending summary |
| Payment reminder | `POST /api/groups/{id}/reminders` | Generates a message asking a member to repay, in the chosen tone (friendly / funny / formal) |

---

## 🧪 Tests

```bash
cd backend

# Unit tests (no Docker required)
./mvnw test -B -Dtest="com.pactole.common.**,com.pactole.security.**,com.pactole.services.**"

# Integration tests (requires Docker for Testcontainers)
./mvnw test -B -Dtest="com.pactole.controller.**,com.pactole.PactoleApplicationTests"

# All tests
./mvnw test
```

Integration tests use **Testcontainers** to spin up a real PostgreSQL instance, so Docker must be running.

---

## ⚙️ CI/CD

GitHub Actions runs on every push and pull request (`**` branches):

| Job | What it does |
|---|---|
| **Backend – Build & Test** | Compiles with Maven, runs unit tests then integration tests (Testcontainers + PostgreSQL) |
| **Frontend – Build & Audit** | `npm ci` + `npm audit --audit-level=high` + `vite build` |
| **Trivy – Scan** | Scans the full filesystem for vulnerabilities, misconfigurations, and leaked secrets (HIGH/CRITICAL, ignore-unfixed) |

Workflow file: [.github/workflows/ci.yml](.github/workflows/ci.yml)

---

## 📁 Project structure

```
fendly/
├── .github/workflows/
│   └── ci.yml                     # GitHub Actions CI/CD pipeline
│
├── backend/
│   ├── src/main/java/com/pactole/
│   │   ├── common/                # Utilities: SplitCalculator, SettlementCalculator, InputSanitizer…
│   │   ├── config/                # Spring Security, CORS, rate limiting config
│   │   ├── constants/             # Shared constants
│   │   ├── controller/            # REST controllers (auth, groups, expenses, AI, settlement…)
│   │   ├── dto/                   # Request / response DTOs
│   │   ├── entities/              # JPA entities
│   │   ├── exceptions/            # Error types and global exception handler
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── security/              # JWT filter, rate limiter, prompt guard, password policy
│   │   └── services/              # Business logic (expense, balance, AI, email, password reset…)
│   ├── src/main/resources/
│   │   ├── db/migration/          # Flyway SQL migrations
│   │   ├── messages*.properties   # i18n error & email messages
│   │   └── application*.properties
│   └── src/test/
│       ├── java/                  # Unit tests (services, security, common) + integration tests (controllers)
│       └── resources/
│           └── application-test.properties
│
└── frontend/
    ├── src/
    │   ├── api/                   # Typed API client
    │   ├── components/
    │   │   ├── form/              # ExpenseForm, AddMemberForm, AiTextForm
    │   │   └── ui/                # Button, Chip, Modal, Sidebar, FieldGroup…
    │   ├── hooks/                 # usePagination, usePageTitle…
    │   ├── i18n/                  # Translation files (fr, en)
    │   ├── pages/                 # GroupsPage, GroupPage, AccountPage, JoinPage…
    │   ├── schemas/               # Zod schemas shared between forms
    │   ├── types/                 # TypeScript types
    │   └── utils/                 # money, date helpers
    └── nginx.conf                 # Production Nginx config
```