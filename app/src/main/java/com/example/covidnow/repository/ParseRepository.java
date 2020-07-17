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

public class ParseRepository {
    private static final String TAG = "ParseRepository";

    public static void searchPlaces(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
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
                            MapsViewModel.getNearbyPlacesList().getValue().add(com.example.covidnow.models.Location.fromJson(newLocation));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        // The location was saved in parse
                        Log.i(TAG, "* This location WAS previously saved " + placeId);
                        MapsViewModel.getNearbyPlacesList().getValue().add(object);
                    }
                }
            });
        }
    }
}
