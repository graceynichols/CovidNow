package com.example.covidnow.models

import com.parse.ParseClassName
import com.parse.ParseFile
import com.parse.ParseObject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.parceler.Parcel
import java.util.*

@Parcel(analyze = [Location::class])
@ParseClassName("Location")
class Location : ParseObject() {
    var placeId: String?
        get() = getString(KEY_PLACE_ID)
        set(id) {
            if (id != null) {
                put(KEY_PLACE_ID, id)
            }
        }

    var address: String?
        get() = getString(KEY_ADDRESS)
        set(address) {
            if (address != null) {
                put(KEY_ADDRESS, address)
            }
        }

    var isHotspot: Boolean
        get() = getBoolean(KEY_IS_HOTSPOT)
        set(hotspotStatus) {
            put(KEY_IS_HOTSPOT, hotspotStatus)
        }

    var name: String?
        get() = getString(KEY_NAME)
        set(name) {
            if (name != null) {
                put(KEY_NAME, name)
            }
        }


    var image: ParseFile?
        get() = getParseFile(KEY_IMAGE)
        set(picture) {
            if (picture != null) {
                put(KEY_IMAGE, picture)
            }
        }

    var latitude: Double
        get() = getDouble(KEY_LATITUDE)
        set(newLat) {
            put(KEY_LATITUDE, newLat)
        }

    var longitude: Double
        get() = getDouble(KEY_LONGITUDE)
        set(newLng) {
            put(KEY_LONGITUDE, newLng)
        }

    var visitors: JSONArray?
        get() = getJSONArray(KEY_VISITORS)
        set(newVisitors) {
            if (newVisitors != null) {
                put(KEY_VISITORS, newVisitors)
            }
        }

    companion object {
        private const val TAG = "Location"
        const val KEY_ADDRESS = "address"
        const val KEY_IS_HOTSPOT = "isHotspot"
        const val KEY_NAME = "name"
        const val KEY_UPDATED_AT = "updatedAt"
        const val KEY_IMAGE = "image"
        const val KEY_PLACE_ID = "place_id"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_VISITORS = "visitors"

        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Location {
            val location = Location()
            // Find the formatted address
            location.address = json.getString("vicinity")
            // If it's not saved it must not be marked as a hotspot
            location.isHotspot = false
            location.name = json.getString("name")
            location.placeId = json.getString("place_id")
            // Save latitude and longitude
            location.latitude = json.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
            location.longitude = json.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
            return location
        }

        @JvmStatic
        @Throws(JSONException::class)
        fun fromGeocodingJson(json: JSONObject): Location {
            val location = Location()
            // Find the formatted address
            if (json.has("formatted_address")) {
                location.address = json.getString("formatted_address")
            }

            // If it's not saved it must not be marked as a hotspot
            location.isHotspot = false
            // This place presumably doesn't have a name
            location.name = location.address
            location.placeId = json.getString("place_id")
            // Save latitude and longitude
            if (json.has("geometry")) {
                location.latitude = json.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                location.longitude = json.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
            }
            return location
        }
    }
}