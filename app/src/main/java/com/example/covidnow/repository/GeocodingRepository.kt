package com.example.covidnow.repository

import android.util.Log
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler

class GeocodingRepository {
    fun queryGeocodeLocation(x: Double, y: Double, apiKey: String, jsonHttpResponseHandler: JsonHttpResponseHandler?) {
        // Make API call to Google's GeoCoding API
        val client = AsyncHttpClient()
        // Build URL for request using lat/lng
        val geoUrl = "$GEOCODE_URL$x,$y&key=$apiKey"
        Log.i(TAG, "Geo URL: $geoUrl")
        // Make GET request
        client[geoUrl, jsonHttpResponseHandler]
    }



    companion object {
        private const val TAG = "GeocodingRepository"
        const val GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
    }
}