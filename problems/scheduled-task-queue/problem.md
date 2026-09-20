# Scheduled Task Queue

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Value Object (immutable task envelope), Producer–Consumer (intended) | topic/key/message envelope, input validation, id generation, per-topic queues, time-based consumption |

## Problem statement
Design a scheduled queue: producers submit tasks that must be executed at a given schedule time rather than immediately. Every task is an envelope of `topic / key / message`, and the system keeps a set of queues, one per topic, from which tasks are consumed when their assigned time arrives. Basic validations must be enforced on both the task and its schedule time.

> Status of the solution in `src/`: the module currently implements the task envelope (`ScheduledTask`) with its validations and demo; the per-topic queues and time-driven consumer are the exercise left for the candidate. The requirements below describe the full system; test cases mark what the shipped code covers.

## Functional requirements
1. A task is described by a `topic`, a `key` and a JSON `message` payload.
2. A task can be created with an explicit key, or the system generates a unique key (UUID) when none is supplied.
3. A task without a message is rejected at construction time.
4. Tasks are submitted with a schedule time; a schedule time in the past (or missing) must be rejected.
5. The system maintains one queue per topic; submitting a task to a new topic creates its queue.
6. A consumer for a topic executes tasks only when their schedule time has been reached, in schedule-time order.
7. A task's textual representation shows its topic, key and message so it can be logged.

## Non-functional requirements & constraints
- In-memory only; tasks are lost on restart.
- `ScheduledTask` is immutable (all fields `final`); equality and hashing are value-based (Lombok `@Data`).
- Payloads are Jackson `ObjectNode`s, so any JSON object is accepted as a message.
- Scheduling, queueing and consumption are not yet implemented; when added they must be safe for concurrent producers and one consumer per topic.

## Clarifying questions to ask
- What is the message format? — A JSON object (`ObjectNode`); the queue does not interpret it.
- Is the key user-supplied or system-generated? — Either; when omitted a random UUID is assigned.
- Is a null or empty message allowed? — No, a task must carry a non-null message.
- What does "execute at schedule time" mean for precision? — Best effort: run at or shortly after the schedule time, never before.
- One queue for everything or one per topic? — One queue per topic so topics are consumed independently.
- Must a task run exactly once? — Yes, a consumed task is removed from its queue.

## Core entities
- `ScheduledTask` — immutable envelope of `topic`, `key` and `message` (`ObjectNode`); two constructors (explicit key / generated UUID key); rejects a null message with a `RuntimeException`; Lombok `@Data` supplies getters/`equals`/`hashCode`, with a custom `toString`.
- `Main` — demo: builds tasks both ways and shows the null-message validation.
- *(To be added by the candidate)* a per-topic queue holder and a time-driven consumer, plus a schedule time on the task.

## Design hints
- **Value object first**: make the task immutable and validated in its constructor so a queue never holds a half-built task; that is what `ScheduledTask` does today.
- **Per-topic queues**: a `Map<String, PriorityBlockingQueue<ScheduledTask>>` keyed by topic, ordered by schedule time, keeps topics independent and lets the consumer `peek` cheaply for the next due task.
- **Time-driven consumption**: either a `ScheduledExecutorService` per submission (`schedule(task, delay)`) or one worker per topic that sleeps until `head.scheduleTime`; the second is easier to reason about for ordering, the first is simpler to write.
- **Validation on time**: compare against an injected `Clock` rather than `System.currentTimeMillis()` so tests can control "now".
- Trade-offs to discuss: `DelayQueue` vs. `PriorityBlockingQueue` + explicit wait; key uniqueness across topics vs. per topic; what to do with duplicate keys (reject, replace, or append).
- Common mistakes: generating a key on every `equals`/`hashCode` call, mutating the payload after submission, busy-polling the queue, letting a throwing task kill the consumer thread.

## Run
mvn -q -pl problems/scheduled-task-queue compile exec:java
