package stardancer.observatory.allsky;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import stardancer.observatory.allsky.AllSkyCamera;
import stardancer.observatory.allsky.Logging;

public class CommandLineController {

    private static final Logger LOGGER = Logger.getLogger(CommandLineController.class);

    public static Options setupCommandLineOptions() {
        Option logLevelOption = new Option("l", Logging.LOG_LEVEL_COMMAND, true, "Log level. Default is " + Logging.DEFAULT_LOG_LEVEL);
        logLevelOption.setArgName("log level");

        Option debugLogOption = new Option("d", Logging.DEBUG_LOG_COMMAND, true, "Debug log file name");
        debugLogOption.setArgName("debug log file name");

        Option hostOption = new Option("s", "server", true, "INDIServer IP address");
        hostOption.setArgName("server ip address");

        Option portOption = new Option("p", "port", true, "INDIServer port number");
        portOption.setArgName("server port number");

        Option helpOption = new Option("h", "help", false, "Print this message");
        helpOption.setArgName("help message");

        Option directoryOption = new Option("f", "destinationDir", true, "Image destination directory");
        directoryOption.setArgName("destination directory");

        Options options = new Options();
        options.addOption(logLevelOption);
        options.addOption(debugLogOption);
        options.addOption(hostOption);
        options.addOption(portOption);
        options.addOption(helpOption);
        options.addOption(directoryOption);

        return options;
    }

    private static void printHelp(Options options, String error) {
        HelpFormatter formatter = new HelpFormatter();
        if (!error.isEmpty()) {
            LOGGER.error("Error: " + error + "\n");
        }

        formatter.printHelp("java -jar <jar-file-name> [-s <server ip>] [-p <port number>] [-l <loglevel>] [-d <log file name>] ", "", options,"");
    }

    public static org.apache.commons.cli.CommandLine parseCommandLine(String[] args) throws RuntimeException {
        Options options = setupCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        org.apache.commons.cli.CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (MissingOptionException m) {
            printHelp(options, m.getLocalizedMessage());
            LOGGER.error(m.getMessage());
            throw new RuntimeException("Error parsing command line!");
        } catch (ParseException p) {
            printHelp(options, p.getLocalizedMessage());
            LOGGER.error("Error: Cannot parse command line: " + options.toString());
            throw new RuntimeException("Error parsing command line!");
        }

        return cmd;
    }
}
