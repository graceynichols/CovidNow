package com.example.covidnow.fragment;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.models.Article;
import com.example.covidnow.adapter.ArticlesAdapter;
import com.example.covidnow.R;
import com.example.covidnow.repository.GeocodingRepository;
import com.example.covidnow.repository.NewsRepository;
import com.example.covidnow.viewmodels.HomeViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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
    private static final String TAG = "HomeFragment";

    private RecyclerView rvArticles;
    private TextView tvCases;
    private HomeViewModel mViewModel;

    private final static String KEY_LOCATION = "location";
    private Location mCurrentLocation;
    private JSONObject location;

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

        // Assign view model class
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        //mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        rvArticles = view.findViewById(R.id.rvArticles);
        tvCases = view.findViewById(R.id.tvCases);

        HomeViewModel.initializeHomeViewModel(this, getViewLifecycleOwner());

        //GeocodingRepository.queryGeocodeLocation(37, -22, getContext());
        rvArticles.setAdapter(HomeViewModel.getAdapter());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvArticles.setLayoutManager(layoutManager);

        // Add lines between recycler view
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvArticles.addItemDecoration(itemDecoration);

        // Retrieve user's current location with permission
        Log.i(TAG, "Getting current location");
        HomeFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);




        final Observer<List<Article>> newsObserver = new Observer<List<Article>>() {
            @Override
            public void onChanged(@Nullable final List<Article> news) {
                // News is ready to be added to recyclerview
                Log.i(TAG, "News received from View Model");
                HomeViewModel.getAdapterArticles().addAll(news);
                HomeViewModel.getAdapter().notifyDataSetChanged();
            }
        };
        // Listen for news to be ready to post on home screen
        HomeViewModel.getAllArticles().observe(getViewLifecycleOwner(), newsObserver);

        // Listen for case count from news API
        final Observer<String> caseCountObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String caseCount) {
                // Case count is ready to be added to be shown
                Log.i(TAG, "News received from View Model");
                tvCases.setText(caseCount);
            }
        };
        // Listen for location to be ready to give to new API
        HomeViewModel.getCaseCount().observe(getViewLifecycleOwner(), caseCountObserver);

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
                            Log.i(TAG, "Google Maps Coordinates: " + location.toString());
                            // Give lat and long to view model for geocoding API
                            HomeViewModel.getCoordinates().setValue(Pair.create(location.getLatitude(), location.getLongitude()));
                            GeocodingRepository.queryGeocodeLocation(location.getLatitude(), location.getLongitude(), getContext());
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

}