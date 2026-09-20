# Test cases — Coffee Vending Machine

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

Setup: `machine = CoffeeMachine.getInstance()` (stock: Coffee 10, Water 10, Milk 10; menu: Espresso $2.5 {Coffee 1, Water 1}, Cappuccino $3.5 {Coffee 1, Water 1, Milk 1}, Latte $4.0 {Coffee 1, Water 1, Milk 2}).

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Display menu | `machine.displayMenu()` | prints the three drinks with prices |
| H2 ✅ | Select a drink by name | `machine.selectCoffee("Espresso")` | returns the `Coffee` named "Espresso" |
| H3 ✅ | Overpay and get change | `machine.dispenseCoffee(espresso, new Payment(3.0))` | "Dispensing Espresso..." then "Please collect your change: $0.5" |
| H4 ✅ | Exact payment | `dispenseCoffee(cappuccino, new Payment(3.5))` | "Dispensing Cappuccino..." and no change line |
| H5 ✅ | Recipe with multiple units of one ingredient | `dispenseCoffee(latte, new Payment(4.0))` | dispensed; Milk decreases by 2 |
| H6 ⬜ | Ingredient stock decreases per recipe | after H3–H5, inspect quantities via the ingredients used in a recipe (`coffee.getRecipe().keySet()`) | Coffee 7, Water 7, Milk 7 |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Case-insensitive lookup | `machine.selectCoffee("LATTE")` | returns the Latte |
| E2 ⬜ | Low-inventory alert | dispense enough Lattes that Milk falls below 3 | after the dispense that crosses the threshold, "Low inventory alert: Milk" is printed |
| E3 ⬜ | Exactly enough ingredients | Milk == 2, dispense a Latte (needs 2) | dispensed; Milk becomes 0 |
| E4 ⬜ | Singleton | `CoffeeMachine.getInstance() == CoffeeMachine.getInstance()` | same instance, shared stock |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | Insufficient payment | `dispenseCoffee(latte, new Payment(3.0))` | "Insufficient payment for Latte"; no ingredients consumed |
| X2 ⬜ | Insufficient ingredients | drain Milk to 1, `dispenseCoffee(latte, new Payment(4.0))` | "Insufficient ingredients to make Latte"; Coffee/Water untouched (all-or-nothing) |
| X3 ⬜ | Unknown drink | `machine.selectCoffee("Mocha")` | returns `null` |
| X4 ⬜ | Dispensing a null selection | `dispenseCoffee(null, new Payment(5))` | throws `NullPointerException` — caller must check `selectCoffee` result |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two customers race for the last ingredients | Milk == 2; two threads each call `dispenseCoffee(latte, new Payment(4.0))` | exactly one dispenses, the other reports insufficient ingredients; Milk never goes negative |

## Interviewer follow-ups / extensions
- How would you let the operator restock or add drinks? — Public `addIngredient`/`refill` and `addCoffee` on the machine; move hard-coded setup out of the constructor.
- How would you model the customer flow (select → pay → dispense → collect)? — `State` pattern as in the vending machine problem.
- How would you notify staff of low stock instead of printing? — `Observer`: `Ingredient` publishes threshold events to registered listeners.
- How would you support custom drinks (extra shot, extra milk)? — `Decorator` over `Coffee` that extends the recipe and price.
- How would you avoid one global lock? — Lock per ingredient in a fixed order, or a reservation step that atomically claims all ingredients.
