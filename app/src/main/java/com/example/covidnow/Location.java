package com.example.covidnow;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseClassName;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

import java.util.Date;

@Parcel(analyze = {Location.class})
@ParseClassName("Location")

public class Location extends ParseObject {
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_IS_HOTSPOT = "isHotspot";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_HOTSPOT_STATUS = "lastHotspotStatus";
    public static final String KEY_PICTURE = "picture";

    public Location() { super(); }

    public String getAddress() {return getString(KEY_ADDRESS); }

    public void setAddress(String address) { put(KEY_ADDRESS, address); }

    public boolean isHotspot() {return getBoolean(KEY_IS_HOTSPOT); }

    public void setIsHotspot(boolean hotspotStatus) { put(KEY_IS_HOTSPOT, hotspotStatus); }

    public String getName() {return getString(KEY_NAME); }

    public void setName(String name) { put(KEY_NAME, name); }

    public Date getLastHotspotStatus() {return getDate(KEY_LAST_HOTSPOT_STATUS); }

    public void setLastHotspotStatus(Date date) { put(KEY_LAST_HOTSPOT_STATUS, date); }

    public ParseFile getPicture() {return getParseFile(KEY_PICTURE); }

    public void setPicture(ParseFile picture) { put(KEY_PICTURE, picture); }


}
