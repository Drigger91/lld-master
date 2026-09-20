# Logging Framework

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Easy | Singleton, Strategy (pluggable appenders) | log-level filtering via enum ordinal, runtime reconfiguration, output abstraction |

## Problem statement
Design a small logging framework that application code can use to emit messages at different severity levels. The framework should let an operator choose a minimum level so that less important messages are dropped, and it should be possible to swap where log lines go (console, a file, a database) without changing the calling code.

## Functional requirements
1. Support the levels DEBUG, INFO, WARNING, ERROR and FATAL, ordered from least to most severe.
2. Provide a convenience method per level (`debug`, `info`, `warning`, `error`, `fatal`) plus a generic `log(level, message)`.
3. Only messages whose level is at or above the configured minimum level are emitted; the rest are silently dropped.
4. Each emitted line carries the level, a timestamp and the message text.
5. Output destinations are pluggable: console, append-to-file and a JDBC database appender must be supported, and new ones should be easy to add.
6. Level and appender can be changed at runtime through a configuration object; subsequent calls use the new configuration.
7. There is a single, globally accessible logger instance; its default configuration is level INFO with console output.

## Non-functional requirements
- In-memory configuration only; nothing is persisted apart from what the file/database appenders write.
- The logger is a Singleton, so every caller shares one configuration.
- No explicit synchronisation in the logger: level checks and appends are not atomic with respect to `setConfig`, and the file appender opens/closes the file on each write. Fine for a demo, not for high-throughput logging.
- Appender I/O failures are caught and printed; they never propagate to the caller.

## Constraints
- Exactly five levels, declared in this order: `DEBUG`, `INFO`, `WARNING`, `ERROR`, `FATAL`; severity is the enum ordinal (`DEBUG` = 0 … `FATAL` = 4) and there is no way to add or reorder levels at runtime.
- A message is emitted iff `level.ordinal() >= config.getLogLevel().ordinal()`; with the default `INFO` configuration exactly `DEBUG` is dropped and the other four levels are emitted.
- Exactly one appender at a time: a `LoggerConfig` holds one `LogLevel` and one `LogAppender`, never a list.
- Exactly three built-in appenders: `ConsoleAppender` (stdout), `FileAppender` (append-mode file at a caller-supplied path) and `DatabaseAppender` (JDBC `INSERT INTO logs (level, message, timestamp)`); no rotation, buffering or connection pooling.
- A log call carries a single `String` message; no format arguments, no `Throwable`, no structured key/value fields.
- The output line format is fixed to `[LEVEL] <epochMillis> - <message>`; the timestamp is `System.currentTimeMillis()` captured when the `LogMessage` is built.
- Single JVM with exactly one logger (`Logger.getInstance()`); no named, hierarchical or per-class loggers.
- Demo-scale throughput (tens of lines per run), not thousands per second.

## Clarifying questions to ask
- Can there be multiple appenders active at once? — No, one appender per configuration (a composite appender is a natural extension).
- Is the level filter per-appender or global? — Global; the level is checked once before any appender is invoked.
- Do we need asynchronous logging? — No, appends happen synchronously on the caller's thread.
- Should the format be configurable? — No, `LogMessage.toString()` fixes the `[LEVEL] timestamp - message` format.
- Does the database appender need to manage connections? — No, it opens a JDBC connection per write; connection pooling is out of scope.

## Core entities
- `Logger` — Singleton entry point; holds the current `LoggerConfig`, exposes per-level methods and applies the level filter in `log`.
- `LoggerConfig` — mutable pair of minimum `LogLevel` and the active `LogAppender`.
- `LogLevel` — enum `DEBUG < INFO < WARNING < ERROR < FATAL`; ordinal order is the severity order.
- `LogMessage` — immutable value: level, message and creation timestamp; `toString` produces the output line.
- `LogAppender` — interface with `append(LogMessage)`; the output strategy.
- `ConsoleAppender`, `FileAppender`, `DatabaseAppender` — concrete strategies writing to stdout, an append-mode file and a `logs` table via JDBC respectively.

## Design hints
- **Strategy for output**: `LogAppender` decouples "what to log" from "where it goes". Adding a new sink means one new class and zero changes to `Logger`.
- **Singleton for the logger**: eager initialisation (`private static final instance`) is thread-safe by JVM class-loading guarantees and avoids double-checked-locking mistakes.
- **Enum ordinal as severity**: `level.ordinal() >= config.getLogLevel().ordinal()` is compact but couples behaviour to declaration order; interviewers may ask you to give the enum an explicit `severity` field instead.
- Trade-off to discuss: a single appender vs. a list of appenders; synchronous vs. asynchronous (queue + worker thread) appends; keeping a file handle open vs. reopening per write.
- Common mistakes: filtering inside each appender instead of once in the logger; making `LogMessage` mutable; letting appender exceptions crash the application.

## Run
| Language | Command |
|---|---|
| Java | `mvn -q -pl :logging-framework compile exec:java` |
