package com.example.covidnow.repository

import android.util.Log
import com.example.covidnow.models.Location
import com.parse.GetCallback
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SignUpCallback

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
        user.put("numReviews", 0)
        // Invoke signUpInBackground
        user.signUpInBackground(signUpCallback)
    }

    companion object {
        private const val TAG = "ParseRepository"
        const val KEY_NUM_REVIEWS = "numReviews"
    }
}