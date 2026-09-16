# HANDOFF — books-api tutoring sessions

## Who / how

mpott is a 7-year **JavaScript** frontend dev learning Java + Spring Boot, aiming to become a Java dev. **Read the "How we work together" section of CLAUDE.md first; it was rewritten on 2026-09-16.** Key corrections from that session:

- Knows TS *typing* only. Has never used classes, constructors, `super`, inheritance, or interfaces. Backend knowledge is the MVC shape and nothing deeper. Don't reach for TS-class or OOP analogies; use plain JS, Express `req`/`res`, and `fetch`.
- Label every new thing as **Java concept** (learn), **Spring concept** (learn), or **library name** (one sentence, move on). The "so many files / so much soup" frustration came from treating those as one pile.
- Stay project-based. When a Java concept bites, take a ~15-minute detour on it using their own code, then return to the feature. They chose this over pausing to study Java; it's how their bootcamp worked.
- Explain first with snippets in chat, they type it in, they run every command (Windows, `.\gradlew`; Claude's WSL has no JDK). Claude reads files and runs `git status`/`git log`; ask before `docker exec` on their behalf.
- Offer "try it without seeing code" for edits that reuse a taught pattern. They did GET-by-id this way and got the controller right first try.
- Postman for manual requests. They rejected `.http` files and saved collections as needless upkeep given Testcontainers is coming in step 7. Throwaway Postman tabs are fine. PowerShell mangles `curl` JSON quoting; don't suggest curl there.

## Done — steps 1, 2, and half of 3 ✅

**Step 1:** `GET /books` hardcoded. Commit `078f746`.

**Step 2:** `Book` entity + JPA repository on Postgres. Commits `e4373b9` → `3798f0a`.

**Step 3 (in progress):**
- `POST /books` → 201 + `Location` header. Commit `51d5b6e`.
- `GET /books/{id}` → 200, or throws `BookNotFoundException` (currently surfaces as a 500 until step 5). Commit `5ecf49b`.

Current source tree (all under `src/main/java/com/books/books_api/`):

- `config/BookSeeder.java` — `@Component implements CommandLineRunner`, count-guarded, seeds Hobbit + Dune. Dev-only; profile-gate or replace at step 6.
- `controller/BookController.java` — `getBooks()`, `getBook(@PathVariable Long id)`, `createBook(@RequestBody CreateBookRequest)` returning `ResponseEntity.created(location).body(dto)`.
- `dto/BookDto.java` — `record BookDto(Long id, String title, String author, int publishedYear)`
- `dto/CreateBookRequest.java` — `record CreateBookRequest(String title, String author, int publishedYear)`; no id, server owns ids.
- `entity/BookEntity.java` — `@Entity @Table("books")`, Lombok getters/setters/no-arg, `IDENTITY` id.
- `exception/BookNotFoundException.java` — `extends RuntimeException`, one `(String message)` constructor calling `super(message)`.
- `repository/BookRepository.java` — `extends JpaRepository<BookEntity, Long>`, empty.
- `service/BookService.java` — `getAllBooks()`, `createBook()` via private `toEntity()`, `getBook(id)` as `findById(id).map(this::toDto).orElseThrow(...)`. Private `toDto()`. **No `@Transactional` yet; introduce at PUT.**

`application.yaml`: `ddl-auto: update`, `show-sql: true`. `.gitattributes` has `* text=auto`.

Session recap artifacts (concepts, vocabulary, Q&A, command crib), one per session:
- Day one / step 1: https://claude.ai/artifact/6csVopQN4W9m1Pkr6Gw8Du
- Day two / step 2: https://claude.ai/artifact/RejnfYAoRgey7asF3yNpvK
- Day three / step 3 first half: not requested this session.

They usually ask for one at the end of a session; reuse the same design system and section order.

## Concepts already taught (don't re-explain unless asked)

**From steps 1–2:** Gradle wrapper/BOM; Docker's role; component scanning; package=dir, class=file; record vs class; DTO-as-firewall; JPA vs Hibernate vs Spring Data; full request path controller → service → repository → Hibernate → JDBC → Postgres; entity as table declaration; `ddl-auto` values; why records can't be entities; Lombok annotations; repository interface pattern; beans + constructor injection; streams and method references; naming strategy; `Long` vs `long`; `CommandLineRunner`; compose container naming.

**From step 3 / 2026-09-16:**
- **Which annotation lives in which package**, as a table keyed on "who talks to this class": controller ↔ HTTP (`@*Mapping`, `@RequestBody`, `@PathVariable`, `ResponseEntity`); service ↔ Java code (`@Service`, `@Transactional`); entity ↔ schema; dto ↔ JSON, no annotations. Each layer holds one field for the layer below and never imports across. They needed this after putting `@PostMapping` in the service and swapping the two `createBook` bodies between files. Re-show the table if placement confusion recurs.
- `ResponseEntity` is Spring's HTTP-response class, imported not written, unrelated to JPA entities despite the name. 405 = path matched, verb didn't; 404 = no path.
- Separate `CreateBookRequest` vs reusing `BookDto`, and why. `save()` return value is the truth (id assigned by `IDENTITY`).
- `Optional`: `.map()` / `.orElseThrow()`. Trap hit: `.stream()` on an `Optional` yields a `Stream`, which has no `orElseThrow`.
- **Constructors (Java concept, taught properly):** same name as class, no return type; free no-arg one disappears when you write any constructor; overloads by parameter list; `@NoArgsConstructor` / `@RequiredArgsConstructor` (final fields) as Lombok-written constructors; records get a canonical constructor from their header; who calls each constructor in this codebase (Spring, Jackson, Hibernate, their own code). They initially said `CreateBookRequest` had zero constructors; corrected.
- **`extends` / `super`:** inherits fields and methods, **not constructors**, hence the hand-written exception constructor with `super(message)`.
- **Interfaces:** a named set of promises; `implements` keeps them; interface-`extends`-interface just merges promise lists; `BookRepository` is the unusual "framework implements it for you" case; `BookSeeder implements CommandLineRunner` and `List<BookDto>` are the normal cases. Why interface over class extension: single inheritance, no implementation baggage, swappability (Mockito fake in step 7). Told honestly that a class would usually "just work" in a solo project and that every `extends`/`implements` in this project so far was dictated by framework or language, not chosen. The bones for choosing will come from step 7 tests, not explanation.
- Checked vs unchecked exceptions: only the one-liner "extend `RuntimeException`, no declaring/catching required." Not yet taught properly.
- Career reassurance given: 6–12 months part-time to junior-ready is realistic; the "must learn" pile is short (classes/constructors, interfaces, static, exceptions, generics, Optional/streams/records) and the "library names" pile is never learned in depth by anyone.

## Next — finish step 3

1. **`PUT /books/{id}`** — show code, this is a Spring concept. Load with `findById` + `orElseThrow`, mutate via setters, no explicit `save`. **Introduce `@Transactional` on the service method** and dirty checking: within a transaction Hibernate flushes changed managed entities at commit. Persistence context, managed vs detached, at first-pass level only. Label it Spring/JPA concept. Inbound DTO: reuse `CreateBookRequest` or add `UpdateBookRequest`; recommend reuse for now and say why a separate one shows up once fields diverge. Return 200 with the updated `BookDto`.
2. **`DELETE /books/{id}`** — offer blind attempt. Hints: `@DeleteMapping("/{id}")`, `ResponseEntity<Void>` + `ResponseEntity.noContent().build()` for 204, `existsById` guard throwing `BookNotFoundException`, `deleteById`.
3. Verify each in Postman. Suggest they write a `PUT` against the Neuromancer row they created, then delete it.
4. Commit, then step 4 (Author entity, one-to-many).

Java concepts likely to bite next and worth a detour when they do: `void` return + `ResponseEntity<Void>` generics, `static` (they haven't seen it yet), checked exceptions if anything forces a `throws`.

## Small open items

- `compose.yaml`: `postgres:latest` unpinned, Initializr placeholder creds — revisit at step 6.
- `BookSeeder` runs in every environment; profile-gate or remove at step 6.
- `show-sql: true` fine for learning; mention `logging.level.org.hibernate.SQL` when it comes up.
- Windows Firewall inbound for Java denied — must allow **Private** networks when the React Native client tests from a phone.
- `TestcontainersConfiguration` uses raw `PostgreSQLContainer` (missing `<?>`) — teachable at step 7.
- `BookNotFoundException` takes a message string; at step 5 consider changing it to take the `Long id` and build the message itself, so the 404 handler can use the id.
