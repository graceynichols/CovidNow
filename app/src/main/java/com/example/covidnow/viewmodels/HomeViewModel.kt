package com.example.covidnow.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.covidnow.models.Article
import com.example.covidnow.models.Article.Companion.fromJson
import com.example.covidnow.models.Location
import com.example.covidnow.repository.GeocodingRepository
import com.example.covidnow.repository.NewsRepository
import com.example.covidnow.repository.ParseRepository
import com.parse.GetCallback
import com.parse.ParseUser
import com.parse.SaveCallback
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private var caseCount: MutableLiveData<String>? = null
    private var allArticles: MutableLiveData<List<Article>>? = null
    private var jsonLocation: MutableLiveData<JSONObject>? = null
    private var finalLocation: MutableLiveData<Location>? = null
    private val newsRepository: NewsRepository = NewsRepository()
    private val parseRepository: ParseRepository = ParseRepository()
    private val geocodingRepository: GeocodingRepository = GeocodingRepository()
    fun getAddress(apiKey: String?, newCoords: Pair<Double?, Double?>) {
        // Retrieve location JSON from GeocodingRepo
        newCoords.first?.let {
            newCoords.second?.let { it1 ->
                apiKey?.let { it2 ->
                    geocodingRepository.queryGeocodeLocation(it, it1, it2, object : JsonHttpResponseHandler() {
                        // Pass in response handler
                        override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                            Log.i(TAG, "Geocoding API Response $json")
                            jsonLocation?.postValue(json.jsonObject)
                        }

                        override fun onFailure(statusCode: Int, headers: Headers, response: String, throwable: Throwable) {
                            Log.e(TAG, "Query Geocoding API Location failed")
                        }
                    })
                }
            }
        }
    }

    fun getLocationAsLocation(currentLocation: JSONObject) {
        // Now add this user to that location's visitors
        val jsonLocation = currentLocation.getJSONArray("results")[0] as JSONObject
        val placeId = (jsonLocation).getString("place_id")
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

    fun updateHistories(newLocation: JSONObject, saveCallback: SaveCallback) {
        // Get the place ID of this new location
        Log.i(TAG, "New Location: $newLocation")
        val finalLocation = newLocation.getJSONArray("results")[0] as JSONObject
        val placeId = (finalLocation).getString("place_id")
        // Only add if this location has not been saved to user history in the past hour
        var mostRecentElement: JSONObject? = parseRepository.getMostRecentInUserHistory()
        if (mostRecentElement != null) {
            if (mostRecentElement?.getString(Location.KEY_PLACE_ID) == placeId) {
                // This is the same location
                if (parseRepository.differenceInHours(ParseRepository.jsonObjectToDate(mostRecentElement), Calendar.getInstance().time) == RECORDING_TIME_LIMIT) {
                    // Most recent entry was within the hour, don't add to history
                    return

                }
            }
        }

        // Make API call to update user in Parse
        var locHistory = parseRepository.addToUserHistory(placeId, saveCallback)
        val user = ParseUser.getCurrentUser()
        // Only delete older history if there's too much history
        if (locHistory.length() > USER_HISTORY_LIMIT) {
            parseRepository.deleteOldUserHistory(locHistory, user)
        }
        // Now add this user to that location's visitors
        parseRepository.searchPlace(placeId, GetCallback { `object`, _ ->
            var location: Location? = null
            if (`object` == null) {
                // no location saved, must create new one
                try {
                    Log.i(TAG, "This location was NOT previously saved $placeId")
                    // Create this location from the given Json
                    location = Location.fromGeocodingJson(finalLocation)
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                    Log.i(TAG, "Error parsing location from JSON")
                }
            } else {
                // The location was saved in parse
                Log.i(TAG, "* This location WAS previously saved $placeId")
                location = `object`
            }
            // Add this user to current location's visitors list
            location?.let { parseRepository.addVisitorToHistory(it) }
        })

    }

    fun getCovidNews(newLocation: JSONObject, apiKey: String?) {
        val stateInfo: Pair<String?, String?>
        Log.i(TAG, "JSON Location received from View Model: $newLocation")
        stateInfo = try {
            // Turn location into ISO code
            locationToISO(newLocation)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "Unable to convert location to ISO code")
            return
        }

        // Query case count from news repo
        stateInfo.first?.let {
            apiKey?.let { it1 ->
                // Make call to news API
                newsRepository.queryCaseCount(it, it1, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    Log.i(TAG, "News Response: $json")
                    try {
                        // Format number of cases
                        val caseNumber =  NumberFormat.getInstance().format(Integer.parseInt(json.jsonObject.getJSONObject("stats")
                                .getString("totalConfirmedCases")))

                        // Format the state/province + case count string
                        val cases = stateInfo.second.toString() + CASE_COUNT_STR + caseNumber

                        // Post case value to case count
                        caseCount?.postValue(cases)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.i(TAG, "Error retrieving stats for current location")
                    }
                }

                override fun onFailure(statusCode: Int, headers: Headers, response: String, throwable: Throwable) {
                    Log.i(TAG, "Error retrieving stats for current location")
                }
            })
            }
        }

        // Query news from news Repo
        apiKey?.let {
            stateInfo.first?.let { it1 ->
                // Make call to news API
                newsRepository.queryNews(it, it1, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    try {
                        // Retrieve news JSONArray
                        val news = json.jsonObject.getJSONArray("news")
                        Log.i(TAG, "News: $news")
                        addNews(news)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.i(TAG, "Error retrieving news for current location")
                    }
                }

                override fun onFailure(statusCode: Int, headers: Headers, response: String, throwable: Throwable) {
                    Log.i(TAG, "Error retrieving news for current location")
                }
            })
            }
        }
    }



    @Throws(JSONException::class)
    private fun addNews(news: JSONArray) {
        // Format news into ArrayList
        val articles: MutableList<Article> = ArrayList()
        for (i in 0 until news.length()) {
            // Add each article as an Article object to articles
            articles.add(fromJson((news[i] as JSONObject)))
        }
        // Add articles to live data structure
        allArticles?.postValue(articles)
    }

    fun getCaseCount(): LiveData<String> {
        if (caseCount == null) {
            caseCount = MutableLiveData()
        }
        return caseCount as MutableLiveData<String>
    }

    fun getJsonLocation(): LiveData<JSONObject> {
        if (jsonLocation == null) {
            jsonLocation = MutableLiveData()
        }
        return jsonLocation as MutableLiveData<JSONObject>
    }

    fun getFinalLocation(): LiveData<Location> {
        if (finalLocation == null) {
            finalLocation = MutableLiveData()
        }
        return finalLocation as MutableLiveData<Location>
    }

    fun getAllArticles(): LiveData<List<Article>> {
        if (allArticles == null) {
            allArticles = MutableLiveData()
        }
        return allArticles as MutableLiveData<List<Article>>
    }

    @Throws(JSONException::class)
    fun locationToISO(location: JSONObject): Pair<String?, String?> {
        // Convert geocoding response to ISO code (country-state/province)
        val types = "types"
        // The abbreviation for country, state, or province
        val shortName = "short_name"
        var country = ""
        var region = ""
        // State or province name
        var stateName = ""
        // Retrieve this location's formatted address
        Log.i(TAG, "res" + location.toString())
        val components = (location.getJSONArray("results")[0] as JSONObject).getJSONArray("address_components")

        for (i in 0 until components.length()) {
            // Search through components for state/province and country
            val element = components[i] as JSONObject
            Log.i(TAG, element.toString())
            if (element.has(types)) {
                if (element.getJSONArray(types)[0] == "country") {
                    // This is the location's country
                    Log.i(TAG, element.getString(shortName))
                    // Add country to ISO
                    country = element.getString(shortName)
                } else if (element.getJSONArray(types)[0] == "administrative_area_level_1") {
                    // Add state/province to ISO
                    Log.i(TAG, element.getString(shortName))
                    region = element.getString(shortName)
                    stateName = element.getString("long_name")
                }
            }
        }
        // Build ISO code (ex: US-CA)
        val iso = "$country-$region"
        Log.i(TAG, "ISO code: $iso")
        return Pair<String?, String?>(iso, stateName)
    }

    fun giveUserMessages(currentUser: ParseUser) {
        parseRepository.giveUserMessagesObject(currentUser)
    }

    companion object {
        private const val TAG = "HomeViewModel"
        const val USER_HISTORY_LIMIT = 50
        const val RECORDING_TIME_LIMIT = 0
        const val CASE_COUNT_STR = " Case Count: "
    }

}