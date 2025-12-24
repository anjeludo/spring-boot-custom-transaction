# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0.1 application demonstrating a custom transaction management system using a domain-driven design approach. The project implements a custom `@UseCaseTransaction` annotation as an alternative to Spring's standard `@Transactional`, providing transaction management decoupled from the infrastructure layer.

## Build and Run Commands

**Build the project:**
```bash
./mvnw clean install
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Run tests:**
```bash
./mvnw test
```

**Run a specific test class:**
```bash
./mvnw test -Dtest=ClassName
```

**Access H2 Console (when running):**
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (empty)

## Architecture

### Custom Transaction Management System

The core architectural pattern is a **custom transaction management** implementation that decouples domain use cases from Spring's infrastructure:

**Key Components:**

1. **`@UseCaseTransaction` annotation** (shared/domain/UseCaseTransaction.java)
   - Custom annotation for marking transactional use cases
   - Defined in the domain layer to avoid infrastructure dependencies
   - Supports propagation, readOnly, timeout, and rollbackFor attributes
   - Mirrors Spring's @Transactional but keeps domain clean

2. **`UseCaseTransactionAttributeSource`** (shared/infra/transactions/UseCaseTransactionAttributeSource.java)
   - Implements `TransactionAttributeSource` to detect `@UseCaseTransaction`
   - Translates custom annotation attributes to Spring's `RuleBasedTransactionAttribute`
   - Bridges domain annotation to Spring's transaction infrastructure

3. **`TransactionConfig`** (shared/infra/transactions/TransactionConfig.java)
   - Configures Spring AOP to intercept methods annotated with `@UseCaseTransaction`
   - Wires `TransactionInterceptor`, `TransactionAttributeSource`, and advisor beans
   - Enables the custom transaction management system

**How it works:**
- Use cases in the application layer are annotated with `@UseCaseTransaction` instead of `@Transactional`
- Spring AOP intercepts these methods through the configured advisor
- The `UseCaseTransactionAttributeSource` provides transaction metadata
- Spring's transaction infrastructure manages the actual transaction lifecycle

### Hexagonal Architecture / Ports & Adapters

The codebase follows hexagonal architecture with clear separation:

**Layers:**
- **domain (model/)**: Core business entities and repository interfaces
  - Example: `User` entity, `UserRepository` interface
  - Contains no infrastructure dependencies

- **application/**: Use cases/application services
  - Example: `UserRegistrar` - orchestrates domain logic
  - Annotated with `@UseCaseTransaction` for transaction boundaries

- **infra/**: Infrastructure implementations
  - **controller/**: REST controllers (delivery mechanism)
  - **persistence/**: Repository implementations
    - Adapter pattern: `H2UserRepository` implements domain `UserRepository`
    - Delegates to `JpaUserRepository` (Spring Data JPA interface)

**Repository Pattern:**
- Domain defines repository interfaces (`UserRepository`)
- Infrastructure provides implementations (`H2UserRepository`)
- Spring Data JPA interfaces (`JpaUserRepository`) are package-private implementation details
- This allows swapping persistence implementations without changing domain or application layers

### Technology Stack

- Java 25
- Spring Boot 4.0.1
- Spring Data JPA
- H2 in-memory database
- Lombok for boilerplate reduction
- Maven for build management

## Important Notes

- `spring.main.allow-bean-definition-overriding=true` is enabled in application.properties
- Database schema is recreated on each startup (`spring.jpa.hibernate.ddl-auto=create-drop`)
- SQL logging is enabled (`spring.jpa.show-sql=true`)
- When adding new use cases, annotate application service methods with `@UseCaseTransaction` instead of `@Transactional`
- Repository interfaces belong in the domain/model layer; implementations go in infra/persistence

## Pending Improvements

**See `/doc/improvements-01.md`** for a detailed list of identified improvements and enhancements for the custom transaction management system. This includes:
- Missing transaction attributes (noRollbackFor, isolation level, transaction manager qualifier)
- Performance optimizations (caching in UseCaseTransactionAttributeSource)
- Bean initialization order fixes (BeanPostProcessor warnings)
- Test coverage recommendations
