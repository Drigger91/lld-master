# Vending Machine

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | State, Singleton | finite state machine, state-dependent behaviour, enums for denominations, inventory, change/refund |

## Problem statement
Design the control logic of a vending machine. A customer selects a product, inserts coins and/or notes, receives the product once enough money has been inserted, and collects any change. The machine must react sensibly to actions performed in the wrong order (paying before selecting, dispensing before paying) and must not sell a product that is out of stock.

## Functional requirements
1. Maintain an inventory of products with quantities; operators can add products, update quantities and remove products.
2. `selectProduct(product)` succeeds only from the idle state and only if the product is in stock.
3. Accept payment in coins (`ONE`, `TWO`, `FIVE`, `TEN`) and notes (`TEN`, `TWENTY`, `FIFTY`, `HUNDRED`); keep a running total for the current transaction.
4. As soon as the total reaches the product's price, the machine becomes ready to dispense; further payment is refused.
5. `dispenseProduct()` decrements the product's stock and hands over the product; it is refused before payment is complete.
6. `returnChange()` after dispensing returns `total - price` and resets the machine to idle.
7. `returnChange()` before payment is complete cancels the transaction and refunds everything inserted.
8. Actions invalid in the current state are rejected with a message rather than changing state.

## Non-functional requirements & constraints
- In-memory, single machine (`VendingMachine` is a lazily-created singleton).
- Prices and amounts are `double`s; no rounding/currency handling.
- The machine has no notion of a limited coin reserve — it can always make change.
- Inventory uses a `ConcurrentHashMap`, but the state transitions themselves are not synchronized; one customer at a time is assumed.
- Feedback is via `System.out` messages; the API methods return `void`.

## Clarifying questions to ask
- What is the order of operations? — Select product first, then pay, then dispense, then collect change.
- What happens if the user inserts money before selecting? — Rejected with a message; money is not accepted.
- Can the user cancel mid-payment? — Yes, `returnChange()` in the paying state refunds the full amount inserted.
- Does overpayment get change? — Yes, after dispensing; the machine assumes unlimited change.
- Is concurrent use expected? — No, one customer session at a time.
- Do products have identity by name? — No, `Product` uses identity equality; the same object must be used for stocking and selecting.

## Core entities
- `VendingMachine` — singleton context; holds the inventory, the four state objects, the current state, selected product and running total; delegates every action to the current state.
- `VendingMachineState` — interface with `selectProduct`, `insertCoin`, `insertNote`, `dispenseProduct`, `returnChange`.
- `IdleState` — accepts a product selection (if in stock) → `ReadyState`; rejects everything else.
- `ReadyState` — accepts coins/notes; moves to `DispenseState` once paid; `returnChange` cancels and refunds.
- `DispenseState` — `dispenseProduct` decrements stock → `ReturnChangeState`; rejects more payment/selection.
- `ReturnChangeState` — `returnChange` pays out `total - price`, clears the transaction → `IdleState`.
- `Inventory` — `Map<Product, Integer>`; add/remove/update/getQuantity/isAvailable.
- `Product` — name + price. `Coin`, `Note` — enums carrying a value.

## Design hints
- **State pattern**: every user action is a method on `VendingMachineState`, and each concrete state decides whether the action is valid and what the next state is. This removes the `if (state == X)` ladders that a naive solution grows in every method.
- The context exposes package-private mutators (`setState`, `addCoin`, `resetPayment`, ...) that only states use; the public API is just the five user actions plus inventory access.
- **Transition table** (the thing to draw on the whiteboard): Idle —select(in stock)→ Ready —pay(total ≥ price)→ Dispense —dispense→ ReturnChange —returnChange→ Idle; Ready —returnChange→ Idle (refund).
- **Common mistake**: computing `change = total - price` in the paying state — a negative "change" must not be silently dropped; cancellation must refund the full amount and reset the total, or the next customer starts with credit.
- **Common mistake**: forgetting to reset `selectedProduct`/`totalPayment` on every path back to idle.
- Interviewers probe: what if the machine cannot make change (coin reserve), how to make it thread-safe (lock around each public action), and why states are pre-built once rather than allocated per transition.

## Run
mvn -q -pl problems/vending-machine compile exec:java
