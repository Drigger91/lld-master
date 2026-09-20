# Test cases — LinkedIn

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register and log in | `service.registerUser(user1)`; `service.loginUser("john@example.com", "password")` | Returns `user1` (same id); name printed as "John Doe" |
| H2 ✅ | Update profile | `profile.setHeadline("Software Engineer")`; `user.setProfile(profile)`; `service.updateUserProfile(user)` | `service.searchUsers("Software")` finds the user via headline |
| H3 ✅ | Send connection request | `service.sendConnectionRequest(user1, user2)` | `user2.getConnections()` contains a `Connection` to `user1`; `service.getNotifications("2")` has a `CONNECTION_REQUEST` notification mentioning "John Doe" |
| H4 ✅ | Accept connection request | `service.acceptConnectionRequest(user2, user1)` | `user2.getConnections()` now holds two `Connection` entries to `user1` (request + accepted); no exception |
| H5 ✅ | Post a job and notify everyone | `service.postJobListing(jobPosting)` | Every registered user receives a `JOB_POSTING` notification with the title |
| H6 ✅ | Search users by name | `service.searchUsers("John")` | List with exactly the user whose name contains "John" |
| H7 ✅ | Search job postings by keyword | `service.searchJobPostings("Software")` | List containing the "Software Developer" posting |
| H8 ✅ | Send a message | `service.sendMessage(user1, user2, "Hi Jane")` | `user2.getInbox().size()==1`, `user1.getSentMessages().size()==1`, `user2` gets a `MESSAGE` notification |
| H9 ✅ | Fetch notifications | `service.getNotifications(user2.getId())` | Returns `CONNECTION_REQUEST`, `JOB_POSTING`, `MESSAGE` in insertion order |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | Search when another user has a null headline | register `user2` with `new Profile()` (no headline); `service.searchUsers("John")` | Returns only `user1`; no NPE while scanning `user2`, whose headline is null |
| E2 ⬜ | Search with no matches | `service.searchUsers("zzz")` | Empty list, not `null` |
| E3 ⬜ | Notifications for an unknown user | `service.getNotifications("999")` | Empty list |
| E4 ⬜ | Accept a request that was never sent | `service.acceptConnectionRequest(user2, user3)` | No-op; `user2.getConnections()` unchanged |
| E5 ⬜ | Register same id twice | `service.registerUser(u)` twice with the same id | Second call overwrites the first (map put); no duplicate |
| E6 ⬜ | Search is case-sensitive | `service.searchJobPostings("software")` | Empty (does not match "Software") — document or fix |
| E7 ⬜ | Duplicate connection request | `service.sendConnectionRequest(user1, user2)` twice | Two `Connection` entries and two notifications — no de-duplication |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Wrong password | `service.loginUser("john@example.com", "bad")` | Returns `null` |
| X2 ⬜ | Unknown email | `service.loginUser("nobody@example.com", "password")` | Returns `null` |
| X3 ⬜ | Message to an unregistered user | `service.sendMessage(user1, unregistered, "hi")` | Message is added to the receiver object's inbox; notification stored under its id (service does not validate registration) |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent notifications to the same user | N threads call `service.sendMessage(x, user2, ...)` | `getNotifications("2")` has N entries; `CopyOnWriteArrayList` prevents lost updates |
| C2 ⬜ | Concurrent registration | N threads call `service.registerUser(...)` with distinct ids | All N users present (`ConcurrentHashMap`) |
| C3 ⬜ | Concurrent connection requests to one user | N threads call `sendConnectionRequest(xi, user2)` | `user2.getConnections()` is a plain `ArrayList` — may lose entries; expected to need external synchronisation |

## Interviewer follow-ups / extensions
- How would you model pending vs accepted connections? — Add `ConnectionStatus {PENDING, ACCEPTED, REJECTED}` on `Connection` and store requests separately from the connection list.
- How would you make search scale? — Inverted index on tokens of name/headline/skills; case-fold; return ranked results.
- How would you stop broadcasting every job to every user? — Match `JobPosting.requirements` against `Profile.skills` and location; or push to a feed lazily on read.
- How would you make notifications real-time? — Publish to a queue per user; WebSocket push; mark read/unread.
- How would you secure login? — Salted hash of password, session tokens, rate limiting.
- How would you support "people you may know"? — BFS over the connection graph to depth 2, rank by mutual connections.
