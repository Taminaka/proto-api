# Tamaka backend — Tourism Aggregation Platform

## Overview
MVP backend for a tourism aggregation platform built with Java 21, Spring Boot 3.x, and PostgreSQL.

## Tech Stack
- Java 21
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Security + JWT (stateless)
- PostgreSQL 16
- Maven
- Docker / Docker Compose
- SpringDoc OpenAPI (Swagger UI)

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development without Docker)

### Running with Docker Compose
```bash
cp .env.example .env
# Edit .env with your values
docker compose up --build
```

The application will be available at:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html

### Running Locally
```bash
# Start PostgreSQL
docker compose up -d postgres

# Run application
./mvnw spring-boot:run
```

### Running the frontend base
The frontend integration base is in `frontend/`. It contains the Next.js App Router setup, an Axios client, JWT session persistence, auth context, response handling for expired sessions, and typed request modules for catalog and recommendations. It intentionally does not include a product UI yet.

```bash
cd frontend
cp .env.example .env.local
npm run dev
```

The default frontend URL is http://localhost:3000 and it calls the backend at `http://localhost:8080/api`. Set `NEXT_PUBLIC_API_URL` in `frontend/.env.local` when the API runs elsewhere.

### Deploying on Render from Git

This repository contains only the Docker-based Spring Boot API and its managed PostgreSQL database. Create a Render Blueprint from this repository using `render.yaml`:

- API: `https://proto-api.taminaka.com`

The separate Next.js frontend repository is configured for:

- Frontend: `https://proto.taminaka.com`

Required production values are configured as Render environment variables:

```text
FRONTEND_URL=https://proto.taminaka.com
JWT_SECRET=<long-random-secret>
```

The API listens on Render's `PORT` variable and exposes Swagger at
`https://proto-api.taminaka.com/api/swagger-ui.html`.

#### Supabase production database

Production uses the external Supabase PostgreSQL database; the Docker Compose PostgreSQL
service is for local development only. In the Render service environment, configure these
values as secret environment variables:

| Variable | Value |
|---|---|
| `DATABASE_URL` | Supabase shared transaction pooler URL on port `6543`, including `?pgbouncer=true` |
| `DIRECT_URL` | Supabase direct/session URL on port `5432`, used for maintenance or migrations |

The container converts `postgresql://`/`postgres://` URLs to JDBC automatically. The API
uses `DATABASE_URL` at runtime and disables PostgreSQL server-side prepared statements
(`prepareThreshold=0`) for compatibility with Supabase's transaction-mode pooler.

To configure Render:

1. Open the `tamaka-api` service in Render and open **Environment**.
2. Add `DATABASE_URL` and paste the Supabase pooler URL as a secret.
3. Add `DIRECT_URL` and paste the Supabase direct URL as a secret.
4. Set `FRONTEND_URL` to `https://proto.taminaka.com`.
5. Set a long random `JWT_SECRET`, then redeploy.

Do not put either database URL in Git, `render.yaml`, frontend code, or public logs.

### Demo data and test accounts

On startup, the application applies the versioned seed migrations in
`src/main/resources/db/migration/` once. They create
sample categories, famous Santa Marta attractions, a tourism operator, services and
future events, with ownership assigned to the seeded GESTOR and OPERADOR accounts.

All demo accounts use the password `Admin123!`:

| Account | Roles |
|---|---|
| `turista@taminaka.com` | TURISTA |
| `admin@taminaka.com` | ADMIN, TURISTA |
| `gestor@taminaka.com` | GESTOR, TURISTA |
| `op@taminaka.com` | OPERADOR, TURISTA |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| POSTGRES_DB | tourism | Database name |
| POSTGRES_USER | tourism | Database user |
| POSTGRES_PASSWORD | tourism | Database password |
| JWT_SECRET | (required) | JWT signing secret (min 32 chars) |
| JWT_EXPIRATION | 86400000 | Token expiration in ms (24h) |
| ADMIN_EMAIL | admin@tamaka.com | Admin seed email |
| ADMIN_PASSWORD | Admin123! | Admin seed password |
| ADMIN_FULL_NAME | System Administrator | Admin seed full name |
| R2_ACCOUNT_ID | (empty) | Cloudflare account ID for image storage |
| R2_ACCESS_KEY_ID | (empty) | R2 access key |
| R2_SECRET_ACCESS_KEY | (empty) | R2 secret key |
| R2_BUCKET | (empty) | Shared object storage bucket |
| R2_ENDPOINT | (empty) | R2 S3-compatible endpoint |
| R2_PUBLIC_URL | `https://cdn.vyde.dev` | Public base URL for uploaded images |

The authenticated `POST /api/storage/upload` endpoint accepts image files up to
10 MB and returns a public URL. It accepts the folders `attractions`,
`commerces`, `services`, and `events`, with role checks applied by the API.
`POST /api/accounts/me/avatar` uses the same bucket under
`users/{id}/avatar`. Profile photos are limited to three changes per rolling
24-hour window; when a replacement succeeds, the previous R2 object is
deleted. In Render, configure the R2 values as secret environment variables;
do not commit them.

## API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /register | Public | Register tourist account |
| POST | /verify | Public | Verify account with token |
| POST | /login | Public | Login, returns JWT |
| POST | /forgot-password | Public | Request password reset |
| POST | /reset-password | Public | Reset password with token |
| POST | /logout | Authenticated | Logout (stateless) |

### Accounts (`/api/accounts`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | /me | Authenticated | Get current user info |
| PUT | /me | Authenticated | Update profile |
| PUT | /me/password | Authenticated | Change password |
| DELETE | /me | Authenticated | Soft delete account |
| POST | /me/operator-mode | Tourist | Enable operator mode |
| POST | /me/accreditation | Operator | Register accreditation |
| PUT | /me/mode | Tourist/Operator | Switch active mode |
| GET | / | Admin | List/filter accounts |
| GET | /{id} | Admin | Get account detail |
| POST | /{id}/gestor | Admin | Assign GESTOR role |
| DELETE | /{id}/gestor | Admin | Remove GESTOR role |
| PUT | /{id}/suspend | Admin | Suspend account |
| PUT | /{id}/reactivate | Admin | Reactivate account |
| GET | /{id}/role-history | Admin | Get role change history |

### Categories (`/api/categories`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | /public | Public | List active categories |
| GET | / | Admin | List all categories |
| GET | /{id} | Public | Get category |
| POST | / | Admin | Create category |
| PUT | /{id} | Admin | Update category |
| PUT | /{id}/status | Admin | Update status |
| DELETE | /{id} | Admin | Delete category |

### Commerces (`/api/commerces`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | / | Operator | Create commerce |
| GET | /mine | Operator | List own commerces |
| GET | /{id} | Operator | Get own commerce |
| PUT | /{id} | Operator | Update own commerce |
| PUT | /{id}/status | Operator | Update own commerce status |
| PUT | /{id}/deactivate | Operator | Deactivate own commerce |
| POST | /{commerceId}/services | Operator | Create service |
| GET | /{commerceId}/services | Operator | List services |
| GET | /public | Public | List/filter active commerces |
| GET | /public/{id} | Public | Get commerce detail |
| GET | /public/{commerceId}/services | Public | List active services |
| PUT | /admin/{id}/status | Admin | Update any commerce status |

### Services (`/api/services`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | /{id} | Operator | Get own service |
| PUT | /{id} | Operator | Update own service |
| DELETE | /{id} | Operator | Delete own service |
| PUT | /{id}/visibility | Operator | Toggle visibility |
| GET | /public | Public | List/filter active services |
| GET | /public/{id} | Public | Get service detail |
| PUT | /admin/{id}/status | Admin | Update any service status |

### Attractions (`/api/attractions`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | / | Gestor | Create attraction |
| GET | /admin | Admin | List all attractions |
| PUT | /{id} | Gestor | Update attraction |
| PUT | /{id}/status | Gestor | Update status |
| PUT | /{id}/occupancy | Gestor | Update occupancy |
| PUT | /{id}/deactivate | Gestor | Deactivate |
| PUT | /{id}/reactivate | Gestor | Reactivate |
| GET | /public | Public | List/filter active attractions |
| GET | /public/{id} | Public | Get attraction detail |
| GET | /public/{id}/alternatives | Public | Get alternatives for high occupancy |

### Events (`/api/events`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /destination | Gestor | Create destination event |
| POST | /commerce | Operator | Create commerce event |
| GET | /admin | Admin | List all events |
| PUT | /destination/{id} | Gestor | Update destination event |
| PUT | /commerce/{id} | Operator | Update commerce event |
| PUT | /destination/{id}/deactivate | Gestor | Deactivate destination event |
| PUT | /commerce/{id}/deactivate | Operator | Deactivate commerce event |
| PUT | /admin/{id}/status | Admin | Update any event status |
| GET | /public | Public | List/filter active events |
| GET | /public/{id} | Public | Get event detail |

### Recommendations (`/api/recommendations`)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | /attractions | Tourist | Recommend attractions |
| GET | /services | Tourist | Recommend services |
| GET | /events | Tourist | Recommend events |
| GET | /attractions/{id}/alternatives | Tourist | Get alternatives for high occupancy |

Query params for recommendations: `categoryId`, `date`

## Role System
- **TURISTA**: Default role, can view public content, get recommendations
- **OPERADOR**: Can manage own commerces/services, create commerce events
- **GESTOR**: Can manage attractions, create destination events
- **ADMIN**: Full access, user management, category management

Users have multiple roles + active mode (TURISTA/OPERADOR).

## Data Model Notes
- Soft deletes via status enums (ACTIVE, INACTIVE, SUSPENDED)
- Images stored as URL lists (no file upload)
- Schedules as embedded entities
- Occupancy level computed: LOW (<50%), MEDIUM (<80%), HIGH (>=80%)

## Assumptions Made
1. No email service - verification/reset tokens logged to console
2. No file storage - images are URL strings
3. No pagination in MVP - returns full lists
4. No rate limiting or advanced security features
5. Simple rule-based recommendations (no ML)
6. Category association via IDs in requests

## Testing
```bash
# Run integration tests (requires Docker)
./mvnw test
```

Tests cover:
- Register + login flow
- Create attraction as GESTOR
- Recommendations with high-occupancy alternatives