# Social Networking Service

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, Observer (notification fan-out) | friend graph, newsfeed aggregation, likes/comments, notifications, thread-safe collections |

## Problem statement
Design a social networking service like Facebook. Users register, become friends with other users, publish posts (text plus optional image/video URLs), like and comment on posts, and see a newsfeed made of their own and their friends' posts, newest first. Users are notified when they receive a friend request, when it is accepted, and when their posts are liked or commented on. In-memory, single process.

## Functional requirements
1. Register a user (name, email, password, profile picture, bio) and log in with email + password.
2. Update a user's profile.
3. Send a friend request to another user; the receiver can accept it, after which both users list each other as friends.
4. Create a post attached to a user, with content, image URLs and video URLs.
5. Like a post (a user can like a given post at most once) and comment on a post.
6. Build a newsfeed for a user: own posts + friends' posts, sorted by timestamp descending.
7. Generate notifications (`FRIEND_REQUEST`, `FRIEND_REQUEST_ACCEPTED`, `LIKE`, `COMMENT`) and let a user fetch them.

## Non-functional requirements & constraints
- In-memory; no persistence; plaintext password comparison.
- One `SocialNetworkingService` singleton for the process.
- `users`, `posts`, `notifications` are `ConcurrentHashMap`s; each notification list is a `CopyOnWriteArrayList`. `User.friends`, `User.posts`, `Post.likes`, `Post.comments` are caller-supplied lists (plain `ArrayList` in the demo) and are not synchronised.
- Newsfeed is computed on read (fan-out on read); cost is O(sum of friends' posts) plus a sort.
- `MENTION` exists in `NotificationType` but nothing emits it.

## Clarifying questions to ask
- Is a friend request stored as a pending relationship? — Assumed: no; sending only creates a notification, accepting adds both ids to each other's friend lists.
- Should the newsfeed be paginated or ranked? — Assumed: full list, sorted newest first by `Post.timestamp`.
- Can a user like a post twice? — Assumed: no; second like is ignored and no notification is sent.
- Are likes/comments tied to a user object or just an id? — Assumed: user id strings.
- Do we need privacy / visibility rules? — Assumed: no; friends see all posts.

## Core entities
- `SocialNetworkingService` — singleton facade holding users, posts and per-user notifications; implements friending, posting, liking, commenting, newsfeed and notifications.
- `User` — id, name, email, password, profile picture, bio, `friends` (ids) and `posts`.
- `Post` — id, author id, content, image/video URLs, timestamp, `likes` (user ids) and `comments`.
- `Comment` — id, author id, post id, content, timestamp.
- `Notification` / `NotificationType` — id, target user id, type, content, timestamp.

## Design hints
- **Singleton** service with `synchronized getInstance()`; interviewer will ask about injecting it instead — answer in terms of testability.
- **Observer-style fan-out**: each social action ends with `addNotification(targetUserId, notification)`; keyed by user id so `getNotifications` is O(1) lookup.
- Newsfeed: fan-out-on-read is trivial to implement and always consistent; fan-out-on-write (pre-materialised feeds) is the alternative when reads dominate. Be ready to discuss the celebrity problem.
- Guard idempotency: `likePost` checks `likes.contains(userId)` before adding (O(n) on a list; a `Set` would be better).
- Common mistakes: forgetting to add the friendship symmetrically; sorting by insertion order instead of timestamp; returning the internal list from `getNotifications` (callers can mutate it); using `ArrayList` for likes and getting O(n) duplicate checks.

## Run
mvn -q -pl problems/social-networking-service compile exec:java
