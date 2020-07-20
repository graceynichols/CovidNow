package com.example.covidnow.repository

import android.util.Log
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler

class NewsRepository {
    fun queryCaseCount(iso: String, apiKey: String, jsonHttpResponseHandler: JsonHttpResponseHandler?) {
        val client = AsyncHttpClient()
        val params = RequestParams()
        // Send API Key
        params["Subscription-Key"] = apiKey

        // Build request URL
        val casesUrl: String = CASES_URL + iso
        Log.i(TAG, "Making request with url: $casesUrl")
        client[casesUrl, params, jsonHttpResponseHandler]
    }

    fun queryNews(apiKey: String, iso: String, jsonHttpResponseHandler: JsonHttpResponseHandler?) {
        val client = AsyncHttpClient()
        val params = RequestParams()
        // Send API key
        params["Subscription-Key"] = apiKey

        // Build request URL
        val newsUrl: String = NEWS_URL + iso
        Log.i(TAG, "Making request with url: $newsUrl")
        // Make GET request
        client[newsUrl, params, jsonHttpResponseHandler]
        Log.i(TAG, "Query articles finished")
    }

    companion object {
        private const val TAG = "NewsRepository"
        const val NEWS_URL = "https://api.smartable.ai/coronavirus/news/"
        const val CASES_URL = "https://api.smartable.ai/coronavirus/stats/"
    }
}