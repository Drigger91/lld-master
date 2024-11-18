package appenders;

import log.LogMessage;

public interface LogAppender {
    void append(LogMessage logMessage);
}
