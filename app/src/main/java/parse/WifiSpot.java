package parse;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import util.myLog;

/**
 * Created by Milenko on 30/07/2015.
 * it represents the objects SSIDS from Parse
 */
@ParseClassName("SSIDS")
public class WifiSpot extends ParseObject {


    public WifiSpot(String selectedSSID, String selectedBSSID, WeaconParse we, double latitude, double longitude) {
        setSSID(selectedSSID);
        setBSSID(selectedBSSID);
        setWeacon(we);
        setGPS(new ParseGeoPoint(latitude, longitude));
        setOwner(ParseUser.getCurrentUser());
        setAutomatic(false);
    }

    public WifiSpot() {
    }

    public String getBSSID() {
        String BSSID = getString("bssid");
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        put("bssid", BSSID);
    }

    public String getSSID() {
        String SSID = getString("ssid");
        return SSID;
    }

    public void setSSID(String SSID) {
        put("ssid", SSID);
    }

    public String getPlaceId() {
        String placeId = getParseObject("associated_place").getObjectId();
        return placeId;
    }

    public boolean isValidated() {
        return getBoolean("validated");
    }

    public void setValidated(boolean validated) {
        put("validated", validated);
    }

    public boolean isRelevant() {
        boolean relevant = getBoolean("relevant");
        return relevant;
    }

    public boolean isUniqueInArea() {
        boolean unico = getBoolean("unico");
        return unico;
    }

    public ParseGeoPoint getGps() {
        ParseGeoPoint gps = getParseGeoPoint("GPS");
        return gps;
    }

    @Override
    public String toString() {
        return "WifiSpot{'" + getSSID() + "' | " + getBSSID() + " | relevant=" + isRelevant() + "}";
    }

    public WeaconParse getWeacon() {
        WeaconParse weacon = (WeaconParse) getParseObject("associated_place");
        return weacon;
    }

    public void setWeacon(WeaconParse weacon) {
        put("associated_place", weacon);
    }

    public void setGPS(ParseGeoPoint GPS) {
        put("GPS", GPS);
    }

    public void setOwner(ParseUser owner) {
        put("owner", owner);
    }

    public void setAutomatic(boolean automatic) {
        put("Automatic", automatic);
    }

    public void Save() {
        this.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("spot saved ");
                } else {
                    myLog.add("spot not saved " + e.getMessage());

                }

            }
        });
    }

    public String summarizeWithWeacon() {
        return getSSID() + "(" + getBSSID() + ") -> \"" + getWeacon().getName() + "\"";
    }
}
