# StudyRooms FILES Guide

Short purpose: this document is a repository-wide, file-by-file catalog that explains what every artifact does, how it connects to others, and where it participates in runtime flows (startup, MVC requests, REST/JWT, SPA assets, Docker/Nginx, and the optional consumer service). It is meant as a navigator for newcomers and as a demo companion.

## How to read this document
- **Scope**: Only files that actually exist in the repository are listed—nothing is invented.
- **Per-file bullets**: Each entry lists the path, type/category, why it exists, key contents, interactions, and demo relevance.
- **Cross-references**: When multiple files collaborate (e.g., controllers → services → repositories), both sides are mentioned to avoid repetition.
- **Profiles & environments**: When a file behaves differently under `default` vs `docker` profiles, or depends on env vars, the note is included.
- **Runtime focus**: Pay attention to “Demo relevance” notes to know what to open when presenting the system.

## Repository map
Top-level layout grouped by role:
```
studyrooms/
├── src/main/java/gr/hua/dit/studyrooms/...        # Spring Boot application code (MVC + REST + adapters)
├── src/main/resources/                            # Application configs, templates, static assets
├── src/test/java/gr/hua/dit/studyrooms/...        # Focused service-level tests
├── consumer-service/                              # Optional API consumer microservice (Spring Boot)
├── nginx/                                         # Reverse proxy config for dockerized demo
├── Dockerfile, docker-compose.yml                 # Containerization for app + DB + nginx + consumer
├── DEMO.md, README.md, Assingment.md, Εκφώνηση....pdf  # Documentation and assignment text
├── demo.postman_collection.json                   # Postman collection for REST demo
├── mvnw, mvnw.cmd, .mvn/wrapper/*                 # Maven wrapper for reproducible builds
├── pom.xml                                        # Parent Maven configuration
├── .dockerignore, .gitignore, .gitattributes      # Tooling metadata
└── consumer-service/Dockerfile & pom.xml          # Container + build for consumer microservice
```
Categories: backend (Java), frontend (Thymeleaf + SPA), consumer service, docs, docker/nginx, tests, configs.

## Core application walkthrough (high-level)
- **UI → Controllers → Services → Repos → DB**: HTML views (Thymeleaf templates under `src/main/resources/templates`) are rendered by MVC controllers in `src/main/java/.../controller`. They call services in `.../service` (business rules, validation) which delegate to repositories in `.../repository` for persistence against the configured DB profile (H2 by default, PostgreSQL in Docker).
- **REST API with JWT**: `/api/**` controllers under `controller/api` expose JSON endpoints secured by `SecurityConfig`’s API filter chain (stateless JWT). `JwtAuthenticationFilter` parses tokens issued by `AuthApiController` + `JwtService`. Controllers call the same service layer as MVC.
- **External services via ports/adapters**: Interfaces in `external/*Port` describe required behaviors (holidays, weather, notifications). Adapters (e.g., `HolidayApiAdapter`, `OpenMeteoWeatherAdapter`, `NotificationApiAdapter`) use `WebClient` beans to call upstream systems and translate responses into domain DTOs.
- **SPA client flow**: Static SPA assets (`static/spa/index.html`, `static/js/spa.js`, `static/css/spa.css`) use fetch/XHR calls against `/api/**` endpoints. Tokens are stored in browser memory and attached to requests; rendering is done client-side.
- **Optional consumer service**: Independent Spring Boot app under `consumer-service/` authenticates against the main API via JWT, then fetches spaces/reservations and logs a digest. Demonstrates distributed consumption and uses its own `application.yml` + Dockerfile.

## File-by-file documentation
### Root-level documentation & metadata
- **README.md** — Markdown; primary project overview and distributed-systems report. Describes features, architecture, security, deployment, and demo steps. Refer to it for conceptual context; FILES.md complements it by enumerating files.
- **DEMO.md** — Markdown; scripted demo steps for showing both MVC and API interactions. Useful during presentations; pairs with `demo.postman_collection.json` for API calls.
- **Assingment.md** — Markdown; assignment instructions (Greek). Provides the original problem statement for context.
- **Εκφώνηση Εργασίας.pdf** — PDF; official assignment brief. Not used at runtime but relevant for academic submission.
- **demo.postman_collection.json** — JSON; Postman collection defining REST requests (auth, spaces, reservations). Consumed by Postman to drive API demos.
- **.gitignore / .gitattributes** — Git config; ignore rules (target/, logs, IDE files) and default text attributes to normalize line endings.
- **.dockerignore** — Docker build filter; excludes target folders, IDE files, git metadata to speed Docker builds.
- **mvnw / mvnw.cmd / .mvn/wrapper/maven-wrapper.properties** — Executable wrapper scripts and config pinning Maven distribution for consistent builds without a global install.
- **pom.xml** — Maven project descriptor for the main app: defines Java 17, Spring Boot starters (Web, Security, Thymeleaf, Data JPA, Validation, WebFlux), OpenAPI, JJWT, test deps, and enforcer plugin to ensure dependency convergence.【F:pom.xml†L29-L139】
- **Dockerfile** — Container build for the main app: copies source, runs Maven wrapper, builds jar, and exposes port 8080. Works with `docker-compose.yml`.
- **docker-compose.yml** — Multi-service orchestration: builds `app` from Dockerfile, provisions PostgreSQL, nginx reverse proxy, and optional `consumer` service; wires environment variables for DB credentials, Spring profiles, and public ports.

### Backend application (Java sources)
- **src/main/java/gr/hua/dit/studyrooms/StudyRoomsApplication.java** — Main Spring Boot entrypoint annotated with `@SpringBootApplication`; launches the context for both MVC and REST APIs.【F:src/main/java/gr/hua/dit/studyrooms/StudyRoomsApplication.java†L6-L13】 Demo relevance: required to start any flow (local or Docker).

#### Domain entities
- **entity/User.java** — JPA entity for users with username, password, full name, email, role, penalties, and reservations. Contains getters/setters and mappings (`@Table("users")`, `@OneToMany` to reservations). Supports both security and reservation ownership.【F:src/main/java/gr/hua/dit/studyrooms/entity/User.java†L8-L116】
- **entity/UserRole.java** — Enum of `STUDENT` and `STAFF` roles used across security rules.【F:src/main/java/gr/hua/dit/studyrooms/entity/UserRole.java†L3-L6】
- **entity/StudySpace.java** — JPA entity for rooms/spaces with validation constraints (name, description, capacity, open/close times) and relation to reservations. Drives availability checks and UI listings.【F:src/main/java/gr/hua/dit/studyrooms/entity/StudySpace.java†L12-L114】
- **entity/Reservation.java** — JPA entity connecting `User` and `StudySpace` with date/time window and `ReservationStatus`. Persists booking states for both MVC and API flows.【F:src/main/java/gr/hua/dit/studyrooms/entity/Reservation.java†L7-L105】
- **entity/ReservationStatus.java** — Enum for reservation lifecycle: `PENDING`, `CONFIRMED`, `CANCELLED`, `CANCELLED_BY_STAFF`, `NO_SHOW`. Used in business rules and UI badges.【F:src/main/java/gr/hua/dit/studyrooms/entity/ReservationStatus.java†L3-L9】

#### Availability helper models
- **availability/TimeSlotAvailability.java** — POJO capturing slot start/end, availability flag, and reason; used by `SpaceAvailabilityService` to expose slot status in UI/API.
- **availability/TimeSlotView.java** — DTO/POJO for presenting individual slots with label and status (often to templates or SPA JSON).
- **availability/SpaceAvailabilityService.java** — Service that builds daily availability grids by consulting `ReservationRepository`, study space hours, and reservation rules; feeds controllers rendering slots.

#### DTOs and mappers
- **dto/ReservationFormDto.java** — Request DTO for reservation creation (studySpaceId, date, start/end time) with validation annotations; shared by MVC form binding and REST body parsing.
- **dto/UserRegistrationDto.java** — Form DTO for user registration; fields for username, password, confirm password, email, full name.
- **dto/LoginRequestDto.java / LoginResponseDto.java** — Request/response DTOs for API login; used by `AuthApiController` and consumer/SPA clients.
- **dto/StudySpaceDto.java** — Presentation DTO for study spaces (id, name, description, capacity, hours) used in API responses.
- **dto/StudySpaceMapper.java** — Utility to convert `StudySpace` entities into DTOs for REST responses; keeps controller code slim.
- **dto/HomeStats.java / HomeStatsService.java** — Aggregates daily stats (upcoming reservations, counts, weather) for the home/dashboard views; service composes repositories and weather service.
- **dto/WeatherDto.java** — Weather snapshot (temperature, weather code/description) returned by weather adapters and exposed in API/UI.
- **dto/OccupancyStatsEntry.java** — Data carrier for staff occupancy charts; used by statistics service and staff templates.

#### Repositories
- **repository/UserRepository.java** — Extends Spring Data JPA for `User` with queries by username and email; used by security, registration, and data seeding.
- **repository/StudySpaceRepository.java** — CRUD + custom finders by name; used for space management, availability checks, and seeding.
- **repository/ReservationRepository.java** — Rich query interface: find by user/date/space, count overlapping reservations for capacity enforcement, existence checks for staff closures, etc.; core to reservation rules and slot calculations.

#### Services (interfaces + implementations)
- **service/UserService.java / impl/UserServiceImpl.java** — Handles registration and profile needs, including password hashing and role assignment; invoked by `AuthController`/`AuthApiController`.
- **service/StudySpaceService.java / impl/StudySpaceServiceImpl.java** — CRUD and listing of study spaces with validation on hours; used by staff controllers and APIs.
- **service/ReservationService.java / impl/ReservationServiceImpl.java** — Central business logic: validates penalties, holidays, opening hours, overlapping capacity, daily limits, duration caps, and staff closures before persisting reservations; supports cancellations and staff no-show marking with penalties.【F:src/main/java/gr/hua/dit/studyrooms/service/impl/ReservationServiceImpl.java†L26-L271】
- **service/ReservationStatisticsService.java / impl/ReservationStatisticsServiceImpl.java** — Computes occupancy summaries and trends for staff dashboards using repository counts and DTOs.
- **service/WeatherService.java / impl/WeatherServiceImpl.java** — Retrieves weather forecast via `WeatherPort` and caches/unwraps response data for UI/API; exercised by tests.
- **service/NotificationService.java / impl/NotificationServiceImpl.java** — Abstraction for sending notifications (currently logs or forwards via `NotificationPort` adapter); called from reservation lifecycle events.

#### External ports and adapters
- **external/HolidayApiPort.java** — Interface representing holiday calendar checks.
- **external/HolidayApiAdapter.java** — Adapter using `WebClient` to query holiday API; returns boolean for `isHoliday` and converts errors to `ExternalServiceException`.
- **external/WeatherPort.java** — Interface for weather lookups; implemented by `external/weather/OpenMeteoWeatherAdapter.java` which calls the Open-Meteo API and maps JSON to `WeatherDto`.
- **external/ExternalServiceException.java** — Custom runtime exception for upstream failures, propagated to controllers.
- **external/notification/NotificationPort.java / NotificationApiAdapter.java / NotificationRequest.java** — Defines notification contract and simple adapter to send reservation events outward (configurable via `NotificationClientProperties`).

#### Configuration
- **config/WebClientConfig.java** — Defines reusable `WebClient.Builder` for external HTTP calls (holidays, weather, notifications).
- **config/NotificationClientProperties.java** — `@ConfigurationProperties` binding for notification endpoint URL, API key, and toggle flags.
- **config/NotificationConfig.java** — Creates `NotificationPort` bean using properties and WebClient; allows disabling via feature flag.
- **config/OpenApiConfig.java** — Configures Springdoc OpenAPI and JWT security scheme for Swagger UI exposure.
- **config/DataInitializer.java** — `CommandLineRunner` seeding default users, study spaces, and sample reservations when `demo.seed.enabled=true` profile flag is set; used in demos and Docker profile.【F:src/main/java/gr/hua/dit/studyrooms/config/DataInitializer.java†L22-L265】

#### Security
- **security/SecurityConfig.java** — Two filter chains: stateless JWT for `/api/**` (disables sessions/forms) and session-based form login for MVC (permits static assets/public pages, restricts `/staff/**`, custom login/logout). Inserts `JwtAuthenticationFilter` before username/password filter.【F:src/main/java/gr/hua/dit/studyrooms/security/SecurityConfig.java†L18-L109】
- **security/JwtService.java** — Issues and validates JWT tokens using JJWT library, embedding username and roles; used by `AuthApiController` and `JwtAuthenticationFilter`.
- **security/JwtAuthenticationFilter.java** — Extracts bearer token, validates via `JwtService`, loads user details, and sets authentication in the security context for API requests.
- **security/CustomUserDetails.java / CustomUserDetailsService.java** — Bridges `User` entity to Spring Security’s `UserDetails`, including roles and password; shared across API and MVC auth flows.

#### MVC Controllers (Thymeleaf)
- **controller/HomeController.java** — Handles `/` and `/home`; gathers `HomeStats` and weather for landing/home page.
- **controller/AuthController.java** — Registration and login pages; binds `UserRegistrationDto` and delegates to `UserService`.
- **controller/DashboardController.java** — Authenticated landing after login (`/dashboard`); shows upcoming reservations and stats.
- **controller/ReservationController.java** — Student reservation flows: list “my reservations”, render creation form, post new reservations, cancel own reservations; relies on `ReservationService` and availability helper.
- **controller/StudySpaceController.java** — Public and student-facing space listings/details with availability grids; uses `StudySpaceService` and `SpaceAvailabilityService`.
- **controller/StaffStatsController.java** — Staff-only dashboard for occupancy/stats; consumes `ReservationStatisticsService` and weather.
- **controller/WebExceptionHandler.java / ErrorPagesController.java** — Global MVC error handling for validation/business exceptions and HTTP error pages (`access-denied`, custom error views).

#### REST API Controllers
- **controller/api/AuthApiController.java** — `/api/auth/login` and `/api/auth/register`; issues JWT via `JwtService` and registers new users via `UserService`.
- **controller/api/ReservationApiController.java** — `/api/reservations` endpoints for authenticated users: list own reservations, create, cancel; uses `ReservationService` and principal extraction.【F:src/main/java/gr/hua/dit/studyrooms/controller/api/ReservationApiController.java†L18-L68】
- **controller/api/StudySpaceApiController.java** — Public space listing/detail endpoints returning DTOs; used by SPA/consumer.
- **controller/api/StaffApiController.java** — Staff-only operations (e.g., mark no-show, cancel as staff) guarded by role and calling `ReservationService`.
- **controller/api/StatsApiController.java** — Returns occupancy statistics for staff dashboards and charts.
- **controller/api/WeatherApiController.java** — Exposes weather lookup results from `WeatherService` for the SPA/UI.
- **controller/api/ApiExceptionHandler.java** — `@RestControllerAdvice` translating domain exceptions into structured HTTP responses for API clients.

### Resources (configs, templates, static assets)
- **src/main/resources/application.properties** — Default profile settings: H2 in-memory DB, demo seed toggle, JWT secret/expiry, notification defaults, Swagger config.
- **src/main/resources/application-docker.properties** — Overrides for Docker profile: PostgreSQL datasource, `demo.seed.enabled=true`, hostnames matching docker-compose, external API base URLs.
- **templates/*.html** — Thymeleaf views:
  - `layout.html` base layout with navbar and message blocks.
  - `home.html` public landing with stats and weather.
  - `login.html` / `register.html` auth screens.
  - `dashboard.html` logged-in overview of reservations/stats.
  - `spaces.html`, `space_details.html`, `reservation_form.html`, `reservations_my.html` for student flows.
  - `manage_spaces.html`, `space_form.html`, `staff_reservations.html`, `staff_occupancy.html` for staff management and stats.
  - `access-denied.html` for forbidden responses.
- **static/js/reservation-form.js** — Client-side helpers for reservation form validation and dynamic slot fetching.
- **static/js/spa.js** — SPA script: handles login to REST API, stores JWT in memory, calls `/api/spaces` and `/api/reservations`, renders results and error states, and exposes staff-only sections for space management, staff reservations, and occupancy stats when authorized.
- **static/css/spa.css** — Styling for SPA UI panels, forms, cards, and staff-only sections.
- **static/spa/index.html** — SPA entrypoint served as a static file; links `spa.js`/`spa.css`, renders token-aware controls, and includes staff-only sections for space management, staff reservations, and occupancy stats.

### Tests
- **src/test/java/gr/hua/dit/studyrooms/StudyRoomsApplicationTests.java** — Context load sanity test for the main app.
- **service/ReservationServiceImplTest.java** — Unit tests asserting reservation business rules: overlapping capacity, daily limits, past/holiday validations, and penalties.
- **service/WeatherServiceImplTest.java** — Tests weather service behavior with mocked `WeatherPort` responses and error handling.

### Docker & Nginx assets
- **nginx/nginx.conf** — Reverse proxy routing `/` to app container, serves SPA/static, forwards `/api/` and `/swagger-ui` to backend; used in Docker demo behind port 80.
- **Dockerfile (root)** — Builds main backend image (see above); supports multistage caching via Maven.
- **docker-compose.yml** — Orchestrates `app`, `db` (PostgreSQL), `nginx`, and `consumer` (optional). Mounts configs, sets `SPRING_PROFILES_ACTIVE=docker`, publishes ports 80 (nginx) and 8080 (app direct access).
- **consumer-service/Dockerfile** — Builds consumer microservice image; expects environment variables for API base URL, credentials, and enable flag.

### Consumer service (independent module)
- **consumer-service/pom.xml** — Maven config for consumer app: Spring Boot webflux/client dependencies and configuration processor.
- **consumer-service/src/main/java/gr/hua/dit/studyrooms/consumer/ConsumerApplication.java** — Entrypoint; runs `ApiConsumerClient.runDigest()` at startup via `CommandLineRunner`, demonstrating remote API consumption.【F:consumer-service/src/main/java/gr/hua/dit/studyrooms/consumer/ConsumerApplication.java†L9-L20】
- **consumer-service/src/main/java/gr/hua/dit/studyrooms/consumer/ApiConsumerClient.java** — Core client: performs login to StudyRooms API (JWT), fetches spaces and reservations using `WebClient`, logs digest output; respects enable flag from properties.
- **consumer-service/src/main/java/gr/hua/dit/studyrooms/consumer/ConsumerProperties.java** — `@ConfigurationProperties` binding for `studyrooms.consumer.*` (base URL, username, password, enable toggle).
- **consumer-service/src/main/java/gr/hua/dit/studyrooms/consumer/dto/*.java** — DTOs mirroring API payloads (spaces, reservations, auth request/response) for deserialization.
- **consumer-service/src/main/resources/application.yml** — Default properties (disabled by default, placeholders for base URL/credentials) and logging config.
- **consumer-service/README.md** — Usage instructions for running the consumer locally with CLI arguments; highlights enable flag and credentials expectation.【F:consumer-service/README.md†L1-L12】

## Cross-cutting indexes
- **By feature**
  - Reservations: `controller/ReservationController.java`, `controller/api/ReservationApiController.java`, `service/ReservationService.java`, `repository/ReservationRepository.java`, templates `reservation_form.html`, `reservations_my.html`, `staff_reservations.html`, SPA `spa.js`.
  - Spaces: `StudySpaceController`, `StudySpaceApiController`, `StudySpaceService`, `StudySpaceRepository`, templates `spaces.html`, `space_details.html`, `manage_spaces.html`, `space_form.html`.
  - Auth: MVC login/register controllers + templates; API `AuthApiController`, security classes (`SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`).
  - Stats: `ReservationStatisticsServiceImpl`, `StatsApiController`, `StaffStatsController`, templates `staff_occupancy.html`, DTO `OccupancyStatsEntry`.
  - Weather: `WeatherServiceImpl`, `WeatherApiController`, external `WeatherPort` + `OpenMeteoWeatherAdapter`, DTO `WeatherDto`, UI sections in `home.html`/`dashboard.html`.
  - Notifications: `NotificationServiceImpl`, external `notification/*`, config `NotificationClientProperties`/`NotificationConfig`.
  - Holidays: `HolidayApiPort`, `HolidayApiAdapter`, invoked from `ReservationServiceImpl` for validation.
  - Seeding/demo data: `config/DataInitializer.java`, properties `demo.seed.enabled` in `application*.properties`.

- **By entrypoints**
  - Main app startup: `StudyRoomsApplication.java`.
  - MVC entry: controllers under `controller/*`, templates in `templates/`.
  - REST entry: controllers under `controller/api/*` (JWT-protected except auth/weather/public space endpoints).
  - Security config: `security/SecurityConfig.java`, JWT helper `security/JwtService.java`.
  - External adapters: `external/*Adapter.java`, `external/weather/OpenMeteoWeatherAdapter.java`, `external/notification/NotificationApiAdapter.java`.
  - Docker compose: `docker-compose.yml`; reverse proxy: `nginx/nginx.conf`.

- **By endpoints**
  - `/api/auth/login`, `/api/auth/register` → `AuthApiController`.
  - `/api/reservations` (POST/GET `/my`/DELETE `/{id}`) → `ReservationApiController`.
  - `/api/spaces` and `/api/spaces/{id}` → `StudySpaceApiController`.
  - `/api/staff/*` (no-show, cancel, closures) → `StaffApiController`.
  - `/api/stats/*` → `StatsApiController`.
  - `/api/weather` → `WeatherApiController`.
  - Swagger/OpenAPI: `/swagger-ui.html`, `/v3/api-docs` configured in `OpenApiConfig` and `SecurityConfig` permit lists.

## Known edges / pitfalls
- Profiles matter: Docker profile (`SPRING_PROFILES_ACTIVE=docker`) switches DB to PostgreSQL and enables seeding; local default uses H2. Mismatched profiles can cause connection failures.
- JWT vs session: API calls require bearer token; MVC uses session cookies. Using the wrong auth mode will yield 401/redirect loops.
- Holiday validation: If the holiday API is unreachable and exceptions propagate, reservation creation can fail; `ExternalServiceException` is mapped by `ApiExceptionHandler` for APIs and `WebExceptionHandler` for MVC.
- Capacity checks: Overlapping reservation counts rely on repository queries; invalid DB data (e.g., endTime before startTime inserted manually) may distort availability grids.
- Notification adapter: Disabled or misconfigured notification endpoint can lead to log-only behavior; check `NotificationClientProperties` when demonstrating notifications.
- Consumer enable flag: `studyrooms.consumer.enabled` defaults to false—forgotten flag leads to the consumer exiting immediately without API calls.

## Quick start pointers
- **Want MVC flow?** Start with `controller/ReservationController.java` and templates `reservation_form.html`/`reservations_my.html`; supporting logic in `ReservationServiceImpl` and `SpaceAvailabilityService`.
- **Need REST/JWT?** Check `controller/api/AuthApiController.java` (login/register) and `ReservationApiController.java`; security behavior in `SecurityConfig` + `JwtService`.
- **Debug external calls?** See `external/HolidayApiAdapter.java`, `OpenMeteoWeatherAdapter.java`, and `NotificationApiAdapter.java` along with `WebClientConfig`.
- **Run Docker demo?** Inspect `docker-compose.yml`, `application-docker.properties`, and `nginx/nginx.conf` for ports and routing.
- **Explore consumer microservice?** Open `consumer-service/ApiConsumerClient.java` and `ConsumerProperties.java`; run via `consumer-service/README.md` instructions.
