package stardancer.observatory.allsky;

import org.apache.log4j.Logger;
import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CameraHandler implements INDIPropertyListener {

    public enum ImageDepth {
        DEPTH_8, DEPTH_16
    }


    private static final Logger LOGGER = Logger.getLogger(CameraHandler.class);

    private Device device;
    private IndiClient indiClient;
    private boolean ready = true;
    private String fileLocation;
    private Settings settings;
    private int gainRandomNumber = 0;

    private int imageWidth;
    private int imageHeight;

//    private long imageCounter = 1;

    List<INDIProperty> properties;

    public CameraHandler(Settings settings) {
        this.settings = settings;

    }

    public void connectToCamera(IndiClient indiClient) throws IOException {

        LOGGER.debug("Connecting to camera!");
        this.indiClient = indiClient;

        device = indiClient.getDevice("ZWO CCD ASI178MM");
        properties = device.getAllProperties();

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

    private void updateGain() {
        INDIProperty specificProperty = null;
        for (INDIProperty property : properties) {
            if (property.getName().equals("CCD_CONTROLS")) {
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
                    if (elementName.equals("Gain")) {
                        LOGGER.debug("Gain is at present - " + element.getValue());
                        Random random = new Random();
                        int tmpRandom;
                        while((tmpRandom = random.nextInt(11)) == gainRandomNumber) {
                            continue;
                        }
                        gainRandomNumber = tmpRandom;
                        element.setDesiredValue(settings.getDoubleSettingFor(Settings.CAMERA_GAIN) + gainRandomNumber); //We set the gain to what we want + a random number to force an update
                        specificProperty.sendChangesToDriver();
                        Thread.sleep(1000);
//                        element.setDesiredValue(settings.getDoubleSettingFor(Settings.CAMERA_GAIN)); //Here we set the gain value to what we want.
//                        specificProperty.sendChangesToDriver();
//                        Thread.sleep(1000);
                        LOGGER.debug("Now gain is  - " + element.getValue());
                        break;
                    }
                }

            } catch (INDIValueException i) {

            } catch (IOException i) {

            } catch (InterruptedException i) {

            }
        }
    }

    public void propertyChanged(INDIProperty property) {
        if (imageWidth == 0.0 && imageHeight == 0.0) {
            imageWidth = (int) Math.round((double) property.getDevice().getProperty("CCD_FRAME").getElement("WIDTH").getValue());
            imageHeight = (int) Math.round((double) property.getDevice().getProperty("CCD_FRAME").getElement("HEIGHT").getValue());
        }

        if (indiClient.hasNewPicture() && property.getName().equals("CCD1")) {
            ready = false;
            INDIBLOBValue imageValue = (INDIBLOBValue) property.getElement("CCD1").getValue();

//            saveImage(imageValue);
            saveImageAsPNG(imageValue);
            try {
                Thread.sleep(1000); //We wait a second or so to make sure the server has time to keep up.
            } catch (InterruptedException i) {
                LOGGER.info("Gotta get up and fly!");
            }
            ready = true;
        }
    }

    public void setImageDepth(ImageDepth imageDepth) {

    }

    public boolean isReady() {
        return ready;
    }

    public void exposeCCD() throws InterruptedException, NullPointerException {
        LOGGER.debug("Running exposure - exposure length is " + settings.getStringSettingFor(Settings.CAMERA_EXPOSURE_TIME) + " seconds!");
        while (!this.isReady()) {
            Thread.sleep(1000);
        }

        updateGain(); //We update the gain value for every exposure. Earlier we only did this on camera connect and that, for some reason, resulted in the gain sliding downwards over time... Why I have no idea!

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

        for (INDIProperty property : properties) {
            if (property.getName().equals("CCD_EXPOSURE")) {
                try {
                    property.getElement("CCD_EXPOSURE_VALUE").setDesiredValue(settings.getDoubleSettingFor(Settings.CAMERA_EXPOSURE_TIME));
                    property.sendChangesToDriver();
                    ready = false;
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

    /**
     * This method saves the images from the camera as png-files.
     * @param picture The raw picture data from the camera
     */
    public void saveImageAsPNG(INDIBLOBValue picture) {
        try {
            if (picture != null) {
                File file = new File(settings.getStringSettingFor(Settings.CAMERA_IMAGE_DOWNLOAD_DIRECTORY) + "/" + "image_" + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy_MMM_d-H-m-s")) + ".png");
//                File file = new File(fileLocation + "/" + "image_" + imageCounter + ".png");
//                imageCounter++;
                BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                bufferedImage.setData(Raster.createRaster(bufferedImage.getSampleModel(), new DataBufferByte(picture.getBlobData(), picture.getSize()), new Point()));
                ImageIO.write(bufferedImage, "png", file);
            }
        } catch (IOException i) {
            LOGGER.error("Could not save image! " + i.getMessage());
        }
    }

    /**
     * This method saves the images from the camera as fits-files.
     * @param picture The raw picture data from the camera
     */
    public void saveImage(INDIBLOBValue picture) {
        try {
            if (picture != null) {
                File file = new File(settings.getStringSettingFor(Settings.CAMERA_IMAGE_DOWNLOAD_DIRECTORY) + "/" + "image_" + LocalDateTime.now().
                        format(DateTimeFormatter.ofPattern("yyyy_MMM_d-H-m-s")) + ".fits");
                picture.saveBLOBData(file);
                ready = true;
            }
        } catch (IOException i) {
            LOGGER.error("Some error happened writing to a file! " + i.getMessage());
        }
    }
}

