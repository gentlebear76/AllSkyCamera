package stardancer.observatory.allsky;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.IOException;
import java.util.Map;

public class TwilightDataRetriever {

    private static final String TWILIGHT_WEB_SERVICE = "https://api.sunrise-sunset.org/json?lat=56.175251&lng=9.813759&date=today";

//    public TwilightDataRetriever() {
//
//    }

    public static String getTwilightInformation() {
        try {
//            String json = Request.Get(TWILIGHT_WEB_SERVICE).execute().returnContent().asString();
            String json = "{\"results\":{\"sunrise\":\"6:10:09 AM\",\"sunset\":\"3:59:52 PM\",\"solar_noon\":\"11:05:00 AM\",\"day_length\":\"09:49:43\",\"civil_twilight_begin\":\"5:31:38 AM\",\"civil_twilight_end\":\"4:38:22 PM\",\"nautical_twilight_begin\":\"4:48:06 AM\",\"nautical_twilight_end\":\"5:21:55 PM\",\"astronomical_twilight_begin\":\"4:04:56 AM\",\"astronomical_twilight_end\":\"6:05:05 PM\"},\"status\":\"OK\"}";
            JSONObject obj = new JSONObject(json);

            JSONObject jsss = obj.getJSONObject("results");

            String adsf = jsss.getString("sunrise");

            String asdas = "";

//        } catch (ClientProtocolException c) {
//
//        } catch (IOException i) {

        } catch (JSONException j) {
            String hula = "";
        }


        return null;
    }
















}
