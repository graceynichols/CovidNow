package com.example.covidnow.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
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

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.covidnow.R;
import com.example.covidnow.adapter.PlacesAdapter;
import com.example.covidnow.models.Article;
import com.example.covidnow.models.Location;
import com.example.covidnow.repository.ParseRepository;
import com.example.covidnow.repository.PlacesRepository;
import com.google.android.gms.common.api.internal.LifecycleCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class MapsViewModel extends AndroidViewModel {

    private static String TAG = "MapsViewModel";
    private MutableLiveData<List<Location>> nearbyPlacesList;
    private MutableLiveData<JSONArray> nearbyPlacesJson;
    private PlacesRepository placesRepository;
    private ParseRepository parseRepository;

    public MapsViewModel(@NonNull Application application) {
        super(application);
        this.placesRepository = new PlacesRepository();
        this.parseRepository = new ParseRepository();
    }

    public void getPlaces(final Pair<Double, Double> newCoords, final String search, final Context context, LifecycleOwner lfOwner) {
        // Listen for coordinates from MapsFragment
        Log.i(TAG, "Coordinates received from MapsFragment");
        placesRepository.findAPlace(search, newCoords.first, newCoords.second, context.getString(R.string.google_maps_key), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Log.i(TAG, "Places API Response: " + json.toString());
                    JSONArray array = json.jsonObject.getJSONArray("results");
                    nearbyPlacesJson.postValue(array);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error retrieving places results");
                    Toast.makeText(context, "Error retrieving places results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Error searching places");
                Toast.makeText(context, "Error searching places", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for nearby places JSON from PlacesRepository
        final Observer<JSONArray> placesJSONObserver = new Observer<JSONArray>() {
            @Override
            public void onChanged(@Nullable final JSONArray jArray) {
                // Location is ready to be passed to Places API
                Log.i(TAG, "Places JSON received from PlacesRepo");
                // Search each place in Parse
                try {
                    final List<Location> finalPlaces = new ArrayList<>();
                    for (int i = 0; i < jArray.length(); i++) {
                        final int ii = i;
                        final JSONObject newLocation = (JSONObject) jArray.get(i);
                        final String placeId = newLocation.getString("place_id");
                        parseRepository.searchPlace(placeId, new GetCallback<Location>() {
                            @Override
                            public void done(Location object, ParseException e) {
                                if (object == null) {
                                    // no location saved, must create new one
                                    try {
                                        Log.i(TAG, "This location was NOT previously saved " + placeId);
                                        finalPlaces.add(com.example.covidnow.models.Location.fromJson(newLocation));
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                        Log.i(TAG, "Error parsing location from JSON");
                                    }
                                } else {
                                    // The location was saved in parse
                                    Log.i(TAG, "* This location WAS previously saved " + placeId);
                                    finalPlaces.add(object);
                                }
                                if (ii == jArray.length() - 1) {
                                    // We've reached the end of the list
                                    nearbyPlacesList.postValue(finalPlaces);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Error parsing JSON location");
                }
            }
        };
        getNearbyPlacesJson().observe(lfOwner, placesJSONObserver);
    }

    public LiveData<List<Location>> getNearbyPlacesList() {
        if (nearbyPlacesList == null) {
            nearbyPlacesList = new MutableLiveData<List<Location>>();
        }
        return nearbyPlacesList;
    }

    public LiveData<JSONArray> getNearbyPlacesJson() {
        if (nearbyPlacesJson == null) {
            nearbyPlacesJson = new MutableLiveData<JSONArray>();
        }
        return nearbyPlacesJson;
    }
}
