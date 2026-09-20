# Splitwise (Expense Sharing)

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Strategy, Service layer, Ledger (adjacency map) | who-owes-whom ledger, split validation, integer-paise rounding, settle-up |

## Problem statement
Design an expense-sharing application like Splitwise. A user can add an expense they paid for and split it with one other user or with the members of a group, choosing how the cost is divided: equally, by exact amounts, or by percentages. At any time anyone can ask "who owes whom how much", and a user can pay back (settle) part or all of what they owe someone.

## Functional requirements
1. Create users (name, email; both non-blank) and groups (name + at least two members).
2. Add an expense: name, total amount, the user who paid, the participants, and a split type — `EQUAL`, `EXACT` (amount per participant) or `PERCENT` (percentage per participant).
3. Splits must be positive and must add up to the total (EXACT) or to 100 (PERCENT); otherwise the expense is rejected.
4. Adding an expense updates a balance ledger: every non-payer participant owes the payer their share.
5. Group expenses may only involve group members and are kept in the group's own history and ledger; 1:1 expenses go to a personal ledger.
6. Show balances: a list of "X owes Y amount" lines for every outstanding pair (net of both directions).
7. Settle up: a user pays another user an amount, which must be positive and no more than what they currently owe that user.

## Non-functional requirements & constraints
- In-memory only; users, groups and expenses live in maps and lists.
- Amounts are `double` but every strategy allocates in whole paise so the shares add up exactly (e.g. 100 / 3 = 33.34 + 33.33 + 33.33).
- Single-threaded demo; `UserService` and `GroupService` use `ConcurrentHashMap` but the `BalanceSheet` itself is not synchronized.
- Balances are pairwise (no debt simplification across three or more users).

## Clarifying questions to ask
- Does the payer also consume a share? — Yes if they are in the participant list; their own share is simply not recorded as a debt.
- Can a settlement exceed the debt (i.e. flip who owes whom)? — No, `amount <= owed` is enforced.
- Are group balances separate from personal balances? — Yes, each `Group` owns a `BalanceSheet`; 1:1 expenses use `ExpenseService`'s personal ledger.
- Should we minimise the number of transactions (debt simplification)? — Out of scope; pairwise ledger only.
- Can expenses be edited or deleted? — No, `Expense` is immutable and history is append-only.
- Currency? — Single currency, two decimal places.

## Core entities
- `User` — id (UUID), name, email.
- `Split` — one participant's share of an expense (user + positive amount).
- `Expense` — immutable: name, total, payer, `SplitType`, list of `Split`s; validates that splits add up to the total.
- `Group` — name, members, expense history, and its own `BalanceSheet`; rejects expenses involving non-members.
- `BalanceSheet` — the ledger: `sheet[A][B]` = net amount B owes A, mirrored in both directions; `applyExpense`, `settle`, `amountOwed`, `summary`.
- `SplitType` (enum) — `EQUAL`, `EXACT`, `PERCENT`.
- `SplitStrategy` + `EqualSplitStrategy` / `ExactSplitStrategy` / `PercentSplitStrategy` — turn (total, participants, values) into `Split`s.
- `UserService` — create / get / delete users.
- `SplitService` — validates inputs and dispatches to the strategy for a `SplitType`.
- `ExpenseService` — builds expenses via `SplitService`; owns the personal ledger and its settle-up.
- `GroupService` — create groups, add expenses to a group, settle within a group, print balances.

## Design hints
- **Strategy for split types.** Each `SplitType` maps to a `SplitStrategy` in an `EnumMap`; adding a "by shares" split is one new class, not a bigger `switch`. The strategy owns its validation (sum == total, sum == 100).
- **Expense is a value object.** It validates itself in the constructor (positive total, splits add up), so a bad expense can never reach a ledger.
- **Ledger as a double-entry map.** `sheet[creditor][debtor] += x` and `sheet[debtor][creditor] -= x` keep both views consistent, so "what does Alice owe / collect" is one lookup and a pair's net balance is a single number. Settling is just the debtor "lending" back the amount.
- **Rounding.** Allocate in integer paise and give the remainder to the first (EQUAL) or last (PERCENT) participant; interviewers will ask about 100 / 3.
- **Group vs personal scope.** The `Group` composes its own `BalanceSheet` and history; `ExpenseService` holds the personal one. Trade-off: simple and matches the product, but "total owed across all groups" needs aggregation.
- Common mistakes: validating splits only in one strategy; storing only one direction of the ledger; letting a settlement overshoot and silently reverse the debt; comparing `User` objects by reference instead of id.

## Run
mvn -q -pl problems/splitwise compile exec:java
