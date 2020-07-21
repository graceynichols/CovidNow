package com.example.covidnow.models

import android.os.Parcelable
import android.text.format.DateUtils
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject
import org.parceler.Parcel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Parcel
@Parcelize
class Article() : Parcelable {
    var headline: String? = null
        private set
    var source: String? = null
        private set
    var date: String? = null
        private set
    var summary: String? = null
        private set
    var url: String? = null
        private set
    var imageUrl: String? = null
        private set

    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Article {
            val article = Article()
            article.headline = json.getString("title")
            article.summary = json.getString("excerpt")
            var stringDate =  json.getString("publishedDateTime")
            article.date = getRelativeTimeAgo(stringDate)
            article.source = json.getJSONObject("provider").getString("name")
            article.url = json.getString("webUrl")
            try {
                article.imageUrl = (json.getJSONArray("images")[0] as JSONObject).getString("url")
            } catch (e: Exception) {
                article.imageUrl = null
            }
            return article
        }

        fun getRelativeTimeAgo(stringDate: String): String {
            // Change date to relative time
            val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            var date = formatter.parse(stringDate) as Date
            // User twitter format date
            val format = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"
            val sf = SimpleDateFormat(format, Locale.ENGLISH)
            sf.isLenient = true
            var relativeDate = ""
            val dateMillis = date.time
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString()
            return relativeDate
        }
    }
}