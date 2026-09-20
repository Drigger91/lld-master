# Test cases — Stack Overflow

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register and log in | `stackOverflow.registerUser(new User(1, "Prasuk", "password123", email, 0))`; `stackOverflow.loginUser("Prasuk", "password123")` | Returns the registered `User` |
| H2 ✅ | Post a question with tags | `stackOverflow.postQuestion(new Question(1, "What is Java?", body, user1, new ArrayList<>(), new ArrayList<>(), List.of(new Tag(1,"java"), new Tag(2,"programming")), 0))` | Question stored and indexed under both tag names |
| H3 ✅ | Post an answer | `stackOverflow.postAnswer(new Answer(1, body, user2, question1, new ArrayList<>(), 0))` | `question1.getAnswers()` contains the answer |
| H4 ✅ | Upvote a question | `stackOverflow.voteQuestion(question1, 1)` | `question1.getVoteCount()` is 1 and `user1.getReputation()` is 1 |
| H5 ✅ | Search by keyword | `stackOverflow.searchQuestions("Java")` | Returns `question1` |
| H6 ✅ | Browse by tag | `stackOverflow.getQuestionsByTag("java")` | Returns `question1` |
| H7 ✅ | Questions by user | `stackOverflow.getQuestionsByUser(user1)` | Returns `question1`; `getQuestionsByUser(user2)` is empty |
| H8 ⬜ | Vote an answer | `stackOverflow.voteAnswer(answer1, 1)` | `answer1.getVoteCount()` is 1; `user2.getReputation()` is 1 |
| H9 ⬜ | Downvote | `voteQuestion(question1, -1)` after H4 | Vote count and author reputation return to 0 |
| H10 ⬜ | Search matches body | `searchQuestions("explain")` | Returns `question1` (body contains the word) |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Tag with no questions | `getQuestionsByTag("golang")` | Empty list |
| E2 ⬜ | Search is case-sensitive | `searchQuestions("java")` when title is `What is Java?` and body has no lowercase `java` | Empty list |
| E3 ⬜ | Same tag on multiple questions | Post two questions both tagged `java` | `getQuestionsByTag("java")` returns both, in posting order |
| E4 ⬜ | Reputation can go negative | Downvote a new user's question | `getReputation()` is -1 (no floor enforced) |
| E5 ⬜ | Returned tag list is live | Modify the list from `getQuestionsByTag("java")` | The index is changed too — discuss returning a copy |
| E6 ⬜ | Singleton | `StackOverflow.getInstance()` twice | Same instance |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Wrong password | `loginUser("Prasuk", "nope")` | Returns `null` |
| X2 ⬜ | Unknown username | `loginUser("ghost", "x")` | Returns `null` |
| X3 ⬜ | Duplicate user id | `registerUser` twice with id 1 | Second silently replaces the first — discuss rejecting duplicates |
| X4 ⬜ | Duplicate username, different id | Register two users named `Prasuk` | Both stored; `loginUser` returns whichever is iterated first — discuss enforcing unique usernames |
| X5 ⬜ | Vote value other than ±1 | `voteQuestion(question1, 100)` | Currently applied verbatim; a hardened version validates the value |
| X6 ⬜ | Same user votes twice / votes on own post | `voteQuestion(question1, 1)` twice by the same voter | Both counted (no per-user vote tracking) |
| X7 ⬜ | Post comment | `postComment(comment)` | No effect (`postComment` is an unimplemented stub) |
| X8 ⬜ | Answer to a question whose `answers` list is immutable | `postAnswer` on a question created with `List.of()` for answers | `UnsupportedOperationException` — pass a mutable list or let the service own the list |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent upvotes on one question | 100 threads each call `voteQuestion(q, 1)` | Vote count is 100 and author reputation is 100 (both updates are synchronized on their object) |
| C2 ⬜ | Concurrent posts with the same tag | Two threads post questions tagged `java` at once | `computeIfAbsent` creates one list, but concurrent `add` on the `ArrayList` is unsafe — discuss `CopyOnWriteArrayList` or a synchronized list |

## Interviewer follow-ups / extensions
- How would you prevent double voting? — Keep a `Set<(userId, postId)>` of votes (or a `Vote` entity) and reject or toggle repeat votes.
- How would you add accepted answers? — `Question.acceptedAnswer` settable only by the question's author, with a reputation bonus for the answerer.
- How would you implement comments? — Give `Comment` a constructor, an id and a target (question or answer id), and let `postComment` append to the right list.
- How would you make search scalable? — Tokenise title/body into an inverted index, or delegate to a search engine; make matching case-insensitive.
- How would you decompose the Singleton facade? — Separate `UserService`, `QuestionService`, `VoteService` behind repositories so each can be tested and scaled independently.
