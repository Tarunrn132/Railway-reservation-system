# 🚆 RailReserve — Real-Time Railway Data & Production Setup Guide

This document provides complete instructions for configuring legitimate external railway data providers, SMS OTP gateways, Google OAuth 2.0 authentication, payment processing, and production deployment for the **Railway Reservation System**.

---

## 📑 Table of Contents
1. [Architecture & Provider Abstraction](#1-architecture--provider-abstraction)
2. [Domain Terminology & Legal Boundaries](#2-domain-terminology--legal-boundaries)
3. [Railway Data Provider Setup](#3-railway-data-provider-setup)
4. [Authentication & Social Login Setup](#4-authentication--social-login-setup)
5. [SMS & Email Notification Gateways](#5-sms--email-notification-gateways)
6. [Payment Gateway Configuration](#6-payment-gateway-configuration)
7. [Environment Variables Reference](#7-environment-variables-reference)
8. [Local Development vs Production Execution](#8-local-development-vs-production-execution)

---

## 1. Architecture & Provider Abstraction

The system uses a strict **`RailwayDataProvider`** abstraction:

```
                                  [ Client Browser ]
                                          │
                                          ▼
                             [ Spring Boot REST APIs ]
                                          │
                                          ▼
                             [ RailwayInfoService ]
                                 │              │
                   ┌─────────────┴──┐        ┌──┴─────────────┐
                   ▼                ▼        ▼                ▼
     [ ExternalRailwayDataProvider ]      [ LocalTimetableProvider ]
           (Active Live Feed)                 (Honest Timetable Fallback)
```

- **`ExternalRailwayDataProvider`**: Uses Spring `RestClient` to query configured upstream endpoints. Sets `liveDataAvailable = true` only when authentic live responses are returned.
- **`LocalTimetableProvider`**: Static master reference timetable. Sets `liveDataAvailable = false` and serves genuine schedule/fare models with zero fabricated delays or simulated GPS movements.

---

## 2. Domain Terminology & Legal Boundaries

| Terminology | Scope & Meaning |
|---|---|
| **Application Reservation** | An internal booking created and managed by this application (`PNRxxxx`, coach seat `C1-01`). **Not valid for Indian Railways train travel.** |
| **Official IRCTC PRS Ticket** | Tickets issued through an authorized Indian Railways / IRCTC Principal Service Provider (PSP) integration with valid commercial licenses. |
| **Live Train Tracking** | Real-time GPS/NTES running status from an external provider (delay, current halt, expected arrival). |
| **Master Timetable Reference** | Verified static schedules and halt sequences from public CRIS reference tables. |

---

## 3. Railway Data Provider Setup

### Supported Capabilities
1. **Train Search**: Route queries between station pairs with running day schedules.
2. **Live Train Tracking**: `GET /api/trains/{trainNumber}/live-status` with route progress timeline.
3. **Seat Availability**: `GET /api/trains/{trainNumber}/availability?class=3A&quota=GN`.
4. **Fare Breakdown**: `GET /api/trains/{trainNumber}/fare`.
5. **Official PNR Lookup**: `GET /api/pnr/{pnr}`.

### Configuration
Set the following environment variables:
```bash
export RAILWAY_API_PROVIDER="RapidAPI_IRCTC"
export RAILWAY_API_URL="https://irctc-indian-railway-data.p.rapidapi.com"
export RAILWAY_API_KEY="your_api_key_here"
```

---

## 4. Authentication & Social Login Setup

### A. Phone Number + OTP Login
- **Flow**: User requests OTP $\rightarrow$ Backend generates 6-digit cryptographically secure OTP via `SecureRandom` $\rightarrow$ Hashed in DB via `BCrypt` $\rightarrow$ Sent via SMS $\rightarrow$ Verified within 5 minutes.
- **Security**: 60-second resend cooldown, max 5 attempts, max 10 requests/hour.

### B. Google OAuth 2.0 / OpenID Connect
Set the following credentials from the [Google Cloud Console](https://console.cloud.google.com/):
```bash
export GOOGLE_CLIENT_ID="your_google_client_id.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="your_google_client_secret"
```

---

## 5. SMS & Email Notification Gateways

### Supported SMS Providers
- **Twilio**: Set `SMS_PROVIDER=twilio`, `SMS_API_KEY=your_account_sid`, `SMS_API_SECRET=your_auth_token`.
- **MSG91**: Set `SMS_PROVIDER=msg91`, `SMS_API_KEY=your_auth_key`, `SMS_SENDER_ID=RAILRES`.
- **Mock (Default)**: Safely logs delivery metadata without leaking plain OTPs.

---

## 6. Payment Gateway Configuration

```bash
export PAYMENT_GATEWAY="razorpay"
export PAYMENT_API_KEY="rzp_live_xxxxxx"
export PAYMENT_API_SECRET="your_secret_key"
```

---

## 7. Environment Variables Reference

See [`.env.example`](.env.example) for the full list of template variables.

---

## 8. Local Development vs Production Execution

### Default Development Startup (H2 in MySQL Mode)
```bash
mvn spring-boot:run
```

### Production Execution (MySQL Database)
```bash
export DB_URL="jdbc:mysql://localhost:3306/railway_reservation?useSSL=false&serverTimezone=UTC"
export DB_USERNAME="railway_user"
export DB_PASSWORD="secure_password"

mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Running Automated Test Suite
```bash
mvn clean test
```
