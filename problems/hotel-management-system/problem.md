# Hotel Management System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, State (enum-guarded transitions), Strategy (Payment) | room lifecycle, reservation lifecycle, synchronized booking, fail-fast IllegalStateException, UUID ids |

## Problem statement
Design the reservation system for a hotel. Guests book a room for a check-in/check-out date range, check in on arrival, and pay at check-out; a booking can be cancelled before check-in. A room must never be booked by two guests at the same time, and every room and reservation must always be in a well-defined state.

## Functional requirements
1. Register guests and rooms (rooms have a type and a nightly price).
2. Book an available room for a guest and a date range, producing a `Reservation` with a unique id; refuse if the room is not available.
3. Check a guest in against a confirmed reservation, moving the room to OCCUPIED.
4. Check a guest out: compute the bill (`price × nights`), take payment through a pluggable `Payment`, free the room and close the reservation.
5. Cancel a confirmed reservation before check-in, freeing the room.
6. Reject invalid transitions (check-in without a booking, check-out of a room that is not occupied, cancelling a cancelled reservation) with an exception.

## Non-functional requirements
- `HotelManagementSystem` is a singleton with in-memory `ConcurrentHashMap`s for guests, rooms and reservations; nothing is persisted.
- `bookRoom`, `cancelReservation`, `checkIn`, `checkOut` are `synchronized` on the system; `Room` and `Reservation` state changes are additionally `synchronized` on the entity.
- Bill = `price × DAYS.between(checkIn, checkOut)` (nights, exclusive of check-out day).
- Payment implementations (`CashPayment`, `CreditCardPayment`) are stubs that always succeed.
- Completed and cancelled reservations are removed from the registry (no history).

## Constraints
- Fixed vocabularies: `RoomType` is exactly `SINGLE, DOUBLE, DELUXE, SUITE`; `RoomStatus` is exactly `AVAILABLE, BOOKED, OCCUPIED`; `ReservationStatus` is exactly `CONFIRMED, CANCELLED` (there is no `COMPLETED`).
- Availability is per room, not per date: a room can be booked only while its status is `AVAILABLE`, so a booked room is unavailable for *every* date until it is checked out or cancelled.
- Invariant: a room has at most one live reservation, and its status only changes via `book()`, `checkIn()`, `release()`, `checkOut()` along `AVAILABLE→BOOKED`, `BOOKED→OCCUPIED`, `BOOKED→AVAILABLE` (cancel) and `OCCUPIED→AVAILABLE` (check-out); anything else throws.
- Dates are `LocalDate`s at day granularity; `checkIn < checkOut` is assumed and never validated (a same-day range bills zero nights), and nothing compares them to the real clock.
- Guest and room ids are caller-supplied strings (`G001`, `R001`) and re-adding an id overwrites; reservation ids are `RES` + 8 uppercase hex characters from a UUID.
- One hotel with tens of rooms and hundreds of reservations; every registry is a flat map with no index by room type or date.
- Exactly two `Payment` implementations, `CashPayment` and `CreditCardPayment`; amounts are `double`; one room and one guest per reservation; no deposits, refunds, housekeeping or maintenance states.

## Clarifying questions to ask
- Is availability per date or per room? — Per room status; date-based availability is a follow-up.
- When is payment taken? — At check-out, for the whole stay.
- Can a reservation be cancelled after check-in? — No; `Reservation.cancel()` only releases a BOOKED room, an occupied room must check out.
- What does check-in validate? — That the reservation exists and is CONFIRMED and that the room is BOOKED.
- Multiple rooms per reservation? — No, one room per reservation.
- How are failures reported? — Booking an unavailable room returns `null`; illegal transitions throw `IllegalStateException`.

## Core entities
- `HotelManagementSystem` — singleton facade: registries for guests/rooms/reservations; `bookRoom`, `checkIn`, `checkOut`, `cancelReservation`.
- `Room` — id, `RoomType`, price, `RoomStatus`; guarded transitions `book()`, `checkIn()`, `checkOut()`, `release()`.
- `RoomStatus` — `AVAILABLE → BOOKED → OCCUPIED → AVAILABLE` (and `BOOKED → AVAILABLE` on cancel).
- `RoomType` — `SINGLE, DOUBLE, DELUXE, SUITE`.
- `Reservation` — id, guest, room, check-in/out dates, `ReservationStatus`; `cancel()` releases the room.
- `ReservationStatus` — `CONFIRMED, CANCELLED`.
- `Guest` — id, name, email, phone.
- `payment.Payment` — strategy interface `processPayment(double)`; `CashPayment`, `CreditCardPayment`.

## Design hints
- **Two state machines, one owner each**: `Room` owns its status and throws on illegal transitions; `Reservation` owns its status. The system coordinates them but never mutates a status field directly. Interviewers will ask you to draw both machines and to name the transition that cancel uses (`BOOKED → AVAILABLE`, distinct from check-out's `OCCUPIED → AVAILABLE`).
- **Fail fast with `IllegalStateException`**: a guarded transition is easier to test and reason about than silently ignoring the call. Be ready to argue whether `bookRoom` returning `null` is consistent with that (it isn't; a `RoomNotAvailableException` or `Optional` is cleaner).
- **Strategy for payment**: the system only depends on `Payment`, so new methods are new classes. Payment failure at check-out throws and leaves the room OCCUPIED, which is the desired behaviour.
- **Coarse locking**: one system-wide lock is simple and correct; a per-room lock (or relying on `Room`'s own `synchronized` methods) scales better. Note the check `room.getStatus() == AVAILABLE` followed by `room.book()` must be atomic — the outer lock provides that.
- Common mistakes: modelling availability as a boolean and losing the BOOKED vs. OCCUPIED distinction; freeing a room on cancel via `checkOut()` (the original bug in this module); deleting reservations instead of keeping a `COMPLETED` status; `double` for money.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :hotel-management-system compile exec:java` |
