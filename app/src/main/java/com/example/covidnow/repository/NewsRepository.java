package com.example.covidnow.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.util.Pair;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.models.Article;
import com.example.covidnow.viewmodels.HomeViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class NewsRepository {
    private static final String TAG = "NewsRepository";
    public static final String NEWS_URL = "https://api.smartable.ai/coronavirus/news/";
    public static final String CASES_URL = "https://api.smartable.ai/coronavirus/stats/";

    public void queryCaseCount(String iso, String apiKey, JsonHttpResponseHandler jsonHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        // Send API Key
        params.put("Subscription-Key", apiKey);
        String casesUrl;

        // Build request URL
        casesUrl = CASES_URL + iso;
        Log.i(TAG, "Making request with url: " + casesUrl);
        client.get(casesUrl, params, jsonHttpResponseHandler);
    }

    public void queryNews(String apiKey, String iso, JsonHttpResponseHandler jsonHttpResponseHandler) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        // Send API key
        params.put("Subscription-Key", apiKey);
        String newsUrl;

        // Build request URL
        newsUrl = NEWS_URL + iso;
        Log.i(TAG, "Making request with url: " + newsUrl);
        client.get(newsUrl, params, jsonHttpResponseHandler);
        Log.i(TAG, "Query articles finished");
    }
}
