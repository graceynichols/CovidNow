package com.example.covidnow.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covidnow.models.Location;
import com.example.covidnow.R;
import com.example.covidnow.fragment.LocationDetailsFragment;

import org.parceler.Parcels;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    private static final String TAG = "PlacesAdapter";
    private Context context;
    private Fragment fragment;
    private List<Location> locations;

    public PlacesAdapter(Fragment fragment,List<Location> locations) {
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.locations = locations;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvAddress;
        private ImageView ivHotspot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ivHotspot = itemView.findViewById(R.id.ivHotspot);
        }

        public void bind(final Location location) {
            tvName.setText(location.getName());
            tvAddress.setText(location.getAddress());

            // Make caution sign visible if hotspot
            if (location.isHotspot()) {
                ivHotspot.setVisibility(View.VISIBLE);
            } else {
                ivHotspot.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "Location clicked! Opening details view");
                    Bundle result = new Bundle();
                    result.putParcelable("location", Parcels.wrap(location));
                    // Start article details fragment
                    Fragment newFrag = new LocationDetailsFragment();
                    newFrag.setArguments(result);
                    fragment.getFragmentManager().beginTransaction().replace(R.id.flContainer,
                            newFrag).commit();
                }
            });
            /*
            // On click for article details view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle result = new Bundle();
                    result.putParcelable("article", Parcels.wrap(article));
                    // Start article details fragment
                    Fragment newFrag = new ArticleDetailsFragment();
                    newFrag.setArguments(result);
                    fragment.getFragmentManager().beginTransaction().replace(R.id.flContainer,
                            newFrag).commit();
                }
            });*/
        }

    };

    @NonNull
    @Override
    public PlacesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlacesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesAdapter.ViewHolder holder, int position) {
        Location location = locations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        locations.clear();
        notifyDataSetChanged();
    }
}
