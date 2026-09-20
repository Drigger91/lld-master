# Stack Overflow

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, Facade (service class over entities) | tag inverted index, voting with reputation, per-object synchronization, search |

## Problem statement
Design a simplified Stack Overflow. Users register and log in, post questions tagged with topics, answer other users' questions, and vote questions and answers up or down. Voting affects the author's reputation. Users must be able to search questions by keyword, browse questions by tag, and list the questions posted by a given user.

## Functional requirements
1. Register a user (id, username, password, email, reputation) and log in with username + password.
2. Post a question with a title, body, author and a list of tags.
3. Post an answer to a question; the answer is attached to that question.
4. Vote on a question or an answer with a signed value (e.g. `+1` / `-1`); the vote is added to the post's vote count and to its author's reputation.
5. Search questions whose title or body contains a query string.
6. List questions carrying a given tag name.
7. List questions posted by a given user.
8. Comments on questions and answers are part of the model but posting them is not implemented (`postComment` is a stub).

## Non-functional requirements & constraints
- In-memory only; a `StackOverflow` Singleton holds users, questions and a tag → questions index.
- Passwords are stored and compared in plain text (interview simplification).
- Vote count and reputation updates are guarded by `synchronized` on the question/answer/user object; maps are `ConcurrentHashMap`, but the per-tag lists are plain `ArrayList`s and `getQuestionsByTag` returns the live list.
- Search is a linear, case-sensitive `contains` scan over all questions.
- No vote-per-user tracking: the same user can vote repeatedly and can vote on their own post.
- `User`, `Question` and `Answer` use reference identity (no `equals`/`hashCode` overrides).

## Clarifying questions to ask
- Can a user vote more than once on the same post? — Not prevented in this version; discuss tracking (user, post) pairs.
- How does reputation work? — Reputation moves by exactly the vote value applied to the author's post.
- Can a question have an accepted answer? — Not modelled; extension.
- Is search full-text or substring? — Substring on title/body.
- Are tags free-form or pre-registered? — Free-form `Tag` objects supplied with the question; the index is keyed by tag name.
- Should comments be supported now? — The `Comment` type exists but `postComment` is left unimplemented.

## Core entities
- `StackOverflow` — lazy Singleton service/facade: `registerUser`, `loginUser`, `postQuestion`, `postAnswer`, `postComment` (stub), `voteQuestion`, `voteAnswer`, `searchQuestions`, `getQuestionsByTag`, `getQuestionsByUser`.
- `User` — id, username, password, email and mutable `reputation`.
- `Question` — id, title, body, author, lists of answers/comments/tags, mutable `voteCount`.
- `Answer` — id, body, author, owning `Question`, comments, mutable `voteCount`.
- `Tag` — id and name; the name is the index key.
- `Comment` — placeholder with id, text, author, createdAt (no constructor or accessors yet).

## Design hints
- **Facade + Singleton**: a single `StackOverflow` service owns the maps and exposes use-case methods, keeping entities as plain data holders — simple and what interviewers expect first; then discuss splitting into `UserService`, `QuestionService`, `VoteService`.
- **Tag inverted index**: `Map<tagName, List<Question>>` built in `postQuestion` makes tag lookup O(1); the cost is keeping it consistent if questions are ever edited or deleted.
- **Voting as a side effect on two objects**: the post's count and the author's reputation are updated under separate locks, so the two are not atomically consistent — a good point to raise before the interviewer does.
- **Entities carry their relationships** (`Question.answers`, `Answer.question`): convenient for traversal, but bidirectional references plus caller-supplied mutable lists make invariants easy to break; an alternative is to keep only ids and resolve through the service.
- Common mistakes: returning the internal list from `getQuestionsByTag`; relying on reference equality for `getQuestionsByUser`; not validating that a username is unique on `registerUser`; allowing a `vote` value other than ±1.

## Run
mvn -q -pl problems/stack-overflow compile exec:java
