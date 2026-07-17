# Kickoff Notifications

REST microservice for the **Kickoff Sim / Football League Manager** platform. It owns all
match-notification and subscription logic and runs as an independent Spring Boot application on
its own port and its own database. The main application communicates with it exclusively over a
REST API through a Feign client.

## Tech stack

- **Java** 17
- **Spring Boot** 3.4.0
  - Spring Web (REST API)
  - Spring Data JPA (persistence)
  - Spring Validation (Bean Validation on request DTOs)
  - Spring Scheduling (cron cleanup job)
- **MySQL** 8 (`mysql-connector-j`) — dedicated database `kickoff_notifications`
- **Lombok**
- **Testing:** JUnit 5, Mockito, Spring Boot Test, H2 (in-memory, for the JPA slice test)
- **Build:** Maven (`spring-boot-maven-plugin`)

## Architecture

Layered (three-tier) architecture:

```
controller/   REST endpoints (NotificationController, SubscriptionController)
service/      business logic (NotificationService, SubscriptionService + impl)
repository/   Spring Data JPA repositories
model/        JPA entities (Notification, Subscription) + enums (EntityType, NotificationType)
dto/          request/response payloads
exception/    custom exception + global REST error handler
scheduling/   cron-based cleanup job
```

Every entity uses a **UUID primary key**. The service is stateless and persists everything to
its own MySQL schema — it shares no database with the main application.

## Domain

| Entity | Purpose |
|---|---|
| `Notification` | A single notification delivered to one user (`userId`, `matchId`, `message`, `type`, `read`, `createdAt`). |
| `Subscription` | A user's interest in a team, league, or match (`userId`, `entityType`, `entityId`, `createdAt`). |

Enums:
- `EntityType` — `TEAM`, `LEAGUE`, `MATCH`
- `NotificationType` — `MATCH_RESULT`, `MATCH_KICKOFF`, `MATCH_HALFTIME`, `MATCH_FULLTIME`, `MATCH_UPDATE`, `GOAL`, `UPCOMING_MATCH`

## Functionalities

State-changing operations exposed by the service and triggered (via the main application) by end users:

1. **Broadcast a match notification** — fan out one notification to every user subscribed to the match's home team, away team, league, or the match itself.
2. **Notify a single user** — push one targeted notification (used for the instant match-follow status).
3. **Mark a notification as read** / **mark all as read** for a user.
4. **Clear all notifications** for a user.
5. **Subscribe** a user to a team, league, or match.
6. **Unsubscribe** a user from a subscription.

Read-only helpers (list notifications, unread count, subscription check) support the UI but are not counted as domain functionalities.

## REST API

### Notifications — `/api/notifications`

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/broadcast` | Fan out a match notification to all relevant subscribers | `201` → list of created notification IDs |
| `POST` | `/` | Create a single notification for one user | `201` → `NotificationDto` |
| `GET` | `/?userId={id}` | List a user's notifications | `200` |
| `GET` | `/unread-count?userId={id}` | Count a user's unread notifications | `200` |
| `PUT` | `/{id}/read` | Mark one notification as read | `204` |
| `PUT` | `/read-all?userId={id}` | Mark all of a user's notifications as read | `204` |
| `DELETE` | `/?userId={id}` | Clear all of a user's notifications | `204` |

### Subscriptions — `/api/subscriptions`

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/` | Subscribe a user to a team/league/match | `201` → `SubscriptionDto` |
| `DELETE` | `/{id}` | Remove a subscription | `204` |
| `GET` | `/?userId={id}` | List a user's subscriptions | `200` |
| `GET` | `/check?userId={id}&entityId={id}` | Whether the user is subscribed to an entity | `200` |

All request bodies are validated with Bean Validation (`@Valid`); invalid input returns `400` with field messages.

## Integration with other systems

The microservice is consumed by the **main application** (`kickoff-sim`, package
`bg.softuni.footballleague`) through an **OpenFeign** client. The base URL is configured in the
main app as `notifications.service.url`.

- On match events (kickoff, half-time, full-time, goals, results) the main app's scheduler calls
  `POST /api/notifications/broadcast`, and this service fans the event out to every subscriber.
- When a user stars/follows an entity, the main app calls `POST /api/subscriptions` and, for an
  instant no-spoiler status, `POST /api/notifications`.
- The main app reads a user's notifications and unread badge via the `GET` endpoints and toggles
  read/cleared state via the `PUT`/`DELETE` endpoints.

The two applications exchange `LocalDateTime` values and therefore run in the same JVM timezone
(`Europe/Sofia`).

## Scheduling

- **`NotificationCleanupScheduler`** — cron job `0 0 3 * * *` (daily at 03:00) that deletes
  notifications older than 30 days.

## Validation & error handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) returns structured JSON errors:

| Exception | HTTP status |
|---|---|
| `ResourceNotFoundException` (custom) | `404 Not Found` |
| `DataIntegrityViolationException` (built-in) | `409 Conflict` |
| `MethodArgumentNotValidException` (built-in) | `400 Bad Request` |

## Testing

| Type | Class | Tooling |
|---|---|---|
| Unit | `NotificationServiceImplTest` | Mockito |
| Integration | `SubscriptionRepositoryTest` | `@DataJpaTest` (H2) |
| API | `NotificationControllerTest` | `@WebMvcTest` + MockMvc |
| JSON slice | `BroadcastRequestDeserializationTest` | `@JsonTest` |

## Configuration

`src/main/resources/application.properties`:

| Property | Value |
|---|---|
| `server.port` | `8081` |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/kickoff_notifications` |
| `spring.jpa.hibernate.ddl-auto` | `update` |

The database is created automatically (`createDatabaseIfNotExist=true`).

## Running locally

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8081`. Requires a running MySQL instance on
`localhost:3306`. It is designed to run alongside the main application (default port `8080`),
which drives it via Feign.

A `Dockerfile` is provided for containerized deployment.
