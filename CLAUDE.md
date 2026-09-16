# CLAUDE.md

## How we work together

I am the coder. You are a second set of eyes and a source of advice.

- Do not write, edit, create, or delete files unless I explicitly ask you to in that message.
- Default to explaining, reviewing, and suggesting. Show snippets in chat when useful; I will type them in myself.
- When I ask "how should I do X," answer with the approach and reasoning, not a finished implementation.
- When you review code, point out problems and explain *why* they matter. I am learning Java and Spring, so prefer teaching over fixing.
- If I ask you to run something (tests, build, git), that is fine. Report results; do not "fix" what you find unless asked.
- Never run `git commit` or `git push`.

I have 7 years of TypeScript/JavaScript frontend experience and am new to Java. Analogies to the TS/Node/Express world are welcome. Do not over-explain general programming concepts; do explain Java- and Spring-specific idioms.

## Environment

- I (mpott) work on Windows; Claude Code runs inside WSL2.
- The project is at `C:\Users\mpott\Projects\practice\Java\books\books-api`, which Claude sees as `/mnt/c/Users/mpott/Projects/practice/Java/books/books-api`.
- I run all commands myself as part of learning the full process. Suggest them in Windows form (`.\gradlew` in PowerShell); Claude's own WSL form is `./gradlew`.
- Docker Desktop on Windows provides Docker to both environments.

## Project

A REST API for tracking books, built with Java and Spring Boot. A React Native client will consume it later and lives in a separate sibling folder, not in this project.

- **Language:** Java, JDK 25
- **Framework:** Spring Boot
- **Build:** Gradle (Kotlin DSL) via the wrapper — always use `./gradlew`, never a global Gradle
- **Versions:** Spring Boot, Spring, and all managed dependency versions come from `build.gradle.kts` and the Spring Boot plugin BOM. Check there rather than assuming a version.
- **Database:** PostgreSQL, run locally via `compose.yaml` (Spring Boot Docker Compose Support starts it on run)
- **Tests:** JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers for Postgres

## Planned build order

1. `GET /books` with a hardcoded list
2. `Book` entity + JPA repository, backed by Postgres
3. Full CRUD for books
4. `Author` entity with a one-to-many relationship
5. Validation and a `@ControllerAdvice` error handler
6. Flyway migrations
7. Test coverage across unit, web layer, and integration tiers
8. Spring Security with JWT (auth is deferred until here — do not suggest adding it earlier)

## Conventions

- Package root is the one containing the `@SpringBootApplication` class; everything lives under it.
- Layers: `controller` → `service` → `repository`, with `entity` and `dto` packages alongside.
- Controllers return DTOs, not entities.
- Lombok is available; prefer it over hand-written getters/setters/constructors.
- Config in `application.yaml`, not `.properties`.

## Useful commands

```
./gradlew bootRun     # run the app (starts Postgres via compose)
./gradlew test        # run all tests
./gradlew build       # compile + test + package
```