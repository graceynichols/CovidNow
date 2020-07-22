package com.example.covidnow.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.covidnow.models.Location
import com.example.covidnow.models.Location.Companion.fromJson
import com.example.covidnow.repository.ParseRepository
import com.example.covidnow.repository.PlacesRepository
import com.parse.GetCallback
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private var nearbyPlacesList: MutableLiveData<List<Location>>? = null
    private var nearbyPlacesJson: MutableLiveData<JSONArray>? = null
    private val placesRepository: PlacesRepository = PlacesRepository()
    private val parseRepository: ParseRepository = ParseRepository()
    fun getPlaces(newCoords: Pair<Double?, Double?>, search: String?, apiKey: String?) {
        // Listen for coordinates from MapsFragment
        Log.i(TAG, "Coordinates received from MapsFragment")
        search?.let {
            newCoords.first?.let { it1 ->
                newCoords.second?.let { it2 ->
                    apiKey?.let { it3 ->
                        // Query places API for nearby places matching this query
                        placesRepository.findAPlace(it, it1, it2, it3, object : JsonHttpResponseHandler() {
                            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                                try {
                                    Log.i(TAG, "Places API Response: $json")
                                    // Retrieve the places array from the API response
                                    val array = json.jsonObject.getJSONArray("results")
                                    nearbyPlacesJson?.postValue(array)
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
        // Search each place in Parse
        try {
            val finalPlaces: MutableList<Location> = ArrayList()
            for (i in 0 until jArray.length()) {
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
                        nearbyPlacesList?.postValue(finalPlaces)
                    }
                })
            }
        } catch (e: Exception) {
            Log.i(TAG, "Error parsing JSON location")
        }
    }

    fun getNearbyPlacesList(): LiveData<List<Location>> {
        if (nearbyPlacesList == null) {
            nearbyPlacesList = MutableLiveData()
        }
        return nearbyPlacesList as MutableLiveData<List<Location>>
    }

    fun getNearbyPlacesJson(): LiveData<JSONArray> {
        if (nearbyPlacesJson == null) {
            nearbyPlacesJson = MutableLiveData()
        }
        return nearbyPlacesJson as MutableLiveData<JSONArray>
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }

}