package com.example.covidnow.models

import android.text.format.DateUtils
import org.json.JSONException
import org.json.JSONObject
import org.parceler.Parcel
import java.text.SimpleDateFormat
import java.util.*

@Parcel
class Article() {
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
            article.date = json.getString("publishedDateTime")
            article.source = json.getJSONObject("provider").getString("name")
            article.url = json.getString("webUrl")
            try {
                article.imageUrl = (json.getJSONArray("images")[0] as JSONObject).getString("url")
            } catch (e: Exception) {
                article.imageUrl = null
            }
            return article
        }

        // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
        fun getRelativeTimeAgo(date: Date): String {
            val twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"
            val sf = SimpleDateFormat(twitterFormat, Locale.ENGLISH)
            sf.isLenient = true
            var relativeDate = ""
            val dateMillis = date.time
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString()
            return relativeDate
        }
    }
}