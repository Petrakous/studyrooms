# StudyRooms — Distributed Study-Space Reservation Platform

## Project Overview
### What the application does
StudyRooms is a Spring Boot–based reservation platform for university study spaces. It allows students to browse rooms, book seats for specific time windows, and manage their reservations, while staff manage capacity, close rooms, and monitor usage. The system enforces rich business rules (time validation, overlapping checks, penalties, and staff-only workflows) and exposes both a classic server-rendered UI and a JWT-protected REST API consumed by a bundled SPA and an optional consumer microservice.

### Who the application is for
- **Students**: reserve library rooms or labs, review/cancel upcoming bookings, and receive consistent validation feedback across UI and API clients.
- **Staff**: administer study spaces, close rooms for operational reasons, mark no-shows, and view statistics to understand demand.
- **Developers / instructors**: explore a complete, distributed teaching example that mixes MVC, REST, SPA, and downstream consumers.

### High-level feature summary
- MVC web UI with Thymeleaf for registration, login, dashboard, space browsing, reservation creation, and staff dashboards.
- JWT-secured REST API with Swagger/OpenAPI docs, covering authentication, study spaces, reservations, statistics, and weather lookup.
- Lightweight SPA shipped from `/spa/` that exercises the API (authenticate, list spaces, create/cancel reservations).
- Optional consumer service demonstrating a second process that authenticates and consumes the API headlessly.
- External integrations: public holiday lookup, weather information (also surfaced in the UI), and a pluggable notification client.
- Dockerized stack with PostgreSQL, reverse-proxy fronting via Nginx, and profile-based configuration for H2 vs. PostgreSQL.

## System Architecture
### Layered + distributed view
```
+---------------------+        +---------------------------+
|  Browser clients    |        | Optional Consumer Service |
|  - Thymeleaf MVC UI |<----+  | (separate Spring Boot)    |
|  - SPA (static)     |     |  +---------------------------+
+----------+----------+     |
           |                |  (JWT over HTTP)
           v                |
    +------+----------------+-----------------------------+
    |   StudyRooms Spring Boot Application                |
    |   - MVC controllers (session auth)                  |
    |   - REST controllers (JWT auth)                     |
    |   - Services (business rules, notifications)        |
    |   - Repositories (JPA/Hibernate)                    |
    +------+----------------+-----------------------------+
           |                |
           v                v
+----------+-----+   +--------------+
| PostgreSQL /   |   | External APIs|
| H2 database    |   | - Holidays   |
+----------------+   | - Weather    |
                     | - Notification (optional)
                     +--------------+
```

The system is **distributed** because it spans multiple cooperating processes: browsers calling the application, the main Spring Boot app behind an Nginx reverse proxy, optional downstream consumer service, and outbound calls to external holiday/weather/notification providers. JWT-based stateless APIs enable cross-process communication, while MVC sessions serve server-rendered pages.

### Component interactions
- **Web UI (Thymeleaf MVC)**: Uses session-based authentication. Controllers render templates and call services; services enforce business rules and persist via repositories. Staff-only flows (space management, closing spaces, no-show marking) are restricted by roles.
- **REST API**: Stateless endpoints secured by JWT (bearer). Auth controller issues tokens, which clients use to call space, reservation, stats, and weather endpoints. The same service layer is reused, ensuring rule consistency.
- **SPA client**: Static assets served from `/spa/` call the REST API via fetch with bearer tokens. It demonstrates token lifecycle, optimistic UI, and error propagation from API validation.
- **External services**: Weather and holiday lookups via `WebClient` adapters treat providers as black boxes; failures are handled gracefully. A notification port/adaptor can call an external notification service when enabled. Weather results are also shown in the web UI (space details page) via a small widget that calls the public `/api/weather` endpoint with configured or default demo coordinates, lets the user pick a date/time for their reservation to see a forecast, and degrades gracefully when unavailable.
- **Optional consumer service**: A separate Spring Boot process authenticates via the API, fetches spaces and the current user’s reservations, and logs the digest—showcasing a second distributed client.

## Technology Stack
- **Backend**: Spring Boot 3, Spring MVC, Spring WebFlux `WebClient` for outbound calls, Spring Data JPA/Hibernate for persistence, Jakarta Validation for DTO validation.
- **Database**: H2 (file-based) by default for local development; PostgreSQL profile for Docker deployments. Liquidity handled via `spring.jpa.hibernate.ddl-auto=update` for demo simplicity.
- **Security**: Spring Security with dual filter chains—session-based form login for MVC, stateless JWT (BCrypt-hashed credentials) for `/api/**` endpoints. Method-level authorization protects staff operations.
- **Containerization & proxy**: Dockerfiles for main app and consumer, `docker-compose` orchestrating app + Postgres + Nginx (plus consumer via profile). Nginx terminates HTTP and forwards `/api`, `/swagger-ui`, `/spa`, and root traffic to the app.

## Project Structure
```
root
├── src/main/java/gr/hua/dit/studyrooms
│   ├── controller/               # MVC controllers (Thymeleaf flows)
│   ├── controller/api/           # REST API controllers
│   ├── service/ (+impl/)         # Business services & orchestration
│   ├── repository/               # Spring Data JPA repositories
│   ├── dto/                      # Transport + form models & mappers
│   ├── entity/                   # JPA entities (User, StudySpace, Reservation)
│   ├── availability/             # Availability calculations for time slots
│   ├── external/                 # Ports/adapters to holiday, weather, notifications
│   ├── security/                 # Dual security configs, JWT filter, user details
│   └── config/                   # WebClient beans, OpenAPI, data seeding
├── src/main/resources
│   ├── templates/                # Thymeleaf views (UI, staff pages)
│   ├── static/                   # CSS/JS; includes SPA under /spa
│   ├── application.properties    # H2/local defaults
│   └── application-docker.properties # PostgreSQL + demo seed profile
├── consumer-service/             # Optional standalone API consumer
├── nginx/nginx.conf              # Reverse-proxy routes for docker-compose
├── Dockerfile                    # Multi-stage build for main app
├── docker-compose.yml            # App + DB + Nginx (+ optional consumer)
└── DEMO.md                       # Quick docker demo guide
```

### Responsibilities by layer
- **Controllers (MVC vs API)**: MVC controllers render pages and manage session flows (e.g., `ReservationController`), while API controllers expose JSON endpoints (e.g., `ReservationApiController`, `AuthApiController`). Both delegate to services to centralize business rules.
- **Services**: Implement business logic, validation, and orchestration (reservations, study spaces, users, statistics, weather). Services call repositories and external ports, keeping controllers thin.
- **Repositories**: Spring Data interfaces encapsulating persistence queries, including overlap counting and capacity checks.
- **DTOs**: Form/input payloads (login, reservation form, registration), API responses (login token), mappers for projecting entities.
- **Entities**: JPA models for users (with roles and penalties), study spaces (capacity, hours), and reservations (date/time/status).
- **External ports/adapters**: Interfaces plus adapters for holiday API, weather API, and notification provider, enabling testability and black-box treatment of dependencies.

## Business Logic & Rules
Reservation processing lives in `ReservationServiceImpl`, ensuring consistent enforcement across MVC and API calls.

Key rules:
- **Time validation**: Reject past dates or already-started slots for today; ensure end time is after start time; enforce opening hours per study space; cap duration at 2 hours.
- **Capacity & overlaps**: Count overlapping reservations with active statuses; reject if capacity would be exceeded or if a space is closed by staff.
- **Daily limits**: Maximum of 3 active reservations per student per day (pending/confirmed only).
- **Penalties & no-shows**: Users marked as no-show incur a 3-day penalty blocking new bookings; penalty is checked before creating reservations. Staff can mark no-shows and bulk-cancel when closing a space.
- **Staff-only operations**: Cancelling any reservation, closing spaces for a day, marking no-shows, and viewing staff statistics are gated by role checks (method security and URL rules).

These checks live in the **service layer** to avoid duplication across MVC/REST entry points and to maintain transactional integrity when persisting reservations and penalties.

## Security Design
- **Authentication flows**:
  - *MVC session*: Form login at `/login`, storing session for server-rendered pages. Default success redirects to `/dashboard`.
  - *JWT API*: `/api/auth/login` issues a bearer token after credential verification; clients include it in `Authorization: Bearer <token>` for stateless API calls.
- **Authorization & roles**: `STUDENT` vs `STAFF` roles. MVC routes like `/staff/**` and API operations such as staff stats require `ROLE_STAFF`; other endpoints require authentication or are explicitly permitted (e.g., Swagger, weather lookup, static assets).
- **Dual security configurations**: Two ordered filter chains separate concerns—stateless JWT for `/api/**` and stateful session security for MVC routes—preventing conflicts and ensuring least privilege.

## External Services Integration
- **Holiday API**: `HolidayApiAdapter` queries a public holiday service; failures degrade gracefully (treat as non-holiday) to keep reservation flow available.
- **Weather API**: `OpenMeteoWeatherAdapter` (via `WeatherService`) fetches current conditions or hour-level forecasts. The `/api/weather` endpoint is consumed both by API clients and by the server-rendered space details page via a small JavaScript fetch widget that shows temperature, wind, and precipitation for the selected date/time (defaults to today at space opening). It defaults to demo coordinates (Athens) but can be pointed at any campus via environment/properties, and it fails fast with friendly text if coordinates are missing or the upstream service is down.
- **Notification service (optional)**: `NotificationApiAdapter` posts to an external notification endpoint when enabled; otherwise logs and skips to keep the system resilient.

Adapters treat these services as black boxes, hiding protocol/URL details behind ports. Error handling avoids breaking core booking flows—timeouts and HTTP errors are logged and ignored where appropriate.

## User Interfaces
### Web UI (Thymeleaf)
- Students: register/login, browse study spaces, create bookings with validation feedback, view and cancel “My Reservations”.
- Staff: dashboards for daily reservations, mark no-shows, cancel reservations, close spaces for a date (bulk cancellation), view statistics.
- Space details view includes a lightweight weather card that fetches `/api/weather` asynchronously and shows temperature, wind, and precipitation for a chosen date/time using demo or configured coordinates; failures render friendly text without disrupting the page.

### SPA client (`/spa/`)
A minimalist single-page client that:
1. Authenticates via `/api/auth/login` and stores the JWT in session storage.
2. Calls `/api/spaces` to list rooms and populate dropdowns.
3. Posts to `/api/reservations` to create reservations and `/api/reservations/{id}` (DELETE) to cancel.
4. Fetches `/api/reservations/my` to render current bookings.
It demonstrates token handling, API error surfacing, and is intentionally limited to student flows.

## REST API
- **Philosophy**: JSON-first, stateless, JWT-secured for mutating operations; OpenAPI annotations for discoverability. DTO validation guards inputs.
- **Authentication**: `/api/auth/login` returns a JWT; `/api/auth/register` allows student self-registration.
- **Resources**: `/api/spaces` (CRUD/listing), `/api/reservations` (create/list own/cancel), `/api/stats` (staff occupancy), `/api/weather` (public lookup), `/api/staff/**` (staff actions such as closing spaces or marking no-shows).
- **Documentation**: Swagger UI available at `/swagger-ui.html` (proxied by Nginx in Docker) with bearerAuth security scheme pre-declared.
- **Typical flow**: Authenticate → include `Authorization: Bearer <token>` → call protected endpoints → receive JSON entities mirroring core domain models.

## Optional Consumer Service
- **Purpose**: Illustrates a second Spring Boot process that authenticates against the StudyRooms API, fetches spaces and the authenticated user’s reservations, and logs the digest—proving interoperability and distributed consumption.
- **Authentication**: Uses the same `/api/auth/login` JWT flow; base URL and credentials are configurable via properties or Docker profile (`with-consumer`).
- **Usage**: Disabled by default; enable via `STUDYROOMS_CONSUMER_ENABLED=true` or `docker compose --profile with-consumer up` to run alongside the main stack.

## Database & Persistence
- **Entities & relationships**: `User` (roles, penalty, reservations) ↔ `Reservation` (many-to-one to `User` and `StudySpace`, includes date/time/status) ↔ `StudySpace` (name, description, capacity, open/close hours).
- **Transaction boundaries**: Service methods are transactional, ensuring that reservation creation, cancellations, and penalty updates persist atomically with associated notifications.
- **Profiles**:
  - *H2 (default)*: File-backed DB for local dev, auto schema update, no container dependencies.
  - *PostgreSQL (docker)*: External DB via `application-docker.properties`, seeded with demo data by default.

## Docker & Deployment
- **Dockerfiles**: Multi-stage build for the main app (`mvnw package` → slim JRE image) and a separate Dockerfile for the consumer service.
- **docker-compose services**:
  - `db`: PostgreSQL with health checks and volume persistence.
  - `app`: StudyRooms application, built from source, bound to port 8080, depends on DB.
  - `nginx`: Reverse proxy exposing port 80 and routing `/`, `/api`, `/swagger-ui`, `/spa`, and docs to the app.
  - `consumer` (profiled): Optional API-consuming service.
- **Profiles & env vars**: `SPRING_PROFILES_ACTIVE=docker` selects PostgreSQL settings; `DB_*` vars override connection; `DEMO_SEED` toggles data seeding; notification and holiday country code configurable via properties.

## Running the Application
### Local (without Docker)
1. Ensure Java 17+ and Maven Wrapper available.
2. (Optional) Set `DEMO_SEED=true` to preload demo data.
3. Run:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access:
   - UI: http://localhost:8080/
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - SPA: http://localhost:8080/spa/

### Docker-based demo
1. Build and start everything (app + Postgres + Nginx) with seeding enabled by default:
   ```bash
   docker compose up --build
   ```
2. Optional: include the consumer service using the profile:
   ```bash
   docker compose --profile with-consumer up --build
   ```
3. Access via Nginx on http://localhost/ (proxying to the app). API base http://localhost/api.
4. (Optional) Point the weather widget at your campus by passing coordinates as environment variables (Spring reads them as relaxed properties): `STUDYROOMS_DEMO_LATITUDE`, `STUDYROOMS_DEMO_LONGITUDE`, and `STUDYROOMS_DEMO_LOCATION_LABEL`.

### Default demo users
- **Staff**: `staff` / `staff123`
- **Student**: `student` / `student123`
- **Second student**: `student2` / `student123`

## Demo Walkthrough
1. **Student flow (MVC)**: Login as `student`, browse spaces, create a reservation respecting time and capacity, then view/cancel it in “My Reservations”. Errors surface if rules are violated (e.g., overlapping, exceeding daily limit, outside hours).
2. **Staff flow (MVC)**: Login as `staff`, open the staff reservations page for a date, cancel or mark no-shows, or close a space for the day to bulk-cancel bookings. Observe status changes and penalty application to no-shows.
3. **API demo**: Use Swagger UI to POST to `/api/auth/login`, then call `/api/spaces` and `/api/reservations` with the bearer token. Experiment with validation errors (past date, capacity exceeded) to see consistent messages.
4. **SPA demo**: Visit `/spa/`, authenticate as a student, list spaces, create a booking, and cancel it—all via the REST API.
5. **Consumer demo**: Start the consumer profile; inspect logs to see it obtaining a JWT and fetching spaces/reservations from the API without human interaction.

## Testing & Validation
- The project is wired for unit/integration testing but currently ships without test suites. Validation is enforced through Jakarta Validation on DTOs and comprehensive service-layer checks for reservation rules.

## Design Decisions & Trade-offs
- **Two security chains** to cleanly separate session-based MVC from stateless APIs, preventing cross-interference and matching client needs.
- **Service-layer rule enforcement** to avoid drift between MVC and API inputs and to ensure transactional consistency.
- **Graceful degradation for externals**: Holiday and notification calls fail open (non-blocking) to preserve core functionality in demos/class environments.
- **Schema auto-update** (`ddl-auto=update`) chosen for instructional convenience; for production, migrations would be required.
- **Consumer service minimalism**: Demonstrates distribution and JWT usage without adding operational complexity.

## Conclusion
StudyRooms demonstrates a full-stack **distributed system**: multiple clients (MVC, SPA, optional consumer) interact over HTTP with a JWT-secured REST API fronted by Nginx, backed by a database, and augmented by external holiday, weather, and notification services. Centralized business rules, dual security models, and containerized deployment make it a robust teaching artifact for Distributed Systems coursework and a practical guide for running and extending the platform.
