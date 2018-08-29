package stardancer.observatory.allsky;

import org.apache.log4j.Logger;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.*;
import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class IndiClient implements INDIServerConnectionListener, INDIDeviceListener, INDIPropertyListener {

    private static final Logger LOGGER = Logger.getLogger(IndiClient.class);

    static {
        INDIURLStreamHandlerFactory.init();
    }

    HashMap<String,Device> devices;
    boolean change;


    private INDIServerConnection indiServerConnection;


    public IndiClient(String host, int port) {
        indiServerConnection = new INDIServerConnection(host, port);

        devices = new HashMap<>();

        change = false;

        indiServerConnection.addINDIServerConnectionListener(this);



        try {
            indiServerConnection.connect();
            indiServerConnection.askForDevices();
//            INDIDevice camera = indiServerConnection.getDevice("ZWO CCD");
//
//            List<INDIProperty<?>> tese = camera.getAllProperties();
//
//
//            List<INDIDevice> hasd = indiServerConnection.getDevicesAsList();
//
//            String hasadsf = "";
        } catch (IOException i) {
            LOGGER.error("Cannot connect to INDI server - " + i.getMessage());
        }
    }

//
//    public String test() {
//        INDIProperty property = indiServerConnection.getProperty("ZWO CCD ASI178MM", "DRIVER_INFO");
//        return property.getNameStateAndValuesAsString();
//    }



//
//
//    public List<INDIDevice> getAllDevices() {
//
//
////        String[] asdasd = indiServerConnection.getDeviceNames();
//
//
//        INDIDevice test = indiServerConnection.getDevice("ZWO CCD ASI178MM");
//
//
//
//        return indiServerConnection.getDevicesAsList();
//    }

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
        LOGGER.info("Property changed: " + property.getNameStateAndValuesAsString());
        String device_name = property.getDevice().getName();
        devices.get(device_name).updateProperty(property);
        change = true;
    }

    public HashMap<String,Device> getDevices(){
        return devices;
    }

    public Device getDevice(String device_name){
        return devices.get(device_name);
    }

    public ArrayList<String> getDevicesNames(){
        ArrayList<String> names=new ArrayList<>();

        for (String name : devices.keySet()) {
            names.add(name);
        }
        return names;
    }

    public boolean has_change(){
        return change;
    }

    public void changeRead(){
        change=false;
    }

    public String getNameConecction(){
        return indiServerConnection.getURL().getHost();
    }






}



