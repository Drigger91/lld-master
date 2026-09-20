# Problem template

Every folder under `problems/` has exactly this shape:

```
problems/<kebab-name>/
├── problem.md      # the interview question, requirements, clarifying questions, design hints
├── testcases.md    # behaviours that must work: happy paths, edge cases, errors, follow-ups
├── pom.xml         # module pom; sets <exec.mainClass> so `mvn -q -pl problems/<name> compile exec:java` runs the demo
└── src/main/java   # the solution + a runnable <Name>Main demo
```

## problem.md

```markdown
# <Title>

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy / Medium / Hard | e.g. State, Strategy, Observer | e.g. concurrency, enums as state, id generation |

## Problem statement
2–4 sentences, worded the way an interviewer would say it. No solution leakage.

## Functional requirements
Numbered list. What the system must do.

## Non-functional requirements & constraints
Concurrency, scale assumptions, in-memory vs persistent, etc. Keep it honest to what the code does.

## Clarifying questions to ask
Bullet list of "Q — assumed answer". These are the questions that shape the design.

## Core entities
Short list: `Entity` — one-line responsibility. Match the actual classes in `src/`.

## Design hints
- Which pattern solves which requirement, and why.
- Key decisions and trade-offs an interviewer will probe.
- Common mistakes.

## Run
mvn -q -pl problems/<name> compile exec:java
```

## testcases.md

```markdown
# Test cases — <Title>

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 | ... | ... | ... |

## Edge cases
| # | Scenario | Steps | Expected |

## Invalid input & error handling
| # | Scenario | Steps | Expected |

## Concurrency
(only if the problem has a concurrency angle; otherwise omit the section)

## Interviewer follow-ups / extensions
Bullets: "How would you add X?" with a one-line direction.
```

Rules: test cases describe *behaviour*, not implementation. Every requirement in `problem.md` should be traceable to at least one row. Steps must reference the public API of the solution (`parkingLot.parkVehicle(car)`), so a reader can turn them into unit tests.
