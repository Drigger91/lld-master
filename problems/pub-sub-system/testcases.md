# Test cases — Pub-Sub System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Single topic, multiple subscribers | `topic1.addSubscriber(s1)`, `topic1.addSubscriber(s2)`; `publisher1.registerTopic(topic1)`; `publisher1.publish(topic1, new Message("m1"))` | Both `s1` and `s2` receive `m1` exactly once |
| H2 ✅ | Subscriber on two topics | `s2` subscribed to `topic1` and `topic2`; publish to each | `s2` receives messages from both topics |
| H3 ✅ | Multiple messages preserve publish order per subscriber | Publish `m1` then `m2` on `topic1` | Each subscriber sees `m1` before `m2` |
| H4 ✅ | Unsubscribe stops delivery | `topic1.removeSubscriber(s2)`; publish `m3` on `topic1` | `s1` receives `m3`; `s2` does not; `s2` still receives `topic2` messages |
| H5 ✅ | Topics are isolated | Publish on `topic2` | Subscribers of only `topic1` receive nothing |
| H6 ⬜ | Two publishers on one topic | `p1.registerTopic(t)`, `p2.registerTopic(t)`; each publishes | Subscribers of `t` receive both publishers' messages |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Publish to a topic with no subscribers | `p.registerTopic(t)`; `p.publish(t, msg)` | No output, no exception |
| E2 ⬜ | Subscribe the same subscriber twice | `t.addSubscriber(s)` twice; publish once | `s` receives the message once (set semantics) |
| E3 ⬜ | Remove a subscriber that was never added | `t.removeSubscriber(s)` | No exception; no effect |
| E4 ⬜ | Unsubscribe from inside `onMessage` | Subscriber calls `t.removeSubscriber(this)` while being notified | No `ConcurrentModificationException`; remaining subscribers still notified |
| E5 ⬜ | Register the same topic twice on a publisher | `p.registerTopic(t)` twice; `p.publish(t, msg)` | Publish succeeds once per call; no duplicate delivery |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Publish to an unregistered topic | `new Publisher().publish(topic1, msg)` without `registerTopic` | Prints `This publisher can't publish to topic: Topic1`; subscribers receive nothing |
| X2 ⬜ | Subscriber throws in `onMessage` | First subscriber throws a `RuntimeException`; publish | Currently the exception propagates to the publisher and later subscribers are skipped — discuss isolating subscriber failures |
| X3 ⬜ | Null subscriber / null message | `t.addSubscriber(null)` or `t.publish(null)` | `addSubscriber(null)` stores `null` and `publish` NPEs; a hardened version should reject nulls with `IllegalArgumentException` |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent publish and subscribe | Thread A publishes in a loop while thread B adds/removes subscribers | No exception; every message reaches each subscriber present when its fan-out snapshot was taken |
| C2 ⬜ | Concurrent publishers on the same topic | Two threads publish 1000 messages each to `t` | Each subscriber receives 2000 messages; per-publisher order preserved, interleaving across publishers unspecified |

## Interviewer follow-ups / extensions
- How would you make delivery asynchronous? — Give each subscriber (or topic) a queue drained by an executor so a slow subscriber does not block the publisher.
- How would you add a broker that looks topics up by name? — Introduce a `PubSubBroker` with `createTopic(name)` / `getTopic(name)` backed by a `ConcurrentHashMap`, and route `publish(topicName, message)` through it.
- How would you support message replay for late subscribers? — Keep a bounded per-topic history and let `addSubscriber` optionally replay from an offset (Kafka-style consumer offsets).
- How would you add filtering? — Let subscribers register a `Predicate<Message>` so the topic only delivers matching messages.
- How would you guarantee at-least-once delivery? — Acknowledge on `onMessage` success, retry with back-off on failure, and dead-letter after N attempts.
