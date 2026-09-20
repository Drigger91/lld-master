# Test cases — Document Sharing Service

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Create users | `new User()` twice | Ids 1 and 2; each has an empty document list |
| H2 ✅ | Create document | `user1.createDocumentForUser("Test User 1")` | Document id 1 appears in `user1.getDocuments()`; user1 is `OWNER` |
| H3 ✅ | Owner reads own document | `document1.getContent(user1.getUserId())` | Returns the content |
| H4 ✅ | Grant READ | `user1.giveAccessForDocument(1, 2, AccessLevel.READ)`; `document1.getContent(2)` | Returns true; user2 can read the content |
| H5 ✅ | Upgrade to EDIT and edit | `user1.giveAccessForDocument(1, 2, EDIT)`; `document1.editDocument(2, "Edited by user2")` | Prints "Document edited successfully!"; both users read the new content |
| H6 ✅ | Revoke | `user1.revokeAccessForDocument(1, 2)` | Returns true; `document1.getAccessLevel(2)` is `NO_ACCESS` |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | Downgrade to NO_ACCESS via grant | `user1.giveAccessForDocument(1, 2, NO_ACCESS)`; `getContent(2)` | Read denied even though the user still has an ACL entry |
| E2 ✅ | EDIT implies READ | After H5, `document1.getContent(2)` | Content returned |
| E3 ⬜ | Re-granting the same level | `giveAccessForDocument(1, 2, READ)` twice | Idempotent; still READ |
| E4 ⬜ | Multiple documents per owner | `user1.createDocumentForUser(...)` twice; share only the second | Access on one document does not leak to the other |
| E5 ⬜ | Owner demotes themselves | `user1.giveAccessForDocument(1, 1, READ)` | Current code allows it; owner can no longer edit (design question) |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Read without access | `document1.getContent(user2.getUserId())` before any grant | "You don't have access to read this document" |
| X2 ✅ | Edit without access | `document1.editDocument(2, "Test")` before any grant | "You don't have access to edit this document"; content unchanged |
| X3 ✅ | Edit with READ only | After H4, `document1.editDocument(2, "Test")` | Edit denied; content unchanged |
| X4 ✅ | Non-owner tries to share | `user2.giveAccessForDocument(1, 3, READ)` | "No document found with given Id", returns false; user3 still cannot read |
| X5 ✅ | Edit after revoke | After H6, `document1.editDocument(2, "Should fail")` | Edit denied |
| X6 ✅ | Share unknown document id | `user1.giveAccessForDocument(42, 2, READ)` | Returns false |
| X7 ⬜ | Revoke unknown document id | `user1.revokeAccessForDocument(42, 2)` | Returns false |

## Interviewer follow-ups / extensions
- How would you add a `COMMENT` level between READ and EDIT? — Insert it in `AccessLevel` with priority between the two; checks stay as `>=` comparisons.
- How would you support sharing with a group or "anyone with the link"? — Resolve principal -> effective level (max over user, groups, public) before the check.
- How would you keep the denial out of the return value? — Throw `AccessDeniedException` or return `Optional<String>`.
- How would you find all documents shared with a user? — Central `DocumentRepository` plus a reverse index `userId -> documentIds`.
- Concurrency? — Make the ACL map a `ConcurrentHashMap` and guard content edits with a lock or versioned writes.
- Audit trail? — Record every grant/revoke/edit as an event with actor and timestamp.
