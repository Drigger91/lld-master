# Test cases — Logging Framework

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Default config logs INFO and above to console | `Logger.getInstance()`; `logger.info(..)`, `logger.warning(..)`, `logger.error(..)` | Three lines printed to stdout in `[LEVEL] <timestamp> - <message>` format |
| H2 ✅ | Messages below the configured level are dropped | With default config call `logger.debug("x")` | Nothing is printed |
| H3 ✅ | Reconfigure level and appender at runtime | `logger.setConfig(new LoggerConfig(LogLevel.DEBUG, new FileAppender(path)))`; then `logger.debug(..)`, `logger.info(..)` | Both lines are appended to the file; nothing further appears on the console |
| H4 ⬜ | Generic `log` entry point | `logger.log(LogLevel.FATAL, "boom")` | Line with `[FATAL]` is emitted through the current appender |
| H5 ⬜ | Singleton returns the same instance | Call `Logger.getInstance()` twice | Both references are `==`; a `setConfig` through one is visible through the other |
| H6 ⬜ | File appender appends, not overwrites | Log two messages via a `FileAppender` pointing at an existing file | File keeps its previous content and gains two new lines |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ⬜ | Level exactly equal to threshold | Config level WARNING; `logger.warning("w")` | Message is emitted (comparison is `>=`, not `>`) |
| E2 ⬜ | Highest threshold | Config level FATAL; call `debug`, `info`, `warning`, `error` | Nothing emitted; only `fatal` would pass |
| E3 ⬜ | Change only the level on an existing config | `config.setLogLevel(LogLevel.ERROR)` on the config already held by the logger | Subsequent `info` calls are dropped without calling `setConfig` again |
| E4 ⬜ | Timestamp is captured at creation | Create a `LogMessage`, sleep, inspect `getTimestamp()` | Timestamp reflects creation time, not append time |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ⬜ | File appender path is not writable | `new FileAppender("/nonexistent/dir/app.log")`; `logger.info("x")` | `IOException` is caught and its stack trace printed; the caller is not affected |
| X2 ⬜ | Database appender cannot connect | `new DatabaseAppender("jdbc:bad://", "u", "p")`; `logger.error("x")` | `SQLException` is caught and printed; no exception propagates |
| X3 ⬜ | Null message text | `logger.info(null)` | Currently emits `[INFO] <ts> - null`; discuss whether to reject with `IllegalArgumentException` |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Concurrent logging from many threads | 10 threads each call `logger.info` 100 times with a console appender | All 1000 lines are emitted; lines may interleave in order but each line is intact (stdout is synchronized) |
| C2 ⬜ | `setConfig` while other threads log | One thread swaps the config repeatedly while others log | No exception; each message uses whichever config was visible at its `log` call (no atomicity guarantee) |

## Interviewer follow-ups / extensions
- How would you support multiple appenders at once? — Replace the single `LogAppender` in `LoggerConfig` with a list, or add a `CompositeAppender` that fans out.
- How would you make logging asynchronous? — Push `LogMessage`s onto a `BlockingQueue` drained by a dedicated worker thread; flush on shutdown.
- How would you add a configurable format? — Introduce a `Formatter` strategy consumed by appenders instead of relying on `LogMessage.toString()`.
- How would you avoid reopening the log file for every line? — Keep a buffered writer open in `FileAppender` and add `close()`/flush semantics; add rotation by size or date.
- How would you support per-package/per-class loggers like log4j? — Turn `Logger` into a named-instance factory with a hierarchy of levels instead of a single global Singleton.
