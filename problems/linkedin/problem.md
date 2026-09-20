# LinkedIn

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, Observer (notification fan-out) | in-memory registry, connection graph, keyword search, notifications, thread-safe collections |

## Problem statement
Design a professional networking service like LinkedIn. Users register with a profile (headline, summary, experience, education, skills), connect with other users, exchange direct messages, and search for people and job postings. Whenever something relevant happens to a user — a connection request, a new message, a new job posting — they should receive a notification. Keep everything in memory; a single JVM is fine.

## Functional requirements
1. Register a user with name, email, password and a `Profile`; log in with email + password.
2. Update a user's profile (headline, summary, experiences, educations, skills).
3. Send a connection request from one user to another; the receiver can accept it.
4. Post a job listing (title, description, requirements, location) and search job postings by keyword.
5. Search users by keyword against name or profile headline.
6. Send a direct message from one user to another; it lands in the receiver's inbox and the sender's sent list.
7. Notify users on `CONNECTION_REQUEST`, `MESSAGE` and `JOB_POSTING` events, and allow a user to fetch their notifications.

## Non-functional requirements
- In-memory only; no persistence, no real authentication (plaintext password equality).
- A single `LinkedInService` instance serves the whole process (singleton).
- Registries (`users`, `jobPostings`, `notifications`) are `ConcurrentHashMap`s and notification lists are `CopyOnWriteArrayList`s so concurrent reads/writes do not corrupt them. Per-user lists (connections, inbox) are plain `ArrayList`s and are not synchronised.
- Search is a linear substring scan (case-sensitive); no indexing or ranking.
- Job postings are broadcast to every registered user (O(users) per posting).

## Constraints
- Scale: tens to a few thousand users and job postings per process; every login, search and job fan-out is a full linear scan of `users`, which is acceptable at that size.
- Fixed vocabulary: `NotificationType { CONNECTION_REQUEST, MESSAGE, JOB_POSTING }` — every `Notification` carries exactly one of these.
- User ids and job-posting ids are caller-supplied `String`s and must be unique; `registerUser`, `updateUserProfile` and `postJobListing` with an existing id silently replace the old record (`Map.put`). Message and notification ids are generated `UUID`s.
- A `User` has exactly one `Profile`, which may be `null` until set; `Profile.headline` and `summary` may be `null` and `searchUsers` must tolerate that.
- `loginUser` matches on exact email + password equality and returns at most one `User` (the first match) or `null`.
- Connections are directed, per-user records: `sendConnectionRequest(a, b)` appends one `Connection(a)` to `b`; `acceptConnectionRequest(b, a)` appends a second `Connection(a)` to `b` only if the first exists. `a`'s own list is never touched, and nothing prevents duplicate requests or self-connections.
- Timestamps come from `System.currentTimeMillis()` with no injectable clock; tests must not assert on exact times or ordering across calls.
- Single JVM; there is no e-mail/push delivery — a notification exists only as a record in the target user's list.

## Clarifying questions to ask
- Is a connection request a separate pending state, or is it recorded directly? — Assumed: the request is stored as a `Connection` on the receiver; accepting adds a second `Connection` record (there is no explicit PENDING/ACCEPTED status).
- Should search be case-insensitive or ranked? — Assumed: no; plain `String.contains` on name/headline (users) and title/description (jobs).
- Who gets notified about a new job posting? — Assumed: every registered user.
- Do we need real auth (hashing, sessions)? — Assumed: no; `loginUser` returns the `User` or `null`.
- Does a message require the two users to be connected? — Assumed: no.

## Core entities
- `LinkedInService` — singleton facade: user registry, login, connections, job postings, messaging, notifications.
- `User` — id, name, email, password, `Profile`, list of `Connection`s, inbox and sent messages.
- `Profile` — headline, summary, `Experience`/`Education`/`Skill` lists, profile picture.
- `Connection` — a link to another `User` plus the connection timestamp.
- `JobPosting` — id, title, description, requirements, location, post date.
- `Message` — id, sender, receiver, content, timestamp.
- `Notification` / `NotificationType` — id, target user, type (`CONNECTION_REQUEST`, `MESSAGE`, `JOB_POSTING`), content, timestamp.

## Design hints
- **Singleton** `LinkedInService.getInstance()` (synchronized lazy init) gives one shared registry; the interviewer will ask why not dependency-inject it — answer: testability and multiple tenants argue for injection, singleton is fine for a demo.
- **Observer-style fan-out**: every state change that concerns another user creates a `Notification` via a private `addNotification(userId, ...)`; `notifications` is a `Map<userId, List<Notification>>` so retrieval is O(1) per user.
- Keep IDs opaque (`UUID`) for messages/notifications; user and job IDs are supplied by the caller.
- Trade-off: broadcasting job postings to all users is simple but O(N) per post; a real system would push to a feed on read or filter by skills/location.
- Common mistakes: NPE when a search touches an optional field such as `Profile.headline` (guard nulls); treating "connection request" and "accepted connection" as the same record makes it impossible to list pending requests — a `ConnectionStatus` enum is the usual fix; mutable shared `ArrayList`s on `User` are not thread-safe.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :linkedin compile exec:java` |
