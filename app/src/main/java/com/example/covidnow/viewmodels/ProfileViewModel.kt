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
        val locHistory = user.get("locationHistory") as JSONArray
        // Delete irrelevant/older location history
        val finalHistory = parseRepository.deleteOldHistory(locHistory, user)
        // For each location in history
        for (x in 0 until finalHistory.length()) {
            // Get this location's place ID
            var placeId = (finalHistory.get(x) as JSONObject).getString(Location.KEY_PLACE_ID)
            // Query parse for this location
            parseRepository.searchPlace(placeId, GetCallback { `object`, e ->
                if (`object` == null) {
                    // no location saved, this shouldn't ever happen
                    Log.i(TAG, "Location in history not previously saved")
                } else {
                    // The location was saved in parse
                    notifyAllVisitors(`object`)
                }
            })

        }

    }

    private fun notifyAllVisitors(location: Location?) {

    }
}