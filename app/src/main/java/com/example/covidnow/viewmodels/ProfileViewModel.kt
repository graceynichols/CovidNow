package com.example.covidnow.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.covidnow.models.Location
import com.example.covidnow.repository.ParseRepository
import com.parse.GetCallback
import com.parse.ParseUser
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val parseRepository: ParseRepository = ParseRepository()
    private val TAG: String = "ProfileViewModel"

    fun getNumReviews(user: ParseUser): Int {
        val numReviews = user.getNumber(ParseRepository.KEY_NUM_REVIEWS)
        return numReviews?.toInt() ?: 0
    }

    fun logout(): ParseUser {
        // Logout user
        ParseUser.logOut()
        return ParseUser.getCurrentUser()
    }

    fun contactTracing() {
        val user = ParseUser.getCurrentUser()
        val locHistory = user.getJSONArray("locationHistory") as JSONArray
        // Delete irrelevant/older location history for this user
        val finalHistory = parseRepository.deleteOldUserHistory(locHistory, user)
        // For each location in history
        for (x in 0 until finalHistory.length()) {
            // Get this location's place ID
            val placeId = (finalHistory.get(x) as JSONObject).getString(Location.KEY_PLACE_ID)
            val userDate = parseRepository.jsonObjectToDate(finalHistory.get(x) as JSONObject)
            // Query parse for this location
            parseRepository.searchPlace(placeId, GetCallback { `object`, e ->
                if (`object` == null) {
                    // no location saved, this shouldn't ever happen
                    Log.i(TAG, "Location in history not previously saved")
                } else {
                    // The location was saved in parse
                    findAllExposed(`object`, user.objectId, userDate)
                }
            })

        }

    }

    private fun findAllExposed(location: Location, currentUser: String, userDate: Date) {
        Log.i(TAG, "Finding all exposed at: " + location.placeId)
        if (location.visitors != null) {
            // Remove any visitors who came > TIME_LIMIT days ago
            val visitorHistory = parseRepository.deleteOldVisitorHistory(location.visitors as JSONArray, location)
            for (i in 0 until visitorHistory.length()) {
                val objectId: String = (visitorHistory.get(i) as JSONObject).getString(ParseRepository.KEY_OBJECT_ID)
                // Make sure this isn't just the current user
                if (!objectId.equals(currentUser)) {
                    // Find if exposed user and this visitor were there on the same day
                    if (parseRepository.differenceInDays(userDate, (parseRepository.jsonObjectToDate(visitorHistory.get(i) as JSONObject))) == 0) {
                        Log.i(TAG, "User exposed visitor $objectId")
                        parseRepository.markAsExposed(objectId, location, userDate)
                    }
                }

            }
        } else {
            // No visitor history saved
            location.visitors = JSONArray()
        }


    }
}