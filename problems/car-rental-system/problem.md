# Car Rental System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, Strategy (PaymentProcessor) | date-range overlap, availability flag vs. reservations, synchronized booking, UUID ids |

## Problem statement
Design a car rental service. The company keeps a fleet of cars, customers search for a car by make and model for a date range, reserve it, and pay. Two customers must never end up with the same car for overlapping dates, and a reservation can be cancelled to free the car again.

## Functional requirements
1. Add and remove cars from the fleet, identified by licence plate.
2. Search cars by make and model (case-insensitive) that are available for a given `[startDate, endDate]`.
3. Make a reservation for a customer, car and date range; reject it if the car already has an overlapping reservation.
4. Compute a reservation's total price as `pricePerDay × number of days` (inclusive of both ends).
5. Process payment for a reservation through a pluggable payment processor.
6. Cancel a reservation by id, making the car available again.

## Non-functional requirements
- `RentalSystem` is a process-wide singleton holding cars and reservations in `ConcurrentHashMap`s; nothing is persisted.
- `makeReservation` and `cancelReservation` are `synchronized` so concurrent bookings cannot double-book.
- The payment processor is fixed to `CreditCardPaymentProcessor` at construction; the `PayPalPaymentProcessor` exists but is not wired in.
- Both payment processors are stubs that always return `true`.
- A reserved car is flagged `available = false` until cancellation, which hides it from *all* searches, not just overlapping dates (a simplification to discuss).

## Constraints
- Fleet and booking volume are small — tens of cars and hundreds of reservations — so `searchCars` and `isCarAvailable` scan every car and every reservation linearly.
- A car is identified by its licence plate; `addCar` with an existing plate silently replaces the earlier car.
- Dates are `LocalDate`s at day granularity (no times, no time zones); `startDate <= endDate` is assumed and never validated, and a same-day rental counts as one day.
- Invariant: the reservation registry never holds two reservations for the same car whose ranges overlap (`start < existing.end && end > existing.start`).
- Exactly two `PaymentProcessor` implementations, `CreditCardPaymentProcessor` and `PayPalPaymentProcessor`; amounts are `double` and there is no payment status, refund or partial payment.
- Reservation ids are `RES` + 8 uppercase hex characters from a UUID; a reservation has no status — it is either in the registry or removed by cancellation (no history).
- Single location, single JVM: no pickup/return branches, no customer registry (a `Customer` is a plain value handed to `makeReservation`), and no real clock — the caller supplies every date.

## Clarifying questions to ask
- One location or many? — Single location; no pickup/return branches.
- How are days counted? — Inclusive: a same-day rental costs one day.
- What does overlap mean? — `start < existing.end && end > existing.start`; touching ranges (return day == next start day) do *not* overlap.
- What happens after the rental ends — does the car become available automatically? — No; only cancellation resets the flag in the reference code (follow-up).
- Payment method per customer? — Not in scope; one processor is injected into the system.
- Is payment part of making the reservation? — No; `makeReservation` then `processPayment`, and the caller cancels if payment fails.

## Core entities
- `RentalSystem` — singleton facade: fleet + reservation registry; `addCar`, `removeCar`, `searchCars`, `makeReservation`, `cancelReservation`, `processPayment`.
- `Car` — make, model, year, licence plate, `rentalPricePerDay`, mutable `available` flag.
- `Customer` — name, contact info, driver's licence number.
- `Reservation` — id, customer, car, date range; computes `totalPrice` at construction.
- `payment.PaymentProcessor` — strategy interface `processPayment(double)`; implementations `CreditCardPaymentProcessor`, `PayPalPaymentProcessor`.

## Design hints
- **Availability is derived, not stored**: the correct source of truth is the set of reservations for a car; `isCarAvailable` already checks overlaps. The `available` boolean is a second source of truth that goes stale (a booked car disappears for every date). Interviewers probe this — argue for dropping the flag or for a `RESERVED/RENTED/MAINTENANCE` status that is about the car, not a booking.
- **Strategy for payment**: `RentalSystem` only knows `PaymentProcessor`, so adding UPI or wallet payments is a new class. Suggest passing the processor per reservation rather than fixing it in the constructor.
- **Singleton trade-off**: convenient for a demo, painful for tests (state leaks between tests). Show that you know to inject `RentalSystem` instead, or at least offer a reset hook.
- **Synchronize the check-and-book**: the overlap check and `reservations.put` must be atomic, hence `synchronized makeReservation`. A per-car lock would scale better than one global lock.
- Common mistakes: off-by-one in day counting; exclusive vs. inclusive end dates; overlap conditions that miss containment; `double` for money; returning `null` instead of an explicit result/exception.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :car-rental-system compile exec:java` |
