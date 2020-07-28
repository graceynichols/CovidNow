package com.example.covidnow.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.repository.GeocodingRepository;
import com.example.covidnow.repository.ParseRepository;
import com.google.android.gms.location.LocationResult;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Headers;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    public static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent != null) {
            Log.i(TAG, "On receive");
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    final List<Location> locations = result.getLocations();
                    // Query this location's google place ID
                    GeocodingRepository geocodingRepository = new GeocodingRepository();
                    final ParseRepository parseRepository = new ParseRepository();
                    geocodingRepository.queryGeocodeLocation(locations.get(0).getLatitude(), locations.get(0).getLongitude(), context.getString(R.string.google_maps_key), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            try {
                                Log.i(TAG, "Geocoding background response " + json.jsonObject.toString());
                                com.example.covidnow.models.Location newLocation = com.example.covidnow.models.Location.fromGeocodingJson((JSONObject) ((JSONArray) json.jsonObject.get("results")).get(0));
                                // Find if this is a hotspot from Parse
                                Log.i(TAG, "Searching for place " + newLocation.getPlaceId());
                                parseRepository.searchPlace(newLocation.getPlaceId(), new GetCallback<com.example.covidnow.models.Location>() {
                                    @Override
                                    public void done(com.example.covidnow.models.Location object, ParseException e) {
                                        Log.i(TAG, "Parse query finished");
                                        if (object != null) {
                                            ParseUser user = ParseUser.getCurrentUser();
                                            // The place is saved in parse
                                            String placeId = object.getPlaceId();
                                            if (object.isHotspot() && (!placeId.equals(user.get(ParseRepository.KEY_CURRENT_LOCATION)))) {
                                                Log.i(TAG, "Current location is a hotspot");
                                                // Save this as user's current location, prevents repeated notifications
                                                user.put(ParseRepository.KEY_CURRENT_LOCATION, placeId);
                                                user.saveInBackground();
                                                // This place is a hotspot, send notification
                                                Utils.setLocationUpdatesResult(context, locations);
                                                Utils.sendNotification(context, "Your current location is marked as a hotspot");
                                                Log.i(TAG, Utils.getLocationUpdatesResult(context));
                                            }
                                        } else {
                                            Log.i(TAG, "Current location not saved in Parse");
                                        }
                                        // TODO check if county has a lot of cases
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "Failed to retrieve user's background location place ID");
                        }


                    });
                }
            }
        }
    }
}
