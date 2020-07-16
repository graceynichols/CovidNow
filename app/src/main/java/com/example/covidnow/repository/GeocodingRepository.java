package com.example.covidnow.repository;

import android.content.Context;
import android.util.Log;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.viewmodels.HomeViewModel;

import okhttp3.Headers;

public class GeocodingRepository {
    private static final String TAG = "GeocodingRepository";
    public static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";

    public static void queryGeocodeLocation(double x, double y, Context context) {
        // Make API call to Google's GeoCoding API
        AsyncHttpClient client = new AsyncHttpClient();

        String geoUrl = GEOCODE_URL + x + "," + y + "&key=" + context.getString(R.string.google_maps_key);
        Log.i(TAG, "Geo URL: " + geoUrl);
        client.get(geoUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "Response" + " " + json.toString());
                HomeViewModel.getJsonLocation().setValue(json.jsonObject);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Query location failed");
            }
        });
    }
}
