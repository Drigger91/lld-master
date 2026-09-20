# Task Management System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, Repository-style manager | enum status, per-user index, search/filter, ConcurrentHashMap, per-task locking |

## Problem statement
Design a task management system where users are assigned tasks that have a title, description, due date, priority and status. The system must support creating, updating and deleting tasks, marking them complete, searching by keyword, filtering by status/date range/priority, and viewing all tasks assigned to a given user. Assume many users act on the system concurrently.

## Functional requirements
1. Create a task with an id, title, description, due date, integer priority and an assigned user; a new task starts in status `PENDING`.
2. Update an existing task's title, description, due date, priority and status; if the update carries a different assignee, move the task from the old user's list to the new one.
3. Delete a task by id and remove it from its assignee's list.
4. Mark a task as `COMPLETED` by id.
5. Search tasks whose title or description contains a keyword.
6. Filter tasks by exact status, due-date range (inclusive) and exact priority.
7. Retrieve the list of tasks assigned to a user.
8. Operations on unknown task ids are no-ops rather than errors.

## Non-functional requirements
- In-memory only; a single `TaskManager` Singleton holds all state.
- Thread-safety: `ConcurrentHashMap` for the task and per-user indexes, `CopyOnWriteArrayList` per user, and `synchronized (task)` around multi-field updates; searches iterate live maps without locking, so results are weakly consistent.
- Lists returned by `getTaskHistory` are defensive copies; `searchTasks`/`filterTasks` return fresh lists.
- `User` does not override `equals`, so reassignment detection relies on reference identity.

## Constraints
- Status is exactly `PENDING`, `IN_PROGRESS`, `COMPLETED`; a new task always starts as `PENDING` and any status may be set directly (no transition rules).
- Task ids are caller-supplied `String`s and must be unique: `createTask` with an existing id silently replaces the map entry and appends a second entry to the user's list.
- Every task has exactly one assignee, fixed for the lifetime of the `Task` object (`assignedUser` is `final`); reassignment is expressed by passing a new `Task` with the same id to `updateTask`.
- `priority` is an unbounded `int` (the demo uses 1 and 2); `filterTasks` matches it by exact value.
- `dueDate` is a `java.util.Date` and must be non-null (`filterTasks` NPEs otherwise); the date range is inclusive on both ends and all four filter criteria are mandatory and ANDed — there is no wildcard.
- Keyword search is a case-sensitive `String.contains` on title or description; both fields must be non-null.
- Scale: hundreds of users and thousands of tasks in a single JVM; every search and filter is a full O(n) scan.

## Clarifying questions to ask
- Can a task have more than one assignee? — No, exactly one user.
- Is there a required workflow for status (PENDING → IN_PROGRESS → COMPLETED)? — No, any status may be set directly.
- Is the keyword search case-sensitive? — Yes, plain `String.contains`.
- Is priority an enum or number? — An `int`; lower/higher meaning is up to the caller, filtering is by exact value.
- What should happen when updating or deleting a non-existent task? — Silently ignore.
- Is the date range in `filterTasks` inclusive? — Yes on both ends.

## Core entities
- `TaskManager` — lazy Singleton (`synchronized getInstance`); owns `tasks` (id → `Task`) and `userTasks` (userId → list); implements create/update/delete/search/filter/complete/history.
- `Task` — mutable task record: `id` and `assignedUser` are final; title, description, dueDate, priority and status have setters; status defaults to `PENDING`.
- `User` — immutable id, name, email.
- `TaskStatus` — enum `PENDING`, `IN_PROGRESS`, `COMPLETED`.

## Design hints
- **Two indexes**: a primary `Map<id, Task>` plus a secondary `Map<userId, List<Task>>` makes "tasks for user" O(1) at the cost of keeping both in sync on create/update/delete — the interviewer will probe what happens if you forget one side.
- **Per-task locking**: `synchronized (existingTask)` in `updateTask`/`markTaskAsCompleted` serialises writers on the same task without a global lock; reads are lock-free and may observe a partially applied update.
- **Singleton**: lazy init guarded by `synchronized` on the static method is simple and correct; discuss eager init or an enum for less contention.
- Trade-offs: linear-scan search/filter is fine for in-memory scale; for large data introduce inverted indexes or delegate to a store. Combining all filter criteria with AND is simple but inflexible — a `Specification`/predicate-composition approach is the usual follow-up.
- Common mistakes: returning internal lists (allowing callers to mutate the index); checking reassignment with `!=` on users that lack `equals`; forgetting to remove a task from the user index on delete; letting `filterTasks` NPE on a task with a null due date.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :task-management-system compile exec:java` |
