# HANDOFF — books-api tutoring sessions

## Who / how

mpott is a 7-year **JavaScript** frontend dev learning Java + Spring Boot, aiming to become a Java dev. **Read the "How we work together" section of CLAUDE.md first.** Key points:

- Knows TS *typing* only. Has never used classes, constructors, `super`, inheritance, or interfaces. Backend knowledge is the MVC shape and nothing deeper. Don't reach for TS-class or OOP analogies; use plain JS, Express `req`/`res`, and `fetch`.
- Label every new thing as **Java concept** (learn), **Spring concept** (learn), or **library name** (one sentence, move on). The "so many files / so much soup" frustration came from treating those as one pile.
- Stay project-based. When a Java concept bites, take a ~15-minute detour on it using their own code, then return to the feature. They chose this over pausing to study Java.
- Explain first with snippets in chat, they type it in, they run every command (Windows, `.\gradlew`; Claude's WSL has no JDK). Claude reads files and runs `git status`/`git log`/`git diff`; ask before `docker exec` on their behalf.
- **"Try it blind" works well.** GET-by-id and DELETE were both done from hints only and landed on the right shape. Default to hints-only for anything that reuses a taught pattern; show code only for genuinely new Spring/JPA concepts.
- Postman for manual requests. They rejected `.http` files and saved collections. Throwaway Postman tabs are fine. PowerShell mangles `curl` JSON quoting; don't suggest curl there.
- Review flow: they say "review it," Claude runs `git diff`, points out problems with the why, they fix, Claude re-diffs and confirms. Works well; keep it.

## Done — steps 1, 2, 3 ✅

**Step 1:** `GET /books` hardcoded. Commit `078f746`.

**Step 2:** `Book` entity + JPA repository on Postgres. Commits `e4373b9` → `3798f0a`.

**Step 3: full CRUD.**
- `POST /books` → 201 + `Location` header. Commit `51d5b6e`.
- `GET /books/{id}` → 200, or throws `BookNotFoundException` (surfaces as a 500 until step 5). Commit `5ecf49b`.
- `PUT /books/{id}` → 200 with updated `BookDto`. `@Transactional` service method, `findById` + `orElseThrow`, setters, **no explicit `save`** (dirty checking). Reuses `CreateBookRequest`. Commit `178aafe`.
- `DELETE /books/{id}` → 204 `ResponseEntity<Void>`. Service is `void`, guard-first `existsById` → throw, then `deleteById`. **Reviewed and clean, but uncommitted at end of 2026-09-17 session.** First thing next session: `git status`; if still uncommitted, have them commit it as `Add DELETE /books/{id}`.

All verified in Postman, including not-found paths on PUT and DELETE.

Current source tree (all under `src/main/java/com/books/books_api/`):

- `config/BookSeeder.java` — `@Component implements CommandLineRunner`, count-guarded, seeds Hobbit + Dune. Dev-only; profile-gate or replace at step 6.
- `controller/BookController.java` — `getBooks()`, `getBook(id)`, `createBook(request)`, `updateBook(id, request)`, `deleteBook(id)`. Uses `org.springframework.web.bind.annotation.*` wildcard import.
- `dto/BookDto.java` — `record BookDto(Long id, String title, String author, int publishedYear)`
- `dto/CreateBookRequest.java` — `record CreateBookRequest(String title, String author, int publishedYear)`; used for both POST and PUT.
- `entity/BookEntity.java` — `@Entity @Table("books")`, Lombok `@Getter @Setter @NoArgsConstructor`, `IDENTITY` id. Fields: `id`, `title`, `author` (String), `publishedYear`.
- `exception/BookNotFoundException.java` — `extends RuntimeException`, one `(String message)` constructor calling `super(message)`.
- `repository/BookRepository.java` — `extends JpaRepository<BookEntity, Long>`, empty.
- `service/BookService.java` — `getAllBooks()`, `createBook()`, `getBook()`, `updateBook()` (`@Transactional`, Spring's not Jakarta's), `deleteBook()`. Private `toDto()` / `toEntity()`.

`application.yaml`: `ddl-auto: update`, `show-sql: true`. `.gitattributes` has `* text=auto` (expect CRLF warnings from `git diff`; harmless).

Session recap artifacts (concepts, vocabulary, Q&A, command crib), one per session:
- Day one / step 1: https://claude.ai/artifact/6csVopQN4W9m1Pkr6Gw8Du
- Day two / step 2: https://claude.ai/artifact/RejnfYAoRgey7asF3yNpvK
- Day three / step 3 first half (2026-09-16): not requested.
- Day four / step 3 second half (2026-09-17): not requested.

If they ask for one, reuse the same design system and section order as the first two.

## Concepts already taught (don't re-explain unless asked)

**From steps 1–2:** Gradle wrapper/BOM; Docker's role; component scanning; package=dir, class=file; record vs class; DTO-as-firewall; JPA vs Hibernate vs Spring Data; full request path controller → service → repository → Hibernate → JDBC → Postgres; entity as table declaration; `ddl-auto` values; why records can't be entities; Lombok annotations; repository interface pattern; beans + constructor injection; streams and method references; naming strategy; `Long` vs `long`; `CommandLineRunner`; compose container naming.

**From step 3 first half (2026-09-16):**
- **Which annotation lives in which package**, as a table keyed on "who talks to this class": controller ↔ HTTP (`@*Mapping`, `@RequestBody`, `@PathVariable`, `ResponseEntity`); service ↔ Java code (`@Service`, `@Transactional`); entity ↔ schema; dto ↔ JSON, no annotations. **This bit again on 2026-09-17**: `@DeleteMapping` ended up on the service method (silently ignored by Spring, not a compile error). Re-showed the table. Tell: any `org.springframework.web.*` import in the service is wrong. Watch for a third occurrence; if it happens, do a proper detour on annotations-as-metadata and who reads them.
- `ResponseEntity` is Spring's HTTP-response class, unrelated to JPA entities. 405 = path matched, verb didn't; 404 = no path.
- Separate `CreateBookRequest` vs reusing `BookDto`. `save()` return value is the truth (id assigned by `IDENTITY`).
- `Optional`: `.map()` / `.orElseThrow()`. Trap hit: `.stream()` on an `Optional` yields a `Stream`, which has no `orElseThrow`.
- **Constructors (Java concept, taught properly):** same name as class, no return type; free no-arg one disappears when you write any constructor; overloads by parameter list; Lombok constructor annotations; records get a canonical constructor; who calls each constructor in this codebase.
- **`extends` / `super`:** inherits fields and methods, not constructors.
- **Interfaces:** a named set of promises; `implements` keeps them; `BookRepository` is the "framework implements it for you" case. Why interface over class extension. Told honestly that every `extends`/`implements` so far was dictated by framework or language, not chosen.
- Checked vs unchecked exceptions: only the one-liner. Not yet taught properly.
- Career reassurance: 6–12 months part-time to junior-ready is realistic.

**From step 3 second half (2026-09-17):**
- **`@Transactional` + dirty checking (Spring/JPA concept, taught at first-pass depth):** transaction = all-or-nothing bracket; Spring opens at method start, commits on return, rolls back on throw. Repository methods self-wrap in mini transactions, which is why GET/POST didn't need it. **Managed entity** = Hibernate holds a snapshot after load (analogy: `structuredClone` right after fetch). **Dirty checking** = at commit, Hibernate diffs managed entities against snapshots and emits `UPDATE` for changed ones, so no `save()` call. **Detached** = transaction over, Hibernate stopped watching; setters change a plain object and nothing hits the DB, silently. Vocabulary held: persistence context, managed/detached, flush. Go deeper only when a bug forces it. Two `@Transactional` imports exist; use Spring's.
- Why skip `save()` on update even though it works: idiomatic JPA, and forces understanding the transaction, which step 4 needs.
- When to split `UpdateBookRequest` from `CreateBookRequest`: only once fields diverge.
- `void` (Java concept): must be written; Java requires a declared return type.
- `ResponseEntity<Void>` (generics, light touch): capital-`V` `Void` is a placeholder class because a generic slot needs a class name. Connected to `List<BookDto>` and `ResponseEntity<BookDto>` they'd already used. Generics not yet taught properly.
- Spring Data signals failure by throwing, not by return values; `deleteById` is `void` because there's nothing new to report. `deleteById` on a missing id is a silent no-op in current Spring Data (used to throw), hence the `existsById` guard is the app's job.
- `throw` and `return` are keywords, not functions; `throw(new X())` compiles but the parens are noise. `if`/`while` parens are grammar. Guard-first / early-throw over if/else. `} else {` on one line.
- Idiom: don't bind a boolean to a variable that's read once; inline it in the `if`.

## Next — step 4: `Author` entity, one-to-many

Not yet started or discussed beyond a one-line heads-up that it brings a second entity, a second repository, and `@ManyToOne` as the first relationship annotation. Plan:

1. **Confirm DELETE is committed** (see above).
2. **`AuthorEntity`** — `id`, `name`. Show code; it's a repeat of `BookEntity` so offer blind first. `AuthorRepository` blind.
3. **The relationship (JPA concept, teach properly).** Replace `BookEntity.author` (String) with `@ManyToOne` `AuthorEntity author` + `@JoinColumn(name = "author_id")`. Explain: FK lives on the many side; `@ManyToOne` on `Book` is the owning side and is the one that matters; `@OneToMany(mappedBy = "author")` on `Author` is optional, read-only from JPA's view, and a source of bugs (bidirectional sync, lazy loading, `toString` recursion via Lombok). **Recommend unidirectional `@ManyToOne` only** to start; add `@OneToMany` only when a feature needs `author.getBooks()`. `FetchType.LAZY` and the `LazyInitializationException` will bite once a DTO mapping touches `author` outside a transaction; that's the planned detour for persistence context round two, and where `@Transactional(readOnly = true)` on reads gets introduced.
4. **Schema change.** `ddl-auto: update` will add `author_id` but won't drop the old `author` varchar column. Either drop the volume / `docker compose down -v` and re-seed, or accept the stale column until Flyway at step 6. Discuss briefly; recommend the wipe. **Seeder must change** to create authors first, then books pointing at them.
5. **DTOs.** `BookDto` grows an author representation; recommend a nested `AuthorDto(Long id, String name)` record inside `BookDto` rather than flattening. `CreateBookRequest` takes `Long authorId`, and `createBook`/`updateBook` load the author via `authorRepository.findById(...).orElseThrow(new AuthorNotFoundException)`. Second exception class: blind, it's a copy of the first.
6. **Endpoints.** `POST /authors`, `GET /authors`, `GET /authors/{id}`. Offer blind, it's the Book pattern again. `GET /authors/{id}/books` is the first custom repository method: `List<BookEntity> findByAuthorId(Long authorId)` on `BookRepository`. Spring Data derived queries are a Spring concept worth a short explanation (method name is parsed into a query).
7. Commit, then step 5 (validation + `@ControllerAdvice`), which finally turns `BookNotFoundException` into a real 404.

Java concepts likely to bite in step 4 and worth a detour when they do: **`static`** (still unseen; nested record inside `BookDto` or a `static` factory may surface it), **generics** properly (two repositories with different type args makes it concrete), **checked exceptions** if anything forces a `throws`, and **object identity / `equals`** if bidirectional mapping or `Set` shows up.

## Small open items

- `compose.yaml`: `postgres:latest` unpinned, Initializr placeholder creds — revisit at step 6.
- `BookSeeder` runs in every environment; profile-gate or remove at step 6. Must be updated in step 4 for authors.
- `show-sql: true` fine for learning; mention `logging.level.org.hibernate.SQL` when it comes up.
- Windows Firewall inbound for Java denied — must allow **Private** networks when the React Native client tests from a phone.
- `TestcontainersConfiguration` uses raw `PostgreSQLContainer` (missing `<?>`) — teachable at step 7.
- `BookNotFoundException` takes a message string; at step 5 consider changing it to take the `Long id` and build the message itself, so the 404 handler can use the id. Same shape for `AuthorNotFoundException` when it appears in step 4; maybe do the id-constructor version from the start for that one and retrofit Book.
- `BookController` uses a wildcard import for `org.springframework.web.bind.annotation.*`. Fine, but it hides which annotations are in play; not worth changing unless it confuses them.
