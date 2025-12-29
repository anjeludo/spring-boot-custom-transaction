# 🎯 Spring Boot Custom Transaction - Clean Architecture PoC

> **A proof of concept demonstrating how to decouple transaction management from the Spring Framework using Clean Architecture principles**

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/tests-11%20passed-success.svg)](src/test/java/com/decoupling/springbootcustomtransaction/transaction)

## 📋 Table of Contents

- [Overview](#-overview)
- [The Problem](#-the-problem)
- [The Solution](#-the-solution)
- [Architecture](#-architecture)
- [Features](#-features)
- [Getting Started](#-getting-started)
- [Usage Example](#-usage-example)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Technical Details](#-technical-details)
- [Benefits](#-benefits)
- [Contributing](#-contributing)

## 🌟 Overview

This project demonstrates a **framework-agnostic approach** to transaction management in Spring Boot applications. Instead of coupling your domain layer to Spring's `@Transactional` annotation, we introduce a custom `@UseCaseTransaction` annotation that lives in the domain layer, keeping your business logic clean and independent of infrastructure concerns.

## ❌ The Problem

Traditional Spring applications tightly couple business logic to the framework:

```java
@Service
public class UserRegistrar {

    @Transactional  // ❌ Domain layer depends on Spring!
    public void execute(String username, String email) {
        userRepository.save(User.create(username, email));
    }
}
```

**Issues with this approach:**
- 🔗 Domain layer coupled to Spring Framework
- 🧪 Difficult to test in isolation
- 🔄 Hard to migrate to different frameworks
- 🏗️ Violates Clean Architecture principles

## ✅ The Solution

Our custom transaction system decouples the domain from Spring:

```java
@Service
public class UserRegistrar {

    @UseCaseTransaction  // ✅ Domain annotation, no Spring dependency!
    public void execute(String username, String email) {
        userRepository.save(User.create(username, email));
    }
}
```

**Architecture:**
```
┌─────────────────────────────────────────────┐
│           Domain Layer (Pure)               │
│                                             │
│  @UseCaseTransaction                        │
│  - Custom annotation                        │
│  - No framework dependencies                │
│  - Defines business transaction semantics   │
└─────────────────────────────────────────────┘
                    ▲
                    │
                    │ (used by)
                    │
┌─────────────────────────────────────────────┐
│        Application Layer (Use Cases)        │
│                                             │
│  UserRegistrar                              │
│  - Annotated with @UseCaseTransaction       │
│  - Framework agnostic                       │
└─────────────────────────────────────────────┘
                    ▲
                    │
                    │ (intercepted by)
                    │
┌─────────────────────────────────────────────┐
│      Infrastructure Layer (Spring)          │
│                                             │
│  UseCaseTransactionAttributeSource          │
│  TransactionConfig                          │
│  - Translates to Spring transactions        │
│  - AOP interception                         │
└─────────────────────────────────────────────┘
```

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

- **Domain Layer** (`shared/domain`): Contains the `@UseCaseTransaction` annotation - framework independent
- **Application Layer** (`application`): Use cases annotated with `@UseCaseTransaction`
- **Infrastructure Layer** (`shared/infra/transactions`): Bridges domain annotations to Spring's transaction management

### Key Components

1. **`@UseCaseTransaction`** - Custom annotation in the domain layer
2. **`UseCaseTransactionAttributeSource`** - Detects and translates custom annotations
3. **`TransactionConfig`** - Configures Spring AOP to intercept annotated methods
4. **Performance optimizations** - Thread-safe caching to avoid repeated reflection

## ✨ Features

- ✅ **Framework Independence**: Domain layer has zero Spring dependencies
- ✅ **Full Transaction Support**: Propagation, timeout, rollback rules, read-only
- ✅ **Performance Optimized**: Built-in caching with `ConcurrentHashMap`
- ✅ **Well Tested**: 11 integration tests covering all transaction scenarios
- ✅ **Clean Architecture**: Clear separation of concerns
- ✅ **Production Ready**: Includes `@Role(INFRASTRUCTURE)` for proper bean ordering

## 🚀 Getting Started

### Prerequisites

- Java 25+
- Maven 3.8+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/anjeludo/spring-boot-custom-transaction.git
cd spring-boot-custom-transaction
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

4. Test the API using the provided HTTP file:
   - Open `http/user-api.http` in IntelliJ IDEA or VS Code with REST Client extension
   - Or use curl:
     ```bash
     curl -X POST http://localhost:8080/api/users \
       -H "Content-Type: application/json" \
       -d '{"username":"John Doe","email":"john@example.com"}'
     ```

5. Run the tests:
```bash
./mvnw test
```

## 💡 Usage Example

### 1. Define your domain annotation (already provided)

```java
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCaseTransaction {
    TransactionPropagation propagation() default TransactionPropagation.REQUIRED;
    boolean readOnly() default false;
    int timeout() default -1;
    Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};
}
```

### 2. Use it in your application services

```java
@Service
@RequiredArgsConstructor
public class UserRegistrar {

    private final UserRepository userRepository;

    @UseCaseTransaction  // 🎯 Clean, domain-driven transaction management
    public void execute(String username, String email) {
        userRepository.save(User.create(username, email));
    }
}
```

### 3. Advanced usage with propagation

```java
@UseCaseTransaction(
    propagation = TransactionPropagation.REQUIRES_NEW,
    timeout = 30,
    readOnly = false
)
public void complexOperation() {
    // Independent transaction with 30-second timeout
}
```

## 🧪 Testing

The project includes comprehensive integration tests:

### Test Suites

1. **SpringBootCustomTransactionApplicationTests** (1 test)
   - Context loading test

2. **TransactionRollbackIntegrationTest** (3 tests)
   - Validates rollback on RuntimeException
   - Validates commit on success
   - Validates checked exceptions behavior

3. **TransactionPropagationIntegrationTest** (4 tests)
   - REQUIRED propagation
   - REQUIRES_NEW propagation
   - MANDATORY propagation

4. **TransactionTimeoutIntegrationTest** (3 tests)
   - Timeout configuration
   - Within timeout behavior
   - No timeout behavior

**Run all tests:**
```bash
./mvnw test
```

**Run specific test suite:**
```bash
./mvnw test -Dtest=TransactionRollbackIntegrationTest
```

**Test Results:**
```
✅ 11 tests executed
✅ 11 tests passed
✅ 0 failures
```

## 📁 Project Structure

```
src/
├── main/
│   └── java/
│       └── com/decoupling/springbootcustomtransaction/
│           ├── shared/
│           │   ├── domain/
│           │   │   └── UseCaseTransaction.java          # 🎯 Domain annotation
│           │   └── infra/
│           │       └── transactions/
│           │           ├── TransactionConfig.java       # Spring AOP config
│           │           └── UseCaseTransactionAttributeSource.java
│           └── user/
│               ├── model/                               # Domain entities
│               ├── application/                         # Use cases
│               └── infra/                               # Infrastructure
└── test/
    └── java/
        └── com/decoupling/springbootcustomtransaction/
            └── transaction/                             # Integration tests

http/
└── user-api.http                                        # 📡 REST API examples
```

## 🔧 Technical Details

### How It Works

1. **Annotation Detection**: `UseCaseTransactionAttributeSource` scans for `@UseCaseTransaction`
2. **Translation**: Converts custom annotation attributes to Spring's `TransactionAttribute`
3. **AOP Interception**: Spring's `TransactionInterceptor` handles the actual transaction
4. **Caching**: Results are cached using `ConcurrentHashMap` for performance

### Supported Features

| Feature | Supported | Notes |
|---------|-----------|-------|
| Propagation levels | ✅ | REQUIRED, REQUIRES_NEW, MANDATORY, SUPPORTS, NOT_SUPPORTED, NEVER, NESTED |
| Timeout | ✅ | Configurable in seconds |
| Read-only | ✅ | Optimization for read operations |
| Rollback rules | ✅ | Specify which exceptions trigger rollback |
| Caching | ✅ | Thread-safe with ConcurrentHashMap |

### Future Enhancements

See [doc/improvements-01.md](doc/improvements-01.md) for detailed improvement suggestions:

- `noRollbackFor` attribute
- Isolation level support
- Multiple transaction manager support

## 🎁 Benefits

### For Your Domain Layer
- 🔓 **Independence**: No framework coupling
- 🧪 **Testability**: Easy to test in isolation
- 📖 **Clarity**: Business intent is explicit

### For Your Architecture
- 🏛️ **Clean Architecture**: Proper dependency direction
- 🔄 **Flexibility**: Easy to switch frameworks
- 📦 **Modularity**: Domain can be extracted to separate module

### For Your Team
- 📚 **Learning**: Understand how transactions work under the hood
- 🎯 **Control**: Full visibility into transaction behavior
- 🚀 **Performance**: Optimized with caching

## 🤝 Contributing

Contributions are welcome! Feel free to:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

Created as a proof of concept to demonstrate Clean Architecture principles in Spring Boot applications.

## 🙏 Acknowledgments

- Inspired by Clean Architecture and Hexagonal Architecture principles
- Built with Spring Boot and modern Java practices
- Thanks to the Spring Framework team for their excellent documentation

---

**⭐ If you find this project useful, please consider giving it a star!**

**📢 Check out the accompanying LinkedIn article:** [Coming soon]
