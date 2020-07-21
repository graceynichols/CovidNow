package com.example.covidnow.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.covidnow.models.Location;
import com.example.covidnow.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.parceler.Parcels;

public class LocationDetailsFragment extends Fragment {
    private static final String TAG = "LocationDetailsFragment";
    private Location location;
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvHotspotDate;
    private FloatingActionButton btnEdit;
    private ImageView ivHotspot;
    private ImageView ivImage;


    public LocationDetailsFragment() {
        // Required empty public constructor
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_details, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        location = Parcels.unwrap(getArguments().getParcelable("location"));
        tvName = view.findViewById(R.id.tvName);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvHotspotDate = view.findViewById(R.id.tvHotspotDate);
        btnEdit = view.findViewById(R.id.btnEdit);
        ivHotspot = view.findViewById(R.id.ivHotspot);
        ivImage = view.findViewById(R.id.ivImage);

        // Set text information
        if (location.getName() == null) {
            // This location doesn't have a name, put address at the top
            tvName.setText(location.getAddress());
        } else {
            tvName.setText(location.getName());
            tvAddress.setText(location.getAddress());
        }

        if (location.getUpdatedAt() != null) {
            tvHotspotDate.setText(location.getUpdatedAt().toString());
        }
        if (location.isHotspot()) {
            // Make caution sign appear
            ivHotspot.setVisibility(View.VISIBLE);
        }

        if (location.getImage() != null) {
            Log.i(TAG, "Location has picture");
            if (location.getImage().getUrl() == null) {
                // Picture is saved in arguments
                Log.i(TAG, "Picture is saved in arguments");
                if (getArguments().getString(getString(R.string.picture_url)) != null) {
                    Log.i(TAG, "Got picture");
                    Glide.with(getContext()).load(getArguments().getString(getString(R.string.picture_url))).centerCrop().into(ivImage);
                }
            } else {
                Glide.with(getContext()).load(location.getImage().getUrl()).centerCrop().into(ivImage);
            }

        } else {
            Log.i(TAG, "Location has no picture");
        }

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Edit button clicked!");
                Fragment newFrag = new ComposeReviewFragment();
                Bundle result = new Bundle();
                // Send this location to the compose fragment
                result.putParcelable("location", Parcels.wrap(location));
                newFrag.setArguments(result);
                // Start compose review fragment
                getFragmentManager().beginTransaction().replace(R.id.flContainer,
                        newFrag).addToBackStack("LocationDetailsFragment").commit();
            }
        });

    }
}