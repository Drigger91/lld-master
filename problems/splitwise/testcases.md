# Test cases — Splitwise (Expense Sharing)

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Create users | `userService.createUser("Alice", "alice@example.com")` | Returns a `User` with a generated id; `userService.getUser(id)` returns it |
| H2 ✅ | 1:1 EXACT split | `expenseService.addExpense("Groceries", 50, alice, EXACT, [alice, bob], [30, 20])` | Expense has splits Alice 30 / Bob 20; ledger summary contains "Bob owes Alice 20.00" |
| H3 ✅ | 1:1 EQUAL split nets against existing debt | then `expenseService.addExpense("Cab", 30, bob, EQUAL, [alice, bob], null)` | Splits 15 / 15; summary is exactly "Bob owes Alice 5.00" |
| H4 ✅ | Settle up fully | `expenseService.settleUp(bob, alice, 5)` | `getPersonalLedger().summary()` is empty; `isSettled()` true |
| H5 ✅ | Create group | `groupService.createGroup([alice, bob, carol], "Road trip")` | Group with 3 members, empty history and ledger |
| H6 ✅ | Group EQUAL expense | `expenseService.createExpense("Hotel", 900, alice, EQUAL, members, null)`; `groupService.addExpenseToGroup(trip, hotel)` | Returns true; Bob owes Alice 300, Carol owes Alice 300 |
| H7 ✅ | Group PERCENT expense | `createExpense("Fuel", 300, bob, PERCENT, members, [50, 25, 25])`; add to group | Splits 150 / 75 / 75; net: Bob owes Alice 150, Carol owes Alice 300, Carol owes Bob 75 |
| H8 ✅ | Group history and balances | `trip.getAllExpenses()`; `groupService.printBalances(trip)` | 3 expenses; one "X owes Y" line per outstanding pair, none for settled pairs |
| H9 ✅ | Settle inside a group | `groupService.settleUp(trip, carol, bob, trip.getBalanceSheet().amountOwed(carol, bob))` | Carol–Bob line disappears; other pairs unchanged |
| H10 ⬜ | Delete user | `userService.DeleteUser(id)` | Returns true; `getUser(id)` is null; deleting again returns false |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | EQUAL split with remainder | `createExpense("Snacks", 100, carol, EQUAL, [alice, bob, carol], null)` | Shares 33.34 / 33.33 / 33.33, total exactly 100.00 |
| E2 ✅ | Payer is a participant | Any expense where `paidBy` is in the participant list | Payer's own share is not recorded as a debt |
| E3 ✅ | Debts in both directions net out | H2 then H3 | Only the net (5.00) is shown, not two opposite lines |
| E4 ⬜ | Payer not in participant list | `createExpense("Gift", 40, alice, EQUAL, [bob, carol], null)` | Bob and Carol each owe Alice 20 |
| E5 ⬜ | PERCENT rounding | `createExpense("X", 10, a, PERCENT, [a, b, c], [33.33, 33.33, 33.34])` | Last participant absorbs the rounding; shares sum to 10.00 |
| E6 ⬜ | Adding an existing member | `groupService.addMember(trip, alice)` | Returns false; member count unchanged |
| E7 ⬜ | Partial settlement | Bob owes Alice 150; `settleUp(trip, bob, alice, 100)` | Bob owes Alice 50 |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Settle more than owed | Bob owes 5; `expenseService.settleUp(bob, alice, 10)` | `IllegalArgumentException` "owes ... only 5.00"; ledger unchanged |
| X2 ✅ | Settle when nothing is owed | `expenseService.settleUp(alice, bob, 1)` | `IllegalArgumentException` |
| X3 ✅ | EXACT amounts do not add up | `createExpense(..., EXACT, [alice, bob], [60, 30])` for total 100 | `IllegalArgumentException` |
| X4 ✅ | PERCENT not 100 | `createExpense(..., PERCENT, [alice, bob], [70, 40])` | `IllegalArgumentException` |
| X5 ✅ | Negative split value | `createExpense(..., EXACT, [alice, bob], [120, -20])` | `IllegalArgumentException` "must be positive" |
| X6 ✅ | Zero-amount expense | `createExpense("Nothing", 0, alice, EQUAL, ...)` | `IllegalArgumentException` |
| X7 ✅ | Group with one member | `groupService.createGroup([alice], "Solo")` | `IllegalArgumentException` |
| X8 ✅ | Expense involving a non-member | `groupService.addExpenseToGroup(trip, expensePaidByDave)` | Returns false, prints reason; group history and ledger unchanged |
| X9 ⬜ | Blank user name/email | `userService.createUser("", "x@y.com")` | `Exception` "cannot be blank" |
| X10 ⬜ | Wrong number of values | `createExpense(..., EXACT, [alice, bob], [100])` | `IllegalArgumentException` "one amount per participant" |
| X11 ⬜ | Settle between non-members of a group | `groupService.settleUp(trip, dave, alice, 10)` | `IllegalArgumentException` |

## Interviewer follow-ups / extensions
- How would you simplify debts (minimum number of transactions)? — Compute each user's net position and greedily match largest creditor with largest debtor.
- How would you add a "by shares" split (e.g. 2:1:1)? — New `SplitStrategy` implementation registered in `SplitService`; no other change.
- How would you show a user's balance across all groups? — Aggregate `balancesFor(user)` over every group's `BalanceSheet` plus the personal ledger, or move to one global ledger keyed by group.
- How would you make it thread-safe? — Lock per `BalanceSheet` (or per user pair), and make `applyExpense` + history append atomic.
- How would you support editing/deleting an expense? — Reverse its splits on the ledger (apply with negated amounts) and record a new version.
- Persistence? — Expenses as an append-only event log; ledger is a projection that can be rebuilt.
