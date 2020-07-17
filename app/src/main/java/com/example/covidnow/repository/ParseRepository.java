package com.example.covidnow.repository;

import android.util.Log;

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

    public static void searchPlaces(final JSONArray array) throws JSONException {
        final List<Location> finalPlaces = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final int ii = i;
            final JSONObject newLocation = (JSONObject) array.get(i);
            final String placeId = newLocation.getString("place_id");

            // Search if this location is already saved
            Log.i(TAG, "Searching for Place id: " + placeId);
            ParseQuery<Location> query =  ParseQuery.getQuery("Location");
            query.include(com.example.covidnow.models.Location.KEY_PLACE_ID);
            query.whereEqualTo("place_id", placeId);
            query.getFirstInBackground(new GetCallback<Location>() {
                @Override
                public void done(com.example.covidnow.models.Location object, ParseException e) {
                    if (object == null) {
                        // no location saved, must create new one
                        try {
                            Log.i(TAG, "This location was NOT previously saved " + placeId);
                            finalPlaces.add(com.example.covidnow.models.Location.fromJson(newLocation));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            Log.i(TAG, "Error parsing location from JSON");
                        }
                    } else {
                        // The location was saved in parse
                        Log.i(TAG, "* This location WAS previously saved " + placeId);
                        finalPlaces.add(object);
                    }
                    if (ii == array.length() - 1) {
                        // We've reached the end of the list
                        MapsViewModel.getNearbyPlacesList().setValue(finalPlaces);
                    }
                }
            });
        }
    }
}
