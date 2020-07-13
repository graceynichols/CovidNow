package com.example.covidnow.fragment;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.Article;
import com.example.covidnow.ArticlesAdapter;
import com.example.covidnow.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class HomeFragment extends Fragment {
    private static final String TAG = "PostsFragment";
    private static final String NEWS_URL = "https://api.smartable.ai/coronavirus/news/";
    private static final String CASES_URL = "https://api.smartable.ai/coronavirus/stats/";
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private static final int POST_LIMIT = 20;
    private RecyclerView rvArticles;
    private TextView tvCases;
    protected ArticlesAdapter adapter;
    protected List<Article> allArticles;
    Location mCurrentLocation;
    public static JSONObject location;
    private static String stateName;
    private final static String KEY_LOCATION = "location";

    public HomeFragment() {
        // Required empty public constructor
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvArticles = view.findViewById(R.id.rvArticles);
        tvCases = view.findViewById(R.id.tvCases);

        // Recyclerview setup
        allArticles = new ArrayList<>();
        adapter = new ArticlesAdapter(this, allArticles);
        rvArticles.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvArticles.setLayoutManager(layoutManager);

        // Add lines between recycler view
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvArticles.addItemDecoration(itemDecoration);

        // Retrieve user's current location with permission
        Log.i(TAG, "Getting current location");
        HomeFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HomeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMyLocation() {
        // Access users current location
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.i(TAG, "Location: " + location.toString());
                            getLocationFromCoords(location.getLatitude(), location.getLongitude());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }


    private void getLocationFromCoords(double x, double y) {
        AsyncHttpClient client = new AsyncHttpClient();

        String geoUrl = GEOCODE_URL + x + "," + y + "&key=" + getString(R.string.google_maps_key);
        Log.i(TAG, geoUrl);
        client.get(geoUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, getString(R.string.google_maps_key));
                Log.i(TAG, "Response" + " " + json.toString());
                location = json.jsonObject;
                queryNews();

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        });
    }

    private void setCaseCount() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("Subscription-Key", "1fddf7e6b3b1498baa17236a3209d659");
        String casesUrl;

        try {
            casesUrl = CASES_URL + locationToISO(location);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Getting ISO code failed");
            Toast.makeText(getContext(), "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "Making request with url: " + casesUrl);
        client.get(casesUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "News Response: " + json.toString());
                try {
                    String cases = stateName + " Case Count: " +
                            json.jsonObject.getJSONObject("stats")
                                    .getString("totalConfirmedCases");
                    tvCases.setText(cases);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error retrieving stats for current location");
                    Toast.makeText(getContext(), "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Error retrieving stats for current location");
                Toast.makeText(getContext(), "Unable to retrieve stats for current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void queryNews() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("Subscription-Key", "1fddf7e6b3b1498baa17236a3209d659");
        String newsUrl;

        try {
            newsUrl = NEWS_URL + locationToISO(location);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Getting ISO code failed");
            Toast.makeText(getContext(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "Making request with url: " + newsUrl);
        client.get(newsUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "News Response: " + json.toString());
                try {
                    JSONArray news = json.jsonObject.getJSONArray("news");
                    addNews(news);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error retrieving news for current location");
                    Toast.makeText(getContext(), "Unable to retrieve news for current location", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Error retrieving news for current location");
                Toast.makeText(getContext(), "Unable to retrieve news for current location", Toast.LENGTH_SHORT).show();
            }
        });
        Log.i(TAG, "Query articles finished");

    }

    private void addNews(JSONArray news) throws JSONException {
        for (int i = 0; i < news.length(); i++) {
            // Add each article as an Article object to articles
            allArticles.add(Article.fromJson((JSONObject) news.get(i)));
        }
        // Update covid case count
        setCaseCount();
    }

    public static String locationToISO(JSONObject location) throws JSONException {
        String iso = "";
        String country = "";
        String region = "";
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
        return iso;
    }

}