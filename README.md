# 🏦 Bank Simulator

A full-stack banking simulator built to explore real-world backend architecture, secure transaction handling, and modern web development.

## 🚀 Overview

Bank Simulator is a project focused on simulating core banking operations such as account management, transactions, and asset handling, while applying production-level design decisions.

The system emphasizes:
- Data consistency
- Security
- Clean architecture
- Test-driven development

## 🧠 Backend

Built with **Java + Spring**, following a **layered architecture** (Clean Architecture inspired):

### 🔹 Architecture
- Application Layer (Use Cases)
- Domain Layer (Business Rules)
- Infrastructure Layer (DB, Security, External Services)

### 🔹 Key Features
- 💳 Account & Client Management
- 💸 Transaction system with **ACID guarantees**
- 🔐 Custom token-based authentication
- 🔑 RSA cryptography per account  
  - Public key stored in DB  
  - Private key stored securely (file/memory)
- 📩 Email verification & password reset flows
- 🌐 Google OAuth login

### 🔹 Database
- PostgreSQL (production-like environment)
- H2 (for automated tests)
- JDBC (no ORM, full control over queries)

### 🔹 Testing
- ✅ Test-Driven Development (TDD)
- Automated tests using H2 in-memory database

## 🎨 Frontend

Built with **React + TypeScript**

### 🔹 Features
- Authentication (JWT-based)
- Account dashboard
- Balance & transactions visualization
- Asset marketplace interface
- Integration with backend APIs

## ☁️ Infrastructure

- AWS (deployment & backend services)
- AWS Secrets Manager (secure credentials)
- HTTPS-enabled APIs
- Integration with external services (e.g. Ko-fi webhooks)

## 🔐 Security Highlights

- Token-based authentication
- Cryptographic signing of transactions
- Secure key management
- Validation layers across the system

## 📦 Project Purpose

This project was built as a **learning and portfolio project**, aiming to simulate how real banking systems are designed, focusing on:

- Backend engineering best practices
- System design
- Security fundamentals
- Real-world trade-offs

## 🛠️ Tech Stack

### Backend
- Java
- Spring Framework
- JDBC

### Frontend
- React
- TypeScript

### Database
- PostgreSQL
- H2

### Infrastructure
- AWS

## 📌 Future Improvements

- Real-time transactions (WebSockets)
- Better asset trading engine
- Rate limiting & fraud detection
- Observability (logs, metrics, tracing)

## 📄 License

This project is licensed under the MIT License.

You are free to use, modify, and distribute this project, as long as proper credit is given.

## 🤝 Usage

Feel free to use this project for learning, personal, or commercial purposes.

If you use this project, please give proper credit by referencing this repository.

## 👨‍💻 Author

Developed by **Alessandro Bezerra**
