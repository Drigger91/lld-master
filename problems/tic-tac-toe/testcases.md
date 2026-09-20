# Test cases — Tic-Tac-Toe

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

Setup: `p1 = new Player("Player 1", 'X')`, `p2 = new Player("Player 2", 'O')`, `game = new Game(p1, p2)`.

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | First move | `game.getCurrentPlayer()`; `game.makeMove(0, 0)` | current player is p1 before; returns `IN_PROGRESS`; cell (0,0) is 'X'; current player is now p2 |
| H2 ✅ | Turns alternate | moves (0,0), (0,1), (1,1), (0,2) | X, O, X, O placed in order; status stays `IN_PROGRESS` |
| H3 ✅ | Win on main diagonal | (0,0), (0,1), (1,1), (0,2), (2,2) | last call returns `WIN`; `getWinner()` is p1; `getStatus()` is `WIN` |
| H4 ⬜ | Win on a row | X:(0,0) O:(1,0) X:(0,1) O:(1,1) X:(0,2) | `WIN`, winner p1 |
| H5 ⬜ | Win on a column | X:(0,0) O:(0,1) X:(1,0) O:(1,1) X:(2,0) | `WIN`, winner p1 |
| H6 ⬜ | Win on anti-diagonal by O | X:(0,0) O:(0,2) X:(0,1) O:(1,1) X:(2,2) O:(2,0) | `WIN`, winner p2 |
| H7 ✅ | Draw | X:(1,1) O:(0,0) X:(2,2) O:(0,2) X:(2,0) O:(1,0) X:(1,2) O:(2,1) X:(0,1) | last call returns `DRAW`; `getWinner()` is `null`; `getBoard().isFull()` is `true` |
| H8 ✅ | Print board | `game.getBoard().printBoard()` | 3 lines of 3 symbols, '-' for empty |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Win on the 9th move is a win, not a draw | fill the board so that X completes a line with the final move | returns `WIN`, not `DRAW` |
| E2 ⬜ | Game ends immediately on win | after H3, `getStatus()` | `WIN`; no further turn switch (current player is still the winner) |
| E3 ✅ | Invalid move does not pass the turn | X:(1,1); O tries (1,1) → rejected; `getCurrentPlayer()` | still p2 |
| E4 ⬜ | Independent games | two `Game` instances sharing the same `Player` objects | boards and statuses do not interfere |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Occupied cell | `makeMove(1, 1)` twice | second throws `IllegalArgumentException("Invalid move!")`; board unchanged |
| X2 ✅ | Out of bounds | `makeMove(3, 0)` | throws `IllegalArgumentException("Invalid move!")` |
| X3 ⬜ | Negative index | `makeMove(-1, 2)` | throws `IllegalArgumentException` |
| X4 ✅ | Move after game over | after H3, `makeMove(2, 0)` | throws `IllegalStateException("Game is already over.")` |
| X5 ⬜ | Interactive: non-numeric input | run `play()` with stdin "a\n0\n0\n" | prints "Invalid input!..." then accepts 0,0 |
| X6 ⬜ | Interactive: out-of-range number | stdin "5\n1\n1\n" | re-prompts, then accepts 1,1 |
| X7 ⬜ | Interactive: stdin exhausted | run `play()` with empty stdin | throws `IllegalStateException("No more input available.")` instead of looping forever |

## Interviewer follow-ups / extensions
- How would you generalise to N×N with K in a row? — Parameterise `Board`; keep per-row, per-column and two diagonal counters per player for O(1) win checks after each move.
- How would you add undo? — Keep a stack of moves in `Game`; `undo()` clears the cell, decrements the count, restores the turn and status.
- How would you add a computer player? — `Strategy` interface `MoveStrategy.nextMove(Board)`; `Player` holds one; `Game` asks the current player's strategy (human strategy reads stdin).
- How would you support spectators or a UI? — `Observer` on `Game` state changes instead of `printBoard`.
- How would you make it a networked/multi-game server? — `Game` instances keyed by id, moves as commands validated server-side; the current API already returns status per move.
