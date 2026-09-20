# Test cases — Task Management System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Create tasks | `taskManager.createTask(new Task("1", "Task 1", "Description 1", date, 1, user1))` (and two more) | Tasks are stored; each starts with status `PENDING` and appears in its assignee's history |
| H2 ✅ | Update a task's fields | `task2.setDescription("Updated description")`; `taskManager.updateTask(task2)` | Stored task with id `2` reflects the new description |
| H3 ✅ | Keyword search | `taskManager.searchTasks("Task")` | Returns all three tasks (title contains keyword) |
| H4 ✅ | Filter by status, date range and priority | `taskManager.filterTasks(TaskStatus.PENDING, new Date(0), new Date(), 1)` | Returns `Task 1` and `Task 3` only |
| H5 ✅ | Mark completed | `taskManager.markTaskAsCompleted("1")` | `task1.getStatus()` is `COMPLETED` |
| H6 ✅ | Task history per user | `taskManager.getTaskHistory(user1)` | Returns `Task 1` and `Task 3`; not `Task 2` |
| H7 ✅ | Delete a task | `taskManager.deleteTask("3")` | Task `3` no longer found by search; removed from `user1`'s history |
| H8 ⬜ | Reassign a task | `taskManager.updateTask(new Task("2", ..., user1))` where stored task `2` is assigned to `user2` | Task `2` disappears from `user2`'s history and appears in `user1`'s |
| H9 ⬜ | Search matches description | `taskManager.searchTasks("Description 2")` | Returns only `Task 2` |
| H10 ⬜ | Singleton | `TaskManager.getInstance()` twice | Same instance; tasks created through one are visible through the other |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Filter date range boundaries | Task due exactly at `startDate` and one exactly at `endDate` | Both included (inclusive comparison) |
| E2 ⬜ | Filter with no matches | `filterTasks(TaskStatus.IN_PROGRESS, ...)` when no task is in progress | Empty list, no exception |
| E3 ⬜ | Search is case-sensitive | `searchTasks("task")` with titles like `Task 1` | Empty list |
| E4 ⬜ | History for a user with no tasks | `getTaskHistory(new User("9", "Nobody", "n@x"))` | Empty list |
| E5 ⬜ | History is a copy | Modify the list returned by `getTaskHistory(user1)` | Internal index unaffected |
| E6 ⬜ | Mark completed twice | `markTaskAsCompleted("1")` twice | Status remains `COMPLETED`; no error |
| E7 ⬜ | Create with an existing id | `createTask` twice with id `1` and different assignees | Second replaces the first in `tasks`, but the first assignee's history still lists the old task — discuss rejecting duplicates |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Update unknown task | `updateTask(new Task("404", ...))` | No-op; nothing stored |
| X2 ⬜ | Delete unknown task | `deleteTask("404")` | No-op |
| X3 ⬜ | Complete unknown task | `markTaskAsCompleted("404")` | No-op |
| X4 ⬜ | Null due date then filter | Create a task with `dueDate == null`; `filterTasks(...)` | Currently throws `NullPointerException` — a hardened version should skip or reject |
| X5 ⬜ | Reassign to a user object with same id but different instance | `updateTask` with `new User("1", ...)` equal by id only | Treated as a different user (no `equals` on `User`): task moved between two lists keyed by the same id — discuss implementing `equals`/`hashCode` on `User` |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent updates to one task | Two threads call `updateTask` on the same id with different titles | Final state is one of the two updates in full (per-task lock), never a mix |
| C2 ⬜ | Concurrent create/delete on different ids | Many threads create and delete distinct tasks | No exception; final counts consistent in both indexes |
| C3 ⬜ | Search during writes | One thread searches while others create tasks | Search returns a weakly consistent snapshot; no `ConcurrentModificationException` |

## Interviewer follow-ups / extensions
- How would you enforce a status workflow? — Add `TaskStatus.canTransitionTo(next)` (or a State pattern) and validate in `updateTask`/`markTaskAsCompleted`.
- How would you make filters composable (any subset of criteria)? — Accept a `Predicate<Task>` or build a `TaskFilter` Specification with `and`/`or`.
- How would you support multiple assignees or watchers? — Replace `assignedUser` with a set and index every member in `userTasks`.
- How would you add reminders for due tasks? — A scheduled executor scanning tasks due within a window, or a `DelayQueue` keyed by due date, notifying via an Observer.
- How would you make it persistent? — Put the two maps behind a `TaskRepository` interface with an in-memory and a JDBC implementation.
