# CompanyOS — Current State

> Last updated: 2026-08-28
> Purpose: Snapshot of the full repository state before any new feature work begins.
> This file should be updated each time a phase is completed.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Repository Structure](#2-repository-structure)
3. [A — Current Architecture](#a--current-architecture)
4. [B — Working Features](#b--working-features)
5. [C — Broken Features](#c--broken-features)
6. [D — Database Status](#d--database-status)
7. [E — Authentication Status](#e--authentication-status)
8. [F — Frontend Status](#f--frontend-status)
9. [G — Backend Status](#g--backend-status)
10. [H — Immediate Blockers](#h--immediate-blockers)
11. [I — Recommended Development Order](#i--recommended-development-order)
12. [J — Exact Next Step](#j--exact-next-step)

---

## 1. Project Overview

**CompanyOS** is a Business Operating System designed to centralize company operations.
Tagline: *"Build. Learn. Improve. Grow."*

The platform is intended to manage:
- Employees, Departments, Projects, Tasks, Clients
- Notifications, Performance, Finance, Reports
- MATE — an AI-powered company assistant

**Current MVP focus:** Authentication, Company management, Department management.

---

## 2. Repository Structure

```
CompanyOS/
├── .github/
│   └── modernize/java-upgrade/     ← GitHub automation hooks
├── assets/                         ← Static assets (images, etc.)
├── backend/                        ← Spring Boot application
├── diagrams/                       ← Architecture diagrams
├── docs/                           ← Documentation files
│   ├── abstract.md
│   ├── algorithm.md
│   ├── architecture.md
│   ├── features.md
│   ├── mvp.md
│   ├── roadmap.md
│   └── vision.md
├── frontend/                       ← React + Vite application
├── journal/                        ← Development journal (Day-01..Day-08+)
├── Company OS MVP Architecture.png
└── README.md
```

### Backend structure

```
backend/src/main/java/com/companyos/backend/
├── BackendApplication.java
├── config/
│   ├── CorsConfig.java
│   ├── PasswordConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── CompanyController.java
│   ├── DepartmentController.java
│   └── EmployeeController.java
├── dto/
│   └── LoginResponse.java
├── entity/
│   ├── Company.java
│   ├── Department.java
│   ├── Employee.java
│   └── User.java
├── exception/                      ← Empty — no handler implemented yet
├── repository/
│   ├── CompanyRepository.java
│   ├── DepartmentRepository.java
│   ├── EmployeeRepository.java
│   └── UserRepository.java
├── service/
│   ├── CompanyService.java         ← Interface
│   ├── CustomUserDetailsService.java
│   ├── DepartmentService.java      ← Interface
│   ├── EmailService.java
│   ├── EmployeeService.java        ← Interface
│   └── impl/
│       ├── CompanyServiceImpl.java
│       ├── DepartmentServiceImpl.java
│       └── EmployeeServiceImpl.java
└── util/
    └── JwtUtil.java
```

### Frontend structure

```
frontend/src/
├── App.jsx       ← Main application (companies + departments)
├── App.css       ← Main styles
├── Login.jsx     ← Login / registration UI
├── Login.css     ← Login styles
├── index.css     ← Global styles
└── main.jsx      ← Entry point
```

---

## A — Current Architecture

### Technology stack

| Layer | Technology | Version |
|---|---|---|
| Backend framework | Spring Boot | 4.1.0 |
| Language | Java | 21 |
| Build tool | Maven | (spring-boot-starter-parent) |
| ORM | Spring Data JPA / Hibernate | (managed by Boot) |
| Database | MySQL | 8.x |
| Security | Spring Security | (managed by Boot) |
| JWT library | JJWT | 0.11.5 |
| Email | Spring Mail / Gmail SMTP | (managed by Boot) |
| Frontend framework | React | 19 |
| Frontend build | Vite | 8 |
| Frontend language | JavaScript (JSX) | ES Modules |

### Backend layer flow

```
HTTP Request
     ↓
Spring Security Filter Chain   ← currently passes everything through
     ↓
Controller                     ← @RestController, @RequestMapping
     ↓
Service (interface)            ← business logic
     ↓
ServiceImpl                    ← concrete implementation
     ↓
Repository (JpaRepository)     ← data access
     ↓
Entity (JPA @Entity)           ← mapped to MySQL table
     ↓
MySQL (companyos database)
```

### Frontend flow (current)

```
Browser
  ↓
React (Vite dev server — localhost:5173)
  ↓
Login.jsx   ← handles login/register entirely via localStorage (no backend call)
  ↓
App.jsx     ← main app, calls backend only for Company and Department CRUD
  ↓
fetch() to http://localhost:8080/api
```

### Key configuration

- Backend port: `8080`
- Frontend dev port: `5173`
- Database: `jdbc:mysql://127.0.0.1:3306/companyos`
- CORS: configured for `http://localhost:5173` via `CorsConfig.java`
  - Note: `@CrossOrigin` annotations also exist on some controllers — inconsistent

---

## B — Working Features

### Backend

| Feature | Endpoint | Status |
|---|---|---|
| Spring Boot startup | — | ✅ Working |
| MySQL connection | — | ✅ Working |
| JPA / Hibernate DDL auto-update | — | ✅ Working |
| Company: create | `POST /api/companies` | ✅ Working |
| Company: read all | `GET /api/companies` | ✅ Working |
| Company: read one | `GET /api/companies/{id}` | ✅ Working |
| Company: update | `PUT /api/companies/{id}` | ✅ Working |
| Company: delete | `DELETE /api/companies/{id}` | ✅ Working |
| Department: create | `POST /api/departments` | ✅ Working |
| Department: read all | `GET /api/departments` | ✅ Working |
| Department: read one | `GET /api/departments/{id}` | ✅ Working |
| Department: update | `PUT /api/departments/{id}` | ✅ Working |
| Department: delete | `DELETE /api/departments/{id}` | ✅ Working |
| Employee: create | `POST /api/employees` | ✅ Working (backend only) |
| Employee: read all | `GET /api/employees` | ✅ Working (backend only) |
| Employee: read one | `GET /api/employees/{id}` | ✅ Working (backend only) |
| Employee: update | `PUT /api/employees/{id}` | ✅ Working (backend only) |
| Employee: delete | `DELETE /api/employees/{id}` | ✅ Working (backend only) |
| Password hashing | BCrypt via `PasswordConfig` | ✅ Working |
| Verify email endpoint | `POST /api/auth/verify` | ✅ Logic correct |
| Login endpoint | `POST /api/auth/login` | ✅ Partial (no token returned) |

### Frontend

| Feature | Status |
|---|---|
| Login page renders | ✅ Working |
| Register page renders | ✅ Working |
| localStorage-based login/register | ✅ Working (but fake — no backend) |
| Company list display | ✅ Working |
| Company create form | ✅ Working |
| Company edit (prompt-based) | ✅ Working |
| Company delete | ✅ Working |
| Department list display | ✅ Working |
| Department create form | ✅ Working |
| Department edit | ✅ Working |
| Department delete | ✅ Working |

---

## C — Broken Features

### Critical

| Problem | Location | Detail |
|---|---|---|
| Gmail SMTP authentication fails | `EmailService.java`, `application.properties` | `spring.mail.username` and `spring.mail.password` are placeholders (`mi@gmail.com`, `Test@12`). Gmail rejects with `535-5.7.8 Username and Password not accepted`. |
| User saved before email sent | `AuthController.java` line ~50 | `userRepository.save(user)` is called before `emailService.sendVerificationEmail()`. When email fails, user record is orphaned in DB — unverified and unreachable by re-registration. |
| JWT never returned on login | `AuthController.java` `/login` | `JwtUtil.generateToken()` exists and works but is **never called**. Login returns only `LoginResponse(userId, username, role)` — no bearer token. |
| All endpoints are unprotected | `SecurityConfig.java` | `.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())` — every endpoint is public. There is no JWT validation filter. |
| Frontend does not call backend for auth | `Login.jsx` | Login and register use only `localStorage`. The real backend `/api/auth/register` and `/api/auth/login` are never called. |
| JWT secret is hardcoded | `JwtUtil.java` | `"this-is-a-temporary-secret-key-change-me-1234567890"` is hardcoded. If committed, anyone can forge tokens. |
| Real credentials risk in config | `application.properties` | DB password `Mysql#2026` is hardcoded. If real Gmail credentials were added here, they would be committed to Git. |

### Medium

| Problem | Location | Detail |
|---|---|---|
| No request DTOs for auth | `AuthController.java` | `@RequestBody User user` exposes the JPA entity directly — internal fields like `verified`, `verificationCode`, `userId` can be set by the caller. |
| No centralized exception handler | `exception/` (empty) | Service layer throws raw `RuntimeException`. No `@ControllerAdvice`. Stack traces may leak in error responses. |
| `getEmployee()` returns null on 404 | `EmployeeServiceImpl.java` | Returns `null` instead of throwing a 404 exception. |
| No input validation | `AuthController.java` | Only blank-check on email and password. No format validation (email regex), no password strength rule. |
| No `status` field on User | `User.java` | User spec requires ACTIVE/INACTIVE/LOCKED status. Field does not exist — locked accounts cannot be enforced. |
| No `createdAt` / `lastLogin` on User | `User.java` | Audit timestamps missing. |
| No resend-verification endpoint | `AuthController.java` | If email fails, there is no way for the user to request a new verification code. |
| Employee entity schema inconsistency | `Employee.java` | Has both `department` (String) and `departmentId` (Long) — redundant. No proper FK relationship to `Department`. |
| Inconsistent CORS configuration | `CorsConfig.java`, `CompanyController.java`, `DepartmentController.java` | Both a global `CorsConfigurationSource` bean and `@CrossOrigin` annotations exist. This is redundant and can cause unexpected behavior. |
| `@Data` used on Company only | `Company.java` | Lombok `@Data` used inconsistently. Other entities use manual getters/setters. |

### Low

| Problem | Location | Detail |
|---|---|---|
| `EmployeeController` base path | `EmployeeController.java` | Maps to `/api` instead of `/api/employees`. Works but inconsistent with conventions. |
| No pagination on list endpoints | All `GET` list endpoints | `findAll()` with no pagination will become a problem at scale. |
| Frontend uses `alert()` and `confirm()` | `App.jsx` | Browser dialogs are not appropriate for a production UI. Should use in-page feedback. |
| Frontend stores data in localStorage | `App.jsx` | Company and department data is duplicated between localStorage and the database. Stale data risk. |
| `response.txt` committed to backend | `backend/response.txt` | Looks like a temporary test artifact. Should be removed. |

---

## D — Database Status

### Connection

```
URL:      jdbc:mysql://127.0.0.1:3306/companyos
User:     root
DDL:      hibernate.ddl-auto=update
Status:   ✅ CONNECTED AND WORKING
```

### Tables (generated by Hibernate from entities)

| Table | Entity | Key Fields | Status |
|---|---|---|---|
| `users` | `User.java` | `user_id`, `username`, `password`, `role`, `verified`, `verification_code` | ✅ Exists |
| `company` | `Company.java` | `company_id`, `company_name`, `email`, `phone`, `address`, `website`, `industry`, `logo_url` | ✅ Exists |
| `department` | `Department.java` | `department_id`, `department_name`, `description`, `company_id` | ✅ Exists |
| `employee` | `Employee.java` | `employee_id`, `department_id`, `employee_code`, `first_name`, `last_name`, `email`, `phone`, `gender`, `designation`, `salary`, `hire_date`, `status`, `department`, `employee_name`, `position`, `company_id` | ✅ Exists |

### Missing from User entity vs original spec

| Field | Present | Notes |
|---|---|---|
| `status` (ACTIVE/INACTIVE/LOCKED) | ❌ Missing | Required for account locking |
| `created_at` | ❌ Missing | Audit trail |
| `last_login` | ❌ Missing | Activity tracking |
| `employee_id` (FK to employee) | ❌ Missing | Link between user account and employee record |

### Known data problem

Orphaned user records likely exist in the `users` table — users who attempted registration, were saved to the database, but whose email sending failed. These accounts are:
- Unverified (`verified = false`)
- Impossible to re-register (duplicate username error)
- Impossible to log in (verification check blocks login)

---

## E — Authentication Status

### What exists

```
POST /api/auth/register   → validates input, hashes password, saves user, attempts email
POST /api/auth/verify     → matches code, sets verified=true
POST /api/auth/login      → checks credentials, checks verified, returns user info (no token)
```

### What is missing

```
POST /api/auth/resend-verification   → not implemented
POST /api/auth/logout                → not needed for JWT but good to have for token blacklist
JWT filter                           → not implemented (JwtAuthFilter does not exist)
Role-based @PreAuthorize             → not implemented
```

### Authentication flow — current (broken)

```
Client → POST /api/auth/register
  ✅ Validates blank fields
  ✅ Checks duplicate username
  ✅ Hashes password with BCrypt
  ✅ Generates 6-digit verification code
  ✅ Saves user to DB
  ❌ Sends email → FAILS (bad Gmail credentials)
  ❌ Returns HTTP 500

Client → POST /api/auth/verify
  ✅ Looks up user
  ✅ Compares code
  ✅ Sets verified=true
  (unreachable in practice because email never delivered)

Client → POST /api/auth/login
  ✅ Validates blank fields
  ✅ Looks up user
  ✅ BCrypt password match
  ✅ Checks verified flag
  ❌ Returns LoginResponse with NO JWT token
  ❌ Subsequent requests have no way to prove authentication
```

### JwtUtil — exists but unused

`JwtUtil.java` is fully implemented:
- `generateToken(username, role)` — creates a signed JWT
- `extractUsername(token)` — parses subject
- `extractRole(token)` — parses role claim
- `isTokenValid(token)` — validates signature + expiry

But it is never injected into `AuthController` and never called. The secret key is hardcoded.

### Security configuration — current

```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()   // ← everything is open
)
.httpBasic(disabled)
.formLogin(disabled)
// No JWT filter added to filter chain
```

---

## F — Frontend Status

### Tech

- React 19, Vite 8, plain JavaScript (JSX)
- No routing library (React Router not installed)
- No state management library
- No UI component library
- No HTTP client library (raw `fetch`)
- No environment variable usage (API URL hardcoded as `"http://localhost:8080/api"`)

### Pages / components

| Component | File | Connected to backend | Status |
|---|---|---|---|
| Login / Register | `Login.jsx` | ❌ No | Uses localStorage only |
| Main app (companies + departments) | `App.jsx` | ✅ Yes (CRUD calls) | Working |

### Frontend authentication — how it currently works

```
User fills login form → Login.jsx reads localStorage["companyos_accounts"]
→ checks email + password match (plaintext, stored in localStorage)
→ sets localStorage["companyos_current_user"] = email
→ App.jsx reads that key on load → shows main app

Registration:
→ stores {password, companies, departments, employees} in localStorage
→ password stored in PLAINTEXT in browser storage
→ no call to backend whatsoever
```

This means:
- A user can register on the frontend without the backend knowing
- A user can log in on the frontend without the backend knowing
- The backend user accounts and frontend user accounts are completely separate systems

### Frontend missing

- React Router (no routing between pages)
- No email verification step or page
- No token storage / Authorization header on API calls
- No loading states on most actions
- No proper error messages (uses `alert()`)
- No employee management UI (backend employee CRUD exists but is unused)
- No dashboard
- No protected routes

---

## G — Backend Status

### Spring Boot application

| Component | Status |
|---|---|
| Application starts | ✅ |
| MySQL connects | ✅ |
| Hibernate initializes | ✅ |
| Controllers register | ✅ |
| Security filter chain loads | ✅ |
| Mail autoconfiguration loads | ✅ (but fails at send time) |

### Controllers

| Controller | Base path | Status |
|---|---|---|
| `AuthController` | `/api/auth` | ⚠️ Partial — email fails, no JWT |
| `CompanyController` | `/api` | ✅ Working |
| `DepartmentController` | `/api/departments` | ✅ Working |
| `EmployeeController` | `/api` | ✅ Working (no frontend) |

### Services

| Service | Status |
|---|---|
| `CompanyServiceImpl` | ✅ Working |
| `DepartmentServiceImpl` | ✅ Working |
| `EmployeeServiceImpl` | ✅ Working (null return on not-found is a bug) |
| `EmailService` | ❌ Fails (bad credentials) |
| `CustomUserDetailsService` | ✅ Implemented (used by Spring Security internally) |

### Utilities / config

| File | Status |
|---|---|
| `JwtUtil` | ✅ Implemented, ❌ Never used |
| `PasswordConfig` (BCrypt) | ✅ Working |
| `CorsConfig` | ✅ Working for `localhost:5173` |
| `SecurityConfig` | ⚠️ All routes open, no JWT filter |

---

## H — Immediate Blockers

Listed in order of priority:

### Blocker 1 — Gmail SMTP credentials are placeholders
**File:** `backend/src/main/resources/application.properties`
```properties
spring.mail.username=mi@gmail.com      ← placeholder
spring.mail.password=Test@12           ← placeholder
```
Every registration attempt fails with HTTP 500. No user can complete registration.

### Blocker 2 — User saved before email is sent
**File:** `AuthController.java`
```java
userRepository.save(user);                       // ← committed to DB
emailService.sendVerificationEmail(...);         // ← then fails
```
When email fails, user is stuck in DB. They cannot re-register (duplicate) and cannot log in (not verified). There is no recovery path.

### Blocker 3 — Login returns no JWT
**File:** `AuthController.java` `/login` endpoint
```java
return ResponseEntity.ok(new LoginResponse(userId, username, role));
// JwtUtil is never called
```
Even with working auth, the client receives no token. No protected endpoint can be called.

### Blocker 4 — No JWT filter in security chain
**File:** `SecurityConfig.java`
No `JwtAuthFilter` exists. Even if a token were returned, it would never be validated on subsequent requests.

### Blocker 5 — Frontend does not use the backend for auth
**File:** `Login.jsx`
Frontend auth is 100% localStorage. The backend auth system is completely bypassed.

### Blocker 6 — JWT secret is hardcoded
**File:** `JwtUtil.java`
```java
private final Key key = Keys.hmacShaKeyFor(
    "this-is-a-temporary-secret-key-change-me-1234567890".getBytes()
);
```
Must be externalized before the token system is wired up.

---

## I — Recommended Development Order

### Phase 1 — Authentication Stabilization (current priority)

| Step | Task |
|---|---|
| 1.1 | Externalize secrets (Gmail, JWT, DB password) to environment variables |
| 1.2 | Add `RegisterRequest` and `LoginRequest` DTOs |
| 1.3 | Extract auth logic from controller into `AuthService` |
| 1.4 | Fix registration transaction: save only after email succeeds, or implement safe recovery |
| 1.5 | Add `resend-verification` endpoint |
| 1.6 | Add `status` field to `User` entity (ACTIVE / INACTIVE / LOCKED) |
| 1.7 | Wire `JwtUtil` into login — return token in `LoginResponse` |
| 1.8 | Implement `JwtAuthFilter` |
| 1.9 | Update `SecurityConfig` to protect routes by role |
| 1.10 | Connect `Login.jsx` to real backend (register → verify → login) |
| 1.11 | Add email verification page/step to frontend |

### Phase 2 — Employee Management

- Add employee management UI to frontend
- Clean up `Employee` entity (remove redundant fields, proper FK)
- Add validation and proper error responses
- Wire authentication context to employee operations

### Phase 3 — Dashboard

- Backend: `/api/dashboard` summary endpoint
- Frontend: dashboard page with real statistics

### Phase 4 — Projects

- New `Project` entity, service, controller
- Frontend project management UI

### Phase 5 — Tasks

- New `Task` entity linked to Project + Employee
- Frontend task management UI

### Phase 6 — MATE basic intelligence

- MATE service abstraction layer
- Initial rule-based insights from CompanyOS data
- Frontend MATE chat interface

---

## J — Exact Next Step

**Task: Externalize secrets and fix the Gmail configuration**

This is the smallest safe change that unblocks everything else.
No entities change. No endpoints change. No frontend changes.

### What needs to happen

1. Create `backend/src/main/resources/application.properties` — replace hardcoded secrets with `${ENV_VAR_NAME}` placeholders
2. Create `.env.example` at the repo root — document every required environment variable with safe placeholder values
3. Update `JwtUtil.java` — inject JWT secret from `@Value("${jwt.secret}")` instead of hardcoding
4. Verify `.gitignore` ensures no `.env` file with real values can be committed

### Files that will change

| File | Change |
|---|---|
| `application.properties` | Replace hardcoded credentials with `${...}` env var references |
| `JwtUtil.java` | Add `@Value("${jwt.secret}")` injection |
| `.env.example` (new) | Document all required env vars |
| `backend/.gitignore` | Verify `.env` is excluded |

### Files that will NOT change

Everything else. All controllers, services, entities, repositories, and frontend files stay untouched.

---

*This document will be updated at the end of each development phase.*
