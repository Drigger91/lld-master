# Coffee Vending Machine

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Singleton, Composition (recipe as ingredient map) | menu/recipe modelling, ingredient inventory, payment validation, low-stock alerts, synchronized dispensing |

## Problem statement
Design a coffee vending machine that offers a fixed menu of drinks. Each drink is made from a recipe of ingredients drawn from the machine's inventory. A customer picks a drink and pays; the machine should dispense it only if the payment covers the price and there are enough ingredients, return any change, and warn the operator when an ingredient runs low.

## Functional requirements
1. Show a menu of coffees with prices (`displayMenu()`).
2. Look up a coffee by name, case-insensitively (`selectCoffee(name)`); unknown names yield `null`.
3. `dispenseCoffee(coffee, payment)`: reject if the payment is less than the price; reject if any recipe ingredient has insufficient quantity; otherwise consume the ingredients and dispense.
4. Return change when the payment exceeds the price.
5. Print a low-inventory alert whenever an ingredient's quantity drops below 3 after a dispense.
6. There is one machine instance, pre-configured with ingredients (Coffee, Water, Milk — 10 units each) and a menu (Espresso, Cappuccino, Latte).

## Non-functional requirements
- In-memory; menu and ingredient stock are hard-coded in the singleton constructor (eager initialisation). There is no public API to restock or add menu items.
- `selectCoffee` and `dispenseCoffee` are `synchronized` on the machine, so concurrent customers cannot over-consume ingredients; `Ingredient.updateQuantity` is also synchronized.
- Payment is a single upfront `Payment(amount)`; no coin/note modelling, no multi-step payment.
- Results are reported via `System.out`; `dispenseCoffee` returns `void`.

## Constraints
- The menu is exactly three drinks with fixed prices and recipes: Espresso ($2.5 — 1 Coffee, 1 Water), Cappuccino ($3.5 — 1 Coffee, 1 Water, 1 Milk), Latte ($4.0 — 1 Coffee, 1 Water, 2 Milk).
- Exactly three ingredients — `Coffee`, `Water`, `Milk` — each starting at 10 integer units; recipe quantities are positive `int`s.
- An ingredient's quantity is never negative: a drink is dispensed only if **every** recipe entry is satisfied, and consumption is all-or-nothing.
- Because every drink uses 1 unit of Coffee and there is no restock API, the machine can dispense at most 10 drinks in its lifetime.
- The low-stock threshold is fixed at `< 3` units and is evaluated per ingredient immediately after each dispense.
- `Payment.amount` is a non-negative `double`; change is `amount - price` and is never negative; the machine is assumed to always have change.
- Drink names are unique in the menu (compared case-insensitively) and lookups are a linear scan over `<= 10` items; single JVM, one machine.

## Clarifying questions to ask
- Is the menu fixed or configurable at runtime? — Fixed at construction.
- Can a drink be partially made? — No; the check is all-or-nothing before any ingredient is consumed.
- Is payment collected incrementally? — No, a single amount is presented with the request.
- What is the low-stock threshold? — Below 3 units, printed as an alert.
- Multiple customers at once? — Assume yes; dispensing must be atomic per machine.

## Core entities
- `CoffeeMachine` — eager singleton; owns the menu (`List<Coffee>`) and ingredient stock (`Map<String, Ingredient>`); `displayMenu`, `selectCoffee`, `dispenseCoffee`.
- `Coffee` — name, price, and a recipe `Map<Ingredient, Integer>` (ingredient → units required).
- `Ingredient` — name + mutable quantity; `updateQuantity(delta)` is synchronized.
- `Payment` — immutable amount.

## Design hints
- A recipe is naturally a map from shared `Ingredient` objects to quantities; because the `Coffee` recipe references the *same* `Ingredient` instances the machine stocks, decrementing through the recipe updates the shared stock — no name lookups needed.
- Check-then-consume must be atomic: `hasEnoughIngredients` followed by `updateIngredients` is only safe because `dispenseCoffee` is synchronized on the machine. Point this out explicitly; interviewers look for the TOCTOU race.
- Validation order: payment first, then ingredients — a customer should not have ingredients reserved for a drink they cannot pay for.
- **Common mistakes**: consuming ingredients one by one and failing halfway (partial consumption); passing the `null` from `selectCoffee` straight into `dispenseCoffee` (NPE — validate at the call site).
- Probable follow-ups: adding new drinks without editing the singleton (Builder / config), a `State` machine for select → pay → dispense, and a restock API with an `Observer` for low-stock alerts.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :coffee-vending-machine compile exec:java` |
