# HANDOFF — books-api tutoring sessions

## Who / how

mpott is a 7-year TS/JS frontend dev learning Java + Spring Boot, aiming to become a Java dev.
Claude is the tutor, per CLAUDE.md: **explain first with snippets in chat, mpott types all code and runs all commands** (Windows terminal, `.\gradlew` forms — Claude's WSL has no JDK, so `!`-prefix Gradle commands fail there). Teach with TS/Node analogies; explain Java/Spring idioms, skip general programming concepts.

mpott has started asking to attempt changes **without seeing code first** (did the controller→service switch this way). Offer that option for routine edits; show code for new concepts.

Docker is reachable from Claude's WSL side (`docker ps`, `docker exec`), but mpott prefers to run DB inspection commands themself — ask before running `docker exec` on their behalf.

## Done — steps 1 and 2 of the CLAUDE.md build order ✅

**Step 1:** `GET /books` hardcoded. Committed `078f746`.

**Step 2:** `Book` entity + JPA repository, backed by Postgres. Verified end to end: table created by Hibernate, seeded rows persist across restarts, `curl localhost:8080/books` returns both books with ids 1 and 2. Commits `e4373b9` → `3798f0a`.

Current source tree (all under `src/main/java/com/books/books_api/`):

- `config/BookSeeder.java` — `@Component implements CommandLineRunner`, count-guarded, seeds Hobbit + Dune via `saveAll`. Dev-only; to be replaced by a Flyway migration or profile-gated in step 6.
- `controller/BookController.java` — constructor-injected `BookService`, `getBooks()` returns `bookService.getAllBooks()`
- `dto/BookDto.java` — `record BookDto(Long id, String title, String author, int publishedYear)`
- `entity/BookEntity.java` — `@Entity @Table("books")`, Lombok `@Getter @Setter @NoArgsConstructor`, `@Id @GeneratedValue(IDENTITY) Long id`
- `repository/BookRepository.java` — `extends JpaRepository<BookEntity, Long>`, empty
- `service/BookService.java` — `@Service @RequiredArgsConstructor`, `getAllBooks()` streams entities through private `toDto()`. No `@Transactional` yet (promised for step 3).

`application.yaml` has `spring.jpa.hibernate.ddl-auto: update` and `spring.jpa.show-sql: true`.

Housekeeping done this session: `.gitattributes` now has `* text=auto` (IntelliJ on Windows had rewritten files as CRLF, producing phantom diffs).

Session recap artifacts (concepts, vocabulary, Q&A, command crib), same format, one per session:
- Day one / step 1: https://claude.ai/artifact/6csVopQN4W9m1Pkr6Gw8Du
- Day two / step 2: https://claude.ai/artifact/RejnfYAoRgey7asF3yNpvK

mpott asks for one of these at the end of each session; reuse the same design system and section order.

## Concepts already taught (don't re-explain unless asked)

**From step 1:** Gradle task graph / wrapper / BOM / no-install-step; Docker's role (Spring only expects an endpoint; the four layers server→database→schema→rows); component scanning + auto-configuration; package=dir, class=file; record vs class; DTO-as-firewall; JPA spec vs Hibernate impl vs Spring Data JPA; Tomcat ≈ app.listen; `Started BooksApiApplication` = readiness; terminal for build/test, IDE for run/debug.

**From step 2:**
- Full request path: controller → service → repository → Hibernate → JDBC (driver = `org.postgresql`, pool = Hikari) → Postgres. mpott can recite this; the correction they needed twice was "the ORM is Hibernate, a library underneath; no file you write *is* the ORM."
- Entity = Java-side table declaration; source of truth only while `ddl-auto: update`, flips to Flyway migrations + `validate` in step 6. Why Flyway (history, prod safety, team merges) — compared to Prisma Migrate.
- `ddl-auto` values: none / update / create-drop / validate.
- Why records can't be entities (no-arg constructor, mutable fields, reflection). Lombok `@Getter/@Setter/@NoArgsConstructor/@RequiredArgsConstructor` — what each generates; annotation order doesn't matter.
- Repository = interface you declare, Spring Data generates the impl, Hibernate does the labor. "Declare an interface, framework implements it" pattern flagged as recurring (HTTP clients, Mockito later).
- Beans + DI as one idea: Spring makes one instance, hands it to constructors by type. Constructor injection idiom; avoid field `@Autowired` (untestable). Why `BookService.getAllBooks()` on the class fails (not static).
- Streams: `.stream().map(this::toDto).toList()` ≈ `.map()`; method references.
- Hibernate naming strategy (camelCase → snake_case); primitive `int` → `not null`, `String` → nullable `varchar(255)`; `@Column(length/nullable)` for later.
- `Long` vs `long`, boxed vs primitive, why ids are boxed `Long` (null before save) and 64-bit (range); UUID as the alternative.
- `CommandLineRunner`, idempotent seeders, `String... args` varargs.
- Compose container naming `<project>-<service>-<index>`; Docker Desktop's grouping is a UI illusion; `docker compose exec postgres ...` as the name-independent form. Creds come from `compose.yaml` env vars (`myuser` / `secret` / `mydatabase`).
- Career context given: this layering is the standard enterprise Spring shape; real services add package-by-feature, Actuator/observability, springdoc, RFC 9457 Problem Details, OAuth2 resource server with an IdP rather than hand-rolled JWT, Dockerfile/K8s, `@Transactional`/N+1. Reassured that nobody holds all the plumbing in their head; Initializr, spring.io guides, Baeldung, and team templates are normal.

## Next — step 3: full CRUD for books

Planned teaching sequence:
1. `POST /books` — introduce `@RequestBody`, `@PostMapping`, `ResponseEntity` + `201 Created`. Inbound DTO question: reuse `BookDto` with a null id, or a separate `CreateBookRequest` record? Recommend a separate request record (id shouldn't be client-settable) and explain the trade-off. Service gets `toEntity()` as the inbound half of the firewall.
2. `GET /books/{id}` — `@PathVariable`, `Optional` from `findById`, what to do on miss (throw a custom `BookNotFoundException` now; the `@ControllerAdvice` that turns it into a 404 body is step 5, so for now expect a 500 and explain why that's temporary).
3. `PUT /books/{id}` — load, mutate via setters, save. **Introduce `@Transactional` here** with dirty checking: inside a transaction Hibernate flushes changed entities without an explicit `save`. Explain the persistence context / managed vs detached entities at a first-pass level.
4. `DELETE /books/{id}` — `204 No Content`, `existsById` guard.
5. Verify each with curl (`-X POST -H "Content-Type: application/json" -d ...`) or suggest IntelliJ's HTTP client / a `.http` file.

Offer the "try it without code first" mode for steps 2 and 4 once POST has been shown.

## Small open items

- `compose.yaml`: `postgres:latest` unpinned, Initializr placeholder creds — fine for now, revisit at step 6.
- `BookSeeder` runs in every environment; profile-gate (`@Profile("dev")`) or remove at step 6.
- `show-sql: true` is fine for learning; mention `logging.level.org.hibernate.SQL` as the production-grade alternative when it comes up.
- Windows Firewall inbound for Java was denied — harmless for localhost; must allow **Private** networks when the React Native client tests from a phone.
- `TestcontainersConfiguration` uses raw `PostgreSQLContainer` (missing `<?>`) — minor, teachable at step 7.
