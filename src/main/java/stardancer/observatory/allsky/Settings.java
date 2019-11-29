package stardancer.observatory.allsky;

import org.indilib.i4j.Constants;

import java.util.HashMap;
import java.util.Map;

public class Settings {

    //Setting Names
    public static final String INDI_SERVER_IP = "Indi_Server_Ip";
    public static final String INDI_SERVER_PORT = "Indi_Server_Port";
    public static final String CAMERA_EXPOSURE_TIME = "Camera_Exposure_Time";
    public static final String CAMERA_EXPOSURE_INTERVAL = "Camera_Exposure_Interval";
    public static final String CAMERA_GAIN = "Camera_Gain";
    public static final String CAMERA_IMAGE_DOWNLOAD_DIRECTORY = "Camera_Image_Download_Directory";
    public static final String ALL_SKY_CAMERA_SERVER_PORT = "All_Sky_Camera_Server_Port";
    public static final String EXPOSE_CAMERA = "Keep_Exposing_Camera";
    public static final String SINGLE_EXPOSURE = "Single_Exposure";

    private final String STANDARD_INDI_SERVER_IP = "127.0.0.1";
    private final String STANDARD_INDI_SERVER_PORT = Integer.toString(Constants.INDI_DEFAULT_PORT);
    private final String STANDARD_EXPOSURE_TIME = "60";
    private final String STANDARD_EXPOSURE_INTERVAL = "0";
    private final String STANDARD_CAMERA_GAIN = "150";
    private final String STANDARD_CAMERA_IMAGE_DOWNLOAD_DIRECTORY = ".";
    private final String STANDARD_ALL_SKY_CAMERA_SERVER_PORT = "4242";
    private final String STANDARD_EXPOSE_CAMERA = "true";

    Map<String, String> settings = new HashMap<>();

    public Settings() {
        settings.put(INDI_SERVER_IP, STANDARD_INDI_SERVER_IP);
        settings.put(INDI_SERVER_PORT, STANDARD_INDI_SERVER_PORT);
        settings.put(CAMERA_EXPOSURE_TIME, STANDARD_EXPOSURE_TIME);
        settings.put(CAMERA_EXPOSURE_INTERVAL, STANDARD_EXPOSURE_INTERVAL);
        settings.put(CAMERA_GAIN, STANDARD_CAMERA_GAIN);
        settings.put(CAMERA_IMAGE_DOWNLOAD_DIRECTORY, STANDARD_CAMERA_IMAGE_DOWNLOAD_DIRECTORY);
        settings.put(ALL_SKY_CAMERA_SERVER_PORT, STANDARD_ALL_SKY_CAMERA_SERVER_PORT);
        settings.put(EXPOSE_CAMERA, STANDARD_EXPOSE_CAMERA);
    }

    public String getStringSettingFor(String settingName) {
        if (settings.containsKey(settingName)) {
            return settings.get(settingName);
        } else {
            return null;
        }
    }

    public double getDoubleSettingFor(String settingName) {
        if (settings.containsKey(settingName)) {
            return Double.parseDouble(settings.get(settingName));
        } else {
            return 1.0d;
        }
    }

    public int getIntSettingFor(String settingName) {
        if (settings.containsKey(settingName)) {
            return Integer.parseInt(settings.get(settingName));
        } else {
            return 0;
        }
    }

    public boolean getBooleanSettingFor(String settingName) {
        if (settings.containsKey(settingName)) {
            if ("true".equalsIgnoreCase(settings.get(settingName))) {
                return true;
            } else if ("false".equalsIgnoreCase(settings.get(settingName))) {
                return false;
            }
        }

        return false;
    }

    public void setSettingFor(String settingsName,String value) {
        settings.put(settingsName, value);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry entry : settings.entrySet()) {
            stringBuilder.append(entry.getKey() + ";" + entry.getValue() + " --- ");
        }
        return stringBuilder.toString();
    }
}
