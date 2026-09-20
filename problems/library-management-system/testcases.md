# Test cases — Library Management System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Add books to the catalog | `libraryManager.addBook(new Book("ISBN1","Book 1","Author 1",2020))` ×3 | `libraryManager.getBook("ISBN1")` returns the book; `isAvailable()` is true |
| H2 ✅ | Register members | `libraryManager.registerMember(new Member("M1","John Doe","john@example.com"))` | `libraryManager.getMember("M1")` returns the member with an empty borrowed list |
| H3 ✅ | Borrow an available book | `libraryManager.borrowBook("M1","ISBN1")` | Prints `Book borrowed: Book 1 by John Doe`; `getBook("ISBN1").isAvailable()` is false; `getMember("M1").getBorrowedBooks()` contains it |
| H4 ✅ | Different members borrow different books | `borrowBook("M2","ISBN2")` | Succeeds independently of M1's loan |
| H5 ✅ | Return a book | `libraryManager.returnBook("M1","ISBN1")` | Prints `Book returned: Book 1 by John Doe`; book is available again; M1's borrowed list is empty |
| H6 ✅ | Search by keyword | `libraryManager.searchBooks("Book")` | Returns all three books (order unspecified — map iteration) |
| H7 ⬜ | Search by author | `searchBooks("Author 2")` | Returns only `Book 2` |
| H8 ⬜ | Remove a book / unregister a member | `removeBook("ISBN3")`, `unregisterMember("M2")` | `getBook("ISBN3")` and `getMember("M2")` return null |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Borrow limit reached | Member borrows 5 books, then `borrowBook(m, "ISBN6")` | Prints `Member … has reached the maximum number of borrowed books.`; sixth book stays available |
| E2 ⬜ | Borrow again after a return | Borrow, return, borrow the same ISBN by the same member | Second borrow succeeds |
| E3 ⬜ | Search with no matches | `searchBooks("zzz")` | Empty list |
| E4 ⬜ | Search is case-sensitive | `searchBooks("book")` | Empty list (titles are `Book N`) — call out as a usability gap |
| E5 ⬜ | Re-adding an ISBN | `addBook(new Book("ISBN1", …))` twice | Second put overwrites the first, including its availability flag |
| E6 ⬜ | Singleton identity | `LibraryManager.getInstance() == LibraryManager.getInstance()` | true |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Borrow a book that is already lent | `borrowBook("M1","ISBN1")` then `borrowBook("M2","ISBN1")` | Second prints `Book or member not found, or book is not available.`; M2's list unchanged |
| X2 ⬜ | Borrow with unknown member | `borrowBook("M9","ISBN1")` | Prints the not-found message; book stays available |
| X3 ⬜ | Borrow with unknown ISBN | `borrowBook("M1","ISBN9")` | Prints the not-found message |
| X4 ⬜ | Return with unknown member or ISBN | `returnBook("M9","ISBN1")` | Prints `Book or member not found.`; no state change |
| X5 ⬜ | Return a book the member never borrowed | M1 borrows ISBN1; `returnBook("M2","ISBN1")` | Reference code marks the book available and prints `Book returned … by Jane Smith` while M1 still holds it — should be rejected |
| X6 ⬜ | Remove a book that is on loan | Borrow ISBN1, then `removeBook("ISBN1")` | Catalog entry vanishes but the member's list still references it; later `returnBook` reports not found — a consistency gap to discuss |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two members race for the last copy | Two threads call `borrowBook(mX, "ISBN1")` simultaneously | Exactly one succeeds (`borrowBook` is synchronized); the other sees "not available" |
| C2 ⬜ | Concurrent borrow and search | One thread borrows while another calls `searchBooks` | No `ConcurrentModificationException`; search may or may not reflect the in-flight borrow |

## Interviewer follow-ups / extensions
- How would you support multiple copies of a book? — Split `Book` (metadata) from `BookItem` (barcode, status); borrow picks any AVAILABLE item.
- How would you add due dates and fines? — Introduce `Loan(member, item, issuedOn, dueOn)`; return computes overdue days × rate using `LOAN_DURATION_DAYS`.
- How would you add holds/reservations on a lent book? — Per-book FIFO queue of member ids; on return, notify the head of the queue (Observer).
- How would you make search richer? — Index by lower-cased title/author tokens, or a `SearchStrategy` (title, author, ISBN, year).
- How would you replace printed messages? — Return a `BorrowResult` enum or throw domain exceptions (`BookNotAvailableException`, `BorrowLimitExceededException`).
