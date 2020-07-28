package com.example.covidnow.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.covidnow.R
import com.example.covidnow.adapter.ArticlesAdapter
import com.example.covidnow.models.Article
import com.example.covidnow.repository.ParseRepository
import com.example.covidnow.viewmodels.HomeViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.parse.ParseUser
import com.parse.SaveCallback
import org.json.JSONObject
import org.parceler.Parcels
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.*

class HomeFragment : Fragment() {
    private var rvArticles: RecyclerView? = null
    private val fragment: Fragment = this
    private var tvCases: TextView? = null
    private var btnQuickReview: FloatingActionButton? = null
    private var pbLoading: ProgressBar? = null
    private var mViewModel: HomeViewModel? = null
    private val permissionFineLocation= Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionCoarseLocation= Manifest.permission.ACCESS_COARSE_LOCATION
    private val REQUEST_CODE_LOCATION = 100

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, parent, false)
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assign view model class
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        rvArticles = view.findViewById(R.id.rvArticles)
        tvCases = view.findViewById(R.id.tvCases)
        pbLoading = view.findViewById(R.id.pbLoading)
        btnQuickReview = view.findViewById(R.id.btnQuickReview)

        // Initialize recyclerview
        initializeRvArticles(rvArticles)

        // Show progress bar while loading
        pbLoading?.visibility = View.VISIBLE

        // Retrieve user's current location with permission
        Log.i(TAG, "Getting current location")

        // Get user's location permission
        if (validatePermissionsLocation()){
            // Permission granted
            Log.i(TAG, "permission")
            getMyLocation()
        }
        else{
            requestPermissions()
        }

        // Make sure user has a messages object
        if (ParseUser.getCurrentUser()[ParseRepository.KEY_MESSAGES] == null) {
            mViewModel?.giveUserMessages(ParseUser.getCurrentUser())
        }
        // Listen for response from geocoding API to give to news API
        val locObserver: Observer<JSONObject> = Observer<JSONObject> { newLocation ->
            // Location ready to be saved to history
            mViewModel?.updateHistories(newLocation, SaveCallback { Log.i(TAG, "User saved") })
            // Location is ready to be passed to news api
            mViewModel?.getLocationAsLocation(newLocation)
            // TODO Uncomment this when I wanna make news calls
            mViewModel?.getCovidNews(newLocation, getString(R.string.covid_news_key));
        }

        // Listen for JSON location to be put
        mViewModel?.getJsonLocation()?.observe(fragment.viewLifecycleOwner, locObserver)

        // Listen for case count from news API
        val caseCountObserver: Observer<String> = Observer<String> { caseCount -> // Case count is ready to be shown
            Log.i(TAG, "News received from View Model")
            tvCases?.text = caseCount
        }
        // Listen for case count to be put by NewsRepo
        mViewModel?.getCaseCount()?.observe(viewLifecycleOwner, caseCountObserver)

        // Listen for news to be ready to bind to recyclerview
        val newsObserver: Observer<List<Article>> = Observer { news -> // News is ready to be added to recyclerview
            Log.i(TAG, "News received from View Model")
            adapter?.addAll(news)
            // Hide progress bar
            pbLoading?.visibility = View.GONE
        }
        // Listen for news to be ready to post on home screen
        mViewModel?.getAllArticles()?.observe(viewLifecycleOwner, newsObserver)
        btnQuickReview?.setOnClickListener { Toast.makeText(context, "Sorry, wait for us to retrieve your location", Toast.LENGTH_SHORT).show() }

        // Listen for location to be retrieved for quick review
        val finalLocationObserver: Observer<com.example.covidnow.models.Location> = Observer<com.example.covidnow.models.Location> {
            Log.i(TAG, "Location received from view model as Location")
            btnQuickReview?.setOnClickListener(View.OnClickListener {
                Log.i(TAG, "Quick Review button clicked!")
                goQuickReview()
            })
        }
        // Listen for news to be ready to post on home screen
        mViewModel?.getFinalLocation()?.observe(viewLifecycleOwner, finalLocationObserver)
    }

    private fun goQuickReview() {
        // Start compose review fragment
        val newFrag: Fragment = ComposeReviewFragment()
        val result = Bundle()
        // Send this location to the compose fragment
        result.putParcelable("location", Parcels.wrap(mViewModel?.getFinalLocation()?.value))
        newFrag.arguments = result
        // Start compose review fragment
        fragmentManager?.beginTransaction()?.replace(R.id.flContainer,
                newFrag)?.addToBackStack("HomeFragment")?.commit()
    }

    private fun initializeRvArticles(rvArticles: RecyclerView?) {
        // Adapter setup
        adapterArticles = ArrayList()
        adapter = ArticlesAdapter(this, adapterArticles as ArrayList<Article>)
        rvArticles?.adapter = adapter

        // Set recyclerview layoutmanager
        val layoutManager = LinearLayoutManager(context)
        rvArticles?.layoutManager = layoutManager

        // Add lines between recycler view
        val itemDecoration: ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvArticles?.addItemDecoration(itemDecoration)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun getMyLocation() {
        Log.i(TAG, "Getting user's current location ")
        val locationClient = activity?.applicationContext?.let { LocationServices.getFusedLocationProviderClient(it) }
        Log.i(TAG, "LOCATION CLIENT $locationClient")
        mViewModel?.getMyLocation(locationClient, getString(R.string.google_maps_key))

    }

    private fun requestPermissions(){
        val contextProvider: Boolean? = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissionFineLocation) }

        if (contextProvider != null) {
            if(contextProvider){
                Toast.makeText(activity?.applicationContext, "Permission is required to obtain location", Toast.LENGTH_SHORT).show()
            }
            permissionRequest()
        }

    }
    private fun permissionRequest(){
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permissionFineLocation, permissionCoarseLocation), REQUEST_CODE_LOCATION) }
    }
    private fun validatePermissionsLocation():Boolean{
        val fineLocationAvailable= activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionFineLocation) } == PackageManager.PERMISSION_GRANTED
        val coarseLocationAvailable= activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionCoarseLocation) } ==PackageManager.PERMISSION_GRANTED

        return fineLocationAvailable && coarseLocationAvailable
    }

    companion object {
        private const val TAG = "HomeFragment"
        private var adapterArticles: MutableList<Article>? = null
        private var adapter: ArticlesAdapter? = null
    }
}