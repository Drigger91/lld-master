# Test cases — Elevator System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Controller starts N idle elevators | `new ElevatorController(3, 5)` | Three elevators exist, each at floor 1, each with a running worker thread; no output until a request arrives |
| H2 ✅ | Request moves the nearest car upward | `controller.requestElevator(5, 10)` | Elevator 1 (all cars tie at floor 1, lowest id wins) logs `added request: Request{5 -> 10}` then `reached floor 1` … `reached floor 10`, one line per floor |
| H3 ✅ | Second request goes to the closest car at that moment | While elevator 1 is climbing, `controller.requestElevator(3, 7)` | Elevator whose `getCurrentFloor()` is nearest to 3 gets it (elevator 1 once it passes floor 2); served after its current trip finishes |
| H4 ✅ | Downward trip | `controller.requestElevator(8, 2)` when the assigned car is above floor 2 | Car direction becomes DOWN and it logs floors descending to 2 |
| H5 ✅ | Requests are served FIFO per car | Enqueue two requests on the same car | Second is served only after the first trip completes |
| H6 ✅ | Caller waits for the system to drain | After enqueuing, `controller.awaitIdle()` | Returns only when every car has an empty queue and is parked; demo prints `All requests served.` and the JVM exits |
| H7 ⬜ | Idle car resumes on a late request | Let all cars go idle, then `requestElevator(1, 4)` | The parked car wakes from `wait()` and serves the request without busy-waiting |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Source == destination | `requestElevator(4, 4)` on a car at floor 4 | Request is queued and completes with no movement (no floor lines printed) |
| E2 ⬜ | Car already on the destination floor | Car at floor 6, `requestElevator(2, 6)` | Neither branch of `processRequest` runs; car stays at 6 |
| E3 ⬜ | Queue capacity reached | Car with capacity 1 and one pending request; `addRequest(...)` again | Second request is silently dropped (not queued, no log line) |
| E4 ⬜ | Tie-break between equidistant cars | Cars 1 and 2 both at floor 1; `requestElevator(3, 9)` | Elevator 1 (first in list) is chosen |
| E5 ⬜ | Source floor is not visited | Car at floor 1, `requestElevator(5, 3)` | Car goes 1 → 3 directly; floor 5 is never reached (documented limitation) |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Zero elevators | `new ElevatorController(0, 5)` then `requestElevator(1, 2)` | `findOptimalElevator` returns null and `requestElevator` throws `NullPointerException` (no guard in the reference code) |
| X2 ⬜ | Negative or out-of-building floors | `requestElevator(-1, 100)` | Accepted as plain ints; the car walks from its floor to 100. A production design validates against `minFloor..maxFloor` |
| X3 ⬜ | Worker interrupted while idle | Interrupt an elevator thread parked in `wait()` | `processRequests` re-sets the interrupt flag and returns; no stack trace |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ✅ | Requests from the caller thread while cars move | Fire four `requestElevator` calls back to back | No lost or duplicated requests; each is logged once as added and served exactly once |
| C2 ⬜ | Add to a busy car blocks | Enqueue on a car mid-trip from another thread | `addRequest` blocks until the car's `synchronized processRequests` releases the monitor (i.e. until the trip ends) |
| C3 ⬜ | Multiple producer threads | Two threads call `requestElevator` concurrently | Each request lands in exactly one car's queue; `addRequest` is synchronized per car |
| C4 ✅ | Clean shutdown | Main returns after `awaitIdle()` | Worker threads are daemon; JVM exits without hanging |

## Interviewer follow-ups / extensions
- How would you make the car stop at the source floor first? — Turn a `Request` into two stops (pickup, drop-off) and process a sorted set of stops per direction.
- How would you implement SCAN/LOOK? — Replace the FIFO list with two `TreeSet<Integer>` (up-stops, down-stops) and keep moving in the current direction while stops remain ahead.
- How would you avoid holding the lock while moving? — Poll the queue under the lock, release it, then move; use a `BlockingQueue<Request>` and drop `synchronized` from `processRequests`.
- How would you add a smarter dispatcher? — Extract `ElevatorSelectionStrategy` (nearest, same-direction-first, least-loaded) and inject it into the controller.
- How would you model doors, weight limits and emergency stop? — Add an `ElevatorState` enum (IDLE, MOVING, DOORS_OPEN, MAINTENANCE) and gate transitions in `Elevator`.
