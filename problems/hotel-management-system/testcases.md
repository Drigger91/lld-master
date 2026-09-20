# Test cases — Hotel Management System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register guests and rooms | `hms.addGuest(new Guest("G001",…))`, `hms.addRoom(new Room("R001", RoomType.SINGLE, 100.0))` | `hms.getGuest("G001")` and `hms.getRoom("R001")` return the objects; room status is AVAILABLE |
| H2 ✅ | Book an available room | `hms.bookRoom(guest1, room1, today, today+3)` | Non-null `Reservation` with id `RES` + 8 hex chars, status CONFIRMED; `room1.getStatus()` is BOOKED |
| H3 ✅ | Check in | `hms.checkIn(reservation1.getId())` | `room1.getStatus()` is OCCUPIED |
| H4 ✅ | Check out with payment | `hms.checkOut(reservation1.getId(), new CreditCardPayment())` | Payment is charged `100.0 × 3 = 300.0`; `room1.getStatus()` is AVAILABLE; reservation is removed |
| H5 ✅ | Cancel a confirmed reservation | `r2 = hms.bookRoom(guest2, room2, …)`; `hms.cancelReservation(r2.getId())` | `r2.getStatus()` is CANCELLED; `room2.getStatus()` is AVAILABLE; reservation removed from registry |
| H6 ⬜ | Rebook after check-out | After H4, `hms.bookRoom(guest2, room1, …)` | Succeeds with a new reservation id |
| H7 ⬜ | Cash payment strategy | `hms.checkOut(id, new CashPayment())` | Same outcome as H4 |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Zero-night stay | `bookRoom(g, r, d, d)` then check in and out | Bill is `0.0`; transitions still succeed |
| E2 ⬜ | Same guest books two rooms | `bookRoom(g1, r1, …)` and `bookRoom(g1, r2, …)` | Two independent confirmed reservations |
| E3 ⬜ | Cancel then rebook the same room | Cancel `r2`, then `bookRoom(g1, room2, …)` | New reservation succeeds; room goes AVAILABLE → BOOKED again |
| E4 ⬜ | Cancel an unknown reservation id | `hms.cancelReservation("RESNOPE")` | No-op, no exception |
| E5 ⬜ | Singleton identity | `HotelManagementSystem.getInstance() == HotelManagementSystem.getInstance()` | true |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Book a room that is already booked | `bookRoom(g1, r1, …)` then `bookRoom(g2, r1, …)` | Second call returns `null`; demo branch prints `Room not available for booking.` |
| X2 ⬜ | Book a room that is occupied | Book, check in, then `bookRoom(g2, sameRoom, …)` | Returns `null` |
| X3 ⬜ | Check in with an unknown or cancelled reservation | `hms.checkIn("RESNOPE")` / check in after cancel | `IllegalStateException("Invalid reservation or reservation not confirmed.")` |
| X4 ⬜ | Check in twice | `checkIn(id)` twice | Second call throws `IllegalStateException("Room is not booked.")` from `Room.checkIn()` |
| X5 ⬜ | Check out without checking in | Book then `checkOut(id, payment)` | `IllegalStateException("Room is not occupied.")`; reservation stays CONFIRMED |
| X6 ⬜ | Check out with failing payment | `checkOut(id, amount -> false)` | `IllegalStateException("Payment failed.")`; room stays OCCUPIED and reservation is kept |
| X7 ⬜ | Cancel after check-in | Book, check in, then `cancelReservation(id)` | `IllegalStateException("Room is not booked.")` from `Room.release()`; guest must check out instead |
| X8 ⬜ | Cancel a cancelled reservation object directly | `reservation.cancel()` twice | Second call throws `IllegalStateException("Reservation is not confirmed.")` |
| X9 ⬜ | Check-out date before check-in date | `bookRoom(g, r, d5, d1)` | Accepted by the reference code and produces a negative bill; should be rejected with `IllegalArgumentException` |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two guests race for the same room | Two threads call `bookRoom(gX, room, …)` at once | Exactly one gets a reservation, the other gets `null` (`bookRoom` is synchronized on the system) |
| C2 ⬜ | Direct `Room.book()` race | Two threads call `room.book()` | One succeeds, the other throws `IllegalStateException` (method is synchronized) |

## Interviewer follow-ups / extensions
- How would you support date-based availability so a room can hold future bookings? — Keep a list of reservations per room and check date overlap instead of a single `RoomStatus`; keep status only for the physical room (OCCUPIED, MAINTENANCE).
- How would you keep reservation history? — Add `CHECKED_IN` and `COMPLETED` to `ReservationStatus` and never remove from the map.
- How would you search rooms by type and price? — `findAvailableRooms(RoomType, LocalDate, LocalDate)` over the room registry plus the per-room reservation list.
- How would you add extra charges (room service, minibar)? — A `Folio`/`Bill` aggregate per reservation with line items; check-out pays the folio total.
- How would you notify guests? — Observer on reservation events (confirmed, cancelled, checked-out) feeding an email/SMS notifier.
