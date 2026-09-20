# Test cases — Parking Lot

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Park a car | `parkingLot.addLevel(new Level(1, 10))`; `parkingLot.parkVehicle(new Car("ABC123"))` | returns `true`; first `CAR` spot on level 1 shows "Occupied by ABC123" |
| H2 ✅ | Park a truck and a motorcycle | `parkVehicle(truck)`, `parkVehicle(motorcycle)` | both `true`; each lands in a spot of its own type |
| H3 ✅ | Display availability | `parkingLot.displayAvailability()` | every level lists every spot with number, type and Available/Occupied |
| H4 ✅ | Unpark a parked vehicle | `parkingLot.unparkVehicle(motorcycle)` | returns `true`; its spot is Available on the next display |
| H5 ⬜ | Same spot reused after unpark | park M1, unpark M1, park M2 | M2 occupies the spot M1 vacated |
| H6 ✅ | Spill over to the next level | fill all motorcycle spots on level 1, park another motorcycle | `true`; it is placed on level 2 |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | No compatible spot anywhere | fill every motorcycle spot on all levels, `parkVehicle(new Motorcycle("M5"))` | returns `false`; nothing changes |
| E2 ⬜ | Compatible spots free only on another type | all `CAR` spots full, `TRUCK` spots free, `parkVehicle(car)` | `false` — a car never takes a truck spot |
| E3 ⬜ | Level with few spots | `new Level(3, 1)` | 0 motorcycle, 0 car, 1 truck spot (20%/50% truncate to 0) |
| E4 ⬜ | Same vehicle parked twice | `parkVehicle(car)` twice without unparking | second call also returns `true` and occupies a second spot (no duplicate check) |
| E5 ⬜ | Singleton | `ParkingLot.getInstance() == ParkingLot.getInstance()` | same instance |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Unpark a vehicle that is not in the lot | `parkingLot.unparkVehicle(new Car("NOTHERE"))` | returns `false` |
| X2 ⬜ | Direct spot misuse | `spot.parkVehicle(truck)` on a `CAR` spot, or on an occupied spot | throws `IllegalArgumentException("Invalid vehicle type or spot already occupied.")` |
| X3 ⬜ | Unpark with an equal-but-different object | park `new Car("A")`, unpark another `new Car("A")` | `false` — identity equality, plate is not compared |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two gates park into one free spot | level with 1 car spot; two threads call `parkVehicle(car1)` / `parkVehicle(car2)` simultaneously | exactly one returns `true`, the other `false` |
| C2 ⬜ | Park and unpark racing | thread A `unparkVehicle(car1)`, thread B `parkVehicle(car2)` on a full level | no exception; final state has at most one vehicle per spot |

## Interviewer follow-ups / extensions
- How would you allow a motorcycle to use a car spot when motorcycle spots are full? — Make compatibility a method on `VehicleType` (or a spot-size ordering) instead of `==`.
- How would you add tickets and fees? — Return a `Ticket` (plate, spot, entry time) from `parkVehicle`; fee via a `Strategy` per vehicle type, computed on `unpark`.
- How would you find a vehicle quickly? — Keep a `Map<licensePlate, ParkingSpot>` in `ParkingLot`, updated under the same lock.
- How would you show live counts on a display board? — `Observer` on spot state changes, or per-level counters updated in park/unpark.
- How would you reduce lock contention? — Per-type free-spot queues instead of scanning a list under a level-wide lock.
