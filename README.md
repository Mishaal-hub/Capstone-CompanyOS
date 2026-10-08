# CompanyOS

> **An Adaptive Business Operating System powered by MATE Intelligence.**

CompanyOS is a modern Business Operating System designed to bring essential company operations into one unified platform.

The goal is to reduce the need for disconnected tools by providing a centralized system for managing people, departments, projects, tasks, company information, activity, and intelligent assistance.

---

## 🚀 Project Vision

CompanyOS aims to become an intelligent operating layer for organizations.

The long-term platform will bring together:

* 👥 Employee & Team Management
* 🏢 Company & Department Management
* 📁 Projects
* ✅ Tasks
* 📅 Calendar & Events
* 🎯 Goals
* 📈 Growth & Business Insights
* 🔔 Notifications
* ⚙️ Settings
* 🤖 MATE — AI-powered company intelligence
* 📊 Dashboards & Reports

> **Vision:** Build. Learn. Improve. Grow.

---

## 🎯 Problem Statement

Many organizations depend on multiple disconnected applications for:

* Employee information
* Project management
* Tasks
* Calendars
* Communication
* Business information
* Reports
* Company knowledge

This can result in:

* Scattered information
* Missed deadlines
* Poor collaboration
* Duplicate data
* Difficult decision-making
* Lack of centralized company knowledge

CompanyOS aims to provide a unified platform where these operations can eventually exist in one place.

---

## 🧩 Current MVP

The current CompanyOS MVP contains the foundation for a full business operating system.

### Backend

* Spring Boot REST API
* MySQL database
* Spring Data JPA / Hibernate
* Company management
* Department management
* Employee management
* Project-related backend foundation
* Task-related backend foundation
* Goal-related backend foundation
* Notification foundation
* Authentication services
* BCrypt password hashing
* Email services
* JWT infrastructure
* Google authentication foundation
* Service and repository architecture
* Exception handling foundation

### Frontend

* React
* Vite
* JavaScript / JSX
* CompanyOS dashboard interface
* Sidebar navigation
* Authentication UI
* Login
* Registration
* Password reset flow
* OTP verification interfaces
* Company management
* Department management
* Employee/team-related interface foundation
* Projects
* Tasks
* Goals
* Growth
* Activity
* Settings
* MATE interface foundation
* CompanyOS branding and logo

---

## 🔐 Authentication

CompanyOS uses a modern authentication architecture.

Current authentication work includes:

* Email/password registration
* BCrypt password hashing
* Email verification
* OTP-based verification
* Password reset
* Password reset OTP
* OTP expiry handling
* OTP resend support
* Secure random OTP/session handling
* Maximum OTP attempt protection
* One-time OTP usage
* Google OAuth foundation
* JWT authentication infrastructure

Sensitive configuration such as database and mail credentials is kept outside the repository through environment configuration.

> Never commit real passwords, API keys, SMTP credentials, OAuth secrets, or other sensitive values to GitHub.

---

## 🏗️ Technology Stack

| Layer               | Technology                                |
| ------------------- | ----------------------------------------- |
| Frontend            | React                                     |
| Frontend Build Tool | Vite                                      |
| Backend             | Spring Boot                               |
| Language            | Java                                      |
| Database            | MySQL                                     |
| ORM                 | Spring Data JPA / Hibernate               |
| Security            | Spring Security                           |
| Authentication      | JWT + Email/OTP + Google OAuth foundation |
| Password Hashing    | BCrypt                                    |
| Email               | Spring Mail / SMTP                        |
| API Communication   | REST / Fetch                              |
| Version Control     | Git                                       |
| Repository          | GitHub                                    |

---

## 🏛️ Architecture

```text
                         COMPANYOS
                             │
              ┌──────────────┴──────────────┐
              │                             │
       React Frontend                 Spring Boot Backend
              │                             │
              │                        REST Controllers
              │                             │
              │                          Services
              │                             │
              │                         Repositories
              │                             │
              └─────────────────────────────┤
                                            │
                                       MySQL Database
```

Authentication:

```text
User
 │
 ▼
React Authentication UI
 │
 ▼
Spring Boot Auth API
 │
 ├── Email / OTP
 ├── BCrypt
 ├── JWT
 └── Google OAuth
 │
 ▼
MySQL
```

---

## 📂 Repository Structure

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

## 📅 Upcoming Calendar Module

The next major frontend feature is the **CompanyOS Calendar**.

The planned Calendar module will provide:

* Monthly calendar view
* Week view
* Day view
* Previous/next navigation
* Today button
* Event creation
* Event editing
* Event deletion
* Event time
* Event descriptions
* Event categories
* Calendar event persistence
* Backend API integration
* MySQL event storage

Planned architecture:

```text
React Calendar
      │
      ▼
Calendar REST API
      │
      ▼
Calendar Service
      │
      ▼
Calendar Repository
      │
      ▼
MySQL
```

---

## 🤖 MATE Intelligence

MATE stands for:

> **Management & Adaptive Task Engine**

MATE is the planned intelligence layer of CompanyOS.

Its long-term capabilities include:

* Company knowledge assistance
* Business questions
* Employee workload analysis
* Task insights
* Business health analysis
* Daily business briefings
* Decision support
* Intelligent recommendations

MATE will gradually become more deeply integrated with CompanyOS data.

---

## 🛠️ Development Workflow

CompanyOS is developed using Git and GitHub.

Typical workflow:

```bash
git status

git add .

git commit -m "Describe your change"

git push origin main
```

Before major feature development:

1. Make sure the project is working.
2. Commit the current state.
3. Push the commit to GitHub.
4. Develop the new feature.
5. Test the feature.
6. Commit the completed feature.

---

## 🔒 Security

The repository intentionally excludes sensitive environment files.

Do **not** commit:

```text
.env
database passwords
SMTP passwords
OAuth client secrets
JWT secrets
API keys
private credentials
```

Use:

```text
.env.example
```

for safe configuration documentation.

---

## 📌 Project Status

**Current phase:** CompanyOS MVP development

**Current focus:**

> Building the core business platform and expanding the frontend modules.

### Recently completed / improved

* CompanyOS repository consolidated with Git
* GitHub repository connected
* Authentication improvements
* Password reset flow
* OTP-based flows
* Email configuration
* CompanyOS frontend structure
* Sidebar navigation
* CompanyOS branding/logo
* Core business management modules

### Next

1. 📅 Build the actual Calendar
2. 🔗 Connect Calendar to backend
3. 💾 Persist events in MySQL
4. 👥 Continue employee/team management
5. 📊 Improve dashboard
6. 🤖 Expand MATE
7. 🔐 Continue authentication/security hardening

---

## 📖 Documentation

Additional project documentation is available in:

```text
docs/
journal/
CURRENT_STATE.md
```

The development journal records progress throughout the project.

---

## 👨‍💻 Project

**CompanyOS**

An evolving Business Operating System designed to bring company operations, intelligence, and collaboration into one platform.

> **Build. Learn. Improve. Grow.**
