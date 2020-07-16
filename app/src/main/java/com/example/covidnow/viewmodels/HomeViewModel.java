package com.example.covidnow.viewmodels;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.adapter.ArticlesAdapter;
import com.example.covidnow.models.Article;

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


    // Recyclerview setup
    private static ArticlesAdapter adapter;

    public static void initializeHomeViewModel(Fragment fragment) {
        adapter = new ArticlesAdapter(fragment, adapterArticles);
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




}
