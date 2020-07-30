package com.example.covidnow.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.covidnow.R
import com.example.covidnow.helpers.Utils
import com.example.covidnow.models.Location.Companion.KEY_PLACE_ID
import com.example.covidnow.models.Location.Companion.fromGeocodingJson
import com.example.covidnow.repository.GeocodingRepository
import com.example.covidnow.repository.ParseRepository
import com.google.android.gms.location.LocationResult
import com.parse.GetCallback
import com.parse.ParseUser
import com.parse.SaveCallback
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "On receive")
        val action = intent.action
        if (ACTION_PROCESS_UPDATES == action) {
            val result = LocationResult.extractResult(intent)
            if (result != null) {
                val locations = result.locations
                // Query this location's google place ID
                findById(locations, context)
            }
        }
    }

    private fun findById(locations: List<Location>, context: Context) {
        val geocodingRepository = GeocodingRepository()
        val parseRepository = ParseRepository()
        geocodingRepository.queryGeocodeLocation(locations[0].latitude, locations[0].longitude, context.getString(R.string.google_maps_key), object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    Log.i(TAG, "Geocoding background response " + json.jsonObject.toString())
                    val newLocation = fromGeocodingJson(((json.jsonObject["results"] as JSONArray)[0] as JSONObject))
                    // If this location has been recently recorded for this user, do nothing
                    val user = ParseUser.getCurrentUser()
                    // Get user's most recent recorded location visit
                    val recentUserHistory = parseRepository.getMostRecentInUserHistory()
                    // Get current date
                    val currentDate = Calendar.getInstance().time
                    // Get date of user's most recent recorded visit
                    val historyDate = recentUserHistory?.let { ParseRepository.jsonObjectToDate(it) };
                    // Check if recent recording is the same place and within the same day
                    if ((recentUserHistory != null) && (recentUserHistory.getString(KEY_PLACE_ID) == newLocation.placeId) && (historyDate?.let { parseRepository.differenceInDays(currentDate, it) } == 0)) {
                        Log.i(TAG, "User has recently been to the same place " + newLocation.placeId)
                        // Don't need to notify
                        Log.i(TAG, currentDate.toString())
                        Log.i(TAG, historyDate.toString())
                        if ((historyDate?.let { parseRepository.differenceInHours(currentDate, it) } != 0)) {
                            // Visit was not within the hour, need to record
                            Log.i(TAG, "Most recent visit to this place was not within the hour")
                            recordHistory(parseRepository, newLocation)
                        }
                    } else {
                        // Not the same place, or same place + not same day, need to record and notify
                        Log.i(TAG, "User's most recent visit is not to the same place and/or same day")
                        checkIfHotspot(newLocation, parseRepository, user, context)
                        recordHistory(parseRepository, newLocation)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(statusCode: Int, headers: Headers, response: String, throwable: Throwable) {
                Log.i(TAG, "Failed to retrieve user's background location place ID")
            }
        })
    }

    private fun recordHistory(parseRepository: ParseRepository, newLocation: com.example.covidnow.models.Location) {
        Log.i(TAG, "Recording history for user " + ParseUser.getCurrentUser().objectId + " and place: " + newLocation.placeId)
        // Add this location to user's history
        newLocation.placeId?.let { parseRepository.addToUserHistory(it, SaveCallback { Log.i(TAG, "Location saved to user history") }) }
        // Add this user to location's history
        newLocation.placeId?.let {
            parseRepository.searchPlace(it, GetCallback { `object`, e ->
                if (`object` != null) {
                    // Add this visitor to previously saved location
                    parseRepository.addVisitorToHistory(`object`)
                } else {
                    // Location not saved in parse, create a new one
                    parseRepository.addVisitorToHistory(newLocation)
                }
        })
        }

    }

    private fun checkIfHotspot(newLocation: com.example.covidnow.models.Location, parseRepository: ParseRepository, user: ParseUser?, context: Context) {
        Log.i(TAG, "Checking if " + newLocation.placeId + " is a hotspot from Parse")
        newLocation.placeId?.let {
            parseRepository.searchPlace(it, GetCallback { `object`, e ->
                Log.i(TAG, "Parse query finished")
                if (`object` != null) {
                    // The place is saved in parse
                    val placeId = `object`.placeId
                    if (`object`.isHotspot) {
                        // Notify user they're in a hotspot
                        user?.let { it1 -> hotspotNotification(it1, placeId, context) }
                    }
                } else {
                    // Not saved, can't be a hotspot
                    Log.i(TAG, "Current location not saved in Parse")
                }
                // TODO check if county has a lot of cases
            })
        }
    }


    private fun hotspotNotification(user: ParseUser, placeId: String?, context: Context) {
        Log.i(TAG, "Current location is a hotspot, user is being notified")
        // Save this as user's current location, prevents repeated notifications
        placeId?.let { user.put(ParseRepository.KEY_CURRENT_LOCATION, it) }
        user.saveInBackground()
        // This place is a hotspot, send notification
        Utils.sendNotification(context, "Your current location is marked as a hotspot")
        Utils.getLocationUpdatesResult(context)?.let { Log.i(TAG, it) }
    }

    companion object {
        private const val TAG = "LUBroadcastReceiver"
        const val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                ".PROCESS_UPDATES"
    }
}