# HANDOFF — books-api tutoring sessions

## Who / how

mpott is a 7-year TS/JS frontend dev learning Java + Spring Boot, aiming to become a Java dev.
Claude is the tutor, per CLAUDE.md: **explain first with snippets in chat, mpott types all code and runs all commands** (Windows terminal, `.\gradlew` forms — Claude's WSL has no JDK, so `!`-prefix Gradle commands fail there). Teach with TS/Node analogies; explain Java/Spring idioms, skip general programming concepts.

## Done — step 1 of the CLAUDE.md build order ✅

`GET /books` returns a hardcoded list, verified end to end with `bootRun` + curl.

- `src/main/java/com/books/books_api/dto/BookDto.java` — `record BookDto(Long id, String title, String author, int publishedYear)`
- `src/main/java/com/books/books_api/controller/BookController.java` — `@RestController` + `@RequestMapping("/books")` + `@GetMapping`, returns `List.of(...)` with two books
- Scaffold verified healthy: `.\gradlew build` passes (JDK 25 + Spring Boot 4.1.1, Boot 4.x starter names)
- Git initialized inside `books-api` (one repo per deployable; the RN client will get its own). Scaffold commit + step 1 commit suggested — verify both happened.
- Docker Desktop confirmed working; Postgres runs via compose, pulled on first `bootRun`.

Session recap artifact (concepts, vocabulary, Q&A, command crib):
https://claude.ai/code/artifact/2d7fd84d-781b-4f11-846c-0a386861ff28

## Concepts already taught (don't re-explain unless asked)

Gradle task graph / wrapper / BOM / no-install-step; Docker's role (Spring only expects an endpoint; Docker runs the server; the four layers server→database→schema→rows); component scanning + auto-configuration; package=dir, class=file; record vs class; DTO-as-firewall + mapping in the service layer; JPA spec vs Hibernate impl vs Spring Data JPA; Tomcat ≈ app.listen; `Started BooksApiApplication` = readiness (the 80% Gradle bar is normal for bootRun); terminal for build/test, IDE for run/debug.

## Next — step 2: Book entity + JPA repository

Planned teaching sequence:
1. `entity` package, `BookEntity` with `@Entity`, `@Id`, `@GeneratedValue` — introduce Lombok here (CLAUDE.md prefers it; records don't work for JPA entities, explain why)
2. Turn on `spring.jpa.hibernate.ddl-auto` (dev-only; currently unset = no schema created) and watch Hibernate create the table
3. `repository` package, `BookRepository extends JpaRepository` — generated queries
4. `service` package, `BookService` — this is where entity→DTO mapping lives (promised as "the decision point / firewall" — connect back to that)
5. Controller switches from `List.of(...)` to the service; constructor injection idiom
6. Seed a couple of rows, restart, show persistence across restarts

## Small open items

- `application.yaml` vs CLAUDE.md's stated `.yml` — mpott to pick one and make file + doc agree
- `compose.yaml`: `postgres:latest` unpinned, generic creds (`mydatabase`/`myuser`) — fine for now, revisit
- Windows Firewall inbound for Java was denied — harmless for localhost; must allow **Private** networks when the React Native client tests from a phone
- `TestcontainersConfiguration` uses raw `PostgreSQLContainer` (missing `<?>`) — minor, teachable when tests come up (step 7)
