package stardancer.observatory.allsky;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIProperty;

import java.util.List;
import java.util.Map;

public class AllSkyCamera {


    private static final Logger LOGGER = Logger.getLogger(AllSkyCamera.class);

    public static void main(String[] args)  throws Exception {

        Logging.setupLogConsoleLogger();
        CommandLine cmd = CommandLineController.parseCommandLine(args);
//        Logging.setupFileLogger(cmd);

        String host = cmd.getOptionValue("s", "127.0.0.1");
        String port = cmd.getOptionValue("p", Integer.toString(Constants.INDI_DEFAULT_PORT));


        IndiClient indiClient = new IndiClient(host, Integer.parseInt(port));


//        new IndiClient(host, Integer.parseInt(port));

//        List<INDIDevice> devices = indiClient.getAllDevices();

        Map<String, Device> test = indiClient.getDevices();

        List<String> deviceNames = indiClient.getDevicesNames();

        String hest  = "";

        Thread.sleep(1000);

        while (true) {
            if (indiClient.has_change()) {
                System.out.println("Hest: ");
                for (String deviceName : indiClient.getDevicesNames()) {
                    for (INDIProperty hes : indiClient.getDevice(deviceName).getAllProperties()) {
                        System.out.println(hes.getNameStateAndValuesAsString());
                    }
                }
                System.exit(0);
            }

        }


    }


}
