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

## Non-functional requirements & constraints
- In-memory; menu and ingredient stock are hard-coded in the singleton constructor (eager initialisation). There is no public API to restock or add menu items.
- `selectCoffee` and `dispenseCoffee` are `synchronized` on the machine, so concurrent customers cannot over-consume ingredients; `Ingredient.updateQuantity` is also synchronized.
- Payment is a single upfront `Payment(amount)`; no coin/note modelling, no multi-step payment.
- Results are reported via `System.out`; `dispenseCoffee` returns `void`.

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
mvn -q -pl problems/coffee-vending-machine compile exec:java
