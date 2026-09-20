# Traffic Signal System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, State-as-enum, (Observer hook), thread-per-light | timed state cycling, background threads, daemon/interrupt lifecycle, emergency override, synchronized state |

## Problem statement
Design the controller for the traffic lights at an intersection. Each road has its own light that cycles RED → GREEN → YELLOW → RED with configurable durations, and the lights must keep cycling autonomously once started. The controller must also support an emergency override that immediately turns a given road's light green, and it must be possible to stop the system cleanly.

## Functional requirements
1. Register roads with the controller (`addRoad`, `removeRoad`, `getRoad`); each road has an id, a name and one `TrafficLight`.
2. A `TrafficLight` has an id, a current `Signal` (initially `RED`) and configurable red/yellow/green durations (ms) with getters/setters.
3. `startTrafficControl()` starts, for every registered road, a background loop: wait red duration → GREEN → wait green duration → YELLOW → wait yellow duration → RED, repeating.
4. Every signal change is published (currently printed) so observers can react.
5. `handleEmergency(roadId)` immediately sets that road's light to `GREEN`; unknown ids are ignored.
6. `stopTrafficControl()` stops all cycling threads and waits for them to finish; calling `start` twice does not spawn duplicate threads.
7. `getCurrentSignal()` reports a light's current state at any time.

## Non-functional requirements
- One thread per light, marked daemon so a forgotten `stop` cannot keep the JVM alive; threads exit on interrupt.
- `changeSignal`/`getCurrentSignal` are `synchronized`; the emergency override and the cycling thread may race, and the cycling thread will overwrite an emergency GREEN on its next transition (the override is not sticky).
- Lights on different roads are **not** coordinated — nothing prevents two crossing roads from being green simultaneously. This is a known limitation and the main follow-up.
- `TrafficController` is a lazily-created singleton; roads live in a `HashMap` (no ordering guarantee).
- Timing uses `Thread.sleep`; no scheduler, no wall-clock precision guarantees.

## Constraints
- Exactly three signals — `Signal.RED`, `YELLOW`, `GREEN` — every light starts at `RED`, and the only cycle is RED → GREEN → YELLOW → RED (no RED+YELLOW, flashing or off states).
- `1 <= roads <= 20`: one cycling thread per road, so tens of threads, not thousands. Each road has exactly one `TrafficLight`, assigned via `setTrafficLight` **before** `startTrafficControl()` (a road with a `null` light throws `NullPointerException` on start).
- Durations are `int` milliseconds with `0 <= duration <= 60_000`; the setters do not validate, and a negative value makes `Thread.sleep` throw.
- Road ids are unique `String`s: `addRoad` with an existing id silently replaces the previous road.
- The set of cycling threads is fixed at `startTrafficControl()`: roads added afterwards are not cycled and `removeRoad` does not stop a running thread — a restart (`stop` then `start`) is required to pick up changes.
- The emergency override only ever sets `GREEN`; it never sets `RED`/`YELLOW` and never touches other roads.
- Single JVM; the controller knows only a flat map of roads — no intersection topology, phases, pedestrians or sensors.

## Clarifying questions to ask
- Do lights at the same intersection need to be mutually exclusive? — Not in the base version; each light cycles independently (ask, then flag the limitation).
- Are durations per light or global? — Per light, changeable at runtime via setters (picked up on the next cycle).
- What does "emergency" mean? — Force one road green immediately; the normal cycle resumes afterwards.
- Should the system run forever? — It runs until `stopTrafficControl()`; threads must be stoppable.
- Do pedestrians/sensors matter? — Out of scope.

## Core entities
- `TrafficController` — singleton; `Map<roadId, Road>`; starts/stops one cycling thread per road; `handleEmergency`.
- `Road` — id, name, and the `TrafficLight` assigned to it.
- `TrafficLight` — id, current `Signal`, three durations; `changeSignal(Signal)` updates state and calls `notifyObservers()` (currently prints `"<id> -> <signal>"`).
- `Signal` — enum `RED`, `YELLOW`, `GREEN`.

## Design hints
- The signal is a plain enum plus durations; the *transition logic* lives in the controller's loop. A richer version moves "next signal + duration" into a `State` object per signal so the loop just calls `light.next()`.
- Background-thread hygiene is what this problem really tests: daemon threads, exiting on `InterruptedException` (re-set the interrupt flag, don't swallow it), `join` on stop, and idempotent `start`. A loop with `while (true)` and `printStackTrace` in the catch will never terminate — a common mistake.
- `notifyObservers()` is the hook for an `Observer` pattern (display boards, logging, the controller itself); registering listeners on `TrafficLight` is the natural first extension.
- Emergency override races with the cycling thread; discuss making it sticky (pause the cycle, or a flag the loop checks) versus the current "momentary" semantics.
- **Common mistakes**: sharing one thread for all lights with sequential sleeps (lights become dependent on each other by accident); not synchronizing `currentSignal` reads; using `HashMap` and assuming insertion order in output.
- Interviewers will push toward intersection safety: a controller that owns the schedule (one road green at a time, or phases of non-conflicting roads) rather than independent lights.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :traffic-signal-system compile exec:java` |
