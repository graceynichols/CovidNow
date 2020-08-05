package com.example.covidnow.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.models.Location
import com.example.covidnow.models.Messages
import com.example.covidnow.repository.ParseRepository
import com.parse.GetCallback
import com.parse.ParseUser
import com.parse.SaveCallback
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val parseRepository: ParseRepository = ParseRepository()

    fun getParseRepo(): ParseRepository {
        return parseRepository
    }

    fun getNumReviews(user: ParseUser): Int {
        // Retrieve user's number of views from Parse
        val numReviews = user.getNumber(ParseRepository.KEY_NUM_REVIEWS)
        return numReviews?.toInt() ?: 0
    }

    fun resetPassword(newPassword: String) {
        //parseRepository.resetPassword(email)
        val user = ParseUser.getCurrentUser()
        user.setPassword(newPassword)
        user.saveInBackground { Log.i(TAG, "User password saved") }
    }

    fun changeUsername(newUsername: String) {
        val user = ParseUser.getCurrentUser()
        user.username = newUsername
        user.saveInBackground { Log.i(TAG, "User username saved") }
    }

    fun logout(): ParseUser? {
        // Logout user
        ParseUser.logOut()
        return ParseUser.getCurrentUser()
    }

    fun contactTracing() {
        // Find people this user might have exposed
        val user = ParseUser.getCurrentUser()
        val locHistory = user.getJSONArray("locationHistory") as JSONArray
        // Delete location history past ParseRepository.TIME_LIMIT days ago (14) for this user
        val finalHistory = parseRepository.deleteOldUserHistory(locHistory, user)
        Log.i(TAG, "Users recent location history: $finalHistory")
        // For each location in history
        for (x in 0 until finalHistory.length()) {
            // Get this location's place ID
            val placeId = (finalHistory.get(x) as JSONObject).getString(Location.KEY_PLACE_ID)
            val userDate = ParseRepository.jsonObjectToDate(finalHistory.get(x) as JSONObject)
            // Query parse for this location
            parseRepository.searchPlace(placeId, GetCallback { `object`, e ->
                if (`object` == null) {
                    // no location saved, this shouldn't ever happen
                    Log.i(TAG, "Location in history not previously saved")
                    Log.i(TAG, "Error message $e")
                } else {
                    // Find who was at that location the same day as the user
                    findAllExposed(`object`, user.objectId, userDate)
                }
            })

        }

    }

    private fun findAllExposed(location: Location, currentUser: String, userDate: Date) {
        // Find who was at that location the same day as the user
        Log.i(TAG, "Finding all exposed at: " + location.placeId)
        if (location.visitors != null) {
            // Remove any visitors who came > TIME_LIMIT days ago
            val visitorHistory = parseRepository.deleteOldVisitorHistory(location.visitors as JSONArray, location)
            for (i in 0 until visitorHistory.length()) {
                // Get the user's object ID
                val objectId: String = (visitorHistory.get(i) as JSONObject).getString(ParseRepository.KEY_OBJECT_ID)
                // Make sure this isn't just the current user
                if (objectId != currentUser) {
                    // Find if exposed user and this visitor were there on the same day
                    if (parseRepository.differenceInDays(userDate, (ParseRepository.jsonObjectToDate(visitorHistory.get(i) as JSONObject)))
                            == DAYS_BETWEEN_VISITS) {
                        Log.i(TAG, "User exposed visitor $objectId")
                        // This person was exposed to our user
                        parseRepository.markAsExposed(objectId, location, userDate)
                    }
                }

            }
        } else {
            // No visitor history saved
            location.visitors = JSONArray()
        }
    }

    private fun jsonToArray(exposureJson: JSONArray): ArrayList<JSONObject> {
        // Convert JSONArray to ArrayList
        val exposureList = ArrayList<JSONObject>()
        for (i in 0 until exposureJson.length()) {
            exposureList.add(exposureJson.get(i) as JSONObject)
        }
        return exposureList
    }

    fun getMessages(): ArrayList<JSONObject>? {
        // Retrieve this users exposure messages
        val userMessages: Messages? = parseRepository.getUserMessages()
        Log.i(TAG, "User messages: " + userMessages.toString())
        return userMessages?.getJSONArray(Messages.KEY_HISTORY)?.let { jsonToArray(it) }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
        const val DAYS_BETWEEN_VISITS = 0
    }
}