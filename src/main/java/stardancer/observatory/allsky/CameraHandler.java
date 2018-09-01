package stardancer.observatory.allsky;

import org.apache.log4j.Logger;
import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.*;
import org.indilib.i4j.properties.INDIStandardProperty;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CameraHandler implements INDIPropertyListener {

    public enum ImageDepth {
        DEPTH_8, DEPTH_16
    }


    private static final Logger LOGGER = Logger.getLogger(CameraHandler.class);

    private Device device;
    private IndiClient indiClient;






    public void connectToCamera(IndiClient indiClient) throws IOException {

        this.indiClient = indiClient;

        device = indiClient.getDevice("ZWO CCD ASI178MM");
        List<INDIProperty> properties = device.getAllProperties();

        INDIProperty specificProperty = null;

        for (INDIProperty property : properties) {
            if (property.getName().equals("CONNECTION")) {
                specificProperty = property;
                break;
            }
        }

        if (specificProperty != null) {
            Iterator<INDIElement> elementIterator = specificProperty.iterator();

            try {
                while (elementIterator.hasNext()) {
                    INDIElement element = elementIterator.next();
                    String elementName = element.getName();
                    if (elementName.equals("CONNECT")) {
                        element.setDesiredValue(Constants.SwitchStatus.ON);
                    } else if (elementName.equals("DISCONNECT")) {
                        element.setDesiredValue(Constants.SwitchStatus.OFF);
                    }
                }
                specificProperty.sendChangesToDriver();
                while (!indiClient.hasChange()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException i) {
                        LOGGER.info("We've been interrupted! " + i.getMessage());
                    }
                }
                device.updateProperty(specificProperty);
            } catch (INDIValueException i) {
                LOGGER.error("Tried to set an invalid value on the camera! We should not get to here - so we probably will");
            } catch (IOException i) {
                LOGGER.error("Could not communicate with server! Is it dead?");
                throw i;
            }
        }
    }

    public void propertyChanged(INDIProperty property) {
        if (indiClient.hasNewPicture() && property.getName().equals("CCD1")) {

            INDIBLOBValue imageValue = (INDIBLOBValue) property.getElement("CCD1").getValue();

            getImage(imageValue);
        }
    }


    public void setImageDepth(ImageDepth imageDepth) {




    }

    public void setCCDExposure(Double seconds) throws NullPointerException {
        if (device == null) {
            throw new NullPointerException("Trying to change the exposure time on a non-existant camera, are we... Tsk tsk...");
        }

        List<INDIProperty> properties = null;
        List<String> groups = device.getGroupsNames();
        for (String group : groups) {
            if (group.equals("Main Control")) {
                properties = device.getGroupProperties(group);
                break;
            }
        }

        if (properties == null) {
            LOGGER.error("Couldn't find the \"Main Control\" property! Something has failed - connection to server ok??");
            return;
        }

        INDIProperty exposure;
        for (INDIProperty property : properties) {
            if (property.getName().equals("CCD_EXPOSURE")) {
                try {
                    property.getElement("CCD_EXPOSURE_VALUE").setDesiredValue(seconds);
                    property.sendChangesToDriver();
                    device.updateProperty(property);
                    break;
                } catch (INDIValueException i) {
                    LOGGER.error("Setting a wrong value for the CCD exposure! Something went wrong!");
                } catch (IOException i) {
                    LOGGER.error("Could not update value of CCD exposure on server!");
                }
            }
        }

    }

    public void getImage(INDIBLOBValue picture) {

//        INDIBLOBProperty indiBlobProperty = null;

//        while (indiBlobProperty == null) {
            try {
//                indiBlobProperty = picture;
                if (picture != null) {
                    File file = new File("c:\\temp\\image_" + LocalDateTime.now().
                            format(DateTimeFormatter.ofPattern("yyyy_MMM_d-H-m-s")) + ".fits");
                    picture.saveBLOBData(file);
                }
            } catch (IOException i) {
                LOGGER.error("Some error happened writing to a file! " + i.getMessage());
            }

    }



}

