# MiniFramework: Educational DI & Web Framework

Welcome to **MiniFramework**, a lightweight, "from-scratch" Java framework designed to demystify the core mechanics of modern application development. This project demonstrates how annotations, reflection, and low-level networking can be combined to automate object lifecycle management, event-driven communication, and HTTP routing.

## Key Features

### 1. Annotation-Based DI
The project implements a custom DI container that automatically instantiates and wires beans using:
- **Stereotype Annotations**: `@Service`, `@Repository`, `@RestController`, `@Bean`, and `@Configuration`.
- **Automatic Constructor Injection**: Dependencies are resolved and injected into constructors automatically during startup.
- **Service Discovery**: Uses the `reflections` library to scan the classpath for annotated classes.

### 2. Asynchronous Event System
A fully decoupled communication layer that allows services to interact without direct dependencies:
- **`@EventListener`**: A custom annotation to mark methods that handle specific events.
- **Type-Aware EventBus**: A centralized bus that ensures events are only delivered to listeners with matching method signatures.
- **Non-Blocking Execution**: Built on virtual threads (via `Executors.newVirtualThreadPerTaskExecutor()`), ensuring that event publishing and processing are highly concurrent and non-blocking.

### 3. Integrated Audit Logging
Demonstrating the power of the event system, the framework includes a built-in auditing mechanism:
- **Audit Events**: Specialized events (`Create`, `Read`) that capture user actions with automatic timestamps.
- **`AuditService`**: A decoupled listener that logs all `AuditEvent` types, providing a clean separation between business logic and cross-cutting concerns.

### 4. Lightweight HTTP Server
A simple server implementation that demonstrates infrastructure bootstrapping and request routing:
- **Low-Level Socket Handling**: Shows how a basic HTTP server can be built using standard Java `ServerSocket`.
- **Dynamic Routing**: Supports path parameters using `{}` placeholders (e.g., `@GET("/user/{id}")`).
- **Automatic Parameter Mapping**: Maps path segments to method arguments with basic type conversion.
- **DI Integration**: Rest controllers are managed beans, allowing seamless injection of services and the event bus.

## Example Application
The `com.di.exampleapp` package contains a vertical slice demonstrating the full stack:
- **`UserController`**: Handles HTTP requests, invoking services and publishing audit events to the `EventBus`.
- **`UserService`**: Contains business logic for user management.
- **`AuditService`**: Asynchronously listens for and logs audit events triggered by controller actions.
- **`EventLoggerService`**: A generic listener that logs all system events.

Example flow:
1. `GET /user` -> `UserController` publishes a `Read` audit event.
2. `EventBus` dispatches the event to `AuditService` and `EventLoggerService` concurrently.
3. `AuditService` prints: `[Timestamp] READ: Getting all users`.

## How it Works
sThe application follows a streamlined startup sequence orchestrated by `ApplicationContext`:

1. **Bean Discovery & Initialization**: Scans for annotated classes and builds the dependency graph. Beans are instantiated in order of their dependencies.
2. **Event Bus Wiring**: Scans initialized beans for `@EventListener` methods and registers them with the `EventBus`. Beans with no listener methods are skipped.
3. **HTTP Server Bootstrapping**: Identifies `@RestController` beans, maps their `@GET` and `@POST` methods to routes, and registers them with the `Server`.
4. **Server Start**: After setup, the `Server` bean is retrieved via `appContext.getBean(Server.class)` and started explicitly.

## Running the Application
To start the framework, run the `main` method in `com.di.Startup`. This creates an `ApplicationContext`, calls `setup()` to initialize the DI container and wire the event system, then retrieves and starts the HTTP server via `appContext.getBean(Server.class).run()`. You can then interact with the API using the provided `src/main/resources/example.http` file or any HTTP client.

---

*Note: This is a simplified implementation for learning purposes. It focuses on clarity rather than handling advanced scenarios like circular dependencies, scoped beans, or complex proxying.*
