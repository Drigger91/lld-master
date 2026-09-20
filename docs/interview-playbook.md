# LLD interview playbook

A 45–60 minute LLD round is judged on **how you get to a design**, not on the final class diagram. Use the same script every time so the process is automatic and your attention goes to the problem.

## The 5-step script (with time budget)

| Step | Time | What to do | What the interviewer is checking |
|---|---|---|---|
| 1. Clarify | 5 min | Restate the problem. Ask 4–6 questions that change the design (see below). Write down the assumptions you're going with. | Do you find the scope before coding? |
| 2. Requirements | 5 min | List functional requirements as verbs ("park a vehicle", "issue a ticket"). Add 2–3 non-functional ones only if they matter (concurrency, extensibility, in-memory). | Can you prioritise? Do you spot the hard requirement? |
| 3. Entities & relationships | 10 min | Nouns → classes, verbs → methods. Draw the core 5–8 classes, their fields, and has-a / is-a lines. Identify what is an enum, what is an interface. | Cohesion, correct abstraction level, no god class. |
| 4. Key flows & patterns | 10 min | Walk the 2 most important flows end to end (e.g. "park" and "unpark"). Name the pattern where it fits — *and why*. Decide where state lives and who owns ids. | Do patterns serve requirements or are they decoration? |
| 5. Code | 20 min | Code the interfaces and the main flow. Skeleton first, then fill in. Say what you're skipping. | Readable, compilable-in-your-head code; SOLID under pressure. |

Leave 5 minutes for follow-ups: concurrency, extension, persistence.

## Clarifying questions that actually change the design

- **Scale / multiplicity**: one parking lot or many? one elevator or a bank? → decides whether you need a manager / controller class.
- **Who are the actors?** admin vs user vs system → decides service boundaries and access checks.
- **What states can the core entity be in?** → if there are ≥3 states with rules on transitions, plan a State pattern or an explicit transition table.
- **Is it in-memory or persisted?** → in-memory is the default for LLD; say so, and keep a `Repository`-shaped interface so persistence is a swap.
- **Concurrency?** Will two users act on the same resource at once? → decides `synchronized` / locks / atomic types and where the critical section is.
- **What must be extensible?** New vehicle type? New payment method? New notification channel? → this is where Strategy / Factory / Observer earn their place.
- **What's out of scope?** Payments gateway, auth, UI. Say it out loud and move on.

## Patterns — when they earn their place

| Pattern | Use when | Example in this repo |
|---|---|---|
| **State** | An entity has a lifecycle with transition rules; behaviour differs per state. | `vending-machine` (State classes), `hotel-management-system`, `traffic-signal-system`, `restaurant-management-system` (enum + transition checks) |
| **Strategy** | The same operation has interchangeable algorithms chosen at runtime. | split rules in `splitwise`, payment processors in `car-rental-system` / `hotel-management-system`, appenders in `logging-framework` |
| **Observer / Pub-Sub** | Many parties react to an event without the emitter knowing them. | `pub-sub-system`, notification fan-out in `linkedin` / `social-networking-service` |
| **Singleton** | Exactly one coordinator with global access (say "or a DI-managed bean" to show you know the trade-off). | almost every `*System` / `*Service` class here |
| **Facade** | One entry point that hides several collaborating subsystems. | `atm`, `airline-management-system`, `library-management-system`, `stack-overflow` |
| **Command** | Requests are objects: queued, logged, undone, or scheduled. | `Transaction` hierarchy in `atm`; the natural next step for `scheduled-task-queue` |
| **Repository** | Separate domain logic from storage; swap in-memory for DB later. | `doctor-appointment-booking`, `task-management-system` |
| **Producer–Consumer / worker threads** | Requests arrive asynchronously and are served by long-running workers. | `elevator-system` (wait/notify), `traffic-signal-system` (thread per light), `online-auction-system` (timer) |
| **Factory** | Object creation depends on a type discriminator; keeps `if/else` out of callers. | — not used yet; a good add-on to `parking-lot` vehicle creation |
| **Builder** | Objects with many optional fields; keeps constructors sane. | — not used yet; try it on `hotel-management-system` reservations |
| **Chain of Responsibility** | A request passes through handlers until one takes it. | — not used yet; the classic refactor of `logging-framework` level filtering |

Rule of thumb: name the pattern *after* you've shown the requirement it solves. "I'll use State here because the ATM's behaviour on `insertCard` differs in each of its four states" beats "I'll use the State pattern".

## SOLID in one line each (and how it's tested)

- **S**ingle responsibility — the interviewer asks "what changes if the pricing rule changes?" and only one class should.
- **O**pen/closed — "add a new vehicle type" should mean a new class, not editing a `switch`.
- **L**iskov — a `Truck` used where a `Vehicle` is expected must not throw `UnsupportedOperationException`.
- **I**nterface segregation — `Notifiable`, `Payable` rather than one fat `User` interface.
- **D**ependency inversion — services depend on `PaymentStrategy`, not `CreditCardPayment`.

## Concurrency checklist

Mention these in the last 5 minutes even if not asked; they separate mid from senior.

1. **Identify the shared mutable state** (the spot map, the seat inventory, the account balance).
2. **Choose the smallest critical section** — lock the spot, not the whole lot.
3. **Prefer JDK primitives**: `ConcurrentHashMap`, `AtomicInteger`, `ReentrantLock`, `synchronized` on the entity.
4. **Idempotency**: booking with the same request id twice must not double-book.
5. **Say what you'd do at scale**: optimistic locking with a version column, or a DB unique constraint.

## Common mistakes

- Starting to code before the requirements are on the board.
- One `System`/`Manager` class that does everything.
- Enums for things that need behaviour (use classes) — or classes for things that are just labels (use enums).
- Inheritance for code reuse instead of composition (`Car extends ParkingSpot`-style errors).
- Forgetting the unhappy paths: full lot, insufficient balance, double booking, invalid state transition.
- Patterns without a requirement behind them.

## How to use this repo

1. Read `problem.md` **only up to "Clarifying questions"**. Set a 45-minute timer and design + code it yourself.
2. Compare with `testcases.md` — every row is a behaviour your design must support. Tick them off.
3. Only then read "Design hints" and the reference solution in `src/`. Note the differences; they're the learning.
4. Run the demo: `mvn -q -pl :<name> compile exec:java`.
