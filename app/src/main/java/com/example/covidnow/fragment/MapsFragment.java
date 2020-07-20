package com.example.covidnow.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.adapter.PlacesAdapter;
import com.example.covidnow.R;
import com.example.covidnow.repository.PlacesRepository;
import com.example.covidnow.viewmodels.HomeViewModel;
import com.example.covidnow.viewmodels.MapsViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Headers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";
    private MapsFragment fragment = this;
    private EditText etSearch;
    private ImageButton btnSearch;
    private RecyclerView rvPlaces;
    private final static String KEY_LOCATION = "location";
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private static List<com.example.covidnow.models.Location> adapterPlaces = new ArrayList<>();
    private static PlacesAdapter adapter;
    private Pair<Double, Double> coordinates;
    private Location mCurrentLocation;
    private GoogleMap map;
    private MapsViewModel mViewModel;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            if (map != null) {
                // Map is ready
                // Get users current location with permission
                MapsFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(fragment);
                MapsFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(fragment);
            } else {
                Toast.makeText(fragment.getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            Log.i(TAG, "mCurrentLocation not null");
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        // Set view model
        mViewModel = ViewModelProviders.of(this).get(MapsViewModel.class);

        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        rvPlaces = view.findViewById(R.id.rvPlaces);

        // Initialize adapter
        adapterPlaces = new ArrayList<>();
        adapter = new PlacesAdapter(fragment, adapterPlaces);


        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // Listen for searches
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search = etSearch.getText().toString();
                if (search.isEmpty()) {
                    Toast.makeText(getContext(), "Must provide search query", Toast.LENGTH_SHORT).show();
                } else {
                    rvPlaces.setAdapter(adapter);
                    // Locate nearby places
                    if (coordinates == null) {
                        Toast.makeText(getContext(), "Error, current location not found yet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mViewModel.getPlaces(coordinates, search, getString(R.string.google_maps_key), getViewLifecycleOwner());

                    // Listen for nearby places JSON from PlacesRepository
                    final Observer<JSONArray> placesJSONObserver = new Observer<JSONArray>() {
                        @Override
                        public void onChanged(@Nullable final JSONArray jArray) {
                            // Location is ready to be passed to Places API
                            Log.i(TAG, "Places JSON received from PlacesRepo");
                            mViewModel.getSavedPlaces(jArray);
                        }
                    };
                    mViewModel.getNearbyPlacesJson().observe(getViewLifecycleOwner(), placesJSONObserver);

                    // Make recyclerview visible at the bottom
                    rvPlaces.setVisibility(View.VISIBLE);
                    // Initialize recyclerview

                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    rvPlaces.setLayoutManager(layoutManager);

                    // Add lines between recycler view
                    RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                    rvPlaces.addItemDecoration(itemDecoration);

                    // Listen for List<Location> received from ParseRepo
                    final Observer<List<com.example.covidnow.models.Location>> placesListObserver = new Observer<List<com.example.covidnow.models.Location>>() {
                        @Override
                        public void onChanged(@Nullable final List<com.example.covidnow.models.Location> newPlaces) {
                            // List of places ready to be given to recyclerview
                            Log.i(TAG, "Places list received from ParseRepo");
                            adapter.addAll(newPlaces);
                        }
                    };
                    mViewModel.getNearbyPlacesList().observe(getViewLifecycleOwner(), placesListObserver);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        // Access users current location
        Log.i(TAG, "Context: " + fragment.getContext());
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(fragment.getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location2) {
                        if (location2 != null) {
                            Log.i(TAG, "Location: " + location2.toString());
                            // Give coordinates to view model
                            coordinates = Pair.create(location2.getLatitude(), location2.getLongitude());
                            mCurrentLocation = location2;
                            moveCameraToLocation(location2);
                            // Listen for location changes
                            onLocationChanged(location2);
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

    private void moveCameraToLocation(Location loc) {
        double x = loc.getLatitude();
        double y = loc.getLongitude();
        // Set camera to user's current location
        LatLng currLocationLatLng = new LatLng(x, y);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currLocationLatLng, 17);
        map.animateCamera(cameraUpdate);

    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(Objects.requireNonNull(getContext()));
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location2) {
        // GPS may be turned off
        if (location2 == null) {
            return;
        }
        // Report to the UI that the location was updated
        mCurrentLocation = location2;
    }
}