# Test cases — Traffic Signal System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

Setup: `tc = TrafficController.getInstance()`; roads R1–R4 each with `new TrafficLight("TLn", red=300, yellow=100, green=400)` (ms) attached via `road.setTrafficLight(...)` and registered with `tc.addRoad(road)`.

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Initial state | `light.getCurrentSignal()` before start | `RED` |
| H2 ✅ | Register roads | `tc.addRoad(road1..4)`; `tc.getRoad("R1")` | returns road1; `getTrafficLight()` is TL1 |
| H3 ✅ | Lights cycle after start | `tc.startTrafficControl()`; sleep ~1 s | each light prints `-> GREEN`, `-> YELLOW`, `-> RED` in that order, once per cycle (300+400+100 ms) |
| H4 ⬜ | Durations are honoured | record timestamps of transitions for one light | GREEN arrives ~300 ms after start, YELLOW ~400 ms later, RED ~100 ms after that |
| H5 ✅ | Emergency override | while running: `tc.handleEmergency("R2")`; `TL2.getCurrentSignal()` | prints `TL2 -> GREEN`; getter returns `GREEN` immediately |
| H6 ✅ | Clean shutdown | `tc.stopTrafficControl()` | returns after all cycling threads exit; no further transitions are printed; JVM exits |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Start called twice | `startTrafficControl()` twice | second call is a no-op; still one thread per road |
| E2 ⬜ | Stop without start | `stopTrafficControl()` on a fresh controller | returns immediately, no exception |
| E3 ⬜ | Change durations at runtime | `light.setGreenDuration(1000)` while running | takes effect on the next cycle (the current sleep is not shortened) |
| E4 ⬜ | Emergency is not sticky | `handleEmergency("R2")` then wait for TL2's cycle to continue | TL2 later moves to YELLOW/RED per its normal loop |
| E5 ⬜ | Road removed while running | `tc.removeRoad("R3")` after start | its thread keeps cycling until `stop` (threads are captured at start) |
| E6 ⬜ | Singleton | `TrafficController.getInstance() == TrafficController.getInstance()` | same instance |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Emergency on unknown road | `tc.handleEmergency("R99")` | ignored; no exception, no output |
| X2 ⬜ | Road without a light | `tc.addRoad(new Road("R5", "No Light"))`; `startTrafficControl()` | thread throws `NullPointerException` on first use (no validation) — follow-up: validate in `addRoad` |
| X3 ⬜ | Emergency on road without a light | `addRoad(roadWithoutLight)`; `handleEmergency(its id)` | `NullPointerException` (no validation) |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Override races with cycle | call `handleEmergency("R1")` repeatedly while TL1 cycles | `getCurrentSignal()` always returns a valid `Signal`; no torn state (both paths use the light's monitor) |
| C2 ⬜ | Independent lights | 4 lights running | each light's own sequence is always GREEN → YELLOW → RED; different lights may be green at the same time (no coordination) |
| C3 ⬜ | Stop is prompt | `stopTrafficControl()` mid-sleep | threads are interrupted and exit without completing the sleep; `stop` returns within a few ms |

## Interviewer follow-ups / extensions
- How would you guarantee crossing roads are never green together? — Move scheduling into the controller: a single scheduler thread runs phases (sets of non-conflicting roads) rather than one free-running thread per light.
- How would you make the emergency override sticky? — A `volatile` override flag the cycle loop checks, or pause the road's thread and resume after the emergency is cleared.
- How would you attach display boards/logging? — Implement the `notifyObservers` hook: `TrafficLight.addObserver(SignalObserver)` and call each on `changeSignal`.
- How would you avoid one thread per light? — A `ScheduledExecutorService` that schedules each light's next transition as a task.
- How would you add sensors/adaptive timing? — Let the controller adjust durations before each phase from a traffic-density input (`Strategy` for timing policy).
