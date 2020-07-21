package com.example.covidnow.repository

import android.util.Log
import com.example.covidnow.models.Location
import com.parse.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class ParseRepository {
    fun searchPlace(placeId: String, locationGetCallback: GetCallback<Location>?) {
        // Search if this location is already saved
        Log.i(TAG, "Searching for Place id: $placeId")
        val query = ParseQuery.getQuery<Location>("Location")
        query.include(Location.KEY_PLACE_ID)
        // Only get places with this place ID
        query.whereEqualTo("place_id", placeId)
        // Only need 1 place
        query.getFirstInBackground(locationGetCallback)
    }

    fun saveLocation(newLocation: Location) {
        // Last updated at = current date
        newLocation.setUpdatedAt()
        newLocation.saveInBackground { e ->
            if (e != null) {
                Log.e(TAG, "Error while saving location", e)
            } else {
                Log.i(TAG, "Location save sucessful")
            }
        }
    }

    fun createNewUser(username: String?, password: String?, email: String?, signUpCallback: SignUpCallback?) {
        val user = ParseUser()
        // Set core properties
        user.username = username
        user.setPassword(password)
        user.email = email
        user.put(KEY_NUM_REVIEWS, 0)
        user.put(KEY_LOCATION_HISTORY, JSONArray())
        // Invoke signUpInBackground
        user.signUpInBackground(signUpCallback)
    }

    fun addToUserHistory(placeId: String, saveCallback: SaveCallback?) {
        Log.i(TAG, "Adding this location to user's history")
        val user = ParseUser.getCurrentUser()
        var newPlace = JSONObject()
        newPlace.put(Location.KEY_PLACE_ID, placeId)
        // Save date visited (current date)
        newPlace.put("date", Calendar.getInstance().time)
        var locHistory: JSONArray? = user.getJSONArray(KEY_LOCATION_HISTORY) as JSONArray

        if (locHistory == null) {
            locHistory = JSONArray()
        }
        Log.i(TAG, locHistory.toString())
        user.put(KEY_LOCATION_HISTORY, locHistory.put(newPlace))
        user.saveInBackground(saveCallback)
    }

    fun deleteOldHistory() {
        Log.i(TAG, "Checking old history")
        // Retrieve current date
        val currDate = Calendar.getInstance().time
        val user = ParseUser.getCurrentUser()
        var locHistory = user.getJSONArray(KEY_LOCATION_HISTORY) as JSONArray
        // Read the date visited
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val strDate = (locHistory.get(0) as JSONObject).getJSONObject("date").getString("iso")
        var date = formatter.parse(strDate) as Date
        // Get rid of history greater than TIME_LIMIT days ago
        while (locHistory.length() > 0 && Math.abs(TimeUnit.DAYS.convert(currDate.time  - date.time, TimeUnit.MILLISECONDS)) > TIME_LIMIT) {
            Log.i(TAG, "Location removed from history")
            locHistory.remove(0)
            if (locHistory.length() > 0) {
                val strDate = (locHistory.get(0) as JSONObject).getJSONObject("date").getString("iso")
                date = formatter.parse(strDate) as Date
            }
        }
        user.put(KEY_LOCATION_HISTORY, locHistory)
        user.saveInBackground()
    }

    companion object {
        private const val TAG = "ParseRepository"
        const val KEY_NUM_REVIEWS = "numReviews"
        const val KEY_LOCATION_HISTORY = "locationHistory"
        const val TIME_LIMIT = 0
    }
}