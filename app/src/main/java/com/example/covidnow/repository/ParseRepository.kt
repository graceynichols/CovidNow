package com.example.covidnow.repository

import android.annotation.SuppressLint
import android.util.Log
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.models.Location
import com.example.covidnow.viewmodels.HomeViewModel
import com.parse.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
        newLocation.visitors = JSONArray()
        newLocation.saveInBackground { e ->
            if (e != null) {
                Log.e(TAG, "Error while saving location", e)
            } else {
                Log.i(TAG, "Location save sucessful")
            }
        }
    }

    fun markAsExposed(objectId: String) {
        // Mark user "objectId" as exposed to COVID
        val query = ParseQuery.getQuery<ParseUser>("_User")
        // Only get user with this objectId
        query.include(KEY_OBJECT_ID)
        query.whereEqualTo(KEY_OBJECT_ID, objectId)
        // Only need 1 place
        query.getFirstInBackground(GetCallback { `object`, e ->
            if (`object` == null) {
                // User not previously saved, this shouldn't happen
                Log.i(TAG, "User not previously saved")
            } else {
                // The location was saved in parse
                Log.i(TAG, "User " +  `object`.username + " found in parse, marking as exposed")
                // Set their "wasExposed" to true
                `object`.put(KEY_WAS_EXPOSED, true)
                `object`.save()
            }
        })
    }

    fun createNewUser(username: String?, password: String?, email: String?, signUpCallback: SignUpCallback?) {
        val user = ParseUser()
        // Set core properties
        user.username = username
        user.setPassword(password)
        user.email = email
        user.put(KEY_NUM_REVIEWS, 0)
        user.put(KEY_WAS_EXPOSED, false)
        user.put(KEY_LOCATION_HISTORY, JSONArray())
        // Invoke signUpInBackground
        user.signUpInBackground(signUpCallback)
    }

    fun addToUserHistory(placeId: String, saveCallback: SaveCallback?): JSONArray {
        // Add this location to user's location history
        Log.i(TAG, "Adding this location to user's history")
        val user = ParseUser.getCurrentUser()
        // Create JSON Object with place ID and date for this "visit"
        var newPlace = JSONObject()
        newPlace.put(Location.KEY_PLACE_ID, placeId)
        // Save date visited (current date)
        newPlace.put("date", Calendar.getInstance().time)
        var locHistory: JSONArray? = user.getJSONArray(KEY_LOCATION_HISTORY) as JSONArray
        // No location history saved yet, need to make a new one
        if (locHistory == null) {
            locHistory = JSONArray()
        }
        Log.i(TAG, "New place: $newPlace")
        // Add this place to user's location history and save
        val finalArray = locHistory.put(newPlace)
        user.put(KEY_LOCATION_HISTORY, finalArray)
        user.saveInBackground(saveCallback)
        return finalArray
    }

    fun addVisitorToHistory(location: Location) {
        Log.i(TAG, "Adding this visitor to location's history")
        val user = ParseUser.getCurrentUser()
        var newVisitor = JSONObject()
        newVisitor.put(KEY_OBJECT_ID, user.objectId)
        // Save date visited (current date)
        newVisitor.put("date", Calendar.getInstance().time)
        // If no location history is saved, make a new array for it
        if (location.visitors == null) {
            location.visitors = JSONArray()
        }
        // Retrieve this location's visitor history
        var locHistory: JSONArray? = location.visitors as JSONArray

        Log.i(TAG, "New visitor: $newVisitor")
        // Add this visitor to visitors list and save
        location.visitors = locHistory?.put(newVisitor)
        Log.i(TAG, "Visitors: $locHistory")
        location.saveInBackground()
        if (locHistory != null && locHistory.length() >= LOC_HISTORY_LIMIT) {
            // There's a ton of visitor history stored for this location, lets delete old stuff
            deleteOldVisitorHistory(locHistory, location)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun deleteOldUserHistory(locHistory: JSONArray, user: ParseUser) : JSONArray {
        // Delete user's location history past TIME_LIMIT days (usually 14)
        Log.i(TAG, "Checking old history")

        // Retrieve current date
        val currDate = Calendar.getInstance().time
        // Read the date visited
        var date = jsonObjectToDate(locHistory.get(0) as JSONObject)
        // Get rid of history greater than TIME_LIMIT days ago
        while (locHistory.length() > 0 && Math.abs(TimeUnit.DAYS.convert(currDate.time  - date.time, TimeUnit.MILLISECONDS)) > TIME_LIMIT) {
            Log.i(TAG, "Location removed from history")
            locHistory.remove(0)
            if (locHistory.length() > 0) {
                // Get the next oldest element in history
                date = jsonObjectToDate(locHistory.get(0) as JSONObject)
            }
        }
        // Set this user's updated location history and save
        user.put(KEY_LOCATION_HISTORY, locHistory)
        user.saveInBackground()
        // Return resulting array
        return locHistory
    }

    @SuppressLint("SimpleDateFormat")
    fun deleteOldVisitorHistory(visitorHistory: JSONArray, location: Location) : JSONArray {
        // Delete visitor history past TIME_LIMIT days ago (usually 14 days)
        Log.i(TAG, "Checking old history")
        // Retrieve current date
        val currDate = Calendar.getInstance().time
        // Read the date visited
        var date = jsonObjectToDate(visitorHistory.get(0) as JSONObject)
        // Get rid of history greater than TIME_LIMIT days ago
        while (visitorHistory.length() > 0 && differenceInDays(currDate, date) > TIME_LIMIT) {
            Log.i(TAG, "Location removed from history")
            visitorHistory.remove(0)
            if (visitorHistory.length() > 0) {
                // Get the next oldest element in history
                date = jsonObjectToDate(visitorHistory.get(0) as JSONObject)
            }
        }
        // Put newly updated list
        location.put(Location.KEY_VISITORS, visitorHistory)
        location.saveInBackground()
        // Return resulting array
        return visitorHistory
    }

    fun jsonObjectToDate(jsonObject: JSONObject): Date {
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val strDate = (jsonObject).getJSONObject("date").getString("iso")
        return formatter.parse(strDate) as Date
    }

    fun differenceInDays(currDate: Date, otherDate: Date): Int {
        return abs(TimeUnit.DAYS.convert(currDate.time - otherDate.time, TimeUnit.MILLISECONDS)).toInt()
    }

    companion object {
        private const val TAG = "ParseRepository"
        const val KEY_NUM_REVIEWS = "numReviews"
        const val KEY_LOCATION_HISTORY = "locationHistory"
        const val KEY_OBJECT_ID = "objectId"
        const val KEY_WAS_EXPOSED = "wasExposed"
        const val TIME_LIMIT = 14
        const val LOC_HISTORY_LIMIT = 50

    }
}