# Parking Lot

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, Inheritance/polymorphism (Vehicle hierarchy), Composition | multi-level layout, typed spots, enums as vehicle type, first-fit allocation, synchronized access |

## Problem statement
Design a multi-level parking lot. Vehicles of different types (motorcycle, car, truck) arrive and must be assigned a free spot compatible with their type; when they leave, the spot must be released. The system should be able to report which spots are free on each level and should behave correctly when several entry/exit gates operate at the same time.

## Functional requirements
1. The lot has multiple levels; each level has a fixed number of numbered spots.
2. Each spot is dedicated to exactly one vehicle type (`MOTORCYCLE`, `CAR`, `TRUCK`); a level allocates a share of its spots to each type.
3. `parkVehicle(vehicle)` finds the first free spot of the matching type across levels and occupies it; returns `true` on success, `false` if no compatible spot is free anywhere.
4. `unparkVehicle(vehicle)` frees the spot occupied by that vehicle; returns `false` if the vehicle is not in the lot.
5. `displayAvailability()` prints, per level, each spot's number, type and whether it is free or which licence plate occupies it.
6. There is exactly one parking lot instance in the process.

## Non-functional requirements
- In-memory only; no persistence, no fees/tickets, no time tracking.
- Multiple gates may park/unpark concurrently: `Level` methods and `ParkingSpot` state changes are `synchronized`, so a spot can never be double-booked.
- Spot allocation is first-fit (lowest level, lowest spot number) — no optimisation for proximity or balancing.
- Levels are added at setup time via `addLevel`; the `ParkingLot` singleton is created lazily.

## Constraints
- `1 <= levels <= 10` and `1 <= spots per level <= 1000`; at most a few thousand vehicles in the lot at once, so an O(spots) linear scan per park/unpark is acceptable.
- Exactly three vehicle types — `VehicleType.MOTORCYCLE`, `CAR`, `TRUCK` — and exactly three `Vehicle` subclasses (`Motorcycle`, `Car`, `Truck`); every spot and every vehicle has exactly one type.
- The type split per level is fixed at construction: the first `floor(0.20 * total)` spots are `MOTORCYCLE`, the next `floor(0.50 * total)` are `CAR`, the rest `TRUCK`. `Level(1, 10)` is 2/5/3; a level with fewer than 5 spots has **zero** motorcycle spots.
- A spot holds at most one vehicle, and only a vehicle whose type equals the spot's type; `ParkingSpot.parkVehicle` throws `IllegalArgumentException` otherwise.
- Spot numbers are `0 .. total-1` within a level and are unique only per level; levels are searched in `addLevel` order.
- Vehicles are identified by object identity, not licence plate: plates are not checked for uniqueness, and the lot does not detect a vehicle that is already parked — the caller must not park the same object twice.
- Single JVM, one lot: levels can be added but never removed, and a level's spot count and type layout never change after construction.

## Clarifying questions to ask
- Can a smaller vehicle take a larger spot (motorcycle in a car spot)? — No, spots are strictly typed.
- How are spots split among types? — Each level reserves 20% motorcycle, 50% car, remainder truck (integer truncation).
- Is there a ticket/fee component? — Out of scope; only occupancy is tracked.
- Do we need to know *where* a vehicle is parked? — Not returned to the caller; only a boolean success is returned.
- Concurrency? — Yes, assume concurrent entry/exit gates; correctness of spot assignment matters, throughput does not.

## Core entities
- `ParkingLot` — singleton facade; holds `Level`s and delegates park/unpark/display to them in order.
- `Level` — a floor with a list of `ParkingSpot`s; allocates spot types at construction; synchronized first-fit search.
- `ParkingSpot` — one numbered spot with a fixed `VehicleType` and an optional parked `Vehicle`; guards its own state.
- `Vehicle` (abstract) — licence plate + `VehicleType`; subclasses `Car`, `Motorcycle`, `Truck` fix the type.
- `VehicleType` — enum `CAR`, `MOTORCYCLE`, `TRUCK`.

## Design hints
- **Singleton** for `ParkingLot` models the single physical lot; interviewers may ask why not just one instance by convention — be ready to discuss testability.
- **Vehicle hierarchy** with the type fixed in the subclass constructor keeps `ParkingSpot` compatibility checks a simple enum comparison; no `instanceof`.
- **Two levels of locking**: `Level.parkVehicle` is synchronized so "find free spot then occupy it" is atomic per level; `ParkingSpot` is also synchronized so its state is safe even if accessed directly. Discuss the coarse-grained lock trade-off (one lock per level serialises all gates on that level).
- **Common mistake**: creating all spots with a default type (every spot `CAR`) so trucks and motorcycles can never park — spot types must be allocated deliberately.
- **Common mistake**: `unparkVehicle` relying on `equals` — `Vehicle` uses identity equality, so the same object must be passed back; a real system would look up by licence plate.
- Follow-ups usually go to tickets with entry time and fee `Strategy`, a display board (`Observer`), or letting small vehicles use larger spots.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :parking-lot compile exec:java` |
