# 🏦 Ceylon Bank — Web-Based Banking System

> A full-featured, role-based web banking system built with Spring Boot and Microsoft SQL Server as a Year 2 Semester 1 group project.

![Java](https://img.shields.io/badge/Java-21-007396?style=flat&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?style=flat&logo=springboot&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL%20Server-Express-CC2927?style=flat&logo=microsoftsqlserver&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template%20Engine-005F0F?style=flat&logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build%20Tool-C71A36?style=flat&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed%20%7C%20Academic%20Submission-brightgreen?style=flat)

---

## 📌 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Roles](#system-roles)
- [Tech Stack](#tech-stack)
- [Design Patterns](#design-patterns)
- [Project Structure](#project-structure)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
- [Team](#team)

---

## Overview

Ceylon Bank is a web-based banking management system that simulates the core operations of a modern bank. The system supports six distinct user roles, each with a dedicated dashboard and access-controlled feature set covering customer account management, loan processing, cashier operations, support ticketing, and full administrative oversight.

Developed as a **Year 2, Semester 1 group project**, this system demonstrates practical application of enterprise Java development: layered MVC architecture, Spring Security with role-based access control, JPA/Hibernate persistence, and intentional use of the **Singleton** and **Observer** design patterns.

---

## Features

### 👤 Customer
- Self-registration with a multi-step signup process
- Personalised dashboard with account overview
- View transaction history
- Apply for loans and upload required documents
- Submit feedback and contact support
- Receive real-time notifications for bank news and updates
- Manage profile information

### 💼 Cashier
- Process deposits and withdrawals for customer accounts
- View and manage customer account details
- Access transaction records

### 🏛️ Bank Manager
- View system-wide dashboard with key metrics
- Monitor loan applications and approvals
- Assign roles to bank staff

### 🔍 Loan Officer
- Review and process loan applications
- Approve or reject loans
- Access loan documents submitted by customers

### 🎧 Customer Service Executive
- Manage customer support tickets
- Respond to and resolve customer queries
- Track open and closed support cases

### 🔧 System Administrator
- Full user management (create, activate, deactivate accounts)
- Staff registration and role management
- Manage bank news and announcements
- Manage FAQs
- View and manage all feedback
- Full audit log access

---

## System Roles

| Role | Access Level |
|---|---|
| `Customer` | Self-service banking portal |
| `Cashier` | Teller operations |
| `LoanOfficer` | Loan processing |
| `CustomerServiceExecutive` | Support ticket management |
| `BankManager` | Branch-level oversight |
| `SystemAdministrator` | Full system control |

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 21 | Core programming language |
| Spring Boot 3.5.5 | Application framework |
| Spring MVC | Web layer (controllers, routing) |
| Spring Data JPA / Hibernate | ORM and database access |
| Spring Security | Authentication, authorisation, session management |
| Spring Validation | Input validation |
| Lombok | Boilerplate reduction |

### Frontend
| Technology | Purpose |
|---|---|
| Thymeleaf | Server-side HTML templating |
| Thymeleaf Spring Security 6 Extras | Role-based UI rendering |
| Vanilla CSS | Custom per-page stylesheets |
| Vanilla JavaScript | Client-side interactivity |

### Database
| Technology | Purpose |
|---|---|
| Microsoft SQL Server Express | Primary relational database |
| MSSQL JDBC Driver 9.4.0 | Java-to-SQL Server connectivity |

---

## Design Patterns

Two Gang-of-Four design patterns were intentionally implemented in this project:

### 🔒 Singleton Pattern — `AuditLogger`

**Location:** `src/main/java/.../util/AuditLogger.java`

The `AuditLogger` is implemented as a thread-safe Singleton to provide a single, centralised point of audit logging across the entire application.

**Key implementation details:**
- **Private constructor** prevents external instantiation
- **`volatile` static instance** ensures correct visibility across threads
- **Double-Checked Locking** in `getInstance()` — checks `instance == null` twice (once outside and once inside a `synchronized` block) for performance without sacrificing thread safety
- Spring's `@Autowired` injects the `AuditLogRepository` via a setter method, since the constructor is private

**Why Singleton here?** Every controller across the system calls `AuditLogger.getInstance().logAction(...)` to record user actions (logins, logouts, transactions, etc.) into the database. Using a single shared instance ensures consistent logging behaviour, avoids redundant object creation, and makes the audit trail a reliable system-wide service.

```
AuditLogger.getInstance().logAction(userId, "LOGIN_SUCCESS", "User logged in from IP ...");
```

---

### 📢 Observer Pattern — News Notification System

**Location:** `src/main/java/.../observer/`

The Observer Pattern powers the bank news notification system. When an admin or manager publishes a news article, all relevant users are automatically notified — without the news publisher needing to know anything about who the subscribers are.

**Structure:**

```
NewsObserver          (interface)  ← Observer contract
NewsSubject           (interface)  ← Subject contract
NewsSubjectImpl       (class)      ← Concrete Subject — holds observer list, triggers notifications
CustomerNewsObserver  (class)      ← Notifies all Customers (public news only)
StaffNewsObserver     (class)      ← Notifies all Staff roles (public + private news)
```

**How it works:**
1. A bank news article is published via the admin panel
2. `NewsSubjectImpl.notifyObservers(news)` is called
3. `CustomerNewsObserver` — queries all users with the `Customer` role and creates a `Notification` record for each (only for public news)
4. `StaffNewsObserver` — queries all users across all 5 staff roles and creates notifications for both public and private/internal news
5. Notifications appear in each user's notification bell in real time

Both observers are auto-registered into `NewsSubjectImpl` through Spring's `List<NewsObserver>` dependency injection — no manual wiring needed.

---

## Project Structure

```
src/
└── main/
    ├── java/com/ceylonbank/webbasedbankingsystem/
    │   ├── config/          # Spring Security configuration
    │   ├── controller/      # MVC Controllers (one per role/feature)
    │   ├── dto/             # Data Transfer Objects
    │   ├── entity/          # JPA Entities (DB models)
    │   ├── exception/       # Custom exception classes
    │   ├── observer/        # Observer pattern (news notifications)
    │   ├── repository/      # Spring Data JPA repositories
    │   ├── security/        # Custom auth handlers, UserDetails
    │   ├── service/         # Business logic layer
    │   └── util/            # AuditLogger (Singleton pattern)
    └── resources/
        ├── static/
        │   ├── css/         # Per-page stylesheets
        │   ├── js/          # Per-page JavaScript
        │   └── images/      # Static assets
        └── templates/       # Thymeleaf HTML templates
```

---

## Screenshots

> 📸 Screenshots below showcase the key interfaces across all user roles.

### Landing & Authentication
| | |
|---|---|
| ![Home Page](screenshots/home.png) | ![Login Page](screenshots/login.png) |
| Home Page | Login Page |

### Customer Registration
| | | |
|---|---|---|
| ![Step 1](screenshots/signup-step1.png) | ![Step 2](screenshots/signup-step2.png) | ![Step 3](screenshots/signup-step3.png) |
| Sign Up — Step 1 | Sign Up — Step 2 | Sign Up — Step 3 |

### Customer Portal
| | |
|---|---|
| ![Customer Dashboard](screenshots/customer-dashboard.png) | ![Loan Application](screenshots/loan-application.png) |
| Customer Dashboard | Loan Application |

| | |
|---|---|
| ![Notifications](screenshots/notifications.png) | ![Profile](screenshots/profile.png) |
| Notifications | Profile Page |

### Staff Dashboards
| | |
|---|---|
| ![Cashier Dashboard](screenshots/cashier-dashboard.png) | ![Loan Officer Dashboard](screenshots/loan-officer-dashboard.png) |
| Cashier Dashboard | Loan Officer Dashboard |

| | |
|---|---|
| ![Bank Manager Dashboard](screenshots/bank-manager-dashboard.png) | ![Customer Service Dashboard](screenshots/customer-service-dashboard.png) |
| Bank Manager Dashboard | Customer Service Dashboard |

### Admin Panel
| | |
|---|---|
| ![Admin — Customer List](screenshots/admin-customers.png) | ![Admin — Staff List](screenshots/admin-staff-list.png) |
| Customer Management | Staff Management |

| | |
|---|---|
| ![News Admin](screenshots/news-admin.png) | ![FAQ Admin](screenshots/faq-admin.png) |
| News Management | FAQ Management |

| | |
|---|---|
| ![Feedback Admin](screenshots/feedback-admin.png) | |
| Feedback Management | |

### Public Pages
| | |
|---|---|
| ![News Page](screenshots/news.png) | ![Contact Page](screenshots/contact.png) |
| Bank News | Contact / Support |

---

## Getting Started

### Prerequisites

Make sure the following are installed on your machine:

- **Java 21** ([Download](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html))
- **Apache Maven** ([Download](https://maven.apache.org/download.cgi))
- **Microsoft SQL Server Express** ([Download](https://www.microsoft.com/en-us/sql-server/sql-server-downloads))
- **SQL Server Management Studio (SSMS)** (recommended)

---

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/ceylon-bank-web-system.git
cd ceylon-bank-web-system
```

### 2. Set Up the Database

1. Open **SSMS** and connect to your SQL Server instance
2. Create a new database:
```sql
CREATE DATABASE BankingSystemDB;
```
3. Create a SQL login for the application:
```sql
CREATE LOGIN bank_user WITH PASSWORD = 'Admin123@';
USE BankingSystemDB;
CREATE USER bank_user FOR LOGIN bank_user;
ALTER ROLE db_owner ADD MEMBER bank_user;
```
4. Run the provided SQL schema scripts from the `/database` folder (if included) to set up tables

### 3. Configure the Application

Open `src/main/resources/application.properties` and update the connection string to match your SQL Server instance:

```properties
spring.datasource.url=jdbc:sqlserver://YOUR_SERVER_NAME\\SQLEXPRESS;databaseName=BankingSystemDB;encrypt=false;trustServerCertificate=true
spring.datasource.username=bank_user
spring.datasource.password=Admin123@
```

Replace `YOUR_SERVER_NAME` with your machine name or SQL Server hostname.

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start at:

```
http://localhost:8080
```

---

### Default Credentials

> ⚠️ Change these before any deployment.

| Role | How to Access |
|---|---|
| Customer | Register via the Sign Up page |
| All Staff Roles | Created by the System Administrator |
| System Administrator | Created directly in the database with role `SystemAdministrator` |

---

## Team

This system was designed and built as a **collaborative group project** for the Year 2, Semester 1 software engineering module. Responsibilities were divided across six feature branches — Account Management, Transaction Management, Loan Management, User Management, Bank Information System, and Customer Support Interface — each developed independently and merged into the main branch.

---

## License

This project was developed for academic purposes. All rights reserved by the project team.
