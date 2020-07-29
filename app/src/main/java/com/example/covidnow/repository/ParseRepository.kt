package com.example.covidnow.repository

import android.annotation.SuppressLint
import android.util.Log
import com.example.covidnow.models.Location
import com.example.covidnow.models.Messages
import com.parse.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
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
        query.whereEqualTo(Location.KEY_PLACE_ID, placeId)
        // Only need 1 place
        query.getFirstInBackground(locationGetCallback)
    }

    fun saveLocation(newLocation: Location) {
        // Last updated at = current date
        newLocation.visitors = JSONArray()
        newLocation.save()
    }

    fun markAsExposed(objectId: String, location: Location, onDate: Date) {
        // Mark user "objectId" as exposed to COVID
        val query = ParseQuery.getQuery<ParseUser>("_User")
        // Only get user with this objectId
        query.include(KEY_OBJECT_ID)
        query.whereEqualTo(KEY_OBJECT_ID, objectId)
        // Find first user with this objectId
        query.getFirstInBackground { `object`, e ->
            if (`object` == null) {
                // User not previously saved, this shouldn't happen
                Log.i(TAG, "User not previously saved, this shouldn't happen")
            } else {
                // User was found in Parse
                Log.i(TAG, "User " +  `object`.username + " found in parse, marking as exposed")
                // Add to this user's exposure messages
                addMessage(`object`, location, onDate)
            }
        }
    }

    fun createNewUser(username: String?, password: String?, email: String?, signUpCallback: SignUpCallback?) {
        val user = ParseUser()
        // Set core properties
        user.username = username
        user.setPassword(password)
        user.email = email
        // Initialize other properties
        user.put(KEY_NUM_REVIEWS, 0)
        // Messages will hold their exposure history
        user.put(KEY_MESSAGES, Messages.createMessages())
        user.put(KEY_LOCATION_HISTORY, JSONArray())
        // Invoke signUpInBackground
        user.signUpInBackground(signUpCallback)
    }

    fun getMostRecentInUserHistory(): JSONObject? {
        var locHistory = ParseUser.getCurrentUser().getJSONArray(KEY_LOCATION_HISTORY)
        if (locHistory != null) {
            if (locHistory.length() > 0) {
                // Most recent entry is at the end of the list
                return locHistory.get(locHistory.length() - 1) as JSONObject
            }
        }
        return null
    }

    fun addToUserHistory(placeId: String, saveCallback: SaveCallback?): JSONArray {
        // Add this location to user's location history
        Log.i(TAG, "Adding this location to user's history")
        val user = ParseUser.getCurrentUser()
        // Create JSON Object with place ID and date for this "visit"
        var newPlace = JSONObject()
        newPlace.put(Location.KEY_PLACE_ID, placeId)
        // Save date visited (current date)
        newPlace.put("date", Calendar.getInstance().time.toString())
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
        val newVisitor = JSONObject()
        newVisitor.put(KEY_OBJECT_ID, user.objectId)
        // Save date visited (current date)
        newVisitor.put("date", Calendar.getInstance().time.toString())
        Log.i(TAG, "Visitor entry saved: $newVisitor")
        // If no location history is saved, make a new array for it
        var locHistory: JSONArray? = null
        if (location.visitors == null) {
            locHistory = JSONArray()
        } else {
            // Retrieve this location's visitor history
            locHistory = location.visitors as JSONArray
        }

        Log.i(TAG, "New visitor: $newVisitor")
        // Add this visitor to visitors list and save
        location.visitors = locHistory.put(newVisitor)
        Log.i(TAG, "Visitors: $locHistory")
        location.saveInBackground()
        if (locHistory.length() >= LOC_HISTORY_LIMIT) {
            // There's a ton of visitor history stored for this location, lets delete old stuff
            deleteOldVisitorHistory(locHistory, location)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun deleteOldUserHistory(locHistory: JSONArray, user: ParseUser) : JSONArray {
        // Delete user's location history past TIME_LIMIT days (usually 14)
        Log.i(TAG, "Checking user's old history")

        if (locHistory.length() > 0) {
            // Retrieve current date
            val currDate = Calendar.getInstance().time
            // Read the date visited
            var date = jsonObjectToDate(locHistory.get(0) as JSONObject)
            Log.i(TAG, "History element date: $date")
            // Get rid of history greater than TIME_LIMIT days ago
            while (differenceInDays(currDate, date) > TIME_LIMIT) {
                Log.i(TAG, "Location "+ locHistory.get(0).toString() + "removed from user's history")
                locHistory.remove(0)
                if (locHistory.length() > 0) {
                    // Get the next oldest element in history
                    date = jsonObjectToDate(locHistory.get(0) as JSONObject)
                } else {
                    break
                }
            }
            // Set this user's updated location history and save
            user.put(KEY_LOCATION_HISTORY, locHistory)
            user.saveInBackground()
        }

        // Return resulting array
        return locHistory
    }

    @SuppressLint("SimpleDateFormat")
    fun deleteOldVisitorHistory(visitorHistory: JSONArray, location: Location) : JSONArray {
        // Delete visitor history past TIME_LIMIT days ago (usually 14 days)
        Log.i(TAG, "Checking old location's history")
        if (visitorHistory.length() > 0) {
            // Retrieve current date
            val currDate = Calendar.getInstance().time
            // Read the date visited
            var date = jsonObjectToDate(visitorHistory.get(0) as JSONObject)
            // Get rid of history greater than TIME_LIMIT days ago
            while (differenceInDays(currDate, date) > TIME_LIMIT) {
                Log.i(TAG, "Visitor removed from location's history")
                visitorHistory.remove(0)
                if (visitorHistory.length() > 0) {
                    // Get the next oldest element in history
                    date = jsonObjectToDate(visitorHistory.get(0) as JSONObject)
                } else {
                    break
                }
            }
            // Put newly updated list
            location.put(Location.KEY_VISITORS, visitorHistory)
            location.saveInBackground()
        }

        // Return resulting array
        return visitorHistory
    }

    fun differenceInDays(currDate: Date, otherDate: Date): Int {
        return abs(TimeUnit.DAYS.convert(currDate.time - otherDate.time, TimeUnit.MILLISECONDS)).toInt()
    }

    fun differenceInHours(currDate: Date, otherDate: Date): Int {
        return abs(TimeUnit.HOURS.convert(currDate.time - otherDate.time, TimeUnit.MILLISECONDS)).toInt()
    }

    private fun addMessage(user: ParseUser, location: Location, onDate: Date) {
        // Add this location and date as a "message" to their messages
        val messagesObject: Messages = user.getParseObject(KEY_MESSAGES)?.fetchIfNeeded() as Messages
        val messagesHistory: JSONArray = messagesObject.history as JSONArray

        // Check if they've already been notified for this place and day
        if (messagesHistory.length() > 0) {
            // Find the most recent date they got a message
            val mostRecentHistory = messagesHistory.get(0) as JSONObject
            if (mostRecentHistory.getString(Location.KEY_PLACE_ID).equals(location.placeId)) {
                // Find most recent element's date
                val mostRecentDate: Date = jsonObjectToDate((mostRecentHistory))
                if (differenceInDays(onDate, mostRecentDate) == 0) {
                    Log.i(TAG, "User " + user.objectId + "has already been notified about " + location.placeId)
                    // It was the same day, don't notify them
                    return
                }
            }
        }

        val newMessage = JSONObject()
        // Create new message with place ID and date
        newMessage.put(Location.KEY_PLACE_ID, location.placeId)
        newMessage.put(KEY_DATE, onDate.toString())
        // Add to messages object's array
        messagesObject.history = messagesHistory.put(0, newMessage)
        messagesObject.saveInBackground { Log.i(TAG, "Message saved") }
    }

    fun giveUserMessagesObject(currentUser: ParseUser) {
        // Give them an empty messages object
        currentUser.put(KEY_MESSAGES, Messages.createMessages())
    }

    fun getUserMessages(): Messages? {
        return ParseUser.getCurrentUser().getParseObject(KEY_MESSAGES)?.fetchIfNeeded()
    }

    companion object {
        private const val TAG = "ParseRepository"
        const val KEY_NUM_REVIEWS = "numReviews"
        const val KEY_LOCATION_HISTORY = "locationHistory"
        const val KEY_MESSAGES = "exposureHistory"
        const val KEY_DATE = "date"
        const val KEY_OBJECT_ID = "objectId"
        const val KEY_CURRENT_LOCATION = "currentLocation"
        const val TIME_LIMIT = 14
        const val LOC_HISTORY_LIMIT = 50

        @JvmStatic
        fun jsonObjectToDate(jsonObject: JSONObject): Date {
            val formatter: DateFormat = SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy")
            val strDate = (jsonObject).getString("date")
            return formatter.parse(strDate) as Date
        }
    }
}