import java.io.PrintWriter;
import java.util.Date;

public class BasicLogger implements Logger {
    private final LogLevel loggerLevel;
    private final PrintWriter writer;

    public BasicLogger(LogLevel loggerLevel) {
        this.writer = new PrintWriter(System.out, true);
        this.loggerLevel = loggerLevel;
    }

    private synchronized void printLog(LogLevel level, Object message) {
        String messageToPrint = format(message, level);
        writer.println(messageToPrint);
    }

    private String format(Object message, LogLevel level) {
        if (level.getPriority() > loggerLevel.getPriority()) {
            return String.format("[%s] You cannot print %s from %s logger", new Date().toInstant().toString(), level.getName(), loggerLevel.getName());
        }
        return String.format("[%s] %s : %s", new Date().toInstant().toString(), level, message.toString());
    }

    @Override
    public void info(Object message) {
        printLog(LogLevel.INFO, message);
    }

    @Override
    public void debug(Object message) {
        printLog(LogLevel.DEBUG, message);
    }

    @Override
    public void error(Object message) {
        printLog(LogLevel.ERROR, message);
    }

    @Override
    public void warn(Object message) {
        printLog(LogLevel.WARN, message);
    }
}
