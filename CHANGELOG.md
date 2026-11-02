# CompanyOS — Changelog

> Format: Added | Changed | Fixed | Security | Testing
> Chronological order, newest first.

---

## [Session 1] — 2026-11-02

### Fixed
- **pom.xml** — Replaced three non-existent Spring Boot test starters (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test`) with the correct `spring-boot-starter-test`. Maven build was completely broken.
- **MATE.jsx** — Added missing `FolderKanban` import from lucide-react. Page crashed at runtime with ReferenceError.
- **Tasks.jsx** — Removed hardcoded `organizationId: 1` from task creation payload. Added projectId required validation with user-facing error message.
- **Goals.jsx** — Removed hardcoded `organizationId: 1`. Added full edit/delete functionality.
- **Projects.jsx** — Removed hardcoded `organizationId: 1` from project creation.
- **DashboardServiceImpl** — Changed `employeeRepository.count()` (global) to `employeeRepository.countByOrganizationId(orgId)` — dashboard was showing wrong employee count across all tenants.
- **DashboardController** — Removed `@PreAuthorize` that excluded MEMBER role from dashboard access.
- **DepartmentController** — Removed `@CrossOrigin(origins="http://localhost:5173")` hardcoded annotation; global CorsConfig handles this correctly.
- **Dashboard.jsx** — Wired "View Calendar" and "View All Projects" buttons to `navigate()` calls (previously inert).

### Security
- **Employee IDOR (CRITICAL)** — `EmployeeController`, `EmployeeServiceImpl`, `EmployeeRepository` all updated to scope operations to the authenticated organization. Previously, any authenticated user could read/modify employees from any organization.
- **Department IDOR (CRITICAL)** — `DepartmentController`, `DepartmentServiceImpl`, `DepartmentRepository` all updated to scope operations to the authenticated organization. Previously, any authenticated user could read/modify departments from any organization.
- **Employee entity** — Added `organizationId` column. `saveEmployee()` stamps orgId from TenantContext, never from client input.
- **Department entity** — `saveDepartment()` stamps orgId from TenantContext, never from client input.

### Added
- **NotificationController** — New REST controller exposing: `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all`. Previously the full notification service was unreachable from the frontend.
- **Notifications.jsx** — New frontend page with notification list, type icons, unread badge, mark-read and mark-all-read actions.
- **Team.jsx** — Full CRUD form added. Previously read-only display only.
- **Goals.jsx** — Edit and delete buttons added per goal card.
- **Sidebar** — Notifications link added to WORKSPACE section.
- **App.jsx** — `/notifications` route added.
- **api.js** — Added: `getNotifications`, `getUnreadCount`, `markNotificationRead`, `markAllNotificationsRead`, `getEmployees`, `createEmployee`, `updateEmployee`, `deleteEmployee`, `getDepartments`, `createDepartment`, `updateDepartment`, `deleteDepartment`.

### Testing
- **Backend** — `./mvnw.cmd test`: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 — **BUILD SUCCESS**
- **Frontend** — `npm run build`: ✓ 1845 modules, 0 errors — **BUILD SUCCESS**

---

---

## Pre-Session State (as of 2026-10-08, commit 10a9147)

### Added (Already Present)
- Complete authentication system: register, login, email OTP verification, resend verification, Google OAuth (callback + OTP), forgot password, verify reset OTP, reset password
- JWT generation with userId/role/organizationId claims
- TenantContext ThreadLocal for multi-tenant request scoping
- Task management: full CRUD, status updates, dependencies, comments — all org-scoped
- Project management: full CRUD, members, milestones, progress calculation — org-scoped
- Goal management: full CRUD, progress tracking — org-scoped
- Calendar module: full CRUD with Instant timestamps, IANA timezone support, event types/statuses — org-scoped
- Dashboard: real database data — employees, projects, tasks, health score, critical alerts, recent activity
- Audit logging: creation/update/deletion events with orgId + userId
- Notification service layer: send, retrieve, markAsRead, markAllAsRead, unreadCount
- Frontend routing: all pages wired (Dashboard, Tasks, Projects, Goals, Team, Calendar, Activity, Growth, MATE, Settings, Company)
- Complete Login UI: multi-screen flow with OTP boxes, password strength meter, resend cooldowns
- Sidebar: sectioned navigation, mobile overlay, logout
- All major frontend pages: Dashboard (live data), Tasks (CRUD table), Projects (card grid), Goals (card grid), Calendar (month/week/day), Activity (audit feed), Growth (health metrics), MATE (data-driven insights), Team (read-only list), Company (CRUD), Settings (shell)
- GlobalExceptionHandler: consistent JSON error responses
- CORS configuration for localhost development

### Security (Already Present)
- BCrypt password hashing
- JWT signed with configurable secret
- OTP BCrypt hashing (stored as hash, raw only sent via email)
- OTP 5-minute expiry
- OTP maximum 5 attempts
- OTP 60-second resend cooldown
- One-time OTP use (consumed flag)
- Organization-level data isolation for: Task, Project, Goal, Calendar (not yet Employee/Department)
- Spring Security stateless session (JWT only)
- @PreAuthorize on project creation/deletion

### Known Issues (Pre-Session)
- Employee and Department controllers have NO organization-level scoping (IDOR vulnerability)
- pom.xml contains non-existent test starters that break Maven build
- MATE.jsx missing FolderKanban import (runtime crash)
- No NotificationController — service is unreachable from frontend
- DashboardServiceImpl.totalEmployees uses global count (wrong across tenants)
- Tasks.jsx sends projectId:"" which fails @NotNull validation when no project selected

---

_This changelog will be updated after each individual task is completed._
