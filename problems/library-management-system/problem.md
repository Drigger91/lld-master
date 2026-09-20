# Library Management System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Singleton, Facade | catalog + member registry, borrow limit, availability flag, synchronized borrow/return, keyword search |

## Problem statement
Design a library's book-lending system. The library keeps a catalog of books and a register of members. A member can borrow an available book, up to a maximum number of books at a time, and return it later. Staff should be able to search the catalog by title or author.

## Functional requirements
1. Add and remove books in the catalog, identified by ISBN.
2. Register and unregister members, identified by member id.
3. Borrow a book: only if the member and book exist, the book is available, and the member holds fewer than `MAX_BOOKS_PER_MEMBER` (5) books.
4. Return a book: mark it available again and remove it from the member's borrowed list.
5. Search the catalog by a keyword matched against title or author.
6. Report the outcome of borrow/return operations (printed messages in the reference code).

## Non-functional requirements
- `LibraryManager` is a singleton; catalog and members live in `ConcurrentHashMap`s in memory.
- `borrowBook` and `returnBook` are `synchronized` so the availability check and the flag flip are atomic.
- Search is case-sensitive substring matching.
- A `LOAN_DURATION_DAYS` (14) constant exists but due dates and fines are not implemented.
- Operations report through `System.out` rather than return values or exceptions.

## Constraints
- `0 <= member.borrowedBooks.size() <= MAX_BOOKS_PER_MEMBER` (5) at all times; the limit is one library-wide constant, not per member type.
- One physical copy per ISBN: `Book.available` is a single boolean, so a book is either on the shelf or held by exactly one member, never both.
- Books are keyed by ISBN and members by member id, both caller-supplied strings; adding an existing key silently replaces the entry.
- Catalog of hundreds to a few thousand books and hundreds of members; `searchBooks` is a linear scan over the catalog with no index.
- Single JVM, no real clock: nothing in the model reads the current date, so borrowing and returning are instantaneous flag flips with no loan duration in practice.
- Removal is unguarded: `removeBook` and `unregisterMember` do not check for outstanding loans, so a borrowed book can leave the catalog while still in a member's list.

## Clarifying questions to ask
- Multiple copies of the same book? — No, one copy per ISBN.
- Is there a borrow limit? — Yes, 5 books per member.
- Due dates, fines, reservations/holds? — Out of scope for v1 (follow-ups).
- Can any member return any book? — The reference code does not verify the returning member actually borrowed it (a gap to call out).
- Search semantics? — Substring in title or author, case-sensitive.
- How are failures reported? — Messages to stdout; a production design would return a result or throw.

## Core entities
- `LibraryManager` — singleton facade: `addBook`, `removeBook`, `getBook`, `registerMember`, `unregisterMember`, `getMember`, `borrowBook`, `returnBook`, `searchBooks`.
- `Book` — isbn, title, author, publication year, mutable `available` flag.
- `Member` — member id, name, contact info, list of currently borrowed `Book`s.

## Design hints
- **Facade over two registries**: `LibraryManager` coordinates `Book` and `Member` so neither knows about the other's rules. The borrow limit belongs to the manager (a library policy), the borrowed list to the member.
- **Availability is the invariant to protect**: check-and-set on `book.available` plus `member.borrowBook` must be atomic, hence `synchronized`. An interviewer will ask how you would scale that (per-book lock, or a `BookCopy` with an `AtomicReference<Status>`).
- **Separate the book from the copy**: real libraries have `Book` (metadata) and `BookItem`/`Copy` (barcode, status). The single boolean here is the simplest possible model — know why you'd split it.
- **Return should be validated**: `returnBook` currently sets available even if the member never borrowed it. Track a `Loan` (member, copy, due date) and return by loan id.
- Common mistakes: singleton state leaking between tests; printing instead of returning results; forgetting the borrow limit; case-sensitive search when users expect case-insensitive; `List.remove(Object)` relying on `equals` (works here only because the same `Book` instance is used).

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :library-management-system compile exec:java` |
