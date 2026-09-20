# Test cases — Vending Machine

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

Setup for all rows: `vm = VendingMachine.getInstance()`; `coke = new Product("Coke", 15)`, `pepsi = new Product("Pepsi", 20)`, `water = new Product("Water", 10)`; `vm.getInventory().addProduct(coke, 5)`, `(pepsi, 3)`, `(water, 1)`.

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Select an in-stock product | `vm.selectProduct(coke)` | "Product selected: Coke"; machine is in Ready state |
| H2 ✅ | Exact payment with coins and a note | after H1: `insertCoin(ONE)` ×5, `insertNote(TEN)` | each insertion acknowledged; on reaching 15 the machine is ready to dispense |
| H3 ✅ | Dispense reduces stock | `vm.dispenseProduct()` | "Product dispensed: Coke"; `getInventory().getQuantity(coke)` == 4 |
| H4 ✅ | Collect change after exact payment | `vm.returnChange()` | "No change to return."; machine back to Idle |
| H5 ✅ | Overpayment returns change | `selectProduct(pepsi)`, `insertCoin(ONE)`, `insertNote(TWENTY)`, `dispenseProduct()`, `returnChange()` | dispensed; "Change returned: $1.0"; Idle |
| H6 ✅ | Sequential transactions | run H1–H4 then H5 | second transaction starts with a zero balance |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | Cancel with partial payment | `selectProduct(water)`, `insertCoin(FIVE)`, `returnChange()` | "Transaction cancelled. Refunded: $5.0"; total reset; Idle |
| E2 ⬜ | Cancel with nothing inserted | `selectProduct(water)`, `returnChange()` | "Transaction cancelled. No change to return."; Idle |
| E3 ✅ | Last unit sold, then out of stock | `selectProduct(water)`, `insertNote(TEN)`, `dispenseProduct()`, `returnChange()`, `selectProduct(water)` | first sale succeeds; second selection prints "Product not available: Water" |
| E4 ⬜ | Product removed from inventory | `getInventory().removeProduct(coke)`, `selectProduct(coke)` | "Product not available: Coke" |
| E5 ⬜ | Singleton | `VendingMachine.getInstance() == VendingMachine.getInstance()` | same instance, same inventory |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Pay before selecting | from Idle: `insertCoin(ONE)` | "Please select a product first."; money not counted |
| X2 ✅ | Select while a product is already selected | after H1: `selectProduct(pepsi)` | "Product already selected. Please make payment."; selection unchanged |
| X3 ✅ | Dispense before payment is complete | `selectProduct(pepsi)`, `insertCoin(ONE)`, `dispenseProduct()` | "Please make payment first."; still Ready |
| X4 ✅ | Insert money after payment is complete | after total ≥ price: `insertCoin(FIVE)` | "Payment already made. Please collect the dispensed product."; not added to total |
| X5 ⬜ | Return change before dispensing | in Dispense state: `returnChange()` | "Please collect the dispensed product first." |
| X6 ⬜ | Actions while change is pending | in ReturnChange state: `selectProduct(x)` / `insertCoin(ONE)` / `dispenseProduct()` | each rejected with "Please collect the change first." / "Product already dispensed..." |
| X7 ✅ | Select a product that was never stocked | `selectProduct(new Product("Juice", 25))` | "Product not available: Juice" |
| X8 ⬜ | Dispense / returnChange in Idle | `dispenseProduct()`, `returnChange()` from Idle | "Please select a product and make payment." / "No change to return."; still Idle |

## Interviewer follow-ups / extensions
- How would you make the machine unable to give change it doesn't have? — Track a coin/note reserve in `Inventory`-like structure; on `returnChange`, run a greedy change-making algorithm and refuse the sale (refund) if impossible.
- How would you make it thread-safe? — Guard the five public actions with a single lock on `VendingMachine` (state transitions must be atomic).
- How would you return status to the caller instead of printing? — Have state methods return a result enum / `TransactionResult` and let the context bubble it up.
- How would you add a maintenance/out-of-service state? — Another `VendingMachineState` that rejects all customer actions; entered by an operator API.
- How would you support product codes (A1, B2)? — Key inventory by code and give `Product` value-based `equals`/`hashCode`.
