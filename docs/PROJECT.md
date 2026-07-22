# FILLINUS ERP — Project Documentation

> Entertainment Resource Planning System  
> Last updated: 2026-07-22

---

## 🌐 Live URLs

| Service | URL | Status |
|---------|-----|--------|
| **Frontend** | https://fillinus-erp-fe.onrender.com | 🟢 Live |
| **Backend API** | https://fillinus-erp.onrender.com/api | 🟢 Live |
| **Swagger UI** | https://fillinus-erp.onrender.com/api/swagger-ui.html | 🟢 Live |
| **Health Check** | https://fillinus-erp.onrender.com/api/actuator/health | 🟢 Live |

---

## 📦 GitHub Repositories

| Repo | Link | Description |
|------|------|-------------|
| **Backend** | https://github.com/trngochan/fillinus-erp | Spring Boot REST API |
| **Frontend** | https://github.com/trngochan/fillinus-erp-fe | React + Vite SPA |

---

## 🗄️ Database

### Connection Info (Render PostgreSQL — Free Tier)

| Property | Value |
|----------|-------|
| **Provider** | Render.com Managed PostgreSQL |
| **Version** | PostgreSQL 16 |
| **Plan** | Free |
| **Region** | Singapore |
| **DB Name** | `fillinus_erp_b37d` |
| **Host** | `dpg-d9gaiebbc2fs73fo8f7g-a` (internal) |

> [!IMPORTANT]
> Render free PostgreSQL databases **expire after 90 days**. Export your data before then.
> To connect externally, use the **External Connection String** from your Render dashboard.

### Schema — Tables

#### `users`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGSERIAL` | PRIMARY KEY | Auto-increment ID |
| `username` | `VARCHAR(50)` | UNIQUE NOT NULL | Login username |
| `password` | `VARCHAR(255)` | NOT NULL | BCrypt hashed |
| `email` | `VARCHAR(100)` | UNIQUE NOT NULL | Email address |
| `full_name` | `VARCHAR(100)` | NOT NULL | Display name |
| `phone_number` | `VARCHAR(20)` | | Contact number |
| `address` | `TEXT` | | Physical address |
| `department` | `VARCHAR(100)` | | Department name |
| `position` | `VARCHAR(100)` | | Job position |
| `role` | `VARCHAR(20)` | NOT NULL | `ADMIN` / `MANAGER` / `EMPLOYEE` |
| `status` | `VARCHAR(20)` | NOT NULL | `ACTIVE` / `INACTIVE` |
| `created_at` | `TIMESTAMP` | NOT NULL | Record created time |
| `updated_at` | `TIMESTAMP` | | Last updated time |

#### `refresh_tokens`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGSERIAL` | PRIMARY KEY | Auto-increment ID |
| `user_id` | `BIGINT` | FK → users.id | Token owner |
| `token` | `TEXT` | UNIQUE NOT NULL | JWT refresh token |
| `expires_at` | `TIMESTAMP` | NOT NULL | Expiry time |
| `created_at` | `TIMESTAMP` | NOT NULL | Created time |

#### `password_reset_tokens`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGSERIAL` | PRIMARY KEY | Auto-increment ID |
| `user_id` | `BIGINT` | FK → users.id | Token owner |
| `token` | `VARCHAR(255)` | UNIQUE NOT NULL | Reset token (UUID) |
| `expires_at` | `TIMESTAMP` | NOT NULL | Expires in 30 min |
| `used` | `BOOLEAN` | DEFAULT false | One-time use flag |
| `created_at` | `TIMESTAMP` | NOT NULL | Created time |

#### `audit_logs` *(for future use)*
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGSERIAL` | PRIMARY KEY | Auto-increment ID |
| `user_id` | `BIGINT` | FK → users.id | Who performed action |
| `action` | `VARCHAR(100)` | NOT NULL | Action type |
| `entity_type` | `VARCHAR(100)` | | What entity |
| `entity_id` | `BIGINT` | | Entity ID |
| `details` | `TEXT` | | Additional info |
| `created_at` | `TIMESTAMP` | NOT NULL | When it happened |

### Schema Migrations (Flyway)

| Version | Description |
|---------|-------------|
| `V1` | Create `roles` table + seed ADMIN / MANAGER / EMPLOYEE |
| `V2` | Create `departments` table |
| `V3` | Create `positions` table |
| `V4` | Create `users` table |
| `V5` | Create `password_reset_tokens` table |
| `V6` | **Java migration** — seed test accounts with BCrypt-hashed passwords |

> [!NOTE]
> Flyway runs automatically on every startup. To deploy to a new database, just set `DATABASE_URL` — all tables are created automatically.

### Test Accounts (seeded by V6)

> [!IMPORTANT]
> These accounts are created automatically when the app starts and runs the V6 Flyway migration.

| Username | Password | Role | Email |
|----------|----------|------|-------|
| `admin` | `Admin@123456` | ADMIN | admin@fillinus.com |
| `manager` | `Manager@123` | MANAGER | manager@fillinus.com |
| `emp01` | `Employee@123` | EMPLOYEE | emp01@fillinus.com |

---

## 🔧 Backend (BE)

### Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.2 |
| Build | Gradle | 8.8 |
| Security | Spring Security + JWT | 6.3.1 |
| Database ORM | Spring Data JPA / Hibernate | 6.1 |
| Migrations | Flyway | 10.10 |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.6.0 |
| Runtime | Eclipse Temurin JRE Alpine | 21 |
| Deployment | Render.com (Docker) | Free tier |

### Environment Variables

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `DATABASE_URL` | ✅ | PostgreSQL connection URL | `postgresql://user:pass@host/db` |
| `JWT_SECRET` | ✅ | JWT signing secret (min 32 chars) | `super-secret-key-...` |
| `JWT_EXPIRATION_MS` | ✅ | Access token TTL in ms | `86400000` (24h) |
| `APP_BASE_URL` | ✅ | Used in reset-password email links | `https://fillinus-erp-fe.onrender.com` |
| `RESET_TOKEN_EXPIRY_MINUTES` | ✅ | Password reset token TTL | `30` |
| `MAIL_HOST` | ⬜ | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | ⬜ | SMTP port | `587` |
| `MAIL_USERNAME` | ⬜ | SMTP username / email | `no-reply@fillinus.com` |
| `MAIL_PASSWORD` | ⬜ | SMTP password / app password | `xxxx xxxx xxxx xxxx` |
| `PORT` | Auto | Injected by Render (10000) | — |

### API Endpoints

#### Auth (Public — no token required)
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/login` | Login → returns JWT token |
| `POST` | `/api/auth/forgot-password` | Send password reset email |
| `POST` | `/api/auth/reset-password` | Reset password with token |

#### Users (Protected — Bearer token required)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/api/users/me` | Any | Get my profile |
| `PUT` | `/api/users/me` | Any | Update my profile |
| `PUT` | `/api/users/me/change-password` | Any | Change my password |

#### System (Public)
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/actuator/health` | App health status |

### Authentication Flow

```
1. POST /api/auth/login  →  { token: "eyJ..." }
2. Store token in localStorage
3. Add header to all requests:  Authorization: Bearer eyJ...
4. Token expires after 24 hours → redirect to login
```

### Project Structure

```
fillinus-erp/
├── src/main/java/com/fillinus/erp/
│   ├── config/
│   │   ├── SecurityConfig.java          ← JWT filter chain, CORS
│   │   ├── JwtAuthFilter.java           ← Token validation per request
│   │   ├── RenderDatabaseUrlConverter.java ← postgresql:// → jdbc:postgresql://
│   │   └── SwaggerConfig.java
│   └── module/
│       └── auth/
│           ├── controller/              ← REST endpoints
│           ├── service/                 ← Business logic
│           ├── repository/              ← JPA repositories
│           ├── entity/                  ← JPA entities
│           └── dto/                     ← Request/Response DTOs
├── src/main/resources/
│   ├── application.yml                  ← All config
│   └── db/migration/                    ← Flyway V1–V5 SQL scripts
└── Dockerfile                           ← Multi-stage build
```

---

## 🖥️ Frontend (FE)

### Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | React | 18.3 |
| Build tool | Vite | 5.3 |
| Language | TypeScript | 5.5 |
| Styling | Tailwind CSS | 3.4 |
| Routing | React Router | v6 |
| HTTP client | Axios | 1.7 |
| State management | Zustand (persisted) | 4.5 |
| Forms | React Hook Form + Zod | 7.52 / 3.23 |
| Icons | Lucide React | 0.395 |
| Deployment | Render.com (Static Site) | Free tier |

### Pages

| Screen | Route | Description |
|--------|-------|-------------|
| AUTH-001 | `/login` | Login form |
| AUTH-002 | `/forgot-password` | Request password reset email |
| AUTH-003 | `/reset-password?token=xxx` | Set new password |
| AUTH-004 | `/profile` | View & edit profile |
| AUTH-005 | `/profile/change-password` | Change password |

### Environment Variables

| Variable | Description | Value |
|----------|-------------|-------|
| `VITE_API_URL` | Backend API base URL | `https://fillinus-erp.onrender.com/api` |

### Project Structure

```
fillinus-erp-fe/
├── src/
│   ├── api/
│   │   ├── axios.ts          ← Axios instance with JWT interceptor
│   │   └── auth.ts           ← All API calls
│   ├── components/
│   │   └── RouteGuards.tsx   ← ProtectedRoute / PublicRoute
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── ForgotPasswordPage.tsx
│   │   ├── ResetPasswordPage.tsx
│   │   ├── ProfilePage.tsx
│   │   └── ChangePasswordPage.tsx
│   ├── store/
│   │   └── authStore.ts      ← Zustand auth store (localStorage)
│   ├── types/
│   │   └── auth.ts           ← TypeScript interfaces
│   ├── App.tsx               ← Router setup
│   ├── main.tsx              ← Entry point
│   ├── index.css             ← Tailwind + custom components
│   └── vite-env.d.ts         ← Vite type declarations
├── vite.config.ts
├── tailwind.config.js
├── tsconfig.json
└── render.yaml               ← Static site deploy config
```

### Auth State Flow

```
Login success → token stored in localStorage (Zustand persist)
                ↓
All pages check isAuthenticated
  - False → redirect to /login
  - True  → render page, attach Bearer token to every API call
                ↓
Token 401 → auto logout → redirect to /login
```

---

## 🚀 Deployment

### Render.com Services

| Service | Type | Plan | Auto-deploy |
|---------|------|------|-------------|
| `fillinus-erp` | Web Service (Docker) | Free | ✅ On push to `main` |
| `fillinus-erp-fe` | Static Site | Free | ✅ On push to `main` |
| `fillinus-db` | PostgreSQL 16 | Free | — |

### Deploy to a New Server

1. **Database**: Create a PostgreSQL database → get connection URL
2. **Backend**: Set env vars (see table above) → deploy with Docker
3. **Frontend**: Set `VITE_API_URL` → `npm install && npm run build` → serve `dist/`

> [!TIP]
> All tables are created automatically by Flyway on first startup. No manual SQL needed.

---

## 🔐 Security Notes

- Passwords are hashed with **BCrypt** (strength 12)
- JWT tokens expire in **24 hours**
- Password reset tokens expire in **30 minutes** and are **single-use**
- All API endpoints except login/forgot-password require **Bearer token**
- HTTPS enforced by Render on all services
- Database SSL required (`sslmode=require`)
