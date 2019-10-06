package stardancer.observatory.allsky;


import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import java.io.IOException;

public class AllSkyCamera {


    private static final Logger LOGGER = Logger.getLogger(AllSkyCamera.class);

    IndiClient indiClient;
    static String destinationDirectory;

    Device device;
    CameraHandler cameraHandler;
    boolean keepRunning = false;
    double exposureTime = 10.0;
    Settings settings;


    public AllSkyCamera(Settings settings) {
        this.settings = settings;
    }

    //TODO - Set gain and offset - 60 secs are quite dark on default settings!

    private void connectToServer() {
        indiClient = new IndiClient(settings.getStringSettingFor(Settings.INDI_SERVER_IP),settings.getIntSettingFor(Settings.INDI_SERVER_PORT), cameraHandler);

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
        cameraHandler = new CameraHandler(settings);
        connectToServer();

        try {
            cameraHandler.connectToCamera(indiClient);
            Thread.sleep(1000); //We wait for a second to make sure, the server is ready with the camera.

            keepRunning = true;

            startImagingLoop();

        } catch (IOException i) {
            LOGGER.error("Got an error when trying to connect to the camera! " + i.getMessage());
        } catch (InterruptedException i) {
            LOGGER.info("Someone called?? I was taking a nap.");
        }
    }

    private void startImagingLoop() {

        LOGGER.debug("Starting imaging loop!");
        while (settings.getBooleanSettingFor(Settings.EXPOSE_CAMERA)) {
            try {
                Thread.sleep(1000);
                LOGGER.debug("Starting exposure!");
                cameraHandler.exposeCCD();

                while (!indiClient.pictureArrived) {
                    Thread.sleep(500);
                    LOGGER.debug("Waiting for image!");
                }
            } catch (InterruptedException i) {
                LOGGER.info("Sleep interrupted for some reason... No worries, I'll get up now.");
            }
        }
        LOGGER.debug("Ending imaging loop!");
    }

    public static void main(String[] args) {
        Settings settings = new Settings();
//        Logging.setupLogConsoleLogger();
        CommandLine cmd = CommandLineController.parseCommandLine(args);
        Logging.setupFileLogger(cmd);

        settings.setSettingFor(Settings.INDI_SERVER_IP, cmd.getOptionValue("s", settings.getStringSettingFor(Settings.INDI_SERVER_IP)));
        settings.setSettingFor(Settings.INDI_SERVER_PORT, cmd.getOptionValue("p", settings.getStringSettingFor(Settings.INDI_SERVER_PORT)));
        settings.setSettingFor(Settings.CAMERA_IMAGE_DOWNLOAD_DIRECTORY, cmd.getOptionValue("f", settings.getStringSettingFor(Settings.CAMERA_IMAGE_DOWNLOAD_DIRECTORY)));
        settings.setSettingFor(Settings.CAMERA_EXPOSURE_TIME, cmd.getOptionValue("e", settings.getStringSettingFor(Settings.CAMERA_EXPOSURE_TIME)));

        Server inputServer = new Server(settings);
        Thread serverThread = new Thread(inputServer);
        serverThread.start();

//        while (true) {
//            try {
//                Thread.sleep(1000);
//                System.out.println(settings.toString());
//            } catch (InterruptedException i) {
//
//            }
//
//        }



//        TwilightDataRetriever.getTwilightInformation();

        AllSkyCamera allSkyCamera = new AllSkyCamera(settings);

        allSkyCamera.start();
    }

}



