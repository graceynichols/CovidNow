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

    private static MutableLiveData<String> caseCount;
    private static MutableLiveData<List<Article>> allArticles;
    private static List<Article> adapterArticles = new ArrayList<>();
    private static MutableLiveData<JSONObject> jsonLocation;
    private static MutableLiveData<Pair<Double,Double>> coordinates;
    private static NewsRepository newsRepository;
    private static GeocodingRepository geocodingRepository;

    // Recyclerview setup
    private static ArticlesAdapter adapter;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.newsRepository = new NewsRepository();
        this.geocodingRepository = new GeocodingRepository();
    }

    public static ArticlesAdapter createAdapter(Fragment fragment) {
        adapter = new ArticlesAdapter(fragment, adapterArticles);
        return adapter;
    }

    public static void getNews(final Fragment fragment, LifecycleOwner lfOwner) {
        // Retrieve news from API's, put in recyclerview
        // Listen for coordinates from HomeFragment
        final Observer<Pair<Double, Double>> coordsObserver = new Observer<Pair<Double, Double>>() {
            @Override
            public void onChanged(@Nullable final Pair<Double, Double> newCoord) {
                // Location is ready to be passed to news api
                Log.i(TAG, "Location received from HomeFragment");
                // Query location from geocoding API
                GeocodingRepository.queryGeocodeLocation(newCoord.first, newCoord.second, fragment.getContext());
            }
        };
       getCoordinates().observe(lfOwner, coordsObserver);

        // Listen for response from geocoding API to give to news API
        final Observer<JSONObject> locObserver = new Observer<JSONObject>() {
            @Override
            public void onChanged(@Nullable final JSONObject newLocation) {
                // Location is ready to be passed to news api
                Log.i(TAG, "Location received from View Model: " + newLocation.toString());
                // Query news and case count from news API
                NewsRepository.queryNews(fragment.getContext(), newLocation);
            }
        };
        getJsonLocation().observe(lfOwner, locObserver);

        final Observer<List<Article>> newsObserver = new Observer<List<Article>>() {
            @Override
            public void onChanged(@Nullable final List<Article> news) {
                // News is ready to be added to recyclerview
                Log.i(TAG, "News received from View Model");
                getAdapterArticles().addAll(news);
                getAdapter().notifyDataSetChanged();
            }
        };
        // Listen for news to be ready to post on home screen
        getAllArticles().observe(lfOwner, newsObserver);
    }

    public static List<Article> getAdapterArticles() {
        return adapterArticles;
    }

    public static ArticlesAdapter getAdapter() {
        return adapter;
    }

    public static MutableLiveData<String> getCaseCount() {
        if (caseCount == null) {
            caseCount = new MutableLiveData<String>();
        }
        return caseCount;
    }

    public static MutableLiveData<JSONObject> getJsonLocation() {
        if (jsonLocation == null) {
            jsonLocation = new MutableLiveData<JSONObject>();
        }
        return jsonLocation;
    }

    public static MutableLiveData<List<Article>> getAllArticles() {
        if (allArticles == null) {
            allArticles = new MutableLiveData<List<Article>>();
        }
        return allArticles;
    }

    public static MutableLiveData<Pair<Double, Double>> getCoordinates() {
        if (coordinates == null) {
            coordinates = new MutableLiveData<Pair<Double, Double>>();
        }
        return coordinates;
    }
}
