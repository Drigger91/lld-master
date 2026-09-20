import appenders.FileAppender;
import log.LogLevel;

public class LoggingFrameworkMain {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();

        // Logging with default configuration
        logger.info("This is an information message");
        logger.warning("This is a warning message");
        logger.error("This is an error message");
        // This won't be printed as we have specified check for log level in config
        logger.debug("This is a debug message");

        // Changing log level and appender
        LoggerConfig config = new LoggerConfig(LogLevel.DEBUG, new FileAppender("LoggingFramework/src/main/java/app.log"));
        logger.setConfig(config);

        logger.debug("This is a debug message");
        logger.info("This is an information message");
    }
}
