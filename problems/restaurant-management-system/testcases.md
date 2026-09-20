# Test cases — Restaurant Management System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Add menu items and list menu | `restaurant.addMenuItem(burger)`, `addMenuItem(pizza)`, `addMenuItem(salad)`; `restaurant.getMenu()` | Returns 3 items in insertion order with names and prices |
| H2 ✅ | Place an order | `restaurant.placeOrder(new Order(1, [burger, salad], 17.98, PENDING, now))` | Order stored under id 1 with status `PENDING`; kitchen notified (stub) |
| H3 ✅ | Advance order status | `restaurant.updateOrderStatus(1, PREPARING)`, then `READY`, then `COMPLETED` | `order.getStatus()` is `PREPARING`, `READY`, `COMPLETED` after each call |
| H4 ✅ | Make a reservation | `restaurant.makeReservation(new Reservation(1, "John Doe", "1234567890", 4, time))` | Reservation stored; no exception |
| H5 ✅ | Record a payment | `restaurant.processPayment(new Payment(1, 17.98, CREDIT_CARD, PENDING))` | Payment stored under id 1 |
| H6 ✅ | Add staff | `restaurant.addStaff(new Staff(1,"Alice","Manager",...))`, `addStaff(bob)` | Staff roster has 2 entries |
| H7 ⬜ | Remove a menu item | `restaurant.removeMenuItem(pizza)` (same instance) | `getMenu()` no longer contains pizza |
| H8 ⬜ | Cancel a reservation | `restaurant.cancelReservation(reservation)` | Reservation removed |
| H9 ⬜ | Remove staff | `restaurant.removeStaff(bob)` | Roster shrinks by one |
| H10 ⬜ | Cancel an order | `restaurant.updateOrderStatus(1, CANCELLED)` | Status is `CANCELLED` |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | `getMenu()` returns a copy | `menu = restaurant.getMenu(); menu.clear()` | `restaurant.getMenu()` still has all items |
| E2 ⬜ | Remove a menu item using an equal-but-different instance | `restaurant.removeMenuItem(new MenuItem(2,"Pizza",...))` | Not removed (no `equals`/`hashCode` on `MenuItem`) — document or implement equality |
| E3 ⬜ | Duplicate order id | `placeOrder(orderA)`, `placeOrder(orderB)` both with id 1 | Second replaces the first in the map |
| E4 ⬜ | Order with unavailable item | Place an order containing a `MenuItem` with `available=false` | Accepted — availability is not checked; candidate validation |
| E5 ⬜ | Backward status transition | `updateOrderStatus(1, COMPLETED)` then `updateOrderStatus(1, PENDING)` | Currently allowed; a transition table should reject it |
| E6 ⬜ | Singleton identity | `Restaurant.getInstance() == Restaurant.getInstance()` | `true` |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Update status of unknown order | `restaurant.updateOrderStatus(42, READY)` | No-op, no exception |
| X2 ⬜ | Remove a menu item not on the menu | `restaurant.removeMenuItem(unknown)` | No-op |
| X3 ⬜ | Cancel a reservation that does not exist | `restaurant.cancelReservation(unknown)` | No-op |
| X4 ⬜ | Order total does not match item prices | `new Order(2, [burger], 999.0, PENDING, now)` | Accepted as-is — total is caller-supplied; discuss computing it from items |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent order placement | N threads call `placeOrder` with distinct ids | All N orders present (`ConcurrentHashMap`) |
| C2 ⬜ | Read menu while another thread adds items | Thread A iterates `getMenu()`; thread B calls `addMenuItem` | No `ConcurrentModificationException` (`CopyOnWriteArrayList` + copy) |
| C3 ⬜ | Concurrent status updates on one order | Two threads set `READY` and `COMPLETED` simultaneously | Last writer wins; no corruption, but ordering is not enforced |

## Interviewer follow-ups / extensions
- How would you enforce valid order transitions? — A `Map<OrderStatus, Set<OrderStatus>>` of allowed moves, or a State pattern with `Order.advance()`.
- How would you tie payments to orders? — `Payment.orderId`, and mark the order `COMPLETED` only once payment is `COMPLETED`.
- How would you model tables and seating? — `Table{id, capacity, status}`; a reservation reserves a table for a time window; check for overlaps.
- How would you notify the kitchen for real? — Observer/`KitchenDisplay` subscribers on `placeOrder`, or a queue.
- How would you support multiple restaurants? — Replace the singleton with a `RestaurantService` keyed by restaurant id.
- How would you compute the bill? — Sum item prices at order time (snapshot prices), add tax/tips, support split payments.
