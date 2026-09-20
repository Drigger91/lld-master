# ATM

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Command (Transaction), Facade (ATM), Dependency Injection | abstract base class, id generation, synchronized dispenser, separation of bank vs. machine |

## Problem statement
Design the software for an ATM. A customer inserts a card, authenticates, and can check their balance, withdraw cash, or deposit cash. The ATM talks to the bank for account operations and physically dispenses notes from its own cash reserve, which is finite.

## Functional requirements
1. Accept a card (card number + PIN) and authenticate the user.
2. Check the balance of an account.
3. Withdraw an amount: debit the bank account and dispense that much cash from the machine.
4. Deposit an amount: credit the bank account.
5. Every withdrawal or deposit is recorded as a `Transaction` with a unique id and executed through the bank.
6. The cash dispenser refuses to dispense more cash than it holds.

## Non-functional requirements & constraints
- In-memory only: accounts live in a `ConcurrentHashMap` inside `BankingService`; nothing is persisted.
- `CashDispenser.dispenseCash` is `synchronized`, so concurrent withdrawals cannot over-dispense the machine's cash.
- Transaction ids are unique per JVM (timestamp + `AtomicLong` counter).
- Authentication is a stub in the reference code (`authenticateUser` does nothing); balances are `double` and `Account.debit` does not check for overdraft. Both are deliberate simplifications to discuss, not features.

## Clarifying questions to ask
- Does the ATM own the accounts or does a bank? — A separate `BankingService` owns accounts; the ATM only orchestrates.
- Do we model denominations? — No, the dispenser tracks a single integer cash total.
- Should withdrawal fail on insufficient account balance? — Not enforced in the reference code; it is the first follow-up.
- What if the ATM runs out of cash? — `dispenseCash` throws `IllegalArgumentException`.
- One card maps to one account? — Yes; the demo uses the card number as the account number.
- Is the order "debit then dispense" or "dispense then debit"? — Debit first, then dispense; discuss what happens if dispensing fails.

## Core entities
- `ATM` — facade: `authenticateUser`, `checkBalance`, `withdrawCash`, `depositCash`; creates transactions and generates their ids.
- `BankingService` — the "bank": creates and looks up `Account`s, executes a `Transaction`.
- `Account` — account number + mutable balance; `debit` / `credit`.
- `Card` — card number + PIN.
- `CashDispenser` — machine's cash reserve; `dispenseCash(int)` is synchronized and throws when short.
- `Transaction` (abstract) — id, account, amount; `execute()` is implemented by `WithdrawalTransaction` (debit) and `DepositTransaction` (credit).

## Design hints
- **Command pattern for transactions**: each operation is an object with `execute()`, so the bank can log, retry or reverse them uniformly. The interviewer will ask how you would add `TransferTransaction` (new subclass, no change to `ATM`/`BankingService`).
- **Keep the machine and the bank apart**: the ATM must not touch balances directly; it asks `BankingService` to process a transaction. This is what makes the ATM replaceable and the bank testable.
- **Where does validation belong?** Insufficient-funds belongs in `WithdrawalTransaction.execute()` (or `Account.debit`), not in `ATM`; insufficient-cash belongs in `CashDispenser`. Be explicit about the order (validate cash *before* debiting, or compensate on failure).
- **Concurrency**: `CashDispenser` is synchronized; `Account` is not. Two ATMs debiting the same account concurrently can race — mention `synchronized`/`AtomicReference<BigDecimal>` or a per-account lock.
- Common mistakes: using `double` for money (use `BigDecimal` or long cents); casting `double` to `int` for the dispenser; unique ids from timestamps alone; putting business rules in the `main` flow.

## Run
mvn -q -pl problems/atm compile exec:java
