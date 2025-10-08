# 🏦 BTBS Banking Platform

The following is the proposed architecture and project structure of BTBS banking platform app.

A modular **banking platform** built with **Domain-Driven Design (DDD)** and the **Hexagonal (Ports & Adapters)** architecture pattern.

The goal is to maintain a clean, testable, and extensible codebase where the **domain model** represents real business concepts (Customers, Accounts, Transactions) and the **infrastructure** simply adapts those concepts to technology (JPA, REST, Messaging, etc.).

---

## 🧭 Architectural Overview

### 🧩 Hexagonal (Ports & Adapters)

The system is organized around **core business logic** (the Domain) surrounded by adapter layers:
┌────────────────────────────┐
│         API Layer          │  ← Controllers, DTOs
├──────────────┬─────────────┤
│     Application Layer      │  ← Use Cases / Services
├──────────────┬─────────────┤
│      Domain Layer          │  ← Entities, Value Objects, Policies
├──────────────┴─────────────┤
│     Infrastructure Layer   │  ← JPA, Messaging, Security
└────────────────────────────┘
- **Domain** — pure business logic, independent of frameworks.
- **Application** — orchestrates domain objects (use cases).
- **Infrastructure** — adapts domain to external tech (DB, Kafka, etc.).
- **API** — exposes functionality via REST or other protocols.

## 🏗️ Package Structure
com.btbs 
├─ domain # Core business model (no frameworks)
│ ├─ shared
│ │ ├─ value # Common value objects
│ │ │ ├─ PhoneNumber.java
│ │ │ └─ Money.java
│ │ ├─ id # Typed identifiers
│ │ │ ├─ AccountId.java
│ │ │ └─ CustomerId.java
│ │ └─ event # Domain events
│ │ ├─ FundsDebited.java
│ │ └─ FundsCredited.java
│ ├─ customers
│ │ ├─ Customer.java
│ │ ├─ KycStatus.java
│ │ └─ CustomerRepository.java # Port (interface)
│ └─ accounts
│ ├─ CustomerAccount.java
│ ├─ AccountNumber.java
│ ├─ AccountRepository.java # Port (interface)
│ └─ policies
│ └─ OverdraftPolicy.java
│
├─ application # Use cases and orchestration
│ ├─ customers
│ │ ├─ CreateCustomerUseCase.java
│ │ └─ VerifyKycUseCase.java
│ └─ accounts
│ ├─ OpenAccountUseCase.java
│ ├─ DepositFundsUseCase.java
│ ├─ WithdrawFundsUseCase.java
│ └─ TransferFundsService.java
│
├─ infrastructure # Technical adapters
│ ├─ persistence
│ │ ├─ jpa
│ │ │ ├─ entities
│ │ │ │ ├─ CustomerEntity.java
│ │ │ │ └─ AccountEntity.java
│ │ │ ├─ converters
│ │ │ │ └─ PhoneNumberConverter.java
│ │ │ ├─ repositories
│ │ │ │ ├─ SpringDataCustomerJpa.java
│ │ │ │ └─ SpringDataAccountJpa.java
│ │ │ ├─ mappers
│ │ │ │ ├─ CustomerMapper.java
│ │ │ │ └─ AccountMapper.java
│ │ │ └─ JpaAccountRepository.java # Adapter implementing domain port
│ │ └─ migrations
│ │ └─ (Flyway/Liquibase scripts)
│ ├─ messaging
│ │ └─ (Kafka/SQS adapters)
│ └─ security
│ ├─ PasswordHasher.java
│ └─ TokenProvider.java
│
├  ─ api # Delivery layer (REST controllers)
│ ├─ web
│ │ ├─ controllers
│ │ │ ├─ CustomerController.java
│ │ │ └─ AccountController.java
│ │ ├─ dto
│ │ │ ├─ CreateCustomerRequest.java
│ │ │ ├─ CustomerResponse.java
│ │ │ ├─ OpenAccountRequest.java
│ │ │ └─ AccountResponse.java
│ │ ├─ mappers
│ │ │ ├─ CustomerDtoMapper.java
│ │ │ └─ AccountDtoMapper.java
│ │ └─ filters
│ │ └─ (Auth, Logging, Tracing)
│ └─ config
│ ├─ WebConfig.java
│ ├─ SecurityConfig.java
│ └─ ObjectMapperConfig.java
│
├─ support # Cross-cutting utilities
│ ├─ exceptions
│ ├─ util
│ └─ logging
│
└─ bootstrap
└─ Application.java # Spring Boot entry point

## 🔌 Dependency Rules

- **Domain** → depends on nothing.
- **Application** → depends only on Domain.
- **Infrastructure** → implements Domain ports; depends on Domain.
- **API** → depends on Application (and may use Domain value types).
- **Support** → reusable utilities; no circular dependencies.

Keep all arrows **pointing inward** toward the domain core.

---

## 🚀 Development Setup

### Requirements
- Java 17+
- Maven or Gradle
- PostgreSQL (or H2 for local dev)
- Docker (optional for DB/test containers)
