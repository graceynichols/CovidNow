package com.example.covidnow.repository;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.viewmodels.MapsViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import okhttp3.Headers;

public class PlacesRepository {

    private static final String TAG = "PlacesRepository";
    private static final String PLACE_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?";
    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String SEARCH_RADIUS = "30000";

    public static void findAPlace(String search, double lat, double lng, final Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("key",  context.getString(R.string.google_maps_key));

        // Search for relevant nearby locations
        String coords = lat + "," + lng;
        params.put("location", coords);
        params.put("radius", SEARCH_RADIUS);
        params.put("keyword", search);
        params.put("opennow", true);
        Log.i(TAG, "Coordinates: " + coords);

        client.get(NEARBY_SEARCH_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "Places API Response: " + json.toString());
                // Add places to recyclerview
                try {
                    JSONArray array = json.jsonObject.getJSONArray("results");
                    MapsViewModel.getNearbyPlacesJson().setValue(array);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error retrieving places results");
                    Toast.makeText(context, "Error retrieving places results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Error searching places");
                Looper.prepare();
                Toast.makeText(context, "Error searching places", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
