package stardancer.observatory.allsky;


import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.indilib.i4j.Constants;

import java.io.IOException;

public class AllSkyCamera {


    private static final Logger LOGGER = Logger.getLogger(AllSkyCamera.class);

    IndiClient indiClient;
    static String host;
    static String port;
    static String destinationDirectory;

    Device device;
    CameraHandler cameraHandler;
    boolean keepRunning = false;
    double exposureTime = 60.0;


    //TODO - Set gain and offset - 60 secs are quite dark on default settings!

    private void connectToServer() {
        indiClient = new IndiClient(host, Integer.parseInt(port), cameraHandler);

        while (!indiClient.hasChange()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException i) {
                LOGGER.info("Something interrupted my sleep! Good morning!");
            }
        }

        if (indiClient.hasChange()) { //Server is ready
            indiClient.resetChanged();
        }
    }


    private void start() {
        cameraHandler = new CameraHandler(destinationDirectory);
        connectToServer();

        try {
            cameraHandler.connectToCamera(indiClient);
            Thread.sleep(1000); //We wait for a second to make sure, the server is ready with the camera.

            keepRunning = true;

            takePictures(exposureTime);

        } catch (IOException i) {
            LOGGER.error("Got an error when trying to connect to the camera! " + i.getMessage());
        } catch (InterruptedException i) {
            LOGGER.info("Someone called?? I was taking a nap.");
        }
    }

    private void takePictures(double exposureTime) {
        while (keepRunning) {
            try {
                Thread.sleep(1000);
                cameraHandler.setCCDExposure(exposureTime);

                while (!indiClient.pictureArrived) {
                    Thread.sleep(500);
                }
            } catch (InterruptedException i) {
                LOGGER.info("Sleep interrupted for some reason... No worries, I'll get up now.");
            }
        }
    }

    public static void main(String[] args) {

//        Logging.setupLogConsoleLogger();
        CommandLine cmd = CommandLineController.parseCommandLine(args);
        Logging.setupFileLogger(cmd);

        host = cmd.getOptionValue("s", "127.0.0.1");
        port = cmd.getOptionValue("p", Integer.toString(Constants.INDI_DEFAULT_PORT));
        destinationDirectory = cmd.getOptionValue("f", ".");

        AllSkyCamera allSkyCamera = new AllSkyCamera();

        allSkyCamera.start();
    }

}



