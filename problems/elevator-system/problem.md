# Elevator System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Controller/Dispatcher, Producer–Consumer (wait/notify) | multithreading, monitor locks, nearest-car dispatch, FCFS per car, enums as direction |

## Problem statement
Design the control software for a building with N elevators. Passengers press a call button on a floor and pick a destination; the system must choose which elevator serves the request and each elevator must move floor by floor to serve its queue. Elevators operate concurrently and independently of the callers.

## Functional requirements
1. Create a controller with a configurable number of elevators; each elevator starts idle at floor 1.
2. Accept a request `(sourceFloor, destinationFloor)` from any caller thread.
3. Dispatch each request to the elevator whose current floor is closest to the source floor (ties go to the lowest-numbered elevator).
4. Each elevator serves its own queue in FIFO order, moving one floor at a time and reporting every floor it reaches; it sets its direction to UP or DOWN accordingly.
5. An elevator's queue has a bounded capacity; requests beyond the capacity are dropped.
6. An idle elevator must block (not busy-wait) until a new request arrives, and must stop cleanly when interrupted.
7. A caller can wait until every elevator is idle (`awaitIdle()`), which is how the demo terminates.

## Non-functional requirements
- Each elevator runs on its own daemon thread; the caller thread must never be blocked by an elevator that is *not* serving its request.
- Floor movement is simulated with a 1-second sleep per floor, so the demo takes ~45 s to run.
- The elevator monitor is held while the car is moving, so `addRequest` to a moving elevator blocks until that elevator finishes its current trip (a known simplification, see design hints).

## Constraints
- `numElevators >= 1` and `capacity >= 1` are fixed at construction (the demo uses 3 cars with capacity 5); cars are numbered `1..numElevators` and every car starts at floor 1.
- Floors are plain positive `int`s with no upper bound and no validation; `sourceFloor == destinationFloor` is legal and produces no movement.
- `capacity` bounds the *pending requests per car*, not passengers: a request that arrives when the car's queue is full is silently dropped, never queued elsewhere or retried.
- Direction vocabulary is exactly `Direction.UP` and `Direction.DOWN` (no `IDLE`); an idle car keeps its last direction, initially `UP`.
- A request is served by exactly one car, in the order it was queued; once dispatched it is never moved to another car.
- Single JVM, in memory only: floors are integers and nothing else of the building is modelled (no doors, buttons, weight or passenger count) and nothing is persisted.

## Clarifying questions to ask
- How many elevators and floors? — Configurable elevator count; floors are plain ints with no bounds check.
- What does "capacity" mean? — The maximum number of *pending requests* an elevator queues, not passenger count.
- Scheduling algorithm? — Simple nearest-car selection at request time, then FIFO within the car (no SCAN/LOOK batching of same-direction stops).
- Does the elevator visit the source floor first? — The reference implementation moves directly from its current floor to the destination floor; picking up at the source floor is a follow-up.
- What happens if all elevators are busy? — Request is still assigned to the closest one and waits in its queue.
- Do we need to simulate time? — Yes, one second per floor, printing every floor reached.

## Core entities
- `ElevatorController` — owns the elevators, starts one worker thread per car, dispatches requests to the nearest car, exposes `awaitIdle()`.
- `Elevator` — a car with `id`, `capacity`, `currentFloor`, `currentDirection` and a FIFO request queue; `run()` loops forever serving requests, `wait()`ing when idle.
- `Request` — immutable `(sourceFloor, destinationFloor)` value object.
- `Direction` — enum `UP`/`DOWN`.

## Design hints
- **Producer–consumer with a monitor**: `addRequest` is `synchronized` and calls `notifyAll()`; `processRequests` `wait()`s when the queue is empty. The interviewer will ask why `wait()` sits in a loop (spurious wake-ups) and why the shutdown path re-sets the interrupt flag.
- **Dispatching is a separate concern from moving**: the controller only decides *which* car; the car decides *how* to move. This lets you swap the dispatch strategy (nearest-car, least-loaded, same-direction-first) without touching `Elevator`.
- **Trade-off the code makes**: `processRequests` is `synchronized`, so the lock is held during the whole simulated trip. That makes `isIdle()` trivially correct but means a producer adding a second request to a busy elevator blocks until the trip ends. A better design keeps the lock only around queue operations and moves outside it — be ready to draw that.
- **Nearest-car is greedy**: `getCurrentFloor()` is read without synchronization while the car moves, so the choice is a best-effort snapshot. That is acceptable for dispatch, but say so.
- Common mistakes: busy-waiting instead of `wait/notify`; non-daemon threads that keep the JVM alive; forgetting that `Thread.sleep` inside a `synchronized` block holds the lock; ignoring the source floor entirely (the reference code does — call it out as the first thing you would fix).

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :elevator-system compile exec:java` |
