# Concert Ticket Booking System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, State (enums), custom exception | seat inventory, atomic multi-seat booking, locking, booking lifecycle, search |

## Problem statement
Design a ticket booking system for concerts. Each concert has a venue, date and a fixed set of seats in tiers (regular, premium, VIP) with different prices. Users search for a concert, pick one or more available seats and book them; the booking is confirmed once payment succeeds. A user can cancel a confirmed booking, which frees the seats for others. Two users must never end up with the same seat. In-memory, single process.

## Functional requirements
1. Add concerts (id, artist, venue, date-time, seat list) and look one up by id.
2. Search concerts by artist, venue and date-time.
3. Book a set of seats for a user on a concert: all requested seats must be `AVAILABLE`, otherwise the whole booking fails with `SeatNotAvailableException`.
4. Compute the booking's total price as the sum of seat prices; process payment (stub) and mark the booking `CONFIRMED`.
5. Cancel a booking by id: status becomes `CANCELLED`, its seats return to `AVAILABLE`, and the booking is removed.
6. Track seat status (`AVAILABLE`, `BOOKED`, `RESERVED`) and booking status (`PENDING`, `CONFIRMED`, `CANCELLED`).

## Non-functional requirements & constraints
- In-memory; `ConcertTicketBookingSystem` is a process-wide singleton.
- `bookTickets` runs inside a single global lock so the availability check and the seat state change are atomic across all seats in the request; `Seat.book()`/`release()` are additionally `synchronized`.
- `concerts` and `bookings` are `ConcurrentHashMap`s.
- Payment is a no-op stub that always succeeds; there is no timeout for `PENDING` bookings and `RESERVED` is never set by the code.
- Search requires an exact `LocalDateTime` match (to the nanosecond), not a date.

## Clarifying questions to ask
- Is booking all-or-nothing when several seats are requested? — Assumed: yes; if any seat is unavailable, no seat is booked.
- Should there be a hold/reservation window before payment? — Assumed: no; seats go straight to `BOOKED` and the booking is confirmed in the same call.
- Can the system pick seats for the user ("best available")? — Assumed: no; the caller passes explicit `Seat` objects.
- Is one global lock acceptable? — Assumed: yes for now; discuss per-concert locking as a follow-up.
- Does cancelling refund the payment? — Assumed: out of scope (payment is a stub).

## Core entities
- `ConcertTicketBookingSystem` — singleton facade: concert registry, search, `bookTickets`, `cancelBooking`, payment stub.
- `Concert` — id, artist, venue, `LocalDateTime`, list of `Seat`s.
- `Seat` / `SeatType` / `SeatStatus` — id, seat number, tier, price, mutable status with synchronized `book()`/`release()`.
- `Booking` / `BookingStatus` — id, user, concert, seats, computed total price, status with `confirmBooking()`/`cancelBooking()` guards.
- `User` — id, name, email.
- `SeatNotAvailableException` — unchecked exception thrown when a requested seat is not `AVAILABLE`.

## Design hints
- **Atomic multi-seat booking**: check every seat first, then book all, all under one lock — this is the crux. Without the lock, two threads can both pass the availability check and double-book. Point out the trade-off: a single global lock serialises all bookings for all concerts; a per-concert lock (or per-seat CAS with rollback) scales better.
- **State via guarded methods**: `Booking.confirmBooking()` only works from `PENDING`, `cancelBooking()` only from `CONFIRMED`, `Seat.release()` only from `BOOKED`. Enum + guard methods keeps illegal transitions out without a full State class hierarchy.
- **Fail fast with a domain exception**: `SeatNotAvailableException` carries the seat number so the client can retry with different seats.
- Price is computed once in the `Booking` constructor from seat prices — immutable total, no drift if prices change later.
- Common mistakes: booking seats one by one and leaving partial bookings on failure; comparing `LocalDateTime.now()`-derived values in search (they never match); forgetting to release seats on cancel; removing the booking from the map but leaving seats `BOOKED`; using `RESERVED` without an expiry.

## Run
mvn -q -pl problems/concert-ticket-booking-system compile exec:java
