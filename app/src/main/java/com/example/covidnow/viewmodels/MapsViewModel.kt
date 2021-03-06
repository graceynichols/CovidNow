package com.example.covidnow.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.covidnow.R
import com.example.covidnow.models.Location
import com.example.covidnow.models.Location.Companion.fromJson
import com.example.covidnow.repository.GeocodingRepository
import com.example.covidnow.repository.ParseRepository
import com.example.covidnow.repository.PlacesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import com.parse.GetCallback
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import permissions.dispatcher.NeedsPermission
import java.util.*
import kotlin.math.max

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private var nearbyPlacesList: MutableLiveData<List<Location>>? = null
    private var nearbyPlacesJson: MutableLiveData<JSONArray>? = null
    private var finalLocation: MutableLiveData<Location>? = null
    private var coordinates: MutableLiveData<Pair<Double, Double>>? = null
    private val placesRepository: PlacesRepository = PlacesRepository()
    private val parseRepository: ParseRepository = ParseRepository()
    private val geocodingRepository: GeocodingRepository = GeocodingRepository()
    fun getPlaces(newCoords: Pair<Double, Double>, search: String?, apiKey: String?) {
        // Listen for coordinates from MapsFragment
        Log.i(TAG, "getPlaces, Coordinates received from MapsFragment")
        Log.i(TAG, "Search: " + search.toString() + " Coords " + newCoords.toString() + " apikey $apiKey")
        search?.let {
            newCoords.first?.let { it1 ->
                newCoords.second?.let { it2 ->
                    apiKey?.let { it3 ->
                        // Query places API for nearby places matching this query
                        Log.i(TAG, "making FindAPlace call in places repo")
                        placesRepository.findAPlace(it, it1, it2, it3, object : JsonHttpResponseHandler() {
                            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                                try {
                                    Log.i(TAG, "Places API Response: $json")
                                    // Retrieve the places array from the API response
                                    val array = json.jsonObject.getJSONArray("results")
                                            //nearbyPlacesJson?.postValue(array)
                                    Log.i(TAG, "Posting value to nearbyPlaces")
                                    getSavedPlaces(array)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    Log.i(TAG, "Error retrieving places results")
                                }
                            }

                            override fun onFailure(statusCode: Int, headers: Headers, response: String, throwable: Throwable) {
                                Log.i(TAG, "Error searching places")
                            }
                        })
                    }
                }
            }
        }
    }

    fun getSavedPlaces(jArray: JSONArray) {
        Log.i(TAG, "in getSavedPlaces")
        // Search each place in Parse
        try {
            val finalPlaces: MutableList<Location> = ArrayList()
            for (i in 0 until jArray.length()) {
                Log.i(TAG, i.toString())
                val newLocation = jArray[i] as JSONObject
                // Retrieve location's unique placeId
                val placeId = newLocation.getString("place_id")
                // Search parse for a location matching this placeID
                parseRepository.searchPlace(placeId, GetCallback { `object`, _ ->
                    if (`object` == null) {
                        // no location saved, must create new one
                        try {
                            Log.i(TAG, "This location was NOT previously saved $placeId")
                            finalPlaces.add(fromJson(newLocation))
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                            Log.i(TAG, "Error parsing location from JSON")
                        }
                    } else {
                        // The location was saved in parse
                        Log.i(TAG, "* This location WAS previously saved $placeId")
                        finalPlaces.add(`object`)
                    }
                    if (i == jArray.length() - 1) {
                        // We've reached the end of the list
                        Log.i(TAG, "Reached the end of nearby places Json")
                        nearbyPlacesList?.postValue(finalPlaces)
                    }
                })
            }

        } catch (e: Exception) {
            Log.i(TAG, "Error parsing JSON location")
        }
    }

    @SuppressLint("MissingPermission")
    fun getMyLocation(locationClient: FusedLocationProviderClient?, apiKey: String) {
        Log.i(TAG, "In getMyLocation")

        locationClient?.lastLocation
                ?.addOnSuccessListener { location ->
                    onLocationChanged(location, apiKey)
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "Exception", e)
                }
    }

    private fun onLocationChanged(location: android.location.Location, apiKey: String) {
        // GPS may be turned off
        if (location == null) {
            Log.i(TAG, "Error, Location is NULL")
            return
        } else {
            val coords = Pair.create(location.latitude, location.longitude)
            Log.i(TAG, "Coordinates are " + coords.first + " " + coords.second)
            coordinates?.postValue(coords)
            getCurrentLocation(apiKey, coords)
        }
    }

    private fun getCurrentLocation(apiKey: String?, newCoords: Pair<Double?, Double?>) {
        // Retrieve location JSON from GeocodingRepo
        newCoords.first?.let {
            newCoords.second?.let { it1 ->
                apiKey?.let { it2 ->
                    geocodingRepository.queryGeocodeLocation(it, it1, it2, object : JsonHttpResponseHandler() {
                        // Pass in response handler
                        override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                            Log.i(TAG, "Geocoding API Response $json")
                            // Turn json into Location
                            getLocationAsLocation(json.jsonObject)
                        }

                        override fun onFailure(statusCode: Int, headers: Headers, response: String, throwable: Throwable) {
                            Log.e(TAG, "Query Geocoding API Location failed")
                        }
                    })
                }
            }
        }
    }

    private fun getLocationAsLocation(currentLocation: JSONObject) {
        val jsonLocation = currentLocation.getJSONArray("results")[0] as JSONObject
        val placeId = (jsonLocation).getString("place_id")
        // Search if this place has been saved
        parseRepository.searchPlace(placeId, GetCallback { `object`, _ ->
            if (`object` == null) {
                // no location saved, must create new one
                try {
                    Log.i(TAG, "This location was NOT previously saved $placeId")
                    // Create this location from the given Json
                    finalLocation?.postValue(Location.fromGeocodingJson(jsonLocation))
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                    Log.i(TAG, "Error parsing location from JSON")
                }
            } else {
                // The location was saved in parse
                Log.i(TAG, "* This location WAS previously saved $placeId")
                finalLocation?.postValue(`object`)
            }
        })
    }

    fun getNearbyPlacesList(): LiveData<List<Location>> {
        if (nearbyPlacesList == null) {
            nearbyPlacesList = MutableLiveData()
        }
        return nearbyPlacesList as MutableLiveData<List<Location>>
    }

    fun getFinalLocation(): LiveData<Location> {
        if (finalLocation == null) {
            finalLocation = MutableLiveData()
        }
        return finalLocation as MutableLiveData<Location>
    }

    fun getNearbyPlacesJson(): LiveData<JSONArray> {
        if (nearbyPlacesJson == null) {
            nearbyPlacesJson = MutableLiveData()
        }
        return nearbyPlacesJson as MutableLiveData<JSONArray>
    }

    fun getCoordinates(): LiveData<Pair<Double, Double>> {
        if (coordinates == null) {
            coordinates = MutableLiveData()
        }
        return coordinates as MutableLiveData<Pair<Double, Double>>
    }

    fun createMarker(point: LatLng, newLocation: Location): MarkerOptions {

        var color: Float? = null
        // Change color whether hotspot or not
        if (newLocation.isHotspot) {
            // Red marker
            color = BitmapDescriptorFactory.HUE_RED
        } else {
            // Green marker
            color = BitmapDescriptorFactory.HUE_GREEN
        }
        var defaultMarker = BitmapDescriptorFactory.defaultMarker(color)

        return MarkerOptions()
                .position(point)
                .title(newLocation.name)
                .icon(defaultMarker)
    }

    fun dropPinEffect(marker: Marker) {
        // Handler allows us to repeat a code block after a specified delay
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val duration: Long = 1500

        // Use the bounce interpolator
        val interpolator: Interpolator = BounceInterpolator()

        // Animate marker with a bounce updating its position every 15ms
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                // Calculate t for bounce based on elapsed time
                val t = max(
                        1 - interpolator.getInterpolation(elapsed.toFloat()
                                / duration), 0f)
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t)
                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15)
                } else { // done elapsing, show window
                    marker.showInfoWindow()
                }
            }
        })
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }

}