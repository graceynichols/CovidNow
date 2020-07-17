package com.example.covidnow.viewmodels;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.adapter.ArticlesAdapter;
import com.example.covidnow.fragment.HomeFragment;
import com.example.covidnow.models.Article;
import com.example.covidnow.repository.GeocodingRepository;
import com.example.covidnow.repository.NewsRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = "HomeViewModel";

    private MutableLiveData<String> caseCount;
    private MutableLiveData<List<Article>> allArticles;
    private MutableLiveData<JSONObject> jsonLocation;
    private NewsRepository newsRepository;
    private GeocodingRepository geocodingRepository;

    // Recyclerview setup

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.newsRepository = new NewsRepository();
        this.geocodingRepository = new GeocodingRepository();
    }

    public void getNews(final Fragment fragment, Pair<Double, Double> newCoords) {
        // Retrieve location JSON from GeocodingRepo
        geocodingRepository.queryGeocodeLocation(newCoords.first, newCoords.second, fragment.getContext(), new JsonHttpResponseHandler() {
            // Pass in response handler
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "Geocoding API Response" + " " + json.toString());
                jsonLocation.postValue(json.jsonObject);
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Query Geocoding API Location failed");
            }
        });

        // Listen for response from geocoding API to give to news API
        final Observer<JSONObject> locObserver = new Observer<JSONObject>() {
            @Override
            public void onChanged(@Nullable final JSONObject newLocation) {
                // Location is ready to be passed to news api
                final Pair<String, String> stateInfo;
                Log.i(TAG, "JSON Location received from View Model: " + newLocation.toString());
                try {
                    // Turn location into ISO code
                    stateInfo = locationToISO(newLocation);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to convert location to ISO code");
                    return;
                }

                // Query case count from news repo
                newsRepository.queryCaseCount(stateInfo.first, fragment.getContext().getString(R.string.covid_news_key), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "News Response: " + json.toString());
                        try {

                            String cases = stateInfo.second + " Case Count: " +
                                    json.jsonObject.getJSONObject("stats")
                                            .getString("totalConfirmedCases");
                            // Post case value to case count
                            caseCount.postValue(cases);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG, "Error retrieving stats for current location");
                            Toast.makeText(fragment.getContext(), "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "Error retrieving stats for current location");
                        Toast.makeText(fragment.getContext(), "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
                    }
                });

                // Query news from news Repo
                newsRepository.queryNews(fragment.getContext().getString(R.string.covid_news_key), stateInfo.first, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            JSONArray news = json.jsonObject.getJSONArray("news");
                            Log.i(TAG, "News: " + news.toString());
                            addNews(news);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG, "Error retrieving news for current location");
                            Toast.makeText(fragment.getContext(), "Unable to retrieve news for current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "Error retrieving news for current location");
                        Toast.makeText(fragment.getContext(), "Unable to retrieve news for current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        // Listen for JSON location to be put
        getJsonLocation().observe(fragment.getViewLifecycleOwner(), locObserver);
    }

    private void addNews(JSONArray news) throws JSONException {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < news.length(); i++) {
            // Add each article as an Article object to articles
            articles.add(Article.fromJson((JSONObject) news.get(i)));
        }
        // Add articles to live data structure
        allArticles.postValue(articles);
    }


    public LiveData<String> getCaseCount() {
        if (caseCount == null) {
            caseCount = new MutableLiveData<String>();
        }
        return caseCount;
    }

    public LiveData<JSONObject> getJsonLocation() {
        if (jsonLocation == null) {
            jsonLocation = new MutableLiveData<JSONObject>();
        }
        return jsonLocation;
    }

    public LiveData<List<Article>> getAllArticles() {
        if (allArticles == null) {
            allArticles = new MutableLiveData<List<Article>>();
        }
        return allArticles;
    }

    public Pair<String, String> locationToISO(JSONObject location) throws JSONException {
        String types = "types";
        String shortName = "short_name";
        String iso = "";
        String country = "";
        String region = "";
        String stateName = "";
        JSONArray components = ((JSONObject) location.getJSONArray("results").get(0)).getJSONArray("address_components");
        for (int i = 0; i < components.length(); i++) {
            // Search through components for state/province and country
            JSONObject element = ((JSONObject)components.get(i));
            Log.i(TAG, element.toString());
            if (element.has(types)) {
                if (element.getJSONArray(types).get(0).equals("country")) {
                    Log.i(TAG, element.getString(shortName));
                    // Add country to ISO
                    country = element.getString(shortName);
                }
                else if (element.getJSONArray(types).get(0).equals("administrative_area_level_1")) {
                    // Add state/province to ISO
                    Log.i(TAG, element.getString(shortName));
                    region = element.getString(shortName);
                    stateName = element.getString("long_name");
                }
            }
        }

        iso = country + "-" + region;
        Log.i(TAG, "ISO code: " + iso);
        return new Pair(iso, stateName);
    }

}
