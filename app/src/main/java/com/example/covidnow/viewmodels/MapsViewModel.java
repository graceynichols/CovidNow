package com.example.covidnow.viewmodels;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.covidnow.adapter.PlacesAdapter;
import com.example.covidnow.models.Article;
import com.example.covidnow.models.Location;
import com.example.covidnow.repository.ParseRepository;
import com.example.covidnow.repository.PlacesRepository;
import com.google.android.gms.common.api.internal.LifecycleCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MapsViewModel {

    private static String TAG = "MapsViewModel";
    private static MutableLiveData<Pair<Double, Double>> coordinates;
    private static MutableLiveData<List<Location>> nearbyPlacesList;
    private static MutableLiveData<JSONArray> nearbyPlacesJson;
    private static List<Location> adapterPlaces = new ArrayList<>();
    private static PlacesAdapter adapter;

    public static PlacesAdapter createAdapter(Fragment fragment) {
        adapterPlaces = new ArrayList<>();
        adapter = new PlacesAdapter(fragment, adapterPlaces);
        return adapter;
    }

    public static void getPlaces(final String search, final Context context, LifecycleOwner lfOwner) {
        // Listen for coordinates from MapsFragment
        final Observer<Pair<Double, Double>> coordsObserver = new Observer<Pair<Double, Double>>() {
            @Override
            public void onChanged(@Nullable final Pair<Double, Double> newCoord) {
                // Location is ready to be passed to Places API
                Log.i(TAG, "Location received from MapsFragment");
                // Query nearby places from Places API
                PlacesRepository.findAPlace(search, getCoordinates().getValue().first,  getCoordinates().getValue().second, context);
            }
        };
        getCoordinates().observe(lfOwner, coordsObserver);

        // Listen for nearby places JSON from PlacesRepository
        final Observer<JSONArray> placesJSONObserver = new Observer<JSONArray>() {
            @Override
            public void onChanged(@Nullable final JSONArray jArray) {
                // Location is ready to be passed to Places API
                Log.i(TAG, "Places JSON received from PlacesRepo");
                // Search each place in Parse
                try {
                    ParseRepository.searchPlaces(jArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to search places in parse");
                    Toast.makeText(context, "Unable to retrieve COVID information on places", Toast.LENGTH_SHORT).show();
                }

            }
        };
        getNearbyPlacesJson().observe(lfOwner, placesJSONObserver);

        // Listen for List<Location> received from ParseRepo
        final Observer<List<Location>> placesListObserver = new Observer<List<Location>>() {
            @Override
            public void onChanged(@Nullable final List<Location> newPlaces) {
                // List of places ready to be given to recyclerview
                Log.i(TAG, "Places list received from ParseRepo");
                getAdapter().addAll(newPlaces);
            }
        };
        getNearbyPlacesList().observe(lfOwner, placesListObserver);
    }

    public static PlacesAdapter getAdapter() {
        return adapter;
    }

    public static MutableLiveData<Pair<Double, Double>> getCoordinates() {
        if (coordinates == null) {
            coordinates = new MutableLiveData<Pair<Double, Double>>();
        }
        return coordinates;
    }

    public static MutableLiveData<List<Location>> getNearbyPlacesList() {
        if (nearbyPlacesList == null) {
            nearbyPlacesList = new MutableLiveData<List<Location>>();
        }
        return nearbyPlacesList;
    }

    public static MutableLiveData<JSONArray> getNearbyPlacesJson() {
        if (nearbyPlacesJson == null) {
            nearbyPlacesJson = new MutableLiveData<JSONArray>();
        }
        return nearbyPlacesJson;
    }
}
