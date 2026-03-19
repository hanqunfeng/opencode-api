# AGENTS.md - OpenCode API Development Guide

## Project Overview

This is a Spring Boot 4.0.3 application with Java 21, using Maven for build management and Lombok for boilerplate reduction.

## Build Commands

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=OpencodeApiApplicationTests

# Run a specific test method
./mvnw test -Dtest=OpencodeApiApplicationTests#contextLoads

# Skip tests during build
./mvnw clean package -DskipTests

# Run with Maven wrapper (macOS/Linux)
./mvnw [command]

# Run with Maven wrapper (Windows)
mvnw.cmd [command]
```

## Code Style Guidelines

### Java Conventions

- **Package naming**: `com.example.opencodeapi` (lowercase, dot-separated)
- **Class naming**: PascalCase (e.g., `OpencodeApiApplication`)
- **Method naming**: camelCase (e.g., `contextLoads`)
- **Constant naming**: UPPER_SNAKE_CASE
- **File encoding**: UTF-8

### Import Organization

Standard order (enforced by most IDEs):
1. `java.*` imports
2. `javax.*` imports
3. Third-party imports (`org.*`, `com.*`, etc.)
4. Project imports (`com.example.*`)
5. Blank line between groups
6. Static imports at the end
7. No wildcard imports except for `static org.junit.jupiter.api.Assertions.*`

### Formatting Rules

- **Indentation**: 4 spaces (no tabs)
- **Line length**: ~120 characters max
- **Braces**: K&R style (opening brace on same line)
- **Blank lines**: One blank line between methods, two between classes
- **No trailing whitespace**
- **No unnecessary blank lines** inside methods

### Types

- Use interfaces over concrete types where appropriate
- Prefer `List`, `Map`, `Set` over specific implementations
- Use `@NonNull` and `@Nullable` annotations from Lombok where appropriate
- Avoid raw types; use generics: `List<String>` not `List`

### Lombok Usage

- Use `@Data` for simple DTOs/entities (generates getters, setters, toString, equals, hashCode)
- Use `@Builder` for fluent builder patterns
- Use `@AllArgsConstructor` and `@NoArgsConstructor` as needed
- Use `@Slf4j` for logging: `log.info("message")`, `log.debug("message")`
- Use `@RequiredArgsConstructor` for dependency injection via constructor
- Use `@Value` for immutable objects

### Error Handling

- Use `try-catch` blocks for recoverable errors
- Throw `RuntimeException` (or subclasses) for programming errors
- Create custom exceptions for domain-specific errors
- Always log errors with appropriate level before throwing
- Never swallow exceptions silently: `catch (Exception e) { log.error("...", e); throw e; }`

### Logging

- Use SLF4J via `@Slf4j` Lombok annotation
- Log levels: `log.trace()` < `debug()` < `info()` < `warn()` < `error()`
- Use `log.debug()` for development info
- Use `log.info()` for significant application events
- Never log sensitive data (passwords, tokens, PII)

### Spring Boot Best Practices

- Use constructor injection (via `@RequiredArgsConstructor`) over field injection
- Keep `@SpringBootApplication` in the root package
- Use `@Service` for business logic classes
- Use `@Repository` for data access classes
- Use `@RestController` for REST endpoints
- Use `@Controller` for views
- Externalize configuration via `application.properties` or `application.yml`

### Testing

- Test class naming: `ClassNameTest` or `ClassNameTests`
- Test method naming: `methodName_whenCondition_thenResult()`
- Use JUnit 5 (`org.junit.jupiter.api.Test`)
- Use `@SpringBootTest` for integration tests
- Use `@MockBean` for mocking dependencies
- Keep tests independent; no shared mutable state

### REST API Conventions

- Use nouns for endpoints: `/users`, `/orders` (not `/getUsers`)
- Use plural nouns: `/users` not `/user`
- HTTP methods: GET (read), POST (create), PUT (replace), PATCH (update), DELETE (delete)
- Return appropriate HTTP status codes
- Return `ResponseEntity<T>` for flexibility in status codes and headers

### Dependency Injection

- Prefer constructor injection
- Mark dependencies as `final`
- Use `@RequiredArgsConstructor` (Lombok) to generate constructor
- Avoid `@Autowired` on fields

## Project Structure

```
src/
├── main/
│   ├── java/com/example/opencodeapi/
│   │   ├── OpencodeApiApplication.java  # Main class
│   │   ├── controller/                  # REST controllers
│   │   ├── service/                      # Business logic
│   │   ├── repository/                   # Data access
│   │   ├── model/ or entity/             # Domain models
│   │   ├── dto/                          # Data transfer objects
│   │   ├── config/                       # Configuration classes
│   │   └── exception/                    # Custom exceptions
│   └── resources/
│       ├── application.properties        # Configuration
│       └── application.yml               # Alternative config
└── test/
    └── java/com/example/opencodeapi/     # Test classes
```

## Configuration

Configuration is in `src/main/resources/application.properties`:
- Use Spring profiles (`@ActiveProfiles`) for environment-specific config
- Use `@ConfigurationProperties` for typed configuration
- Externalize secrets via environment variables, not hardcoded

## IDE Setup

Recommended settings for IntelliJ IDEA:
- Java compiler: Java 21
- Maven wrapper: Enabled
- Lombok: Enabled (Annotation Processing)
- Code style: Google Java Style Guide or similar
