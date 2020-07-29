package com.example.covidnow.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.covidnow.helpers.Utils.getLocationUpdatesResult
import com.example.covidnow.helpers.Utils.sendNotification
import com.example.covidnow.helpers.Utils.setLocationUpdatesResult
import com.google.android.gms.location.LocationResult

/**
 * Handles incoming location updates and displays a notification with the location data.
 */
class LocationUpdatesIntentService : IntentService(TAG) {
    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, "Handling intent")
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations = result.locations
                    setLocationUpdatesResult(this, locations)
                    // TODO
                    sendNotification(this, "Handling current location")
                    getLocationUpdatesResult(this)?.let { Log.i(TAG, it) }
                }
            }
        }
    }

    companion object {
        private const val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                ".PROCESS_UPDATES"
        private val TAG = LocationUpdatesIntentService::class.java.simpleName
    }
}