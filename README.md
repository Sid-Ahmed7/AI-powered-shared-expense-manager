# 🔥 Fendly — AI-powered shared expense manager

<div align="center">

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
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
- [AI / Gemini integration](#-ai--gemini-integration)
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
| Database | PostgreSQL | 16 |
| Migrations | Flyway | — |
| Security | Spring Security + JWT | — |
| Email | Spring Mail (Gmail SMTP) | — |
| AI | Google Gemini API | gemini-2.0-flash |
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
| HTTP client | Custom `api.ts` wrapper (fetch) | — |

### Infrastructure

| Service | Technology |
|---|---|
| Containers | Docker + Docker Compose |
| Reverse proxy (prod) | Nginx (inside the frontend container) |
| CI/CD | GitHub Actions |
| Security scan | Trivy (Docker image vulnerabilities) |

---

## 🔐 Environment variables

### 1. Root `.env` — Docker Compose / production

Create a `.env` file at the project root (next to `docker-compose.yml`).

```env
# ── PostgreSQL ────────────────────────────────────────────
POSTGRES_DB=pactole
POSTGRES_USER=pactole
POSTGRES_PASSWORD=change_me_strong_password

# ── JWT (generate with: openssl rand -base64 64) ─────────
JWT_SECRET=your_64_byte_base64_access_secret
JWT_REFRESH_SECRET=your_64_byte_base64_refresh_secret

# ── Google Gemini ─────────────────────────────────────────
GEMINI_API_KEY=your_gemini_api_key

# ── Email (Gmail SMTP with App Password) ──────────────────
MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_PASSWORD=your_16_char_app_password

# ── CORS / URLs ───────────────────────────────────────────
FRONTEND_URL=https://yourdomain.com
APP_BASE_URL=https://yourdomain.com
```

> ⚠️ **Never commit this file.** It is listed in `.gitignore`.

---

### 2. `backend/src/main/resources/application-dev.properties` — backend local dev

This file is **gitignored**. Copy `application-dev.properties.example` and fill in your values.

```properties
# ── DataSource ────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5432/pactole
spring.datasource.username=pactole
spring.datasource.password=change_me_strong_password

# ── JWT ───────────────────────────────────────────────────
app.jwt.secret=your_64_byte_base64_access_secret
app.jwt.refresh-secret=your_64_byte_base64_refresh_secret
app.jwt.expiration-ms=600000
app.jwt.refresh-expiration-ms=2592000000
app.jwt.refresh-expiration-remember-ms=7776000000

# ── Gemini AI ─────────────────────────────────────────────
app.gemini.api-key=your_gemini_api_key
app.gemini.enabled=true

# ── Email ─────────────────────────────────────────────────
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_gmail_address@gmail.com
spring.mail.password=your_16_char_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ── URLs ──────────────────────────────────────────────────
app.base-url=http://localhost:5173
app.frontend-url=http://localhost:5173

# ── Rate limiting (per IP, per minute) ───────────────────
app.rate-limit.login=10
app.rate-limit.register=5
app.rate-limit.password-reset=3
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
- `db` — PostgreSQL 16
- `backend` — Spring Boot JAR
- `frontend` — Nginx serving the Vite build + reverse-proxying `/api` to the backend

Make sure your root `.env` is filled in before running.

---

## 🗄 Database

Migrations live in `backend/src/main/resources/db/migration/` and run automatically via Flyway on startup.

| File | Description |
|---|---|
| `V1__init.sql` | Initial schema: users, groups, members, expenses, refresh_tokens |
| `V2__password_reset.sql` | Adds the `password_reset_tokens` table |

---

## 🛡 Security

| Feature | Detail |
|---|---|
| Access token | JWT, 10 min expiry, signed with HS512 |
| Refresh token | Stored in DB (hashed), 30 days standard / 90 days "remember me" |
| Refresh token rotation | Old token invalidated on every refresh |
| Password reset | Time-limited single-use UUID token, emailed link, expires in 1 hour |
| Rate limiting | Per-IP limits on login, register, and password-reset endpoints |
| Account lockout | After repeated failed login attempts |
| CORS | Restricted to `FRONTEND_URL` |
| CSRF | Disabled (stateless JWT API) |
| Passwords | BCrypt |

---

## 🤖 AI / Gemini integration

The `app.gemini.enabled` flag controls all AI features. When `false`, the frontend hides the assistant tab and disables AI buttons gracefully.

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

# Unit tests only (no Docker required)
./mvnw test -Dgroups="unit"

# Integration tests (requires Docker for Testcontainers)
./mvnw test -Dgroups="integration"

# All tests
./mvnw test
```

Integration tests use **Testcontainers** to spin up a real PostgreSQL instance, so Docker must be running.

---

## ⚙️ CI/CD

GitHub Actions runs on every push and pull request to `main`:

1. **Build & test** — compiles the backend with Maven and runs all tests
2. **Frontend lint/build** — `npm ci && npm run build`
3. **Trivy scan** — scans the Docker image for known CVEs and fails on HIGH/CRITICAL vulnerabilities

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
│   │   ├── config/                # Spring Security, CORS, rate limiting config
│   │   ├── constants/             # Shared constants
│   │   ├── controller/            # REST controllers (auth, groups, expenses, AI…)
│   │   ├── dto/                   # Request / response DTOs
│   │   ├── entities/              # JPA entities
│   │   ├── enums/                 # Domain enums (Category, Tone, RefreshReason…)
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── security/              # JWT filter, problem responses
│   │   └── services/              # Business logic (expense, balance, AI, email…)
│   └── src/main/resources/
│       ├── db/migration/          # Flyway SQL migrations
│       ├── messages*.properties   # i18n error messages
│       └── application*.properties
│
└── frontend/
    ├── src/
    │   ├── api.ts                 # Typed API client
    │   ├── auth.tsx               # Auth context + hooks
    │   ├── components/
    │   │   ├── form/              # ExpenseForm, AddMemberForm, AiTextForm
    │   │   └── ui/                # Button, Chip, Modal, Sidebar, FieldGroup…
    │   ├── hooks/                 # usePagination, usePageTitle…
    │   ├── i18n/                  # Translation files (fr, en)
    │   ├── pages/                 # GroupsPage, GroupPage, AccountPage, JoinPage…
    │   ├── schemas/               # Zod schemas shared between forms
    │   └── types/                 # TypeScript types
    └── nginx.conf                 # Production Nginx config
```
