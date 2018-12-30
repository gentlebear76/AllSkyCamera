package stardancer.observatory.allsky;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.*;

public class Logging {

    private static final String LOG_PATTERN = "[%5p] %d{HH:mm:ss,SSS} <%t> {%F:%M:%L} %m%n";

    public static final String DEFAULT_LOG_LEVEL = "trace";
    public static final String LOG_LEVEL_COMMAND = "loglevel";
    public static final String DEBUG_LOG_COMMAND = "debuglog";
    private static final String DEFAULT_LOG_FILE_NAME = "debug.log";

    private static CommandLine commandLine;
    private static String commandLineLogLevel = LOG_LEVEL_COMMAND;

    public static void setupFileLogger(CommandLine cmd) {
        commandLine = cmd;
        commandLineLogLevel = commandLine.getOptionValue(commandLineLogLevel, DEFAULT_LOG_LEVEL);
        if (commandLine.hasOption(DEBUG_LOG_COMMAND)) {
            Level logLevel = Level.toLevel(commandLineLogLevel);

            FileAppender fileAppender = new FileAppender();
            fileAppender.setLayout(new PatternLayout(LOG_PATTERN));
            fileAppender.setThreshold(logLevel);
            fileAppender.setFile(commandLine.getOptionValue(DEBUG_LOG_COMMAND, DEFAULT_LOG_FILE_NAME));
            fileAppender.setAppend(false);
            fileAppender.activateOptions();

            Logger.getRootLogger().setLevel(logLevel);
            Logger.getRootLogger().addAppender(fileAppender);
        }
    }

    public static void setupLogConsoleLogger() {
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        Logger.getRootLogger().setLevel(Level.toLevel(commandLineLogLevel));
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLayout(new PatternLayout(LOG_PATTERN));
        consoleAppender.setThreshold(Level.toLevel(commandLineLogLevel));
        consoleAppender.activateOptions();
        Logger.getRootLogger().addAppender(consoleAppender);
    }
}
