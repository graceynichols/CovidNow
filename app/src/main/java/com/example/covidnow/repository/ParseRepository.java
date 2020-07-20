package com.example.covidnow.repository;

import android.util.Log;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.models.Location;
import com.example.covidnow.viewmodels.MapsViewModel;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParseRepository {
    private static final String TAG = "ParseRepository";

    public void searchPlace(String placeId, GetCallback<Location> locationGetCallback) {
        // Search if this location is already saved
        Log.i(TAG, "Searching for Place id: " + placeId);
        ParseQuery<Location> query =  ParseQuery.getQuery("Location");
        query.include(com.example.covidnow.models.Location.KEY_PLACE_ID);
        // Only get places with this place ID
        query.whereEqualTo("place_id", placeId);
        // Only need 1 place
        query.getFirstInBackground(locationGetCallback);
    }


    public void saveLocation(Location newLocation) {
        // Last updated at = current date
        newLocation.setUpdatedAt();
        newLocation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving location", e);
                } else {
                    Log.i(TAG, "Location save sucessful");
                }
            }
        });
    }
}
