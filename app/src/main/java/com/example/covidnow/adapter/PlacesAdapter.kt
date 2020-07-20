package com.example.covidnow.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.covidnow.R
import com.example.covidnow.fragment.LocationDetailsFragment
import com.example.covidnow.models.Location
import org.parceler.Parcels

class PlacesAdapter(private val fragment: Fragment, locations: MutableList<Location>) : RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {
    private val context: Context? = fragment.context
    private val locations: MutableList<Location> = locations

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val ivHotspot: ImageView = itemView.findViewById(R.id.ivHotspot)
        fun bind(location: Location) {
            tvName.text = location.name
            tvAddress.text = location.address

            // Make caution sign visible if hotspot
            if (location.isHotspot) {
                ivHotspot.visibility = View.VISIBLE
            } else {
                ivHotspot.visibility = View.GONE
            }
            itemView.setOnClickListener {
                Log.i(TAG, "Location clicked! Opening details view")
                val result = Bundle()
                result.putParcelable("location", Parcels.wrap(location))
                // Start article details fragment
                val newFrag: Fragment = LocationDetailsFragment()
                newFrag.arguments = result
                fragment.fragmentManager?.beginTransaction()?.replace(R.id.flContainer,
                        newFrag)?.commit()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    // Clean all elements of the recycler
    fun clear() {
        locations.clear()
        notifyDataSetChanged()
    }

    fun addAll(newPlaces: Collection<Location>) {
        locations.addAll(newPlaces)
        notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "PlacesAdapter"
    }

}