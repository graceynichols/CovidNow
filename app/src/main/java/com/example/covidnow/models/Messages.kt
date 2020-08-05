package com.example.covidnow.models

import com.parse.ParseClassName
import com.parse.ParseObject
import org.json.JSONArray
import org.parceler.Parcel

@Parcel(analyze = [Messages::class])
@ParseClassName("Messages")
class Messages : ParseObject() {
    var history: JSONArray?
        get() = getJSONArray(KEY_HISTORY)
        set(newHistory) {
            if (newHistory != null) {
                put(KEY_HISTORY, newHistory)
            }
        }

    var alert: Boolean?
        get() = getBoolean(KEY_ALERT)
        set(newAlert) {
            if (newAlert != null) {
                put(KEY_ALERT, newAlert)
            }
        }


    companion object {
        private const val TAG = "Messages"
        const val KEY_HISTORY = "history"
        const val KEY_ALERT = "alert"

        @JvmStatic
        fun createMessages(): Messages {
            val messages = Messages()
            messages.history = JSONArray()
            messages.saveInBackground()
            return messages
        }
    }
}