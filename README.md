# LLD Master — low-level design interview prep

26 classic low-level design problems, each with an interviewer-style **problem statement**, a **behaviour checklist** (test cases), and a runnable **Java 21 reference solution**.

```
problems/<name>/
├── problem.md      # the question, requirements, clarifying questions, entities, design hints
├── testcases.md    # what must work: happy path, edge cases, errors, follow-ups
└── src/main/java   # solution + <Name>Main demo
docs/
├── interview-playbook.md   # the 5-step script, patterns cheat-sheet, concurrency checklist
└── problem-template.md     # format every problem follows (use it to add new ones)
```

## How to practise

1. Open `problems/<name>/problem.md`, read up to **Clarifying questions**, and stop.
2. Set a 45-minute timer. Design and code it yourself.
3. Check yourself against `testcases.md` — every row is a behaviour your design must support.
4. Then read **Design hints** and the reference `src/`. The diff is the lesson.

Start with the playbook: [`docs/interview-playbook.md`](docs/interview-playbook.md).

## Run a solution

```bash
mvn -q compile                                            # build everything
mvn -q -pl problems/parking-lot compile exec:java          # run one demo
```

Requires Java 21 and Maven 3.9+.

## Problems

### Easy (9)

| Problem | Patterns | Key concepts | Run |
|---|---|---|---|
| [ATM](problems/atm/problem.md) · [tests](problems/atm/testcases.md) | Command (Transaction), Facade, DI | abstract transaction, synchronized dispenser, id generation | `mvn -q -pl problems/atm compile exec:java` |
| [Coffee Vending Machine](problems/coffee-vending-machine/problem.md) · [tests](problems/coffee-vending-machine/testcases.md) | Singleton, Composition (recipe map) | recipe inventory, atomic dispense, low-stock alert | `mvn -q -pl problems/coffee-vending-machine compile exec:java` |
| [Document Sharing Service](problems/document-sharing-service/problem.md) · [tests](problems/document-sharing-service/testcases.md) | ACL, ordered enum | authorization checks, owner-only sharing | `mvn -q -pl problems/document-sharing-service compile exec:java` |
| [LRU Cache](problems/lru-cache/problem.md) · [tests](problems/lru-cache/testcases.md) | Hash map + doubly-linked list | O(1) get/put, sentinel nodes, eviction | `mvn -q -pl problems/lru-cache compile exec:java` |
| [Library Management System](problems/library-management-system/problem.md) · [tests](problems/library-management-system/testcases.md) | Singleton, Facade | borrow limit, availability flag, keyword search | `mvn -q -pl problems/library-management-system compile exec:java` |
| [Logging Framework](problems/logging-framework/problem.md) · [tests](problems/logging-framework/testcases.md) | Singleton, Strategy (appenders) | level filtering, pluggable sinks, runtime reconfig | `mvn -q -pl problems/logging-framework compile exec:java` |
| [Pub-Sub System](problems/pub-sub-system/problem.md) · [tests](problems/pub-sub-system/testcases.md) | Observer (Publish–Subscribe) | topic fan-out, publisher authorization, CopyOnWriteArraySet | `mvn -q -pl problems/pub-sub-system compile exec:java` |
| [Restaurant Management System](problems/restaurant-management-system/problem.md) · [tests](problems/restaurant-management-system/testcases.md) | Singleton, State (enum order status) | menu, order lifecycle, reservations, payments | `mvn -q -pl problems/restaurant-management-system compile exec:java` |
| [Tic-Tac-Toe](problems/tic-tac-toe/problem.md) · [tests](problems/tic-tac-toe/testcases.md) | Board/Game/Player separation, enum status | turn management, win/draw detection, validation | `mvn -q -pl problems/tic-tac-toe compile exec:java` |

### Medium (17)

| Problem | Patterns | Key concepts | Run |
|---|---|---|---|
| [Airline Management System](problems/airline-management-system/problem.md) · [tests](problems/airline-management-system/testcases.md) | Facade, Singleton, State (enums) | flight search, booking lifecycle, seat/payment status | `mvn -q -pl problems/airline-management-system compile exec:java` |
| [Car Rental System](problems/car-rental-system/problem.md) · [tests](problems/car-rental-system/testcases.md) | Singleton, Strategy (PaymentProcessor) | date-overlap, availability flag, synchronized booking | `mvn -q -pl problems/car-rental-system compile exec:java` |
| [Concert Ticket Booking System](problems/concert-ticket-booking-system/problem.md) · [tests](problems/concert-ticket-booking-system/testcases.md) | Singleton, State (enums), custom exception | atomic multi-seat booking, locking, cancellation | `mvn -q -pl problems/concert-ticket-booking-system compile exec:java` |
| [Digital Wallet](problems/digital-wallet/problem.md) · [tests](problems/digital-wallet/testcases.md) | Singleton, abstract PaymentMethod, static converter | BigDecimal money, currency conversion, synchronized transfers | `mvn -q -pl problems/digital-wallet compile exec:java` |
| [Doctor Appointment Booking](problems/doctor-appointment-booking/problem.md) · [tests](problems/doctor-appointment-booking/testcases.md) | Repository, Facade, Value object | slot inventory, synchronized booking, value equality | `mvn -q -pl problems/doctor-appointment-booking compile exec:java` |
| [Elevator System](problems/elevator-system/problem.md) · [tests](problems/elevator-system/testcases.md) | Controller/Dispatcher, Producer–Consumer (wait/notify) | threads, monitor locks, nearest-car dispatch | `mvn -q -pl problems/elevator-system compile exec:java` |
| [Hotel Management System](problems/hotel-management-system/problem.md) · [tests](problems/hotel-management-system/testcases.md) | Singleton, State (enum transitions), Strategy (Payment) | room/reservation lifecycle, fail-fast transitions | `mvn -q -pl problems/hotel-management-system compile exec:java` |
| [LinkedIn](problems/linkedin/problem.md) · [tests](problems/linkedin/testcases.md) | Singleton, Observer (notification fan-out) | connections, search, messaging, notifications | `mvn -q -pl problems/linkedin compile exec:java` |
| [Online Auction System](problems/online-auction-system/problem.md) · [tests](problems/online-auction-system/testcases.md) | Singleton, State enum, Observer (stub), Timer | synchronized bidding, timed close, keyword search | `mvn -q -pl problems/online-auction-system compile exec:java` |
| [Parking Lot](problems/parking-lot/problem.md) · [tests](problems/parking-lot/testcases.md) | Singleton, Inheritance (Vehicle hierarchy), Composition | multi-level typed spots, first-fit, synchronized | `mvn -q -pl problems/parking-lot compile exec:java` |
| [Scheduled Task Queue *(entity stub — design it yourself)*](problems/scheduled-task-queue/problem.md) · [tests](problems/scheduled-task-queue/testcases.md) | Value Object, Producer–Consumer (intended) | topic/key/message envelope, validation, per-topic queues | `mvn -q -pl problems/scheduled-task-queue compile exec:java` |
| [Social Networking Service](problems/social-networking-service/problem.md) · [tests](problems/social-networking-service/testcases.md) | Singleton, Observer (notification fan-out) | friend graph, newsfeed, likes, notifications | `mvn -q -pl problems/social-networking-service compile exec:java` |
| [Splitwise (Expense Sharing)](problems/splitwise/problem.md) · [tests](problems/splitwise/testcases.md) | Strategy, Service layer, Ledger map | who-owes-whom ledger, split validation, settle-up | `mvn -q -pl problems/splitwise compile exec:java` |
| [Stack Overflow](problems/stack-overflow/problem.md) · [tests](problems/stack-overflow/testcases.md) | Singleton, Facade | tag inverted index, voting + reputation, per-object sync | `mvn -q -pl problems/stack-overflow compile exec:java` |
| [Task Management System](problems/task-management-system/problem.md) · [tests](problems/task-management-system/testcases.md) | Singleton, Repository-style manager | dual indexes, search/filter, per-task locking | `mvn -q -pl problems/task-management-system compile exec:java` |
| [Traffic Signal System](problems/traffic-signal-system/problem.md) · [tests](problems/traffic-signal-system/testcases.md) | Singleton, State-as-enum, Observer hook, thread-per-light | timed cycling, daemon threads, emergency override | `mvn -q -pl problems/traffic-signal-system compile exec:java` |
| [Vending Machine](problems/vending-machine/problem.md) · [tests](problems/vending-machine/testcases.md) | State, Singleton | FSM, denominations enum, inventory, refund | `mvn -q -pl problems/vending-machine compile exec:java` |

## Adding a problem

Copy the layout from [`docs/problem-template.md`](docs/problem-template.md), add `<module>problems/<name></module>` to the root `pom.xml`, and set `<exec.mainClass>` in the module pom.
