package com.example.covidnow.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.covidnow.R
import com.example.covidnow.activity.MainActivity
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
    private var btnEdit: ImageView? = null
    private var pbLoading: ProgressBar? = null
    private var ivHotspot: ImageView? = null
    private var ivImage: ImageView? = null
    private var btnBack: ImageView? = null
    private var toolbar: Toolbar? = null


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
        ivHotspot = view.findViewById(R.id.ivHotspot)
        ivImage = view.findViewById(R.id.ivImage)
        toolbar = view.findViewById(R.id.my_toolbar)
        pbLoading = view.findViewById(R.id.pbLoading)

        // Show progress bar
        pbLoading?.visibility = View.VISIBLE

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

        // Set up custom toolbar
        setupToolbar(location)



        // Hide progress bar
        pbLoading?.visibility = View.GONE
    }

    private fun setupToolbar(location: Location?) {
        btnEdit = view?.findViewById(R.id.btnEdit)

        // hide vanity action bar
        (activity as MainActivity).hideActionBar()
        //toolbar?.inflateMenu(R.menu.article_details_toolbar)
        toolbar?.let { (activity as MainActivity).setActionBar(it) }
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar?.setNavigationOnClickListener {
            Log.i(TAG, "Going back to maps")
            fragmentManager?.popBackStackImmediate()
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