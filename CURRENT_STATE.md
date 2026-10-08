# CompanyOS — Current State

> **Last updated:** 2026-10-08
> **Purpose:** Current snapshot of the CompanyOS repository, completed work, working systems, and next development priorities.

---

# 1. Project Overview

**CompanyOS** is an Adaptive Business Operating System designed to centralize company operations.

The long-term platform will manage:

* Companies
* Departments
* Employees
* Teams
* Projects
* Tasks
* Calendar & Events
* Goals
* Activity
* Notifications
* Business information
* Dashboards
* Reports
* MATE AI intelligence

### Current development focus

The project is currently transitioning from the authentication/core-management foundation toward a more complete business operating system frontend.

**Immediate next feature:**

> Build the actual interactive CompanyOS Calendar and connect it to the backend.

---

# 2. Repository Status

The local project is connected to GitHub.

```text
Remote:
https://github.com/Mishaal-hub/Capstone-CompanyOS.git

Branch:
main

Working tree:
Clean

Git tracking:
origin/main
```

The current project has been backed up to GitHub.

---

# 3. Repository Structure

```text
CompanyOS/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── start.bat
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── diagrams/
├── docs/
├── journal/
│
├── .env.example
├── .gitignore
├── CURRENT_STATE.md
├── README.md
└── Company OS MVP Architecture.png
```

---

# 4. Technology Stack

| Layer            | Technology                                |
| ---------------- | ----------------------------------------- |
| Frontend         | React                                     |
| Build Tool       | Vite                                      |
| Backend          | Spring Boot                               |
| Language         | Java                                      |
| Database         | MySQL                                     |
| ORM              | Spring Data JPA / Hibernate               |
| Security         | Spring Security                           |
| Password Hashing | BCrypt                                    |
| Authentication   | JWT + Email/OTP + Google OAuth foundation |
| Email            | Spring Mail / SMTP                        |
| API              | REST                                      |
| Version Control  | Git + GitHub                              |

---

# 5. Backend Status

The backend contains the main architecture for CompanyOS.

### Backend layers

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Entity
    ↓
MySQL
```

### Major backend areas

```text
backend/src/main/java/com/companyos/backend/

├── common/
├── config/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
├── security/
├── service/
│   └── impl/
└── util/
```

### Backend modules currently present

* Authentication
* Company
* Department
* Employee
* Dashboard
* Projects
* Tasks
* Goals
* Notifications
* AI/MATE foundation
* Audit services
* Security/JWT infrastructure

---

# 6. Authentication Status

Authentication has progressed significantly beyond the original MVP state.

### Current authentication components

* Email/password registration
* BCrypt password hashing
* Email verification
* OTP verification
* Password reset
* Password reset OTP
* OTP expiry
* OTP resend handling
* OTP attempt limits
* One-time OTP usage
* Secure session/OTP handling
* Google OAuth foundation
* JWT infrastructure
* Email/SMTP integration

### Password reset

The password reset flow has been implemented and tested.

Current conceptual flow:

```text
Forgot Password
      ↓
Enter Email
      ↓
Generate OTP
      ↓
Send OTP by Email
      ↓
Verify OTP
      ↓
Enter New Password
      ↓
Password Updated
```

### Security principles

Sensitive values are kept outside the committed repository.

Examples:

```text
.env
SMTP credentials
Database password
JWT secrets
OAuth secrets
```

These should never be committed to GitHub.

---

# 7. Frontend Status

The frontend is a React/Vite application.

### Current structure

```text
frontend/src/

├── assets/
├── components/
│   └── Sidebar.jsx
│
├── pages/
│   ├── Activity.jsx
│   ├── Company.jsx
│   ├── Dashboard.jsx
│   ├── Goals.jsx
│   ├── Growth.jsx
│   ├── MATE.jsx
│   ├── Projects.jsx
│   ├── Settings.jsx
│   ├── Tasks.jsx
│   └── Team.jsx
│
├── App.jsx
├── App.css
├── Login.jsx
├── Login.css
├── api.js
├── index.css
└── main.jsx
```

### Frontend modules

Current navigation includes:

* Dashboard
* Company
* Team
* Projects
* Tasks
* Calendar
* Goals
* Growth
* Activity
* MATE
* Settings

---

# 8. CompanyOS Branding

CompanyOS now has its actual project logo available in:

```text
frontend/src/assets/companyos-logo.png
```

The logo is intended to be used consistently across the CompanyOS interface.

---

# 9. Calendar Status

## Current state

The Calendar section exists as part of the CompanyOS frontend navigation, but the **actual interactive calendar implementation is the next major feature to build**.

### Planned functionality

```text
Calendar
│
├── Month View
├── Week View
├── Day View
├── Today
├── Previous / Next
│
├── Create Event
├── Edit Event
├── Delete Event
│
├── Event Title
├── Date
├── Time
├── Description
└── Event Category
```

### Planned backend architecture

```text
Calendar.jsx
     ↓
Calendar API
     ↓
CalendarController
     ↓
CalendarService
     ↓
CalendarRepository
     ↓
Calendar Entity
     ↓
MySQL
```

### Important

The Calendar should first be made functional on the frontend and then connected to the backend.

Do not introduce unnecessary dependencies unless they provide clear value.

---

# 10. Database Status

CompanyOS uses MySQL.

Configured database:

```text
companyos
```

The backend uses Spring Data JPA / Hibernate for persistence.

Current entity areas include:

* User
* Company
* Department
* Employee
* Project
* ProjectMember
* Task
* TaskComment
* TaskDependency
* Goal
* Milestone
* Notification
* Client
* Deal
* AIConversation
* AIMessage
* AIProposedAction
* AuditLog
* Organization
* Role

### Future Calendar database model

A Calendar event will eventually need fields similar to:

```text
id
title
description
startDateTime
endDateTime
eventType
status
createdBy
createdAt
updatedAt
```

The exact entity design should be decided before implementing the backend.

---

# 11. Git / GitHub Status

The project was initialized as a Git repository and connected to:

```text
https://github.com/Mishaal-hub/Capstone-CompanyOS.git
```

Current branch:

```text
main
```

The local project was merged safely with the existing GitHub history.

The repository was successfully pushed to GitHub.

### Important workflow

Before starting a major feature:

```bash
git status
```

Then make sure the working tree is clean.

After completing a feature:

```bash
git add .
git commit -m "Describe the feature"
git push origin main
```

---

# 12. Important Security Rule

Never commit the real `.env` file.

The repository should contain:

```text
.env.example
```

but not:

```text
.env
```

Before pushing:

```bash
git status
```

must be checked.

---

# 13. Current Development Priorities

The recommended order from the current state is:

### Priority 1 — Calendar

Build the actual interactive Calendar.

### Priority 2 — Calendar backend

Create:

```text
CalendarEvent
CalendarEventRepository
CalendarEventService
CalendarEventServiceImpl
CalendarEventController
```

and connect it to MySQL.

### Priority 3 — Employee/Team UI

Connect the existing employee backend functionality to the frontend.

### Priority 4 — Dashboard

Use real backend data for:

* Employees
* Projects
* Tasks
* Goals
* Activity
* Upcoming events

### Priority 5 — Projects & Tasks

Connect the existing backend foundation to polished frontend workflows.

### Priority 6 — MATE

Expand MATE from an interface foundation into an intelligent company assistant.

---

# 14. Current Immediate Next Step

## Build the CompanyOS Calendar

The next development session should begin by inspecting the existing Calendar section and then implementing:

```text
1. Calendar layout
2. Month navigation
3. Date selection
4. Today button
5. Event creation
6. Event display
7. Event editing
8. Event deletion
9. Frontend state management
10. Backend API
11. MySQL persistence
```

Do not modify working authentication unnecessarily while implementing Calendar.

---

# 15. Development Rule

CompanyOS should be developed incrementally.

For every major feature:

```text
Plan
 ↓
Implement
 ↓
Run application
 ↓
Test
 ↓
Fix errors
 ↓
Git commit
 ↓
Git push
 ↓
Update CURRENT_STATE.md
```

`CURRENT_STATE.md` should be updated whenever a major development phase is completed.

---

# 16. Project Principle

CompanyOS is being built as a real product rather than only a college demonstration.

The architecture should therefore prioritize:

* Security
* Maintainability
* Clean separation of concerns
* Reusable components
* Proper database persistence
* Good UX
* Scalable backend architecture
* Clear documentation
* Incremental development

> **CompanyOS — Build. Learn. Improve. Grow.**
