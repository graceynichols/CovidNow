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
import android.widget.ProgressBar;
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
import com.example.covidnow.repository.ParseRepository;
import com.example.covidnow.viewmodels.HomeViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.NumberFormat;
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
    private Fragment fragment = this;
    private TextView tvCases;
    private FloatingActionButton btnQuickReview;
    private ProgressBar pbLoading;
    private HomeViewModel mViewModel;
    private static List<Article> adapterArticles;
    private static ArticlesAdapter adapter;

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
        rvArticles = view.findViewById(R.id.rvArticles);
        tvCases = view.findViewById(R.id.tvCases);
        pbLoading = view.findViewById(R.id.pbLoading);
        btnQuickReview = view.findViewById(R.id.btnQuickReview);

        // Adapter setup
        adapterArticles = new ArrayList<>();
        adapter = new ArticlesAdapter(this, adapterArticles);
        rvArticles.setAdapter(adapter);

        // Show progress bar while loading
        pbLoading.setVisibility(View.VISIBLE);

        // Set recyclerview layoutmanager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvArticles.setLayoutManager(layoutManager);

        // Add lines between recycler view
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvArticles.addItemDecoration(itemDecoration);


        // Retrieve user's current location with permission
        Log.i(TAG, "Getting current location");
        HomeFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);

        // Make sure user has a messages object
        if (ParseUser.getCurrentUser().get(ParseRepository.KEY_MESSAGES) == null) {
            mViewModel.giveUserMessages(ParseUser.getCurrentUser());
        }
        // Listen for response from geocoding API to give to news API
        final Observer<JSONObject> locObserver = new Observer<JSONObject>() {
            @Override
            public void onChanged(@Nullable final JSONObject newLocation) {
                // Location ready to be saved to history
                mViewModel.updateHistories(newLocation, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i(TAG, "User saved");
                    }
                });
                // Location is ready to be passed to news api
                mViewModel.getLocationAsLocation(newLocation);
                // TODO Uncomment this when I wanna make news calls
                mViewModel.getCovidNews(newLocation, getString(R.string.covid_news_key));
            }
        };

        // Listen for JSON location to be put
        mViewModel.getJsonLocation().observe(fragment.getViewLifecycleOwner(), locObserver);

        // Listen for case count from news API
        final Observer<String> caseCountObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String caseCount) {
                // Case count is ready to be shown
                Log.i(TAG, "News received from View Model");
                // TODO make number readable
                tvCases.setText(caseCount);
            }
        };
        // Listen for case count to be put by NewsRepo
        mViewModel.getCaseCount().observe(getViewLifecycleOwner(), caseCountObserver);

        // Listen for news to be ready to bind to recyclerview
        final Observer<List<Article>> newsObserver = new Observer<List<Article>>() {
            @Override
            public void onChanged(@Nullable final List<Article> news) {
                // News is ready to be added to recyclerview
                Log.i(TAG, "News received from View Model");
                adapterArticles.addAll(news);
                adapter.notifyDataSetChanged();
                // Hide progress bar
                pbLoading.setVisibility(View.GONE);
            }
        };
        // Listen for news to be ready to post on home screen
        mViewModel.getAllArticles().observe(getViewLifecycleOwner(), newsObserver);

        btnQuickReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Sorry, wait for us to retrieve your location", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for location to be retrieved for quick review
        final Observer<com.example.covidnow.models.Location> finalLocationObserver = new Observer<com.example.covidnow.models.Location>() {
            @Override
            public void onChanged(@Nullable final com.example.covidnow.models.Location currLocation) {
                // News is ready to be added to recyclerview
                Log.i(TAG, "Location received from view model as Location");
                btnQuickReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i(TAG, "Quick Review button clicked!");
                        // Location ready to be used for quick review
                        Fragment newFrag = new ComposeReviewFragment();
                        Bundle result = new Bundle();
                        // Send this location to the compose fragment
                        result.putParcelable("location", Parcels.wrap(mViewModel.getFinalLocation().getValue()));
                        newFrag.setArguments(result);
                        // Start compose review fragment
                        getFragmentManager().beginTransaction().replace(R.id.flContainer,
                                newFrag).addToBackStack("HomeFragment").commit();
                    }
                });
            }
        };
        // Listen for news to be ready to post on home screen
        mViewModel.getFinalLocation().observe(getViewLifecycleOwner(), finalLocationObserver);

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
                            // Retrieve news data from HomeViewModel
                            mViewModel.getAddress(getString(R.string.google_maps_key), Pair.create(location.getLatitude(), location.getLongitude()));
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