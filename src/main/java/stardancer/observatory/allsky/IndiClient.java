package stardancer.observatory.allsky;

import org.apache.log4j.Logger;
import org.indilib.i4j.Constants;
import org.indilib.i4j.client.*;
import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class IndiClient implements INDIServerConnectionListener, INDIDeviceListener, INDIPropertyListener, INDIElementListener {

    private static final Logger LOGGER = Logger.getLogger(IndiClient.class);

    static {
        INDIURLStreamHandlerFactory.init();
    }

    HashMap<String,Device> devices;
    boolean change;
    boolean pictureArrived = false;
    INDIPropertyListener externalPropertyListener;
    INDIProperty imageProperty;
    INDIBLOBElement picture;
    List<INDIProperty> propertyList;

    private INDIServerConnection indiServerConnection;


    public IndiClient(String host, int port, INDIPropertyListener propertyListener) {
        indiServerConnection = new INDIServerConnection(host, port);
        devices = new HashMap<>();
        change = false;
        propertyList = new ArrayList<>();
        indiServerConnection.addINDIServerConnectionListener(this);
        this.externalPropertyListener = propertyListener;

        try {
            indiServerConnection.connect();
            indiServerConnection.askForDevices();
        } catch (IOException i) {
            LOGGER.error("Cannot connect to INDI server - " + i.getMessage());
        }
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        LOGGER.info("New device: " + device.getName());

        try {
            devices.put(device.getName(), new Device(device.getName()));
            device.blobsEnable(Constants.BLOBEnables.ALSO);
        } catch (IOException i) {
            LOGGER.error("Error asking for BLOB - " + i.getMessage());
        }
        device.addINDIDeviceListener(this);
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        LOGGER.info("Device removed: " + device.getName());
        devices.remove(device.getName());
        change = true;
        device.removeINDIDeviceListener(this);
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        LOGGER.info("Connection lost to server!");
        devices.clear();
        change = true;
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp /*Not used*/, String message) {
        LOGGER.info("New server message: " + message);
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        LOGGER.info("New property (" + property.getName() + ") added to device " + device.getName());
        devices.get(device.getName()).addProperty(property);
        change = true;
        property.addINDIPropertyListener(this);
        if (property.getName().equals("CCD1")) {
            property.addINDIPropertyListener(externalPropertyListener);
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        LOGGER.info("Property (" + property.getName() + ") removed from the device " + device.getName());
        devices.get(device.getName()).removeProperty(property);
        change = true;
        property.removeINDIPropertyListener(this);
    }

    @Override
    public void messageChanged(INDIDevice device) {
        LOGGER.info("New Device Message: " + device.getName() + " - " + device.getTimestamp() + " - " + device.getLastMessage());
    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {

        if (!propertyList.contains(property)) {
            propertyList.add(property);
        }

        imageProperty = property;
        LOGGER.info("Property changed: " + property.getNameStateAndValuesAsString());


        String device_name = property.getDevice().getName();
        devices.get(device_name).updateProperty(property);

        if (property.getName().equals("CCD1")) {

            picture = (INDIBLOBElement) property.getElement("CCD1");
            pictureArrived = true;

        }
        change = true;
    }

    @Override
    public void elementChanged(INDIElement element) {
        LOGGER.info("Element changed: " + element.getName());
        change = true;
    }

    public HashMap<String,Device> getDevices() {
        return devices;
    }

    public Device getDevice(String device_name) {
        return devices.get(device_name);
    }

    public ArrayList<String> getDevicesNames() {
        ArrayList<String> names=new ArrayList<>();

        for (String name : devices.keySet()) {
            names.add(name);
        }
        return names;
    }

    public boolean hasNewPicture() {
        return pictureArrived;
    }

    public boolean hasChange() {
        return change;
    }

    public void resetChanged() {
        change = false;
    }

    public String getNameConecction() {
        return indiServerConnection.getURL().getHost();
    }

    public void listenToDevice(Device device) {
        indiServerConnection.addINDIDeviceListener(device.getName(), this);
    }

    public void stopListeningToDevice(Device device) {
        indiServerConnection.removeINDIDeviceListener(device.getName(), this);
    }

    public INDIProperty getPropertyForDevice(Device device, String propertyName){
        INDIDevice someDevice =  indiServerConnection.getDevice(device.getName());
        INDIProperty<?> indiProperty = someDevice.getProperty(propertyName);
        return indiProperty;
    }
}



