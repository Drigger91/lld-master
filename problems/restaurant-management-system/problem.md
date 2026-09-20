# Restaurant Management System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Singleton, State (enum order status) | menu registry, order lifecycle, reservations, payments, thread-safe collections |

## Problem statement
Design the back-office system for a single restaurant. Staff maintain a menu, take customer orders made up of menu items, move each order through preparation to completion, record table reservations, and record payments against orders. The system should also keep a staff roster. Everything lives in memory in one process; the kitchen and staff "notifications" can be stubs.

## Functional requirements
1. Add and remove `MenuItem`s (id, name, description, price, availability) and list the current menu.
2. Place an `Order` consisting of menu items with a total amount and a creation timestamp; the kitchen is notified.
3. Update an order's status through `PENDING -> PREPARING -> READY -> COMPLETED` (or `CANCELLED`); relevant staff are notified.
4. Make and cancel a `Reservation` (customer name, contact, party size, time).
5. Record a `Payment` (amount, `PaymentMethod`, `PaymentStatus`).
6. Add and remove `Staff` (id, name, role, contact).

## Non-functional requirements
- In-memory; single restaurant; `Restaurant` is a process-wide singleton.
- Collections are thread-safe (`CopyOnWriteArrayList` for menu/reservations/staff, `ConcurrentHashMap` for orders/payments), but there is no cross-entity transaction (e.g. payment is not linked to an order).
- `getMenu()` returns a defensive copy.
- Status transitions are not validated — any status can be set at any time.
- Kitchen/staff notification, payment gateway integration and table assignment are stubs (`notifyKitchen`, `notifyStaff`, `processPayment` comments).

## Constraints
- Scale: one venue — a menu of at most a few hundred `MenuItem`s and orders, reservations and staff in the hundreds; menu/reservation/staff lookups are linear, orders and payments are keyed by `int` id.
- Fixed vocabularies: `OrderStatus { PENDING, PREPARING, READY, COMPLETED, CANCELLED }`, `PaymentMethod { CASH, CREDIT_CARD, MOBILE_PAYMENT }`, `PaymentStatus { PENDING, COMPLETED, FAILED }`.
- Ids are caller-supplied `int`s. Order and payment ids must be unique (`Map.put` silently overwrites); menu items, reservations and staff have no uniqueness check and the same item can be added twice.
- `MenuItem.price` and `Order.totalAmount` are caller-supplied `double`s; the total is never recomputed from the items and `>= 0` is not enforced.
- Only `Order.status` is mutable; `MenuItem`, `Reservation`, `Payment` and `Staff` are immutable once constructed — a `Payment` created as `PENDING` stays `PENDING` forever.
- `removeMenuItem`, `cancelReservation` and `removeStaff` remove by object identity (no `equals`/`hashCode`): the exact instance passed to `add` must be passed to remove, otherwise the call is a silent no-op.
- `updateOrderStatus` on an unknown order id is a silent no-op; no public method throws.
- Single JVM, no real clock — `Order.timestamp` and `Reservation.reservationTime` are caller-supplied `Timestamp`s that are stored but never compared.

## Clarifying questions to ask
- Is the order total computed from items or supplied? — Assumed: supplied by the caller in the `Order` constructor.
- Should status transitions be validated (no `COMPLETED -> PREPARING`)? — Assumed: not enforced in code; discuss as a follow-up.
- Are payments tied to orders? — Assumed: no; `Payment` has its own id and amount and is stored independently.
- Do reservations check table capacity / time conflicts? — Assumed: no; reservations are just recorded.
- Single restaurant or a chain? — Assumed: single (hence the singleton).

## Core entities
- `Restaurant` — singleton facade holding menu, orders, reservations, payments, staff; entry point for all operations.
- `MenuItem` — immutable id, name, description, price, availability flag.
- `Order` — id, list of `MenuItem`s, total amount, mutable `OrderStatus`, timestamp.
- `OrderStatus` — `PENDING, PREPARING, READY, COMPLETED, CANCELLED`.
- `Reservation` — id, customer name, contact number, party size, reservation time.
- `payment.Payment` / `PaymentMethod` / `PaymentStatus` — immutable payment record (`CASH, CREDIT_CARD, MOBILE_PAYMENT`; `PENDING, COMPLETED, FAILED`).
- `Staff` — id, name, role, contact number.

## Design hints
- **Singleton** `Restaurant.getInstance()` keeps one source of truth for a single venue; be ready to argue for a `RestaurantService` injected per venue if the interviewer extends to a chain.
- **State as an enum**: `OrderStatus` is a simple enum mutated via `Order.setStatus`. The natural next step (and a common probe) is a transition table or a State pattern that rejects illegal moves.
- Immutability: `MenuItem`, `Payment`, `Reservation`, `Staff` are all-final value objects; only `Order.status` mutates.
- Trade-offs: `CopyOnWriteArrayList` is ideal for read-heavy menus but expensive for frequent writes; `ConcurrentHashMap` for orders keyed by id gives O(1) status updates.
- Common mistakes: recalculating totals inconsistently (item prices vs supplied total); removing a `MenuItem` that is referenced by an open order; no link between `Payment` and `Order` so refunds/reconciliation are impossible; `removeMenuItem` relies on object identity because `MenuItem` has no `equals`.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :restaurant-management-system compile exec:java` |
