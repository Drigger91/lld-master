# Test cases — Car Rental System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Add cars to the fleet | `rentalSystem.addCar(new Car("Toyota","Camry",2022,"ABC123",50.0))` ×3 | Cars are retrievable through `searchCars` |
| H2 ✅ | Search by make/model for a date range | `rentalSystem.searchCars("Toyota","Camry", today, today+3)` | Returns the Camry only (Civic and Mustang filtered out) |
| H3 ✅ | Make a reservation | `rentalSystem.makeReservation(customer, camry, today, today+3)` | Non-null `Reservation` with id `RES` + 8 hex chars; `camry.isAvailable()` is false |
| H4 ✅ | Total price is inclusive of both days | Reservation for `today..today+3` on a 50.0/day car | `reservation.getTotalPrice()` is 200.0 (4 days) |
| H5 ✅ | Process payment | `rentalSystem.processPayment(reservation)` | Returns true (credit-card stub); demo prints `Reservation successful. Reservation ID: RES…` |
| H6 ⬜ | Cancel a reservation | `rentalSystem.cancelReservation(reservation.getReservationId())` | Car's `isAvailable()` is true again; car reappears in `searchCars` for the same dates |
| H7 ⬜ | Remove a car | `rentalSystem.removeCar("XYZ789")` | Subsequent `searchCars("Honda","Civic",…)` returns empty |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Same-day rental | `makeReservation(c, car, d, d)` | Succeeds; `getTotalPrice()` equals one day's price |
| E2 ⬜ | Adjacent, non-overlapping ranges | Reserve `d1..d3`; then `makeReservation(c2, sameCar, d3, d5)` | Succeeds: overlap test is end-exclusive (`d3.isBefore(d3)` is false). Note `searchCars` would *not* have listed the car because its `available` flag is already false |
| E3 ⬜ | Case-insensitive search | `searchCars("toyota","CAMRY", …)` | Returns the Camry |
| E4 ⬜ | Search with no matching make/model | `searchCars("Tesla","Model 3", …)` | Returns an empty list (demo branch prints `No available cars found…`) |
| E5 ⬜ | Cancel twice | `cancelReservation(id)` twice | Second call is a no-op (map `remove` returns null) |
| E6 ⬜ | Singleton identity | `RentalSystem.getInstance() == RentalSystem.getInstance()` | true; cars added through one reference are visible through the other |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Double-booking overlapping dates | Reserve `d1..d5`; then `makeReservation(other, sameCar, d3, d7)` | Returns null (overlap `d3 < d5 && d7 > d1`); demo branch prints `Selected car is not available…` |
| X2 ⬜ | Containing range | Reserve `d2..d4`; then reserve `d1..d6` on the same car | Returns null |
| X3 ⬜ | Payment failure path | Inject a `PaymentProcessor` returning false; reserve then `processPayment` | Returns false; caller cancels the reservation and the car becomes available (demo has the branch but the stub never fails) |
| X4 ⬜ | End before start | `makeReservation(c, car, d5, d1)` | Reference code accepts it and computes a negative day count → negative price; should throw `IllegalArgumentException` |
| X5 ⬜ | Unknown reservation id | `cancelReservation("RESNOPE")` | No-op, no exception |
| X6 ⬜ | Null car / customer | `makeReservation(null, null, d1, d2)` | `NullPointerException` from `reservation.getCar().equals(car)` / `car.getRentalPricePerDay()`; should validate arguments |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two customers race for the same car and dates | Two threads call `makeReservation(cX, car, d1, d3)` simultaneously | Exactly one gets a `Reservation`; the other gets null (`makeReservation` is synchronized) |
| C2 ⬜ | Search during booking | Thread A books while thread B searches | No `ConcurrentModificationException` (`ConcurrentHashMap` iteration is weakly consistent) |

## Interviewer follow-ups / extensions
- How would you make the car available again after the rental period? — Drop the `available` flag and rely solely on overlap checks, or add a scheduled job that closes reservations past `endDate`.
- How would you support multiple pickup locations? — Add `Location` to `Car` and to the search criteria; reservations record pickup and drop-off locations.
- How would you add pricing rules (weekend surcharge, loyalty discount)? — `PricingStrategy` injected into `Reservation`/`RentalSystem` instead of the hard-coded `pricePerDay × days`.
- How would you choose the payment method per reservation? — Pass a `PaymentProcessor` (or a `PaymentMethod` enum resolved through a factory) into `processPayment`.
- How would you track a reservation's lifecycle? — `ReservationStatus` enum (PENDING_PAYMENT, CONFIRMED, ACTIVE, COMPLETED, CANCELLED) with guarded transitions.
