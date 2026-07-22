# FILLINUS ERP — Backend API

Spring Boot 3.x REST API for the FILLINUS Entertainment ERP system.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Build | Gradle |
| Database | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Auth | Spring Security + JWT (jjwt) |
| DB Migration | **Flyway** (auto-run on startup) |
| Email | Spring Mail |
| API Docs | Swagger UI (SpringDoc) |
| Deployment | Railway |

---

## Project Structure

```
fillinus-erp/
├── docs/                        ← All spec documents (Excel)
│   ├── 00_enterprise_architecture/
│   ├── 01_screen_catalog/
│   └── bd/authentication/       ← AUTH-001 to AUTH-005 specs
├── src/main/java/com/fillinus/erp/
│   ├── config/                  ← SecurityConfig, JwtUtil, JwtAuthFilter
│   ├── common/                  ← BaseEntity, ApiResponse
│   └── module/auth/
│       ├── controller/          ← AuthController, UserController
│       ├── service/             ← AuthService, UserService
│       ├── repository/          ← UserRepository, PasswordResetTokenRepository
│       ├── entity/              ← User, Role, Department, Position, PasswordResetToken
│       └── dto/                 ← Request/Response DTOs
└── src/main/resources/
    ├── application.yml
    └── db/migration/            ← Flyway SQL scripts (V1–V5)
```

---

## Database Migrations (Flyway)

All SQL scripts are stored in `src/main/resources/db/migration/` and versioned:

| Script | Description |
|--------|-------------|
| `V1__create_roles.sql` | Roles table + default seed data |
| `V2__create_departments.sql` | Departments table |
| `V3__create_positions.sql` | Positions table (FK → departments) |
| `V4__create_users.sql` | Users table (FK → roles, departments, positions) |
| `V5__create_password_reset_tokens.sql` | Password reset tokens table |

**To deploy to any server:**
1. Set environment variables (see `.env.example`)
2. Start the application
3. Flyway automatically runs all pending migrations ✅

---

## API Endpoints

All endpoints are prefixed with `/api`.

### Authentication (No token required)
| Method | Endpoint | Screen | Description |
|--------|----------|--------|-------------|
| POST | `/auth/login` | AUTH-001 | Login, returns JWT token |
| POST | `/auth/forgot-password` | AUTH-002 | Send reset email |
| POST | `/auth/reset-password` | AUTH-003 | Reset password with token |

### User Profile (🔐 JWT required)
| Method | Endpoint | Screen | Description |
|--------|----------|--------|-------------|
| GET | `/users/me` | AUTH-004 | Get my profile |
| PUT | `/users/me` | AUTH-004 | Update my profile |
| PUT | `/users/me/change-password` | AUTH-005 | Change password |

### API Documentation
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`

---

## Local Development

### Prerequisites
- Java 21
- Docker (for local PostgreSQL)

### Steps

```bash
# 1. Start local PostgreSQL
docker compose up -d

# 2. Copy env template
cp .env.example .env
# Edit .env with your values

# 3. Run the app
./gradlew bootRun

# 4. Open Swagger UI
open http://localhost:8080/api/swagger-ui.html
```

---

## Deploy to Railway

1. Push this repo to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Add a **PostgreSQL** plugin → Railway auto-injects `DATABASE_URL`
4. Add environment variables from `.env.example`:
   - `JWT_SECRET` — strong random secret
   - `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
   - `APP_BASE_URL` — your Railway app URL
5. Deploy → Railway runs `./gradlew bootJar` and starts the app
6. Flyway auto-creates all tables on first startup ✅

---

## Deploy to Other Servers (AWS, GCP, VPS, etc.)

1. Build the JAR:
   ```bash
   ./gradlew bootJar
   ```
2. Transfer `build/libs/fillinus-erp.jar` to your server
3. Set all environment variables from `.env.example`
4. Run:
   ```bash
   java -jar fillinus-erp.jar
   ```
5. Flyway handles all DB migrations automatically ✅

---

## Adding New DB Tables

Never modify existing migration files. Instead:
1. Create `src/main/resources/db/migration/V6__your_description.sql`
2. Write your SQL
3. Restart the app — Flyway runs only the new script
