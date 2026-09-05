# 🚆 Real-Time Indian Railway Reservation System (Production-Ready)

A complete, production-grade, deployable Indian Railway Reservation System built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA / Hibernate**, **MySQL / PostgreSQL** (with automated **H2** fallback for instant local evaluation), **Spring Security (BCrypt + JWT)**, **Payment Gateway with Server-side Cryptographic Verification**, **Actuator Health Metrics**, and a modern, responsive web application.

---

## 🏛️ Architecture Overview

The system follows a clean, decoupled, layered enterprise architecture:

```
                                  [ Responsive Web Client (HTML5 / CSS3 / ES6) ]
                                                        │
                                                        ▼
                                       [ Spring Security & Rate Limiting Filter ]
                                                        │
                                                        ▼
                                            [ Spring Boot REST Controllers ]
                                                        │
    ┌─────────────────────────┬─────────────────────────┼─────────────────────────┬─────────────────────────┐
    ▼                         ▼                         ▼                         ▼                         ▼
[ Station Service ]    [ Train Service ]      [ Availability Engine ]   [ Payment Gateway ]       [ Reservation Engine ]
    │                         │                         │                         │                         │
    ├─ Multi-tier Ranking     ├─ Timetable Schedule     ├─ RailwayProvider        ├─ Razorpay Gateway       ├─ Idempotent Booking
    ├─ 717 Station Dataset    ├─ Running Day Filter     ├─ Short-TTL Cache        ├─ HMAC-SHA256 Verify     ├─ Coach Seat Allocation
    └─ Fast Autocomplete      └─ Station Code Match     └─ Itemized Fare System   └─ Mock Sandbox Mode      └─ Multi-passenger ERS
    │                         │                         │                         │                         │
    └─────────────────────────┴─────────────────────────┼─────────────────────────┴─────────────────────────┘
                                                        ▼
                                          [ Spring Data JPA Repositories ]
                                                        │
                                                        ▼
                                       [ Relational Database (MySQL / H2) ]
                                    Indexed by code, train_number, pnr, user_id
```

---

## 🌟 Key Capabilities & Production Features

1. **Comprehensive Indian Railway Station Master (717 Verified Stations)**:
   - Covers all 17 Railway Zones (`SR`, `SWR`, `NR`, `WR`, `CR`, `ER`, `ECR`, `NER`, `NFR`, `NCR`, `NW`, `SE`, `SECR`, `SCoR`, `WCR`, `MR`, `KR`) and 68 railway divisions across 28 Indian States and 8 Union Territories.
   - Exact station codes (e.g., `SBC`, `MAS`, `NDLS`, `MMCT`, `HWH`, `CBE`, `TPTY`, `ADI`, `PNVL`, `DWR`, `BNC`, `YPR`).
   - Disambiguates multi-station cities (e.g. `Bengaluru` -> `SBC`, `YPR`, `BNC`, `KJM`, `SMVB`; `Chennai` -> `MAS`, `MS`, `TBM`).
   - Autocomplete ranking: Exact Code > Code Prefix > Exact Name > Name Prefix > City > Aliases.
   - Robust JSON stream importer with code format validation (`^[A-Z0-9]{2,7}$`) and duplicate detection.

2. **Authentic Train Catalogue & Day-of-Week Backend Filtering**:
   - Master dataset featuring **Vande Bharat Express**, **Rajdhani Express**, **Shatabdi Express**, **Duronto**, **Superfast**, and **Express** trains.
   - Real 5-digit train numbers (e.g. `20607`, `20608`, `12951`, `12952`, `22691`, `12028`).
   - Strict Day-of-Week backend validation against `DayOfWeek` (e.g., `Except Tuesday`, `Tue, Thu, Fri`, `Daily`).
   - Route timetable viewer showing intermediate halts, arrival/departure timings, and travel distances.

3. **Pluggable Railway Data Provider (`RailwayProvider` SPI)**:
   - Decoupled interface for live train search, seat availability, itemized fare breakdown, official booking, and PNR status.
   - **No-Fake-Availability Policy**: When provider credentials are absent, the application clearly displays truthful status rather than generating synthetic `AVAILABLE-0042` strings.
   - Short TTL in-memory caching for live availability and longer TTL for static station master data.

4. **Transparent Itemized Fare Breakdown**:
   - Clear itemized structure: Base Fare, Reservation Charge, Superfast Surcharge, GST (5%), Catering Charge, and Dynamic Total.
   - Clearly distinguishes estimated fare from live provider fare.

5. **Payment Gateway & Server-Side Cryptographic Verification**:
   - Razorpay API integration with HMAC-SHA256 signature verification.
   - Sandbox mock fallback with instant simulated payment modal.
   - Never trusts client-side `payment_success=true` flags.
   - Server-side payment reconciliation: handles payment success, failed payment, and idempotent retries.

6. **Idempotent Booking Engine & Clear Identifier Separation**:
   - **Application Reservation Reference**: App-generated 10-character alphanumeric booking reference (e.g. `PNR7X9K2L1`).
   - **Official Indian Railways PNR**: Stored and displayed **only** when returned by an authorized railway reservation provider.
   - Thread-safe seat allocation across coach classes (`1A`, `2A`, `3A`, `SL`, `CC`, `2S`, `EC`, `3E`).
   - Berth preferences (`LOWER`, `MIDDLE`, `UPPER`, `SIDE LOWER`, `SIDE UPPER`) with explicit non-guarantee disclaimer.
   - Payment-transaction idempotency preventing duplicate ticket generation on browser refresh or double clicks.

7. **Security, User Profiles & Safe Cancellation**:
   - BCrypt password encryption and JWT token authentication.
   - User ownership verification: users cannot view or cancel other travelers' bookings.
   - Instant ticket cancellation, 15% cancellation fee computation, seat release, and refund logging.
   - Print-ready Electronic Reservation Slip (ERS) with clean `@media print` CSS.

8. **Monitoring & Health Checks**:
   - Spring Boot Actuator health endpoint (`/actuator/health`) reporting overall system health, database status, railway provider connectivity, and payment gateway state.

---

## 📋 System Requirements

- **Java Development Kit (JDK)**: Java 21 LTS or newer.
- **Build Tool**: Maven 3.8+ (wrapper `./mvnw` included).
- **Database**: MySQL 8.x or PostgreSQL (or zero-config built-in H2).
- **Docker**: Docker Engine 24+ and Docker Compose v2+ (for container deployment).

---

## 🚀 Quick Start (Local Development)

### 1. Zero-Setup Launch (In-Memory H2 Database)
```bash
# Clone the repository
git clone <repo-url>
cd railway-reservation-system

# Run with Maven Wrapper
./mvnw spring-boot:run
```
Open **`http://localhost:8080`** in your browser.

### 2. Run with MySQL Database
1. Ensure MySQL is running on port 3306.
2. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS railway_reservation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Run the application with the MySQL profile:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/railway_reservation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
   export DB_USERNAME="root"
   export DB_PASSWORD="your_mysql_password"

   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 🔑 Demo & Test Credentials

| Role | Email | Password |
|---|---|---|
| Demo Traveler | `tarun@example.com` | `password123` |

*(You can also register any new account instantly via the UI navigation bar.)*

---

## ⚙️ Environment Variables Configuration

Copy `.env.example` to `.env` or export these variables in your deployment environment:

| Variable | Description | Default / Example |
|---|---|---|
| `SERVER_PORT` | HTTP port | `8080` |
| `DB_URL` | JDBC Database Connection URL | `jdbc:mysql://localhost:3306/railway_reservation` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET` | Secret key for signing JWT tokens | `RailwayReservationSecretKeyForJwtSigningMustBeAtLeast32BytesLong` |
| `RAILWAY_API_PROVIDER` | Railway provider implementation (`mock`, `rapidapi`, `irctc`) | `mock` |
| `RAILWAY_API_KEY` | Upstream Railway API Secret Key | *(empty in mock mode)* |
| `RAILWAY_API_URL` | Upstream Railway API Endpoint | `https://irctc-indian-railway-pnr-status.p.rapidapi.com` |
| `PAYMENT_PROVIDER` | Payment Provider (`mock`, `razorpay`) | `mock` |
| `RAZORPAY_KEY_ID` | Razorpay Key ID | `rzp_test_xxxx` |
| `RAZORPAY_KEY_SECRET` | Razorpay Key Secret | `xxxx_secret_key` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `*` |

---

## 🐳 Docker Deployment

### Multi-Stage Dockerfile
Build the lightweight Docker container (Java 21 Alpine JRE):
```bash
docker build -t railway-reservation-system:latest .
```

### Run Full Stack with Docker Compose
To run MySQL 8 and the Railway Reservation Application together with health checks:
```bash
docker compose up -d
```
Verify the services are running:
```bash
docker compose ps
docker compose logs -f app
```
Access the application at **`http://localhost:8080`**.

---

## 🩺 Health Check & Monitoring

- **Overall Health**: `GET /actuator/health`
- **Application Metrics**: `GET /actuator/metrics`
- **Provider Status**: `GET /api/railway/provider-status`

Example Health Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "railwayProvider": {
      "status": "UP",
      "details": {
        "provider": "MockRailwayProvider",
        "officialPnrSupported": false
      }
    },
    "paymentGateway": {
      "status": "UP",
      "details": {
        "provider": "MockPaymentGateway",
        "mode": "SANDBOX"
      }
    }
  }
}
```

---

## 📡 REST API Documentation

### 1. Stations API
- `GET /api/stations` — Retrieve all stations in the Station Master.
- `GET /api/stations/search?q={query}` — Search stations by code, name, city, or alias with fuzzy matching.
- `GET /api/stations/{code}` — Get station details by official code (e.g. `SBC`).

### 2. Trains API
- `GET /api/trains/search?source={code}&destination={code}&date={YYYY-MM-DD}` — Search trains on route with running-day filtering.
- `GET /api/trains/{id}` — Get train details by internal ID.
- `GET /api/trains/by-number/{trainNumber}` — Get train details by official 5-digit number.
- `GET /api/trains/{trainNumber}/schedule` — Get complete train timetable and intermediate halt route.
- `GET /api/trains/{trainNumber}/availability?date={YYYY-MM-DD}&travelClass={class}&quota={quota}` — Check seat availability.
- `GET /api/trains/{trainNumber}/fare?date={YYYY-MM-DD}` — Get itemized fare breakdown.

### 3. Payment API
- `POST /api/payments/create-order` — Create payment order (`{ amount, currency, receiptId }`).
- `POST /api/payments/verify` — Verify HMAC-SHA256 signature (`{ orderId, paymentId, signature, amount }`).

### 4. Reservations & PNR API
- `POST /api/reservations` — Book tickets with multi-passenger support and payment ID.
- `GET /api/reservations/{pnr}` — Retrieve booking details by Application Reference or Official PNR.
- `GET /api/reservations/user/{userId}` — Retrieve user's booking history.
- `POST /api/reservations/{pnr}/cancel` — Cancel reservation with seat release and refund computation.
- `GET /api/pnr/{pnr}` — Universal PNR inquiry.

---

## 🧪 Automated Test Suite

Run the full suite of 68 integration, unit, and end-to-end tests:
```bash
./mvnw clean test
```

### Test Coverage Highlights:
- **Station Search & Import**: Verified code prefix ranking, multi-station city search, regex validation, duplicate handling, and 717-station JSON ingestion.
- **Train Running Days & Schedules**: Validated Day-of-Week calculations and route stop timetables.
- **Availability & Fares**: Validated class multipliers, GST, superfast surcharges, and cache behavior.
- **Payment & Verification**: Validated HMAC-SHA256 verification and sandbox mock fallback.
- **Booking Idempotency & Reconciliation**: Validated duplicate callback protection and state transitions.
- **Full End-to-End Integration**: 11-step complete flow test from station lookup to ticket cancellation.

---

## 🔒 Security & Best Practices

- **Zero Hardcoded Secrets**: Secrets are sourced strictly from environment variables.
- **Ownership Authorization**: Server verifies that the authenticated user owns the reservation before viewing or cancelling.
- **Server-Side Signature Check**: Payments must pass server-side HMAC-SHA256 verification before tickets are issued.
- **Departure != Destination Validation**: Backend rejects identical source and destination station requests.
- **XSS & SQL Injection Protection**: Prepared JPA queries and sanitized JSON input parsing.

---

## 📄 License & Disclaimer

This project is developed for educational, demonstration, and enterprise architectural evaluation purposes. Application-generated reservation slips are internal reference documents and do not constitute official IRCTC / PRS tickets for actual train travel on Indian Railways unless issued through an authorized Principal Service Provider (PSP) credentials integration.
