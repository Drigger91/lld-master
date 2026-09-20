# Pub-Sub System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Observer (Publish–Subscribe) | topic-based fan-out, subscriber interface, publisher authorization, CopyOnWriteArraySet |

## Problem statement
Design an in-process publish–subscribe messaging system. Publishers send messages to named topics, and every subscriber of a topic receives each message published to it. Subscribers can join and leave a topic at any time, and a publisher must only be able to publish to topics it has registered for.

## Functional requirements
1. Create topics identified by a name.
2. Subscribers can subscribe to and unsubscribe from a topic.
3. A publisher registers the topics it is allowed to publish to; publishing to an unregistered topic is rejected.
4. Publishing a message to a topic delivers it to every current subscriber of that topic.
5. A subscriber may be subscribed to several topics; a topic may have several subscribers; several publishers may share a topic.
6. After unsubscribing, a subscriber receives no further messages from that topic.
7. Subscribers are notified through a single callback (`onMessage`) so any consumer can plug in.

## Non-functional requirements
- Purely in-memory and in-process; no persistence, no message history, no replay for late subscribers.
- Delivery is synchronous on the publisher's thread, in subscriber-iteration order; there is no ordering guarantee across topics and no retry.
- `Topic` uses a `CopyOnWriteArraySet`, so subscribe/unsubscribe during a publish is safe and never throws; a subscriber added mid-publish is not guaranteed to receive that message.
- `Publisher.registerTopic` is not thread-safe (plain `HashSet`).

## Constraints
- A `Message` is exactly one `String content`; no headers, keys, priorities, timestamps or message ids.
- Topics are constructed directly by callers (`new Topic(name)`); there is no broker or registry, and names are not checked for uniqueness, so two `Topic("A")` instances are two independent channels.
- A subscriber appears at most once in a topic's subscriber set (set semantics on the `Subscriber` reference); the same `Message` published twice is delivered twice.
- Every current subscriber of a topic receives every message published to it; there is no filtering, partitioning or per-subscriber selection.
- A publisher may publish only to `Topic` objects it has passed to `registerTopic`; the check is on object identity, not on topic name.
- Scale: a handful of topics, publishers and subscribers in one JVM (the demo uses 2 topics, 2 publishers, 3 subscribers); no queue depth, buffering or backpressure — a message is delivered during `publish` or not at all.
- Single process only: no network transport, serialization or cross-JVM delivery.

## Clarifying questions to ask
- Push or pull delivery? — Push: the topic calls `subscriber.onMessage(message)` directly.
- Do subscribers need message history or replay? — No, only messages published after they subscribe.
- Should delivery be asynchronous / on a thread pool? — No, synchronous fan-out is acceptable for the first version.
- What happens if a publisher publishes to a topic it did not register? — The publish is rejected and a message is printed; no exception.
- Is a message anything more than a string? — No, `Message` wraps a `String content`.

## Core entities
- `Topic` — named channel; keeps the subscriber set and fans out `publish(Message)` to each subscriber.
- `Subscriber` — interface with `onMessage(Message)`; the observer.
- `PrintSubscriber` — concrete subscriber that prints received content with its name.
- `Publisher` — holds the set of topics it registered via `registerTopic`; `publish(topic, message)` checks membership and delegates to `Topic.publish`.
- `Message` — immutable payload wrapping a `String content`.

## Design hints
- **Observer**: `Topic` is the subject, `Subscriber` the observer; the topic owns the subscriber list so publishers stay ignorant of who listens.
- **Publisher-side authorization**: keeping a `Set<Topic>` on the publisher is a cheap way to enforce "who may publish where" without a central broker; discuss moving it to a broker/registry if topics must be looked up by name.
- **CopyOnWriteArraySet** gives iteration-safe fan-out with cheap reads; it is the right choice when subscriptions change rarely and publishes dominate.
- Trade-offs to discuss: synchronous vs. asynchronous delivery (a slow subscriber blocks the publisher), at-most-once vs. at-least-once, whether a broker should own topics instead of callers constructing them directly.
- Common mistakes: iterating a plain `ArrayList` while a subscriber unsubscribes inside `onMessage` (`ConcurrentModificationException`); forgetting that a subscriber on two topics gets both streams; letting one subscriber's exception stop delivery to the rest.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :pub-sub-system compile exec:java` |
