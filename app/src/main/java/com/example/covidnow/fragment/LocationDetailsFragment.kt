package com.example.covidnow.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.covidnow.R
import com.example.covidnow.adapter.HistoryAdapter
import com.example.covidnow.models.Location
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.parceler.Parcels
import java.util.*

class LocationDetailsFragment : Fragment() {
    private var location: Location? = null
    private var tvName: TextView? = null
    private var tvAddress: TextView? = null
    private var tvHotspotDate: TextView? = null
    private var btnEdit: FloatingActionButton? = null
    private var ivHotspot: ImageView? = null
    private var ivImage: ImageView? = null

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location_details, parent, false)
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        location = Parcels.unwrap<Location>(arguments?.getParcelable("location"))
        tvName = view.findViewById(R.id.tvName)
        tvAddress = view.findViewById(R.id.tvAddress)
        tvHotspotDate = view.findViewById(R.id.tvHotspotDate)
        btnEdit = view.findViewById(R.id.btnEdit)
        ivHotspot = view.findViewById(R.id.ivHotspot)
        ivImage = view.findViewById(R.id.ivImage)

        // Set text information
        if (location?.name == null) {
            // This location doesn't have a name, put address at the top
            tvName?.text = location?.address
        } else {
            tvName?.text = location?.name
            tvAddress?.text = location?.address
        }

        if (location?.updatedAt != null) {
            tvHotspotDate?.text = HistoryAdapter.getRelativeTimeAgo(location?.updatedAt as Date)

        }
        if (location?.isHotspot == true) {
            // Make caution sign appear if a hotspot
            ivHotspot?.visibility = View.VISIBLE
        }
        // Check if there's a photo file to display
        if (location?.image != null) {
            Log.i(TAG, "Location has picture ")
            context?.let { Glide.with(it).load(location?.image?.url).centerCrop().into(ivImage as ImageView) }
        } else {
            Log.i(TAG, "Location has no picture")
        }
        // Listen for the compose review button
        btnEdit?.setOnClickListener(View.OnClickListener {
            Log.i(TAG, "Edit button clicked!")
            val newFrag: Fragment = ComposeReviewFragment()
            val result = Bundle()
            // Send this location to the compose fragment
            result.putParcelable("location", Parcels.wrap(location))
            newFrag.arguments = result
            // Start compose review fragment
            fragmentManager?.beginTransaction()?.replace(R.id.flContainer,
                    newFrag)?.addToBackStack("LocationDetailsFragment")?.commit()
        })
    }

    companion object {
        private const val TAG = "LocationDetailsFragment"
    }
}