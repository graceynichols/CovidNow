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


    public static void queryCaseCount(final Context context, JSONObject location) throws JSONException {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("Subscription-Key", context.getString(R.string.covid_news_key));
        String casesUrl;
        final Pair<String, String> stateInfo = locationToISO(location);

        casesUrl = CASES_URL + stateInfo.first;
        Log.i(TAG, "Making request with url: " + casesUrl);
        client.get(casesUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "News Response: " + json.toString());
                try {
                    String cases = stateInfo.second + " Case Count: " +
                            json.jsonObject.getJSONObject("stats")
                                    .getString("totalConfirmedCases");
                    HomeViewModel.getCaseCount().setValue(cases);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error retrieving stats for current location");
                    Toast.makeText(context, "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Error retrieving stats for current location");
                Toast.makeText(context, "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void queryNews(final Context context, final JSONObject location) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("Subscription-Key",  context.getString(R.string.covid_news_key));
        String newsUrl;

        try {
            newsUrl = NEWS_URL + locationToISO(location);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Getting ISO code failed");
            Toast.makeText(context, "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "Making request with url: " + newsUrl);
        client.get(newsUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "News Response: " + json.toString());
                try {
                    JSONArray news = json.jsonObject.getJSONArray("news");
                    Log.i(TAG, "News: " + news.toString());
                    addNews(news);
                    queryCaseCount(context, location);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error retrieving news for current location");
                    Toast.makeText(context, "Unable to retrieve news for current location", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Error retrieving news for current location");
                Toast.makeText(context, "Unable to retrieve news for current location", Toast.LENGTH_SHORT).show();
            }
        });
        Log.i(TAG, "Query articles finished");
    }

    private static void addNews(JSONArray news) throws JSONException {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < news.length(); i++) {
            // Add each article as an Article object to articles
            articles.add(Article.fromJson((JSONObject) news.get(i)));
        }
        // Send Articles to the View Model
        HomeViewModel.getAllArticles().setValue(articles);
    }

    public static Pair<String, String> locationToISO(JSONObject location) throws JSONException {
        String iso = "";
        String country = "";
        String region = "";
        String stateName = "";
        JSONArray components = ((JSONObject) location.getJSONArray("results").get(0)).getJSONArray("address_components");
        for (int i = 0; i < components.length(); i++) {
            // Search through components for state/province and country
            JSONObject element = ((JSONObject)components.get(i));
            Log.i(TAG, element.toString());
            if (element.has("types")) {
                if (element.getJSONArray("types").get(0).equals("country")) {
                    Log.i(TAG, element.getString("short_name"));
                    // Add country to ISO
                    country = element.getString("short_name");
                }
                else if (element.getJSONArray("types").get(0).equals("administrative_area_level_1")) {
                    // Add state/province to ISO
                    Log.i(TAG, element.getString("short_name"));
                    region = element.getString("short_name");
                    stateName = element.getString("long_name");
                }
            }
        }

        iso = country + "-" + region;
        Log.i(TAG, "ISO code: " + iso);
        return new Pair(iso, stateName);
    }

}
