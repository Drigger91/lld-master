# Test cases — ATM

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Create accounts | `bankingService.createAccount("1234567890", 1000.0)` | `bankingService.getAccount("1234567890").getBalance()` is 1000.0 |
| H2 ✅ | Authenticate with a card | `atm.authenticateUser(new Card("1234567890", "1234"))` | Returns without error (stub – no PIN check yet) |
| H3 ✅ | Check balance | `atm.checkBalance("1234567890")` | Returns 1000.0 |
| H4 ✅ | Withdraw cash | `atm.withdrawCash("1234567890", 500.0)` | Prints `Cash dispensed: 500`; balance becomes 500.0; dispenser cash drops from 10000 to 9500 |
| H5 ✅ | Deposit cash | `atm.depositCash("9876543210", 200.0)` | `atm.checkBalance("9876543210")` returns 700.0 |
| H6 ✅ | Balance reflects the withdrawal | `atm.checkBalance("1234567890")` after H4 | Returns 500.0 |
| H7 ⬜ | Transaction ids are unique | Perform two withdrawals; inspect the generated ids | Both start with `TXN` and differ (counter increments) |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Withdraw exactly the dispenser's cash | `new CashDispenser(500)`; `atm.withdrawCash(acc, 500.0)` | Succeeds; dispenser cash is 0 |
| E2 ⬜ | Withdraw the full account balance | Balance 1000; `withdrawCash(acc, 1000.0)` | Succeeds; balance is 0.0 |
| E3 ⬜ | Fractional withdrawal amount | `withdrawCash(acc, 99.99)` | Account is debited 99.99 but the dispenser dispenses `(int) 99.99 == 99` — mismatch to call out |
| E4 ⬜ | Zero-amount deposit | `depositCash(acc, 0.0)` | Balance unchanged; a transaction is still created |
| E5 ⬜ | Re-creating an existing account | `createAccount("1234567890", 5.0)` twice | Second call overwrites the first (map `put`); no error |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | ATM out of cash | `new CashDispenser(100)`; `withdrawCash(acc, 500.0)` | `IllegalArgumentException("Insufficient cash available in the ATM.")` — note the account has already been debited (no compensation) |
| X2 ⬜ | Unknown account | `atm.checkBalance("0000")` | `getAccount` returns null → `NullPointerException`; a robust design returns an `AccountNotFound` error |
| X3 ⬜ | Overdraft | Balance 100; `withdrawCash(acc, 500.0)` | Reference code allows balance to go to -400.0; expected behaviour is an insufficient-funds rejection before dispensing |
| X4 ⬜ | Wrong PIN | `authenticateUser(new Card("1234567890", "0000"))` | Reference stub accepts anything; expected behaviour is an authentication failure that blocks further operations |
| X5 ⬜ | Negative amount | `withdrawCash(acc, -50.0)` | Reference code credits the account and "dispenses" -50; should be rejected with `IllegalArgumentException` |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Two withdrawals race for the last cash | Dispenser with 500; two threads each `withdrawCash(acc, 500.0)` | Exactly one dispenses; the other gets `IllegalArgumentException` (dispenser is synchronized) |
| C2 ⬜ | Two deposits on the same account | Two threads each `depositCash(acc, 100.0)` 1000 times | Expected final balance +200000, but `Account.credit` is unsynchronized so updates can be lost — fix with a lock or atomic |

## Interviewer follow-ups / extensions
- How would you enforce PIN checks and a 3-strike card lock? — Store PIN hash on the bank side, add `AuthenticationService` returning a session; keep failure counters per card.
- How would you add balance validation and atomicity to withdrawal? — Validate funds and dispenser cash first, then debit and dispense; on dispense failure run a compensating `DepositTransaction`.
- How would you model note denominations? — Chain of Responsibility over `NoteDispenser(100) → NoteDispenser(50) → NoteDispenser(20)`, each holding a count.
- How would you add a transfer between accounts? — `TransferTransaction` subclass debiting one account and crediting another under a consistent lock order.
- How would you make the ATM's UI flow explicit? — State pattern: `Idle → CardInserted → Authenticated → TransactionInProgress`, with the ATM delegating to the current state.
