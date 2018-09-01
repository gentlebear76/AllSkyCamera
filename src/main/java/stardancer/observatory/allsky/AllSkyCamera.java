package stardancer.observatory.allsky;


import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.indilib.i4j.Constants;
import org.indilib.i4j.client.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class AllSkyCamera {


    private static final Logger LOGGER = Logger.getLogger(AllSkyCamera.class);

    IndiClient indiClient;
    static String host;
    static String port;

    Device device;
    CameraHandler cameraHandler;

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
        cameraHandler = new CameraHandler();
        connectToServer();

        try {
            cameraHandler.connectToCamera(indiClient);

//            while (true) {
                cameraHandler.setCCDExposure(0.1);

                while (!indiClient.pictureArrived) {
                    Thread.sleep(500);
                }

//                cameraHandler.getImage((INDIBLOBElement)indiClient.getPicture());
                String aewdasd = "";
//            }
//            INDIProperty property = indiClient.getPropertyForDevice(device, "CCD_EXPOSURE");
//            String hest = "";
        } catch (IOException i) {
            LOGGER.error("Got an error when trying to connect to the camera! " + i.getMessage());
        } catch (InterruptedException i) {

        }
    }




    public static void main(String[] args) throws Exception {

        Logging.setupLogConsoleLogger();
        CommandLine cmd = CommandLineController.parseCommandLine(args);
//        Logging.setupFileLogger(cmd);

        host = cmd.getOptionValue("s", "127.0.0.1");
        port = cmd.getOptionValue("p", Integer.toString(Constants.INDI_DEFAULT_PORT));

        AllSkyCamera allSkyCamera = new AllSkyCamera();

        allSkyCamera.start();
//
//        Map<String, Device> test = indiClient.getDevices();
//
//        List<String> deviceNames = indiClient.getDevicesNames();

//        String hest  = "";


//                INDIProperty cameraConnectedProperty = device.getAllProperties().get(0);
//                device.updateProperty(cameraConnectedProperty);

//                indiClient.listenToDevice(device);

//                INDIProperty property = indiClient.getPropertyForDevice(device);
//
//                Iterator<INDIElement> elementIterator = property.iterator();
//
//                while(elementIterator.hasNext()) {
//                    INDIElement picture = elementIterator.next();
//                    String elementName = picture.getName();
//                    if (elementName.equals("CONNECT")) {
//                        picture.setDesiredValue(Constants.SwitchStatus.ON);
//                    } else if (elementName.equals("DISCONNECT")) {
//                        picture.setDesiredValue(Constants.SwitchStatus.OFF);
//                    }
//                }


//                indiClient.getPropertyForDevice(device);

//            Device hest2 = indiClient.getDevice("ZWO CCD ASI178MM");

//            String hest = "";

    }

}



