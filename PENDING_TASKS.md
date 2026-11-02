# CompanyOS — Pending Tasks Checklist

> Status: [ ] Not started | [~] In progress | [x] Completed | [!] Blocked
> Last updated: 2026-11-02 (Session 1 end)

---

## CRITICAL BUGS

| ID | Status | Priority | Description | Related Files | Status Notes |
|----|--------|----------|-------------|---------------|--------------|
| B001 | [x] | CRITICAL | Employee CRUD had no org scoping — IDOR | Employee.java, EmployeeController, EmployeeServiceImpl, EmployeeRepository | FIXED Session 1 |
| B002 | [x] | CRITICAL | Employee entity missing organizationId column | Employee.java | FIXED Session 1 — column added |
| B003 | [x] | HIGH | Dashboard totalEmployees used global count() | DashboardServiceImpl.java | FIXED Session 1 — uses countByOrganizationId |
| B004 | [x] | HIGH | DepartmentController no org scoping + hardcoded @CrossOrigin | DepartmentController, DepartmentServiceImpl, DepartmentRepository | FIXED Session 1 |
| B005 | [x] | MEDIUM | Tasks/Goals/Projects.jsx sent hardcoded organizationId:1 | Tasks.jsx, Goals.jsx, Projects.jsx | FIXED Session 1 |
| B006 | [x] | HIGH | Tasks.jsx sent empty projectId — backend @NotNull rejection | Tasks.jsx | FIXED Session 1 — validation + required message |
| B007 | [x] | HIGH | pom.xml non-existent test starters broke Maven build | backend/pom.xml | FIXED Session 1 — spring-boot-starter-test |
| B008 | [x] | HIGH | MATE.jsx missing FolderKanban import — runtime crash | MATE.jsx | FIXED Session 1 |
| B009 | [ ] | MEDIUM | Settings page Save Changes does nothing | Settings.jsx | Next: add /api/users/profile endpoint |
| B010 | [x] | MEDIUM | DashboardController excluded MEMBER role | DashboardController.java | FIXED Session 1 — restriction removed |
| B011 | [x] | HIGH | No NotificationController | NotificationService.java | FIXED Session 1 — controller created |

---

## SECURITY FIXES

| ID | Status | Priority | Description | Related Files | Status Notes |
|----|--------|----------|-------------|---------------|--------------|
| S001 | [x] | CRITICAL | Employee cross-org IDOR | EmployeeController.java | FIXED Session 1 |
| S002 | [x] | CRITICAL | Department cross-org IDOR | DepartmentController.java | FIXED Session 1 |
| S003 | [x] | HIGH | Dashboard global employee count | DashboardServiceImpl.java | FIXED Session 1 |
| S004 | [x] | MEDIUM | @CrossOrigin on DepartmentController | DepartmentController.java | FIXED Session 1 |
| S005 | [ ] | MEDIUM | Default JWT secret fallback is predictable in production | application.properties, JwtUtil.java | Next: add startup WARNING if JWT_SECRET not set |
| S006 | [ ] | LOW | Email verification OTP has no attempt counter | AuthServiceImpl.java | Next: add attempt tracking to verifyEmail |

---

## BACKEND INCOMPLETE FEATURES

| ID | Status | Priority | Description | Related Files | Next Action |
|----|--------|----------|-------------|---------------|-------------|
| BE001 | [x] | HIGH | No NotificationController | — | DONE Session 1 |
| BE002 | [x] | MEDIUM | EmployeeController returned raw Entity | EmployeeController.java | DONE Session 1 — returns DTO |
| BE003 | [x] | MEDIUM | DepartmentController returned raw Entity | DepartmentController.java | DONE Session 1 |
| BE004 | [ ] | MEDIUM | Settings/profile update API | — | Create PUT /api/users/profile |
| BE005 | [ ] | LOW | No org settings endpoint | OrganizationRepository.java | Add PATCH /api/organizations/{id} |

---

## FRONTEND INCOMPLETE FEATURES

| ID | Status | Priority | Description | Related Files | Next Action |
|----|--------|----------|-------------|---------------|-------------|
| FE001 | [x] | HIGH | No Notifications page | App.jsx, Sidebar.jsx | DONE Session 1 |
| FE002 | [x] | HIGH | Team page read-only | Team.jsx | DONE Session 1 — full CRUD |
| FE003 | [ ] | MEDIUM | Settings page Save Changes is no-op | Settings.jsx | Connect to profile endpoint |
| FE004 | [x] | MEDIUM | Goals page had no edit/delete | Goals.jsx | DONE Session 1 |
| FE005 | [ ] | LOW | Company page not in sidebar | Sidebar.jsx | Add Company link to sidebar |
| FE006 | [x] | LOW | Dashboard navigation buttons inert | Dashboard.jsx | DONE Session 1 |

---

## TESTING

| ID | Status | Priority | Description | Files | Status |
|----|--------|----------|-------------|-------|--------|
| T001 | [x] | CRITICAL | Fix pom.xml so Maven tests run | backend/pom.xml | DONE Session 1 — 12 tests pass |
| T002 | [x] | HIGH | Run backend tests | backend/ | PASS: 12/12 |
| T003 | [x] | HIGH | Run frontend build | frontend/ | PASS: 0 errors |
| T004 | [ ] | MEDIUM | Integration tests for Employee org isolation | backend/src/test/ | After Session 1 changes are committed |
| T005 | [ ] | MEDIUM | Integration tests for Department org isolation | backend/src/test/ | After Session 1 changes are committed |
| T006 | [ ] | MEDIUM | Integration tests for Notification CRUD | backend/src/test/ | Future session |

---

## DOCUMENTATION

| ID | Status | Description |
|----|--------|-------------|
| D001 | [x] | DEVELOPMENT_LOG.md created and populated |
| D002 | [x] | CURRENT_STATE.md updated with accurate post-fix state |
| D003 | [x] | PENDING_TASKS.md created and maintained |
| D004 | [x] | CHANGELOG.md created |
| D005 | [~] | NEXT_SESSION_PROMPT.md — in progress (session end) |

---

## ADDITIONAL FEATURES (After Core Is Stable)

| ID | Status | Priority | Description |
|----|--------|----------|-------------|
| AF001 | [ ] | P1 | Advanced search/filtering in Tasks and Projects |
| AF002 | [ ] | P1 | PDF/Excel report exports |
| AF003 | [ ] | P1 | Employee permissions system |
| AF004 | [ ] | P2 | Automated task reminders (scheduler) |
| AF005 | [ ] | P2 | Overdue task alerts via email |
| AF006 | [ ] | P3 | MATE natural language query interface |
| AF007 | [ ] | P3 | MATE action suggestions with user approval |
| AF008 | [ ] | P4 | Multiple organizations support |
| AF009 | [ ] | P4 | Subscription/billing architecture |
| AF010 | [ ] | P4 | Data export (CSV/JSON) |
| AF011 | [ ] | P1 | Company page accessible from sidebar |
| AF012 | [ ] | P1 | Settings page connected to profile API |

---

_Last Updated: 2026-11-02 Session 1_
