package com.example.covidnow.viewmodels;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.adapter.ArticlesAdapter;
import com.example.covidnow.models.Article;
import com.example.covidnow.repository.GeocodingRepository;
import com.example.covidnow.repository.NewsRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";

    private static MutableLiveData<String> caseCount;
    private static MutableLiveData<List<Article>> allArticles;
    private static List<Article> adapterArticles = new ArrayList<>();
    private static MutableLiveData<JSONObject> jsonLocation;
    private static MutableLiveData<Pair<Double,Double>> coordinates;

    // Recyclerview setup
    private static ArticlesAdapter adapter;

    public static void initializeHomeViewModel(final Fragment fragment, LifecycleOwner lfOwner) {
        adapter = new ArticlesAdapter(fragment, adapterArticles);

        // Listen for coordinates from HomeFragment
        final Observer<Pair<Double, Double>> coordsObserver = new Observer<Pair<Double, Double>>() {
            @Override
            public void onChanged(@Nullable final Pair<Double, Double> newCoord) {
                // Location is ready to be passed to news api
                Log.i(TAG, "Location received from View Model");
                // Query location from geocoding API
                GeocodingRepository.queryGeocodeLocation(newCoord.first, newCoord.second, fragment.getContext());
                //NewsRepository.queryNews(fragment.getContext(), newCoord);
            }
        };
        // Listen for location to be ready to give to new API
        HomeViewModel.getCoordinates().observe(lfOwner, coordsObserver);

        final Observer<JSONObject> locObserver = new Observer<JSONObject>() {
            @Override
            public void onChanged(@Nullable final JSONObject newLocation) {
                // Location is ready to be passed to news api
                Log.i(TAG, "Location received from View Model");
                // Query news and case count from news API
                NewsRepository.queryNews(fragment.getContext(), newLocation);
            }
        };
        // Listen for location to be ready to give to new API
        HomeViewModel.getJsonLocation().observe(lfOwner, locObserver);
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
