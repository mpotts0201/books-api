# HANDOFF — books-api tutoring sessions

## Who / how

mpott is a 7-year **JavaScript** frontend dev learning Java + Spring Boot, aiming to become a Java dev. **Read the "How we work together" section of CLAUDE.md first.** Key points:

- Knows TS *typing* only. Has never used classes, constructors, `super`, inheritance, or interfaces. Backend knowledge is the MVC shape and nothing deeper. Don't reach for TS-class or OOP analogies; use plain JS, Express `req`/`res`, and `fetch`.
- Label every new thing as **Java concept** (learn), **Spring concept** (learn), or **library name** (one sentence, move on). The "so many files / so much soup" frustration came from treating those as one pile.
- Stay project-based. When a Java concept bites, take a ~15-minute detour on it using their own code, then return to the feature. They chose this over pausing to study Java.
- Explain first with snippets in chat, they type it in, they run every command (Windows, `.\gradlew`; Claude's WSL has no JDK). Claude reads files and runs `git status`/`git log`/`git diff`; ask before `docker exec` on their behalf.
- **"Try it blind" works well for repeats of a taught pattern.** GET-by-id, DELETE, `AuthorEntity`, `AuthorRepository`, the seeder, and the entire Author controller/service stack were all done from hints only and landed right. **It works poorly for a genuinely new concept.** The relationship mapping in `BookService` took about six rounds of compile errors from hints; showing the `createBook`/`toEntity` snippet unblocked it immediately. Show code sooner when the concept is new, hints when it's a repeat.
- They ask about errors by line number ("error on line 72"). Read the file with line numbers (`cat -n`) before answering; don't guess.
- They say "throwing" for compile errors. Corrected once on 2026-09-19 (compile error = TS red squiggle, throw = runtime). Gently reinforce if it recurs.
- Postman for manual requests. They rejected `.http` files and saved collections. Throwaway Postman tabs are fine. PowerShell mangles `curl` JSON quoting; don't suggest curl there.
- Review flow: they say "review it," Claude runs `git diff` plus cats untracked files, points out problems with the why, they fix, Claude re-diffs and confirms. Works well; keep it.

## Done — steps 1, 2, 3 ✅

**Step 1:** `GET /books` hardcoded. Commit `078f746`.

**Step 2:** `Book` entity + JPA repository on Postgres. Commits `e4373b9` → `3798f0a`.

**Step 3: full CRUD.**
- `POST /books` → 201 + `Location` header. Commit `51d5b6e`.
- `GET /books/{id}` → 200, or throws `BookNotFoundException` (surfaces as a 500 until step 5). Commit `5ecf49b`.
- `PUT /books/{id}` → 200 with updated `BookDto`. `@Transactional` service method, `findById` + `orElseThrow`, setters, **no explicit `save`** (dirty checking). Commit `178aafe`.
- `DELETE /books/{id}` → 204 `ResponseEntity<Void>`. Service is `void`, guard-first `existsById` → throw, then `deleteById`. Commit `7df65dc` ("Delete is in").

## In progress — step 4: `Author` entity, one-to-many

Worked on 2026-09-18/19. **Code written and compiling (per the user), but NOT runtime-verified, and review items below are still open.** The user said they would commit at end of session. **First thing next session: `git status` and `git log`**, then `git diff`/read the files to see which review items were addressed before the commit.

What exists now (all under `src/main/java/com/books/books_api/`):

- `config/BookSeeder.java` — injects both repositories. Builds Tolkien, Herbert, Gibson; builds Hobbit, Dune, Neuromancer with `setAuthor(entity)`; `authorRepository.saveAll` **then** `bookRepository.saveAll`. Count-guarded on books. Reviewed clean.
- `controller/BookController.java` — unchanged from step 3.
- `controller/AuthorController.java` — `/authors`: `GET`, `GET /{id}`, `POST` (201 + `Location`), `PUT /{id}`, `DELETE /{id}`. PUT and DELETE were built despite a suggestion to skip them; see open item 1.
- `dto/AuthorDto.java` — `record AuthorDto(Long id, String name)`
- `dto/BookDto.java` — `record BookDto(Long id, String title, AuthorDto author, int publishedYear)`. **Nested**, chosen by the user over flat.
- `dto/CreateAuthorRequest.java` — `record CreateAuthorRequest(String name)`; used for POST and PUT.
- `dto/CreateBookRequest.java` — `record CreateBookRequest(String title, Long authorId, int publishedYear)`; used for POST and PUT.
- `entity/AuthorEntity.java` — `@Entity @Table("authors")`, Lombok trio, `IDENTITY` id, `name`. **No `@OneToMany`** (deliberately unidirectional).
- `entity/BookEntity.java` — `author` is now `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id") AuthorEntity author`.
- `exception/AuthorNotFoundException.java` — copy of the Book one, `(String message)` constructor. The `Long id` constructor upgrade was offered and not taken; do both together at step 5.
- `exception/BookNotFoundException.java` — unchanged.
- `repository/AuthorRepository.java` — `extends JpaRepository<AuthorEntity, Long>`, empty.
- `repository/BookRepository.java` — unchanged, empty.
- `service/AuthorService.java` — `getAllAuthors`, `getAuthor`, `createAuthor`, `updateAuthor` (`@Transactional`, dirty checking), `deleteAuthor` (`existsById` guard). Private `toDto`/`toEntity`. Imports are clean (no `org.springframework.web.*`).
- `service/BookService.java` — injects `AuthorRepository` directly (no service-to-service call). `createBook` and `updateBook` do `authorRepository.findById(request.authorId()).orElseThrow(AuthorNotFoundException)`, then pass the entity on. `toEntity(request, author)` takes the author as a second parameter. `toDto` builds a nested `new AuthorDto(...)`.

Database was wiped with `docker compose down -v` on 2026-09-19 so the old `author` varchar column is gone. `application.yaml` unchanged: `ddl-auto: update`, `show-sql: true`. `.gitattributes` has `* text=auto` (expect CRLF warnings from `git diff`; harmless).

### Open review items from the 2026-09-19 review (check whether each was fixed)

1. **`DELETE /authors/{id}` will fail with a foreign key violation** when the author has books (`DataIntegrityViolationException` → 500). User was asked to trigger it on purpose in Postman, not fix it yet. Three policies were laid out: **refuse** (recommended; service checks and throws, becomes 409 Conflict at step 5), cascade, or orphan/set-null. The "refuse" check needs `bookRepository.existsByAuthorId(id)`, a derived query, which is the next topic anyway.
2. **`updateBook` looks up the author before the book.** Should be book first: the URL names the book, so a bad book id should win over a bad author id. Flagged twice, not yet changed as of the last diff.
3. **`new AuthorDto(id, name)` is duplicated** in `BookService.toDto` and `AuthorService.toDto`. Deliberately left. The idiomatic fix is a `static` factory (`AuthorDto.from(AuthorEntity)`), which is the planned hook for the **`static` detour**. Do it when there are ten quiet minutes.
4. **Formatting nits:** missing space after the comma inside `new AuthorDto(...)` in `BookService.toDto`; `AuthorController.getAuthors()` squeezed onto one line; two blank lines after `package` in `AuthorService`. Suggested Ctrl+Alt+L in IntelliJ.

### Runtime verification still owed (nothing in step 4 has been run and reported)

- `GET /books` → three books, each with a nested `author` object.
- `POST /authors` → 201 + `Location`; then `POST /books` with that new author id → 201.
- `POST /books` with `"authorId": 999` → 500, console shows "Author not found: 999".
- `PUT /books/1` with a different `authorId` → console `UPDATE` includes `author_id` (dirty checking on a relationship).
- `DELETE /authors/1` → the FK failure from open item 1.
- Seeder SQL order in the startup log: three `insert into authors`, then three `insert into books`.
- **Startup warning containing `open-in-view`.** User was asked to look for it and *not* fix it.
- **Count the `select` statements on `GET /books`.** User was asked to guess why there's more than one before it's explained. This is the N+1 problem caused by LAZY: one select for books, then one per distinct author when `toDto` touches `getAuthor().getName()`.

## Concepts already taught (don't re-explain unless asked)

**From steps 1–2:** Gradle wrapper/BOM; Docker's role; component scanning; package=dir, class=file; record vs class; DTO-as-firewall; JPA vs Hibernate vs Spring Data; full request path controller → service → repository → Hibernate → JDBC → Postgres; entity as table declaration; `ddl-auto` values; why records can't be entities; Lombok annotations; repository interface pattern; beans + constructor injection; streams and method references; naming strategy; `Long` vs `long`; `CommandLineRunner`; compose container naming.

**From step 3 first half (2026-09-16):**
- **Which annotation lives in which package**, as a table keyed on "who talks to this class": controller ↔ HTTP (`@*Mapping`, `@RequestBody`, `@PathVariable`, `ResponseEntity`); service ↔ Java code (`@Service`, `@Transactional`); entity ↔ schema; dto ↔ JSON, no annotations. Bit twice (2026-09-16 and 09-17). **Held on 2026-09-19**: the whole blind Author stack had clean imports. Tell: any `org.springframework.web.*` import in a service is wrong.
- `ResponseEntity` is Spring's HTTP-response class, unrelated to JPA entities. 405 = path matched, verb didn't; 404 = no path.
- Separate `CreateBookRequest` vs reusing `BookDto`. `save()` return value is the truth (id assigned by `IDENTITY`).
- `Optional`: `.map()` / `.orElseThrow()`. Trap hit: `.stream()` on an `Optional` yields a `Stream`, which has no `orElseThrow`.
- **Constructors (Java concept, taught properly):** same name as class, no return type; free no-arg one disappears when you write any constructor; overloads by parameter list; Lombok constructor annotations; records get a canonical constructor; who calls each constructor in this codebase.
- **`extends` / `super`:** inherits fields and methods, not constructors.
- **Interfaces:** a named set of promises; `implements` keeps them; `BookRepository` is the "framework implements it for you" case. Why interface over class extension. Told honestly that every `extends`/`implements` so far was dictated by framework or language, not chosen.
- Career reassurance: 6–12 months part-time to junior-ready is realistic.

**From step 3 second half (2026-09-17):**
- **`@Transactional` + dirty checking (Spring/JPA concept, first-pass depth):** transaction = all-or-nothing bracket; Spring opens at method start, commits on return, rolls back on throw. Repository methods self-wrap in mini transactions, which is why GET/POST didn't need it. **Managed entity** = Hibernate holds a snapshot after load (analogy: `structuredClone` right after fetch). **Dirty checking** = at commit, Hibernate diffs managed entities against snapshots and emits `UPDATE` for changed ones, so no `save()` call. **Detached** = transaction over, Hibernate stopped watching. Vocabulary held: persistence context, managed/detached, flush. Go deeper only when a bug forces it. Two `@Transactional` imports exist; use Spring's.
- Why skip `save()` on update even though it works. When to split `UpdateBookRequest` from `CreateBookRequest`: only once fields diverge.
- `void` (Java concept): must be written; Java requires a declared return type.
- `ResponseEntity<Void>`: capital-`V` `Void` is a placeholder class because a generic slot needs a class name.
- Spring Data signals failure by throwing, not by return values. `deleteById` on a missing id is a silent no-op in current Spring Data, hence the `existsById` guard.
- `throw` and `return` are keywords, not functions. Guard-first / early-throw over if/else. `} else {` on one line.
- Idiom: don't bind a boolean to a variable that's read once; inline it in the `if`.

**From step 4 (2026-09-18/19):**
- **Annotations attach to the next declaration** regardless of blank lines; they're metadata stapled to what follows, and nothing happens unless something reads them. Tied back to the ignored `@DeleteMapping` on a service method.
- **Generics (Java concept, now taught properly from the reader's side):** `JpaRepository<T, ID>` is written once with placeholders; `<AuthorEntity, Long>` fills them in, so `findById` returns `Optional<AuthorEntity>`. "A function that takes types as arguments." Connected to `List<BookDto>`, `Optional<BookEntity>`, `ResponseEntity<Void>`, and TS's `Array<string>`/`Promise<User>`. Compiler enforces it. Reading generics is the skill; writing your own generic class is far off.
- **Foreign key lives on the many side**, because a column holds one value per row. User didn't get there unprompted; they reasoned about parent/child and many-to-many instead. Many-to-many acknowledged as the honest real-world model, deferred.
- **`@ManyToOne` (JPA concept, taught properly):** Java side holds the whole object, not the id; `@ManyToOne` = owning side, the side whose table has the column; `@JoinColumn(name = "author_id")` names the column. **Unidirectional only**: `@OneToMany(mappedBy=...)` adds no columns, is read-only to JPA, and opens bidirectional-sync, lazy-collection, and Lombok `toString` recursion bugs. Add it only when a feature needs `author.getBooks()`.
- **`FetchType.LAZY`:** default for `@ManyToOne` is EAGER; LAZY loads a placeholder and queries on first touch; only works while Hibernate is watching (inside a transaction). `LazyInitializationException` named as a planned detour. **Not yet explained:** the proxy mechanism, N+1, or why `open-in-view` is currently masking the exception in web requests.
- **Save order expresses dependency.** A `new` entity that was never saved is "transient" (id null); Hibernate can't write a FK to it. Authors saved before books. Also noticed from their own seeder: `setAuthor(tolkien)` before `saveAll(authors)` works, because it's one object and the id is written into it on save. Reference, not copy.
- **`ddl-auto: update` only adds, never drops.** Hence `docker compose down -v`. "Is this destructive?" reflex before any `-v`.
- **The id-vs-object idea (this was the hard one; bit three times):** in the database a book stores a number, in Java it stores the object, and Hibernate translates. You never hand Hibernate the number. Showed up as `entity.getAuthorId()` (no such getter: Lombok makes one getter per *field*, and `author_id` is a column name), then `setAuthor(request.authorId())` (Long into an AuthorEntity slot), then `existsById` in `createBook` (checks but leaves you holding nothing; `findById` + `orElseThrow` checks *and* fetches). Summed up as a three-row conversion table: request `Long` → entity via `findById`; `AuthorEntity` → `AuthorDto` via `new`; `BookEntity` → `BookDto` via `new`. Nothing crosses automatically.
- **Nominal vs structural typing**, without the jargon: `AuthorEntity` and `AuthorDto` have identical fields and are still unrelated types. JS cares about shape, Java cares about the declared class name.
- **Reading a nested-call type error:** check the outer call's expected type against the inner call's return type; the underline often sits on the inner one and misleads.
- **Two ways a method ends (Java concept):** return or throw, never both. Return type only describes the normal exit. Exception climbs service → controller → Spring's catch-all → 500. `@ControllerAdvice` at step 5 = Express error middleware `(err, req, res, next)`. Service never knows about HTTP status codes.
- **No union types in Java.** `Optional<T>` for "absence is normal flow," exceptions for "the request is invalid." Repositories return `Optional` because they can't know which; the service decides with `orElseThrow`, and that's a real decision. **Over-applied once:** user changed `createBook` to return `Optional<BookDto>` after this explanation; corrected.
- **Checked vs unchecked exceptions, now taught at first-pass depth:** checked must be declared with `throws` and handled by every caller; unchecked (`extends RuntimeException`) climb freely. Spring is almost all unchecked by design: throw deep, handle centrally.
- **Compile error vs throwing** (vocabulary).
- **`Long` vs `long` in request DTOs:** `Long` so a missing field is `null` (detectable by validation) rather than `0`. `publishedYear` as `int` has the same flaw; left for step 5 as a before/after example.
- **JSON key = record component name verbatim.** camelCase in Java and JSON; snake_case only in `@JoinColumn`/SQL. User wrote `author_id` in a record once; corrected.
- **Layering is about direction, not pairing.** A service may use any repository. Service-to-service calls only when there's shared business logic worth centralizing; "find by id or throw" isn't. Mapping helpers (`toDto`/`toEntity`) stay dumb: no repositories, no throwing.
- **Check the URL's resource first, then the body's references**, so error messages are predictable.
- Don't build a class (`AuthorService`) before something needs it.

## Next — finish step 4

1. **`git status` / `git log` / read the files.** See what was committed and which of the four open review items were addressed.
2. **Get the runtime verification results** (list above). Nothing in step 4 has been reported as run. If anything fails, fix that first.
3. **LAZY / N+1 / open-in-view detour (persistence context round two).** Driven by what they saw: the select count on `GET /books` and the `open-in-view` startup warning. Explain: the lazy proxy; why each `getAuthor().getName()` in `toDto` fires a query (N+1); that `spring.jpa.open-in-view=true` (Spring Boot default) keeps the Hibernate session open for the whole HTTP request, which is the only reason `getBook`/`getAllBooks` don't throw `LazyInitializationException` despite having no `@Transactional`. Recommended outcome: set `open-in-view: false` in `application.yaml`, watch it break, then fix with `@Transactional(readOnly = true)` on the read methods. Mention `JOIN FETCH` / `@EntityGraph` as the N+1 fix, but only implement if they want to; naming it may be enough for now.
4. **`GET /authors/{id}/books` — first custom repository method.** `List<BookEntity> findByAuthorId(Long authorId)` on `BookRepository`. **Spring Data derived queries (Spring concept, teach properly):** the method name is parsed into a query; `findBy` + property path + optional operators. `AuthorId` traverses `author.id`. Decide which controller/service owns the endpoint (URL says `AuthorController`; data says `BookService`). Should 404 when the author doesn't exist rather than return an empty list.
5. **Fix `deleteAuthor` using the same tool:** `bookRepository.existsByAuthorId(id)` → throw. Needs a third exception class (something like `AuthorHasBooksException`); becomes a 409 at step 5. Confirm the user wants the "refuse" policy first; it was recommended, not agreed.
6. **`static` detour + `AuthorDto.from(AuthorEntity)`** if time allows (open review item 3).
7. Commit, then **step 5** (validation + `@ControllerAdvice`), which turns `BookNotFoundException`/`AuthorNotFoundException` into real 404s, the delete conflict into a 409, and a missing `authorId` into a 400.

Java concepts still likely to bite: **`static`** (still unseen; planned via item 6), **`equals`/object identity** if a `Set` or bidirectional mapping shows up, **writing** a generic method or class (reading is done).

## Session recap artifacts

Concepts, vocabulary, Q&A, command crib; one per session:
- Day one / step 1: https://claude.ai/artifact/6csVopQN4W9m1Pkr6Gw8Du
- Day two / step 2: https://claude.ai/artifact/RejnfYAoRgey7asF3yNpvK
- Day three / step 3 first half (2026-09-16): not requested.
- Day four / step 3 second half (2026-09-17): not requested.
- Day five / step 4 first half (2026-09-18/19): not requested.

If they ask for one, reuse the same design system and section order as the first two.

## Small open items

- `compose.yaml`: `postgres:latest` unpinned, Initializr placeholder creds — revisit at step 6.
- `BookSeeder` runs in every environment; profile-gate or remove at step 6.
- `show-sql: true` fine for learning; mention `logging.level.org.hibernate.SQL` when it comes up.
- Windows Firewall inbound for Java denied — must allow **Private** networks when the React Native client tests from a phone.
- `TestcontainersConfiguration` uses raw `PostgreSQLContainer` (missing `<?>`) — teachable at step 7, and easier now that generics have been taught.
- **Exception constructors:** both `BookNotFoundException` and `AuthorNotFoundException` take a `String message`. At step 5, change both to take the `Long id` and build the message in the constructor via `super("... not found: " + id)`, so the 404 handler can use the id and the wording lives in one place.
- **`publishedYear` is `int` in `CreateBookRequest`**, so a missing year silently becomes `0`. Change to `Integer` at step 5 alongside `@NotNull`.
- **`authorId` null case** in `createBook`/`updateBook`: `findById(null)` throws `IllegalArgumentException` → 500. Deliberately unhandled until step 5 validation.
- `BookController` and `AuthorController` use a wildcard import for `org.springframework.web.bind.annotation.*`. Fine; not worth changing unless it confuses them.
- The existing tests (if any reference `BookDto`/`CreateBookRequest` with the old `String author` shape) may no longer compile. `.\gradlew build` hasn't been run this session, only `compileJava`. Check at the start of next session.
