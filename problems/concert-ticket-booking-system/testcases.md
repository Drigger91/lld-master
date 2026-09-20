# Test cases — Concert Ticket Booking System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Add concerts | `system.addConcert(concert1)` (100 seats), `system.addConcert(concert2)` (50 seats) | `system.getConcert("C001")` returns `concert1` |
| H2 ✅ | Search by artist, venue, date-time | `system.searchConcerts("Artist 1", "Venue 1", concert1Date)` | List containing exactly `concert1` (artist/venue matched case-insensitively) |
| H3 ✅ | Book several seats | `booking1 = system.bookTickets(user1, concert1, [S1,S2,S3])` | `booking1.getStatus()==CONFIRMED`; all three seats `BOOKED`; `getTotalPrice()==300.0` (3 x VIP 100.0); id starts with `BKG` |
| H4 ✅ | Book on another concert | `booking2 = system.bookTickets(user2, concert2, [S1,S2])` | Independent booking; concert2's S1/S2 `BOOKED`, concert1's unaffected |
| H5 ✅ | Cancel a booking frees its seats | `system.cancelBooking(booking1.getId())` | `booking1.getStatus()==CANCELLED`; S1..S3 of concert1 back to `AVAILABLE` |
| H6 ✅ | Re-book freed seats | `booking3 = system.bookTickets(user2, concert1, [S1,S2])` (first two available) | Succeeds; S1, S2 `BOOKED` again |
| H7 ⬜ | Total price across tiers | Book one VIP (100), one PREMIUM (75), one REGULAR (50) seat | `getTotalPrice()==225.0` |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Cancel the same booking twice | `system.cancelBooking(id)` twice | Second call is a no-op (booking already removed); seats stay `AVAILABLE` |
| E2 ⬜ | Cancel an unknown booking id | `system.cancelBooking("nope")` | No-op, no exception |
| E3 ⬜ | Search with wrong case | `searchConcerts("artist 1", "VENUE 1", date)` | Still matches (case-insensitive) |
| E4 ⬜ | Search with date off by one second | `searchConcerts("Artist 1","Venue 1", date.plusSeconds(1))` | Empty list — exact `LocalDateTime` equality |
| E5 ⬜ | Book zero seats | `system.bookTickets(user, concert, [])` | Booking created with total 0.0 and `CONFIRMED` — arguably should be rejected |
| E6 ⬜ | Confirm an already-confirmed booking | `booking.confirmBooking()` again | Status unchanged (`CONFIRMED`) |
| E7 ⬜ | Cancel a `PENDING` booking directly | `new Booking(...)` then `booking.cancelBooking()` | No change — only `CONFIRMED` bookings can be cancelled |
| E8 ⬜ | Release an `AVAILABLE` seat | `seat.release()` on a free seat | Stays `AVAILABLE` |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Book an already-booked seat | Book `[S1]`, then `system.bookTickets(user2, concert1, [S1])` | Throws `SeatNotAvailableException("Seat S1 is not available.")`; no new booking stored |
| X2 ⬜ | Partial availability is all-or-nothing | Book `[S1]`; then `bookTickets(user2, concert1, [S5, S1])` | Throws; `S5` remains `AVAILABLE` (nothing booked) |
| X3 ⬜ | `Seat.book()` on a booked seat | `seat.book()` twice | Second call throws `SeatNotAvailableException` |
| X4 ⬜ | Seats from a different concert | `bookTickets(user, concert1, seatsOfConcert2)` | Currently accepted — the system does not validate seat ownership |
| X5 ⬜ | Unknown concert lookup | `system.getConcert("C999")` | Returns `null` |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two users race for the same seat | Two threads call `bookTickets(u1, c, [S1])` and `bookTickets(u2, c, [S1])` simultaneously | Exactly one succeeds; the other throws `SeatNotAvailableException`; S1 `BOOKED` once |
| C2 ⬜ | Overlapping multi-seat requests | Thread A wants `[S1,S2]`, thread B wants `[S2,S3]` | One booking wins fully; the loser gets nothing (no partial booking) |
| C3 ⬜ | Concurrent bookings on different concerts | N threads book on distinct concerts | All succeed; note they serialise on the single global lock |
| C4 ⬜ | Cancel while booking | Thread A cancels booking with S1; thread B books S1 | Either B fails (before release) or succeeds (after); never a double booking |

## Interviewer follow-ups / extensions
- How would you reduce lock contention? — Lock per concert (or per seat with ordered acquisition) instead of one global lock.
- How would you add a payment hold? — Set seats `RESERVED` and booking `PENDING`; confirm on payment success; a scheduled task expires holds and releases seats.
- How would you support "best available N seats"? — A seat-selection strategy over the concert's seat map (contiguity, tier preference).
- How would you make payment real? — `PaymentGateway` interface, retry/idempotency keys, compensate (release seats) on failure.
- How would you search by date rather than instant? — Compare `dateTime.toLocalDate()`; add range queries and an index by artist/venue.
- How would you persist and scale out? — Seat rows with optimistic version, `UPDATE ... WHERE status='AVAILABLE'`, or a distributed lock per seat.
