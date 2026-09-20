# Test cases — Digital Wallet

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Create users and accounts | `wallet.createUser(user1)`; `wallet.createAccount(new Account("A001", user1, "1234567890", USD))` | `wallet.getAccount("A001")` returns it with balance 0; it is in `user1`'s account list |
| H2 ✅ | Register payment methods | `wallet.addPaymentMethod(new CreditCard(...))`, `new BankAccount(...)` | `wallet.getPaymentMethod("PM001")` returns the card |
| H3 ✅ | Deposit | `account1.deposit(new BigDecimal("1000.00"))` | Balance 1000.00 |
| H4 ✅ | Same-currency debit, cross-currency credit | account1 USD 1000, account2 EUR 500; `wallet.transferFunds(account1, account2, 100.00, USD)` | account1 900.00 USD; account2 585.00 EUR (100 USD = 85 EUR) |
| H5 ✅ | Transaction recorded on both sides | `wallet.getTransactionHistory(account1)` and `(account2)` | Same transaction id in both; amount 100.00 USD; timestamp set |
| H6 ⬜ | Cross-currency debit | account2 EUR; `transferFunds(account2, account1, 85.00, EUR)` | account2 debited 85.00 EUR; account1 credited 100.00 USD |
| H7 ⬜ | Transfer expressed in a third currency | USD -> EUR accounts, amount in GBP | Debit and credit each converted from GBP through USD |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Transfer exactly the balance | balance 900; `transferFunds(a1, a2, 900, USD)` | Succeeds; balance 0.00 |
| E2 ⬜ | Conversion rounding | `CurrencyConverter.convert(new BigDecimal("1.00"), JPY, USD)` | 0.01 (HALF_UP to 2 decimals) |
| E3 ⬜ | Same currency both sides | USD -> USD transfer | No conversion; amounts identical |
| E4 ⬜ | Singleton | `DigitalWallet.getInstance()` twice | Same instance |
| E5 ⬜ | Multiple accounts per user | Create two accounts for user1 | Both in `user1`'s list; transfer between them works |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Insufficient funds | account2 has 585 EUR; `transferFunds(account2, account1, 5000.00, EUR)` | `InsufficientFundsException`; both balances unchanged; no transaction recorded |
| X2 ⬜ | Direct withdraw over balance | `account.withdraw(balance + 1)` | `InsufficientFundsException` |
| X3 ⬜ | Unknown account id | `wallet.getAccount("nope")` | null |
| X4 ⬜ | Negative or zero amount | `transferFunds(a1, a2, -10, USD)` | Not validated today; should throw `IllegalArgumentException` |
| X5 ⬜ | Transfer to the same account | `transferFunds(a1, a1, 10, USD)` | Not validated today; should be rejected |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent transfers from one account | N threads each transfer 10 from an account holding 50 | Exactly 5 succeed; the rest throw; final balance 0 (wallet-level `synchronized`) |
| C2 ⬜ | Concurrent deposits | N threads deposit 1 each | Balance increases by exactly N (`deposit` is synchronized) |
| C3 ⬜ | Opposite transfers A->B and B->A | Two threads | No deadlock (single lock); both complete |

## Interviewer follow-ups / extensions
- How would you charge a payment method to top up an account? — `topUp(account, paymentMethod, amount)` that calls `processPayment` and then `deposit`, recording a transaction.
- How would you remove the global lock? — Lock the two accounts in id order; or use optimistic versioning on balance.
- How would you make rates live? — Inject a `RateProvider` interface into the converter; cache with TTL.
- How would you record deposits and withdrawals too? — Make `Transaction` typed (`DEPOSIT`, `WITHDRAW`, `TRANSFER`) and append on every balance change.
- Idempotency? — Client-supplied transfer id, checked before executing, so retries do not double-charge.
- Persistence and audit? — Append-only ledger table; balances derived or checked against it.
