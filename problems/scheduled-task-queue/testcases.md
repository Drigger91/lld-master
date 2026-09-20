# Test cases — Scheduled Task Queue

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Create a task with an explicit key | `new ScheduledTask("notifications", "email-42", payload)` | `getTopic()` is `notifications`, `getKey()` is `email-42`, `getMessage()` is the same `ObjectNode` |
| H2 ✅ | Create a task with a generated key | `new ScheduledTask("notifications", payload)` | `getKey()` is a non-blank UUID string; two such tasks have different keys |
| H3 ✅ | Readable representation | `task.toString()` | `topic: <topic> \| key: <key> \| message: <json>` |
| H4 ⬜ | Value equality | Build two tasks with identical topic, key and an equal `ObjectNode` | `equals` is true and `hashCode` matches |
| H5 ⬜ | Submit a task with a future schedule time to a topic queue | Submit `task` scheduled for `now + 2s` | Task is queued under its topic and executed no earlier than its schedule time |
| H6 ⬜ | Tasks on one topic run in schedule order | Submit `t1 @ now+3s` then `t2 @ now+1s` to the same topic | `t2` executes before `t1` |
| H7 ⬜ | Topics are consumed independently | Submit a due task to topic A and a far-future task to topic B | Topic A's task runs without waiting for topic B |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Empty JSON object message | `new ScheduledTask("t", mapper.createObjectNode())` | Accepted; `toString` shows `message: {}` |
| E2 ⬜ | Same key on two topics | Create tasks with key `k` on topics `a` and `b` | Both accepted; they are distinct tasks (topic is part of identity) |
| E3 ⬜ | Schedule time equal to now | Submit a task scheduled for the current instant | Accepted and executed immediately |
| E4 ⬜ | Submitting to an unknown topic | Submit a task whose topic has no queue yet | A queue for that topic is created on demand |
| E5 ⬜ | Payload mutated after submission | Modify the `ObjectNode` after creating the task | Currently visible to the task (shared reference); discuss deep-copying the payload |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Null message with explicit key | `new ScheduledTask("t", "k", null)` | `RuntimeException("Message cannot be null")`; no task is created |
| X2 ⬜ | Null message with generated key | `new ScheduledTask("t", null)` | Same `RuntimeException("Message cannot be null")` |
| X3 ⬜ | Null or blank topic | `new ScheduledTask(null, payload)` | Currently accepted; a hardened version should reject with an exception |
| X4 ⬜ | Schedule time in the past | Submit a task scheduled for `now - 1s` | Rejected with a validation exception; nothing is queued |
| X5 ⬜ | Missing schedule time | Submit a task without a schedule time | Rejected with a validation exception |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent producers on one topic | 10 threads each submit 100 future tasks to topic `t` | All 1000 tasks are queued and each executes exactly once, in schedule order |
| C2 ⬜ | Consumer keeps running when a task throws | Submit a task whose execution throws | The failure is logged and subsequent tasks on that topic still run |

## Interviewer follow-ups / extensions
- How would you add the schedule time to the envelope? — Add an `Instant scheduledAt` field validated in the constructor against an injected `Clock`.
- How would you implement the per-topic queues? — `ConcurrentHashMap<String, PriorityBlockingQueue<ScheduledTask>>` ordered by `scheduledAt`, plus one consumer thread per topic that waits until the head is due.
- How would you support cancellation? — Index tasks by key in a map and remove from the queue; mark as cancelled if already dequeued.
- How would you make it durable? — Write-ahead log or database table of pending tasks, replayed into the queues on start-up.
- How would you handle retries? — On failure re-enqueue with exponential back-off and a max-attempts counter stored on the task.
