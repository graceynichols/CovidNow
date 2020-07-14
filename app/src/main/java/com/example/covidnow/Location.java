package com.example.covidnow;

import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel(analyze = {Location.class})
@ParseClassName("Location")

public class Location extends ParseObject {
    private static final String TAG = "Location";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_IS_HOTSPOT = "isHotspot";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_HOTSPOT_STATUS = "lastHotspotStatus";
    public static final String KEY_PICTURE = "picture";
    public static final String KEY_PLACE_ID = "place_id";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public Location() { super(); }

    public static Location fromJson(JSONObject json) throws JSONException {
        Location location = new Location();
        location.setAddress(json.getString("vicinity"));
        // If it's not saved it must not be marked as a hotspot
        location.setIsHotspot(false);
        location.setName(json.getString("name"));
        location.setPlaceId(json.getString("place_id"));
        location.setLatitude(json.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
        location.setLongitude(json.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
        return location;
    }

    public String getPlaceId() {return getString(KEY_PLACE_ID); }

    public void setPlaceId(String id) { put(KEY_PLACE_ID, id); }

    public String getAddress() {return getString(KEY_ADDRESS); }

    public void setAddress(String address) { put(KEY_ADDRESS, address); }

    public boolean isHotspot() {
        return getBoolean(KEY_IS_HOTSPOT); }

    public void setIsHotspot(boolean hotspotStatus) { put(KEY_IS_HOTSPOT, hotspotStatus); }

    public String getName() {return getString(KEY_NAME); }

    public void setName(String name) { put(KEY_NAME, name); }

    public Date getLastHotspotStatus() {return getDate(KEY_LAST_HOTSPOT_STATUS); }

    public void setLastHotspotStatus(Date date) { put(KEY_LAST_HOTSPOT_STATUS, date); }

    public ParseFile getPicture() {return getParseFile(KEY_PICTURE); }

    public void setPicture(ParseFile picture) { put(KEY_PICTURE, picture); }

    public double getLatitude() {return getDouble(KEY_LATITUDE); }

    public void setLatitude(double newLat) { put(KEY_LATITUDE, newLat); }

    public double getLongitude() {return getDouble(KEY_LONGITUDE); }

    public void setLongitude(double newLng) { put(KEY_LONGITUDE, newLng); }
}
