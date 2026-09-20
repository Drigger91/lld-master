# Airline Management System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Facade, Singleton, State (enums) | flight search, booking lifecycle, seat status, payment status, id generation, locking |

## Problem statement
Design the core of an airline reservation system. The airline operates a fleet of aircraft and a schedule of flights between airports. A passenger searches for flights by source, destination and date, books a seat on a chosen flight at a given price, pays for the booking, and may later cancel it. Bookings must have unique booking numbers, and seat and booking states must be tracked. In-memory, single process.

## Functional requirements
1. Register `Aircraft` (tail number, model, seat count) and `Flight`s (flight number, source, destination, departure and arrival times).
2. Search flights by source, destination (case-insensitive) and departure date.
3. Create a `Booking` for a `Passenger` on a `Flight` with a `Seat` and a price; it is `CONFIRMED` on creation and gets a unique booking number.
4. Cancel a booking by booking number; its status becomes `CANCELLED`.
5. Track seat status (`AVAILABLE`, `RESERVED`, `OCCUPIED`) via `Seat.reserve()` / `Seat.release()`.
6. Process a `Payment`, moving it from `PENDING` to `COMPLETED`.

## Non-functional requirements & constraints
- In-memory; no persistence.
- `BookingManager` and `PaymentProcessor` are singletons; `AirlineManagementSystem` is an ordinary facade object that composes them.
- Booking numbers are `"BKG" + yyyyMMddHHmmss + 6-digit counter` from an `AtomicInteger`, so they are unique within a process.
- `BookingManager` guards its `HashMap` with an explicit lock object; `flights`/`aircrafts` lists are plain `ArrayList`s and not thread-safe.
- Seat allocation is not enforced by the system: the caller reserves the `Seat` object and passes it in; there is no check that the seat belongs to the flight or is available. `Flight.availableSeats` exists but is never populated.
- Payment processing always succeeds; there is no gateway and no linkage between `Payment` and `Booking`.

## Clarifying questions to ask
- Does booking a flight also reserve the seat and take payment atomically? — Assumed: no; `bookFlight`, `Seat.reserve()` and `processPayment` are separate calls made by the client.
- Should search match on exact date or a range? — Assumed: exact departure date (`LocalDate` equality), airports compared case-insensitively.
- Should cancelling a booking release the seat and refund the payment? — Assumed: only the booking status changes; seat release and refund are the caller's job (`REFUNDED` status exists but is unused).
- Is pricing dynamic? — Assumed: caller supplies the price.
- One passenger per booking? — Assumed: yes.

## Core entities
- `AirlineManagementSystem` — facade: holds flights/aircraft, delegates to `FlightSearch`, `BookingManager`, `PaymentProcessor`.
- `Aircraft` — tail number, model, total seats.
- `flight.Flight` — flight number, source, destination, departure/arrival `LocalDateTime`, (unused) seat list.
- `flight.FlightSearch` — stream filter over the flight list by source/destination/date.
- `passenger.Passenger` — id, name, email, phone.
- `seat.Seat` / `SeatType` / `SeatStatus` — seat number, class (`ECONOMY … FIRST_CLASS`), mutable status.
- `booking.Booking` / `BookingStatus` — booking number, flight, passenger, seat, price, status (`CONFIRMED`, `CANCELLED`, `PENDING`, `EXPIRED`).
- `booking.BookingManager` — singleton store of bookings; creates numbers, confirms and cancels under a lock.
- `payment.Payment` / `PaymentProcessor` / `PaymentStatus` — payment record and singleton processor that marks it `COMPLETED`.

## Design hints
- **Facade**: `AirlineManagementSystem` exposes a small API (`addFlight`, `searchFlights`, `bookFlight`, `cancelBooking`, `processPayment`) and hides the subsystems; interviewers like to see search, booking and payment as separate collaborators with one entry point.
- **Singleton** for `BookingManager`/`PaymentProcessor` gives process-wide uniqueness of booking numbers; discuss injecting them instead for testability.
- **State via enums**: `BookingStatus`, `SeatStatus`, `PaymentStatus` are simple enums mutated by dedicated methods (`cancel()`, `reserve()`, `processPayment()`), not by generic setters — a good habit to point out.
- **ID generation**: timestamp + atomic counter is readable and unique per process; for multi-node you need a distributed id (Snowflake, DB sequence).
- Key trade-off to probe: the system does not make "check seat available -> reserve -> create booking -> pay" atomic. A real design puts the seat map on the `Flight`, reserves under a per-flight lock, and holds the seat `RESERVED` with a timeout until payment completes (`PENDING`/`EXPIRED` statuses exist for exactly this).
- Common mistakes: searching by `LocalDateTime` equality instead of date; forgetting that `HashMap` needs external locking; exposing mutable lists; never populating `Flight.availableSeats`.

## Run
mvn -q -pl problems/airline-management-system compile exec:java
