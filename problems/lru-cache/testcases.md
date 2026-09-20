# Test cases — LRU Cache

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

Setup: `cache = new LRUCache<Integer, String>(3)`.

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Put then get | `cache.put(1, "Value 1")`; `cache.get(1)` | returns "Value 1" |
| H2 ✅ | Fill to capacity | `put(1..3)`; `get(1)`, `get(2)`, `get(3)` | all present |
| H3 ✅ | Evict least recently used on overflow | `put(1)`, `put(2)`, `put(3)`, `get(1)`, `get(2)`, `put(4)`; `get(3)` | `get(3)` returns `null` (3 was the least recently used); `get(4)` returns "Value 4" |
| H4 ✅ | `get` refreshes recency | as H3 | key 1 survives the eviction because it was read after key 3 was written: `get(1)` == "Value 1" |
| H5 ✅ | Update existing key | `put(2, "Updated Value 2")`; `get(2)` | returns the new value; size unchanged |
| H6 ⬜ | Update refreshes recency | `put(1)`, `put(2)`, `put(3)`, `put(1, "x")`, `put(4)`; `get(2)` | key 2 evicted (`null`); keys 1, 3, 4 present |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Capacity 1 | `new LRUCache<>(1)`; `put(1)`, `put(2)`; `get(1)`, `get(2)` | `null`, then "Value 2" |
| E2 ⬜ | Repeated puts of the same key never evict | capacity 2; `put(1)` ten times with different values; `put(2)` | both keys present, `get(1)` returns the last value |
| E3 ⬜ | Eviction order after mixed access | capacity 3; `put(1)`, `put(2)`, `put(3)`, `get(1)`, `get(3)`, `put(4)` | key 2 evicted; 1, 3, 4 remain |
| E4 ⬜ | Miss on an evicted key does not affect order | after E3: `get(2)`, then `put(5)` | key 1 evicted (least recent of 1, 3, 4) |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Get a missing key | `get(3)` after it was evicted, or any never-inserted key | returns `null`, no exception |
| X2 ⬜ | Capacity 0 | `new LRUCache<>(0)`; `put(1, "a")`; `get(1)` | entry is evicted immediately; `get` returns `null` (no validation, no exception) |
| X3 ⬜ | Negative capacity | `new LRUCache<>(-1)` | `HashMap` constructor throws `IllegalArgumentException` — a follow-up is to validate explicitly |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent puts never exceed capacity | N threads `put` distinct keys into a capacity-3 cache | no exception; list and map stay consistent; at most 3 keys retrievable |
| C2 ⬜ | Concurrent get/put on the same key | thread A loops `get(1)`, thread B loops `put(1, v)` | never returns a corrupted/partial value; list links never break (both methods hold the cache lock) |

## Interviewer follow-ups / extensions
- How would you add `remove(key)` and `size()`? — Unlink the node and delete from the map under the same lock; size is `cache.size()`.
- How would you add TTL? — Store an expiry timestamp in `Node`; check on `get` and lazily evict, or run a background sweeper.
- How would you reduce lock contention? — Segment the cache (N independent LRU shards keyed by `hash % N`) or use `ConcurrentHashMap` plus a lock only around list mutations.
- How would you implement LFU instead? — Frequency buckets, each an LRU list, plus a min-frequency pointer.
- Why not `LinkedHashMap(capacity, 0.75f, true)` + `removeEldestEntry`? — It works; interviewers usually ask you to explain its internals or to implement it without the library.
