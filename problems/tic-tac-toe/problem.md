# Tic-Tac-Toe

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Separation of concerns (Board / Game / Player), enum for game status | 2-D grid state, turn management, win/draw detection, input validation, immutability of finished games |

## Problem statement
Design a two-player tic-tac-toe game on a 3×3 board. Players alternate placing their symbol; the game ends as soon as a player has three in a row (row, column or diagonal) or the board is full. The design should make it possible to drive the game both interactively from the console and programmatically (e.g. from tests or an AI).

## Functional requirements
1. Two `Player`s, each with a name and a distinct symbol; player 1 moves first.
2. `Game.makeMove(row, col)` places the current player's symbol and returns the resulting `GameStatus` (`IN_PROGRESS`, `WIN`, `DRAW`).
3. A move outside the board or on an occupied cell is rejected and the turn does not pass.
4. A move after the game has ended is rejected.
5. Win detection covers all 3 rows, 3 columns and both diagonals; a full board with no winner is a draw.
6. Expose `getStatus()`, `getWinner()`, `getCurrentPlayer()`, and the `Board` for display.
7. `Game.play()` runs an interactive loop on stdin, re-prompting on invalid input until the game ends.

## Non-functional requirements & constraints
- Single JVM, in-memory; no move history, undo or persistence.
- Fixed 3×3 board; `'-'` marks an empty cell.
- `Board.makeMove` and `Game.makeMove` are `synchronized`, though the game is inherently sequential.
- Win check is a full O(9) scan after each move (fine for 3×3; not designed for N×N).

## Clarifying questions to ask
- Board size? — Fixed 3×3.
- Who starts? — Player 1 (X).
- What happens on an invalid move? — Rejected; same player retries.
- Do we need to support an AI or replay? — Not now, but the API should not be tied to console input.
- How should the winner be reported? — Via `getStatus()`/`getWinner()` after the move that ends the game.

## Core entities
- `Board` — the 3×3 `char` grid and move counter; `makeMove(row, col, symbol)` validates and places; `hasWinner()`, `isFull()`, `printBoard()`.
- `Game` — orchestrates turns: holds two `Player`s, the `Board`, the current player, winner and `GameStatus`; `makeMove(row, col)` for programmatic play, `play()` for the console loop.
- `Player` — name + symbol.
- `GameStatus` — enum `IN_PROGRESS`, `WIN`, `DRAW`.

## Design hints
- Keep **board mechanics** (placement validity, win/draw geometry) in `Board` and **game flow** (whose turn, when it ends, who won) in `Game`. Interviewers check that you do not mix I/O into either.
- Return an explicit status from `makeMove` rather than making callers poll; it makes tests trivial and lets the console loop be a thin wrapper.
- Only switch players on a *successful* move; record the winner *before* switching (a classic off-by-one is announcing the wrong winner).
- Validate row/col bounds before indexing the array; throw `IllegalArgumentException` for bad moves and `IllegalStateException` for moves after game over — different failure kinds deserve different exceptions.
- **Common mistakes**: checking for a draw before checking for a win on the 9th move; reading `System.in` inside `Board`; creating a new `Scanner` on `System.in` for every prompt — the first one buffers ahead and later ones find no input when stdin is piped.
- Follow-ups go to N×N with O(1) win detection (per-row/col/diagonal counters), undo (move stack), and pluggable players (`Strategy` for human vs computer).

## Run
mvn -q -pl problems/tic-tac-toe compile exec:java

Interactive mode (reads moves from stdin):

mvn -q -pl problems/tic-tac-toe compile exec:java -Dexec.args="--interactive"
