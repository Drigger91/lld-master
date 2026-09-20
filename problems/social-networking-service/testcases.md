# Test cases — Social Networking Service

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register and log in | `service.registerUser(user1)`; `service.loginUser("john@example.com", "password")` | Returns `user1`; demo prints "User logged in: John Doe" |
| H2 ✅ | Send friend request | `service.sendFriendRequest("1", "2")` | `service.getNotifications("2")` contains a `FRIEND_REQUEST` notification |
| H3 ✅ | Accept friend request | `service.acceptFriendRequest("2", "1")` | `user1.getFriends()` contains "2" and `user2.getFriends()` contains "1"; `getNotifications("1")` has `FRIEND_REQUEST_ACCEPTED` |
| H4 ✅ | Create posts | `service.createPost(post1)` (author "1"), `service.createPost(post2)` (author "2") | `user1.getPosts()` contains `post1`; `user2.getPosts()` contains `post2` |
| H5 ✅ | Like a post | `service.likePost("2", "post1")` | `post1.getLikes().size()==1`; author "1" gets a `LIKE` notification |
| H6 ✅ | Comment on a post | `service.commentOnPost(new Comment("c1","2","post1","Great post!",ts))` | `post1.getComments().size()==1`; author "1" gets a `COMMENT` notification |
| H7 ✅ | Newsfeed shows own + friends' posts, newest first | `service.getNewsfeed("1")` | Both `post1` and `post2` present, sorted by timestamp descending |
| H8 ✅ | Fetch notifications | `service.getNotifications("1")` | `FRIEND_REQUEST_ACCEPTED`, `LIKE`, `COMMENT` in that order |
| H9 ⬜ | Update profile | `service.updateUserProfile(updatedUser)` with same id | `loginUser` afterwards returns the updated object |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Like the same post twice | `service.likePost("2","post1")` twice | Likes count stays 1; only one `LIKE` notification |
| E2 ⬜ | Newsfeed for a user with no friends and no posts | `service.getNewsfeed("3")` | Empty list |
| E3 ⬜ | Newsfeed for unknown user | `service.getNewsfeed("999")` | Empty list, no exception |
| E4 ⬜ | Notifications for user with none | `service.getNotifications("999")` | Empty list |
| E5 ⬜ | Post by an unregistered user | `service.createPost(post)` where `userId` not registered | Post is stored in `posts` but attached to no user; it never appears in any feed |
| E6 ⬜ | Accept friend request twice | `service.acceptFriendRequest("2","1")` twice | Friend ids are duplicated in both lists (no de-duplication) — document or fix with a `Set` |
| E7 ⬜ | Posts with identical timestamps | Two posts with the same `Timestamp` | Both appear; relative order is unspecified but stable |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Wrong password | `service.loginUser("john@example.com","nope")` | Returns `null` |
| X2 ⬜ | Friend request to unknown user | `service.sendFriendRequest("1","999")` | No-op, no notification, no exception |
| X3 ⬜ | Accept with unknown user id | `service.acceptFriendRequest("1","999")` | No-op; no friendship created |
| X4 ⬜ | Like unknown post | `service.likePost("1","nope")` | No-op, no exception |
| X5 ⬜ | Comment on unknown post | `service.commentOnPost(comment)` with unknown `postId` | No-op, no notification |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent likes from distinct users | N threads call `service.likePost(ui, "post1")` | Expected N likes; `Post.likes` is a plain `ArrayList` and the check-then-add is unsynchronised, so entries may be lost — needs a lock or a concurrent `Set` |
| C2 ⬜ | Concurrent notifications to one user | N threads trigger notifications for "1" | `getNotifications("1").size()==N` (`CopyOnWriteArrayList`) |
| C3 ⬜ | Concurrent registration | N threads register distinct users | All present (`ConcurrentHashMap`) |

## Interviewer follow-ups / extensions
- How would you model pending friend requests? — A `FriendRequest{from,to,status}` store; accept validates that a request exists.
- How would you paginate or rank the feed? — Cursor by `(timestamp, postId)`; rank by engagement; cap per-friend contribution.
- Fan-out-on-write vs fan-out-on-read? — Pre-materialise feeds on post for normal users, read-time merge for celebrities with millions of followers.
- How would you emit `MENTION` notifications? — Parse `@handle` tokens in post/comment content and notify each mentioned user.
- How would you add privacy? — `Visibility {PUBLIC, FRIENDS, PRIVATE}` on `Post`, filtered in `getNewsfeed`.
- How would you persist this? — Users/posts tables, a friendships edge table, notifications keyed by user with a read flag.
