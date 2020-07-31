package com.example.covidnow.repository

import android.util.Log
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler

class PlacesRepository {
    fun findAPlace(search: String, lat: Double, lng: Double, apiKey: String, jsonHttpResponseHandler: JsonHttpResponseHandler?) {
        val client = AsyncHttpClient()
        val params = RequestParams()
        params["key"] = apiKey

        // Search for relevant nearby locations
        val coords = "$lat,$lng"
        params["location"] = coords
        //params["radius"] = SEARCH_RADIUS
        params["rankby"] = "distance"
        params["keyword"] = search
        params["opennow"] = "true"
        Log.i(TAG, "Coordinates: $coords")
        Log.i(TAG, "Making Places API call")
        client[NEARBY_SEARCH_URL, params, jsonHttpResponseHandler]
    }

    companion object {
        private const val TAG = "PlacesRepository"
        private const val PLACE_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?"
        private const val NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
        private const val SEARCH_RADIUS = "30000"
    }
}