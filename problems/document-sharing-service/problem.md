# Document Sharing Service

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Access-control list, ordered enum (priority levels) | authorization checks, owner-only sharing, enum ordering |

## Problem statement
Design a minimal document-sharing service like a stripped-down Google Docs. Users create documents. A document's owner can share it with other users at a given access level (read or edit), change that level, or revoke it. Reading and editing a document must be checked against the caller's access level.

## Functional requirements
1. Create users with generated ids.
2. A user creates a document with initial content; the creator becomes its `OWNER`.
3. The owner grants another user an `AccessLevel` (`READ`, `EDIT`, `NO_ACCESS`) on one of their documents, and can change it later.
4. The owner revokes a user's access entirely.
5. Reading returns the content only if the caller's level is at least `READ`; otherwise a denial message.
6. Editing replaces the content only if the caller's level is at least `EDIT`; otherwise a denial message.
7. Only the owner can share: `giveAccessForDocument` / `revokeAccessForDocument` look the document up in the caller's own documents and fail if it is not there.

## Non-functional requirements
- In-memory; documents are held in the owning `User`'s list, no central repository.
- Ids are static counters in `Environment`.
- Not thread-safe; single-threaded demo.

## Constraints
- Exactly four access levels with fixed priorities: `NO_ACCESS(0) < READ(1) < EDIT(2) < OWNER(3)` (`AccessLevel`); every check compares `getPriority()`, so "can read" is `level >= READ` and "can edit" is `level >= EDIT`.
- Exactly one owner per document, the creator, assigned in the `Document` constructor; ownership is never transferred.
- A document holds at most one `AccessLevel` per user id; granting again replaces the previous level, and a missing entry reads as `NO_ACCESS`.
- User ids and document ids are sequential `int`s starting at 1, global across the process.
- Content is a single `String` replaced wholesale on edit; no versions, no size limit.
- Small scale: a handful of users each owning a few documents; owner-side lookups are linear scans over the owner's list.
- Single JVM, no persistence, no authentication: callers pass a `userId` and it is trusted; no groups, links or public sharing.

## Clarifying questions to ask
- Is access hierarchical (does EDIT imply READ)? — Yes, via priority comparison.
- Can an editor share the document? — No, only the owner (the document must be in the sharer's own list).
- Can ownership be transferred or can there be several owners? — Not in this version.
- Is there a difference between `NO_ACCESS` and no entry? — Behaviourally no; both fail the `>= READ` check.
- Versioning / history of edits? — Out of scope; content is overwritten.

## Core entities
- `User` — id, list of owned `Document`s; `createDocumentForUser`, `giveAccessForDocument`, `revokeAccessForDocument`.
- `Document` — id, content, `Map<userId, AccessLevel>`; `getContent(userId)`, `editDocument(userId, content)`, `editAccessForDocument`, `revokeAccessForDocument`, `getAccessLevel`.
- `AccessLevel` — enum with a priority: `NO_ACCESS(0) < READ(1) < EDIT(2) < OWNER(3)`.
- `Environment` — static id counters.

## Design hints
- **ACL on the document.** The document is the thing being protected, so it owns the `userId -> AccessLevel` map and performs its own checks; `User` only decides *who may change the ACL* (owner-only, because it searches its own list).
- **Ordered enum instead of booleans.** One `priority` int gives `canRead = level >= READ`, `canEdit = level >= EDIT` and makes adding `COMMENT` between READ and EDIT a one-line change.
- **Check by level, not by presence.** A user downgraded to `NO_ACCESS` still has a map entry; `getContent` must compare priority, not `containsKey`.
- Trade-off an interviewer will probe: returning a denial *string* from `getContent` conflates data and errors; an exception or `Optional` is cleaner.
- Common mistakes: letting any user with a reference to the `Document` call `editAccessForDocument` (the model does not stop this; only `User` guards it); global mutable counters.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :document-sharing-service compile exec:java` |
