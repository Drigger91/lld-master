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
        String logFile = System.getProperty("java.io.tmpdir") + "/logging-framework-app.log";
        LoggerConfig config = new LoggerConfig(LogLevel.DEBUG, new FileAppender(logFile));
        logger.setConfig(config);

        logger.debug("This is a debug message");
        logger.info("This is an information message");
        System.out.println("DEBUG and INFO messages appended to " + logFile);
    }
}
