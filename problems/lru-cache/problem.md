# LRU Cache

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Doubly-linked list + hash map | O(1) get/put, eviction policy, sentinel nodes, generics, synchronized access |

## Problem statement
Design an in-memory key-value cache with a fixed capacity that evicts the least recently used entry when full. Both reads and writes must run in constant time, and both count as "use" for recency purposes.

## Functional requirements
1. `LRUCache(capacity)` creates an empty cache holding at most `capacity` entries.
2. `get(key)` returns the value if present (and marks the entry as most recently used), otherwise `null`.
3. `put(key, value)` inserts a new entry or updates an existing one; either way the entry becomes most recently used.
4. When an insert would exceed capacity, the least recently used entry is evicted.
5. Keys and values are generic (`K`, `V`).

## Non-functional requirements & constraints
- `get` and `put` are O(1); memory is O(capacity).
- Thread-safe via `synchronized` on both public methods (coarse lock; no lock striping).
- No TTL, no size-based weighting, no eviction callbacks, no persistence.
- `null` is used as the "miss" sentinel, so `null` values are indistinguishable from misses.

## Clarifying questions to ask
- Does a `get` count as use? — Yes; a `get` or `put` of an existing key moves it to most-recent.
- What does `get` return on a miss? — `null`.
- Are keys/values nullable? — Not supported; `null` is reserved for "miss".
- Is it accessed by multiple threads? — Yes; correctness under concurrent access is required, contention is acceptable.
- Do we need `remove`, `size`, or iteration? — Not required.

## Core entities
- `LRUCache<K, V>` — the cache: `Map<K, Node<K,V>>` for O(1) lookup plus a doubly-linked list ordered by recency with `head` (most recent) and `tail` (least recent) sentinels.
- `Node<K, V>` — list node holding key, value, `prev`, `next`. The key is stored so the map entry can be removed on eviction.

## Design hints
- The classic combination: the hash map gives O(1) lookup, the doubly-linked list gives O(1) move-to-front and remove-from-back. Neither alone is enough.
- **Sentinel head/tail nodes** eliminate every null check in `addToHead`/`removeNode`/`removeTail` — say this out loud; it is what separates a clean solution from a buggy one.
- Store the key in the node: eviction pops the tail node and must delete its key from the map.
- Order of operations in `put`: for a new key, add to the map and list first, *then* evict if `size > capacity` (or evict first — either is fine, but be consistent so capacity 1 works).
- **Common mistakes**: forgetting to move a node to the head on `get`; updating a value without moving it; evicting `tail` itself instead of `tail.prev`; using `LinkedHashMap` without being able to explain how it works internally.
- Java one-liner alternative interviewers may accept and then probe: `LinkedHashMap` with `accessOrder=true` and `removeEldestEntry`.

## Run
mvn -q -pl problems/lru-cache compile exec:java
