# Test cases — Airline Management System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register fleet and schedule | `airline.addAircraft(new Aircraft("N12345","Boeing 737",180))`; `airline.addFlight(f1)`, `addFlight(f2)`, `addFlight(f3)` | No exception; flights are searchable |
| H2 ✅ | Search by route and date | `airline.searchFlights("nyc","lax", date)` | Returns only `AA101` (NYC->LAX on `date`); `AA202` (other destination) and `AA303` (next day) excluded; airport match is case-insensitive |
| H3 ✅ | Book a seat | `seat.reserve()`; `booking = airline.bookFlight(flight, passenger, seat, 250.0)` | `booking.getStatus()==CONFIRMED`; booking number starts with `BKG` and ends with `000001`; `seat.getStatus()==RESERVED` |
| H4 ✅ | Pay for the booking | `payment = new Payment("PAY1","CREDIT_CARD", booking.getPrice())`; `airline.processPayment(payment)` | Status goes `PENDING -> COMPLETED` |
| H5 ✅ | Cancel booking and release seat | `airline.cancelBooking(booking.getBookingNumber())`; `seat.release()` | `booking.getStatus()==CANCELLED`; `seat.getStatus()==AVAILABLE` |
| H6 ⬜ | Multiple bookings get distinct numbers | Call `airline.bookFlight(...)` twice | Booking numbers differ (counter suffix `000001`, `000002`) |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | Search a date with no departures | `airline.searchFlights("NYC","LAX", date.plusDays(30))` | Empty list |
| E2 ✅ | Cancel an unknown booking number | `airline.cancelBooking("BKG-DOES-NOT-EXIST")` | No-op; no exception |
| E3 ⬜ | Search with unknown airport | `airline.searchFlights("XXX","LAX", date)` | Empty list |
| E4 ⬜ | Cancel the same booking twice | `cancelBooking(n)` twice | Status stays `CANCELLED`; no exception |
| E5 ⬜ | Book an already-reserved seat | Reserve `seat`, book it, then `bookFlight(flight, other, seat, price)` again | Currently succeeds (no availability check) — desired: reject with an exception |
| E6 ⬜ | Release a seat that is already available | `seat.release()` on an `AVAILABLE` seat | Stays `AVAILABLE` |
| E7 ⬜ | Flight departing at 23:59 vs 00:01 next day | Two flights on adjacent days; search by each date | Each search returns only its own day's flight |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Book with a seat that does not belong to the flight | `bookFlight(f1, p, seatFromOtherFlight, price)` | Currently accepted — the design should validate seat ownership via `Flight` |
| X2 ⬜ | Negative or zero price | `bookFlight(f1, p, seat, -10)` | Currently accepted — should throw `IllegalArgumentException` |
| X3 ⬜ | Process the same payment twice | `airline.processPayment(payment)` twice | Status stays `COMPLETED`; idempotent |
| X4 ⬜ | Null flight or passenger | `bookFlight(null, p, seat, price)` | Currently creates a booking with null flight — should validate arguments |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent bookings | N threads call `airline.bookFlight(...)` | N bookings stored, all numbers unique (`AtomicInteger` + lock around the map) |
| C2 ⬜ | Cancel while creating | One thread cancels, another creates | No `ConcurrentModificationException`; both operations take the same lock |
| C3 ⬜ | Two passengers grab the same seat concurrently | Two threads call `seat.reserve()` then `bookFlight` on the same `Seat` | Both succeed today (no check) — the fix is a compare-and-set on `Seat` under a per-flight lock |
| C4 ⬜ | Add flight while searching | Thread A `addFlight`, thread B `searchFlights` | `ArrayList` is unsynchronised; may throw `ConcurrentModificationException` — use `CopyOnWriteArrayList` |

## Interviewer follow-ups / extensions
- How would you make seat selection safe? — Put the seat map on `Flight`, expose `Flight.reserveSeat(seatNumber)` that checks-and-sets under a per-flight lock, and have `bookFlight` call it.
- How would you hold a seat while the passenger pays? — Booking starts `PENDING`, seat `RESERVED`; a scheduler expires unpaid bookings (`EXPIRED`) and releases the seat.
- How would you link payments and refunds to bookings? — `Payment.bookingNumber`; `cancelBooking` triggers `PaymentProcessor.refund` and sets `REFUNDED`.
- How would you price seats? — A `PricingStrategy` per `SeatType` and demand; price computed at booking, not supplied.
- How would you scale search? — Index flights by `(source, destination, date)`; add filters for class availability and time windows.
- How would you generate ids across nodes? — Snowflake-style ids or a DB sequence instead of timestamp + local counter.
