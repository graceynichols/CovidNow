package com.example.covidnow.repository;

import android.util.Log;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.models.Location;
import com.example.covidnow.viewmodels.MapsViewModel;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

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
        query.whereEqualTo("place_id", placeId);
        query.getFirstInBackground(locationGetCallback);
    }
}
