# CompanyOS — Next Session Prompt

> Copy this entire prompt to start the next Kiro session exactly where this one stopped.

---

## PROJECT LOCATION

```
C:\Users\Admin\OneDrive\Desktop\CompanyOS
```

Backend: Spring Boot (Java 21) — `backend/`
Frontend: React/Vite — `frontend/`
Database: MySQL 8 — `companyos`
GitHub: https://github.com/Mishaal-hub/Capstone-CompanyOS.git

---

## SESSION 1 SUMMARY (2026-11-02) — COMPLETED

### Last commit pushed to GitHub:
**Hash:** `d0ff168` (pending push — or check `git log --oneline -3`)
**Branch:** `main`

### What was done in Session 1:
1. Full codebase audit — identified all bugs and security vulnerabilities
2. Fixed pom.xml build blocker (non-existent test starters replaced with spring-boot-starter-test)
3. Fixed MATE.jsx missing FolderKanban import (runtime crash)
4. Fixed Employee IDOR security vulnerability — all CRUD now org-scoped (was findAll() global)
5. Fixed Department IDOR security vulnerability — all CRUD now org-scoped (was findAll() global)
6. Fixed Dashboard totalEmployees using global count()
7. Removed @CrossOrigin from DepartmentController
8. Removed MEMBER role exclusion from DashboardController
9. Created NotificationController (GET/PATCH endpoints)
10. Removed hardcoded organizationId:1 from Tasks.jsx, Goals.jsx, Projects.jsx
11. Fixed Tasks.jsx empty projectId validation bug
12. Added Goals edit/delete functionality
13. Replaced Team.jsx read-only view with full CRUD form
14. Created Notifications.jsx page
15. Added Notifications route to App.jsx and Sidebar
16. Fixed Dashboard navigation buttons (View Calendar, View All Projects)
17. Created DEVELOPMENT_LOG.md, PENDING_TASKS.md, CHANGELOG.md, updated CURRENT_STATE.md
18. Pushed all changes to GitHub (commit d0ff168)

### Test results after Session 1:
- Backend: `./mvnw.cmd test` → **12/12 PASS, BUILD SUCCESS**
- Frontend: `npm run build` → **0 errors, 0 warnings, BUILD SUCCESS**

---

## START OF NEXT SESSION CHECKLIST

Before doing anything else:

```powershell
cd "C:\Users\Admin\OneDrive\Desktop\CompanyOS"
git status
git log --oneline -5
```

Then read:
1. CURRENT_STATE.md
2. PENDING_TASKS.md
3. DEVELOPMENT_LOG.md (last 50 lines)

---

## OUTSTANDING TASKS (in priority order)

### HIGH — Settings page profile update (B009)
**Problem:** Settings page "Save Changes" button does nothing — no API call made.
**What to do:**
1. Create `PUT /api/users/profile` or `PATCH /api/users/me` backend endpoint
2. Create a UserProfileController or add to existing AuthController
3. Allow updating: fullName, email (with re-verification if changed), avatar
4. Wire Settings.jsx to call this endpoint
**Files:** `Settings.jsx`, need new controller + service method

### MEDIUM — Company page not in sidebar (FE005)
**Problem:** Company page exists at `/companies` but there is no sidebar link.
**What to do:** Add Building2/Company entry to Sidebar.jsx navItems under WORKSPACE section

### MEDIUM — JWT secret startup enforcement (S005)
**Problem:** If `JWT_SECRET` env var is not set, app silently uses a predictable fallback secret.
**What to do:** In `JwtUtil.java` `@PostConstruct initKey()`, add a log WARNING or throw an `IllegalStateException` in production profiles when the secret equals the default value.

### LOW — Email verification OTP attempt tracking (S006)
**Problem:** `AuthServiceImpl.verifyEmail()` has no attempt counter — brute-force risk.
**What to do:** Add an in-memory attempt counter to the verifyEmail method (same pattern as password reset OTP).

### MEDIUM — Integration tests for Employee/Department org isolation (T004/T005)
**What to do:** Write tests that verify one org's users cannot see another org's employees/departments.
**File:** `backend/src/test/java/com/companyos/backend/`

---

## FILES MODIFIED IN SESSION 1

### Backend (modified):
- `backend/pom.xml`
- `backend/src/main/java/com/companyos/backend/controller/DashboardController.java`
- `backend/src/main/java/com/companyos/backend/controller/DepartmentController.java`
- `backend/src/main/java/com/companyos/backend/controller/EmployeeController.java`
- `backend/src/main/java/com/companyos/backend/dto/EmployeeRequestDTO.java`
- `backend/src/main/java/com/companyos/backend/entity/Employee.java`
- `backend/src/main/java/com/companyos/backend/repository/DepartmentRepository.java`
- `backend/src/main/java/com/companyos/backend/repository/EmployeeRepository.java`
- `backend/src/main/java/com/companyos/backend/service/impl/DashboardServiceImpl.java`
- `backend/src/main/java/com/companyos/backend/service/impl/DepartmentServiceImpl.java`
- `backend/src/main/java/com/companyos/backend/service/impl/EmployeeServiceImpl.java`
- `backend/src/test/java/com/companyos/backend/BackendApplicationTests.java`

### Backend (new):
- `backend/src/main/java/com/companyos/backend/controller/CalendarEventController.java`
- `backend/src/main/java/com/companyos/backend/controller/NotificationController.java`
- `backend/src/main/java/com/companyos/backend/dto/CalendarEventRequestDTO.java`
- `backend/src/main/java/com/companyos/backend/dto/CalendarEventResponseDTO.java`
- `backend/src/main/java/com/companyos/backend/entity/CalendarEvent.java`
- `backend/src/main/java/com/companyos/backend/repository/CalendarEventRepository.java`
- `backend/src/main/java/com/companyos/backend/service/CalendarEventService.java`
- `backend/src/main/java/com/companyos/backend/service/impl/CalendarEventServiceImpl.java`
- `backend/src/test/java/com/companyos/backend/CalendarEventIntegrationTests.java`

### Frontend (modified):
- `frontend/src/App.jsx`
- `frontend/src/api.js`
- `frontend/src/components/Sidebar.jsx`
- `frontend/src/pages/Activity.jsx`
- `frontend/src/pages/Dashboard.jsx`
- `frontend/src/pages/Goals.jsx`
- `frontend/src/pages/MATE.jsx`
- `frontend/src/pages/Projects.jsx`
- `frontend/src/pages/Tasks.jsx`
- `frontend/src/pages/Team.jsx`

### Frontend (new):
- `frontend/src/pages/Calendar.jsx`
- `frontend/src/pages/Calendar.css`
- `frontend/src/pages/Notifications.jsx`
- `frontend/src/calendar/calendarUtils.mjs`
- `frontend/src/calendar/calendarUtils.test.mjs`

### Documentation (new/updated):
- `DEVELOPMENT_LOG.md`
- `PENDING_TASKS.md`
- `CHANGELOG.md`
- `CURRENT_STATE.md`
- `NEXT_SESSION_PROMPT.md` (this file)

---

## IMPORTANT CONSTRAINTS

1. **Never delete working functionality** — existing auth, tasks, projects, goals, calendar all work
2. **Never drop or recreate the MySQL database** — use `spring.jpa.hibernate.ddl-auto=update`
3. **Never commit .env files** — secrets stay out of Git
4. **Never modify authentication unnecessarily** — it is complete and tested
5. **Run tests after every backend change:** `./mvnw.cmd test`
6. **Run build after every frontend change:** `npm run build`
7. **Update DEVELOPMENT_LOG.md after every task**

---

## ENVIRONMENT SETUP REMINDER

The backend needs these environment variables set before running:
```
DB_USERNAME, DB_PASSWORD, MAIL_USERNAME, MAIL_PASSWORD, JWT_SECRET
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET (optional, for Google OAuth)
```

See `.env.example` for details. Never commit `.env`.

---

## GitHub Repository

https://github.com/Mishaal-hub/Capstone-CompanyOS.git

Branch: `main`
Latest commit: `d0ff168` (Session 1 — feat: complete CompanyOS security hardening...)
