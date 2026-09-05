# AGENTS.md

Spring Boot REST API + vanilla JS frontend for a travel planner. Single Maven module.

## Build / run

- **Java 25** (Temurin) and Maven are required. Spring Boot is **3.5.16** — trust `pom.xml`, not README (which says 3.4.1).
- Run all tests: `mvn test` (CI runs exactly this). Tests use **H2 in-memory**, not Postgres, so no Docker is needed to test.
- Run one test: `mvn test -Dtest=TripServiceTest`
- Build jar: `mvn clean package -DskipTests`
- Run dev server: `mvn spring-boot:run` (needs Postgres up first: `docker-compose up -d`).

## Environment / config gotchas

- Runtime config is loaded from a gitignored `.env` file via `spring-dotenv`. Copy `.env.example` to `.env` to run locally. `.env` currently holds real credentials — never commit it or echo its contents.
- `DATABASE_URL` must be a **URL without the `jdbc:` prefix**, e.g. `postgresql://user:pass@localhost:5432/travelplanner`. `DataSourceConfig` rewrites it to `jdbc:postgresql://...` and parses username/password/host out of the URL. Passing `jdbc:...` or separate host/port fields won't work as expected.
- `spring.jpa.hibernate.ddl-auto=update`; tables are auto-created on first run against Postgres.
- Test config lives in `src/test/resources/application.properties` (H2, `create-drop`, separate JWT secret).

## Architecture

- Standard layering under `src/main/java/com/example/travelplanner`: `controller/`, `service/`, `repository/`, `model/`, plus `security/` (JWT) and `config/`.
- Frontend is **plain HTML/JS/CSS served statically** from `src/main/resources/static` — no build step. i18n strings are in `static/js/i18n/{en,pl,es}.json`.
- Auth is stateless JWT. `SecurityConfig` permits `/api/auth/**` and the static pages; everything else requires a token.
- `DataInitializer` (a `CommandLineRunner`) seeds countries from `countries.json` and, on first run / stale data, fetches currency rates from the NBP API (`api.nbp.pl`) — this needs network on startup.
- `AppConfig` builds a custom SSL context merging the JVM cacerts with `nbp-truststore.jks` so `RestTemplate` can call NBP.
- Email is behind the `MailService` interface: `SmtpMailService` activates when `spring.mail.host` is set; otherwise `LoggingMailService` writes reset links to the app log.

## Spec-driven workflow

Features are specified in `specs/NNN-slug.md` **before** implementation. Copy `specs/000-template.md`, get review, then implement. See `specs/README.md` for statuses (Proposed / Approved / In progress / Done) and numbering. Add new features this way rather than coding directly.
