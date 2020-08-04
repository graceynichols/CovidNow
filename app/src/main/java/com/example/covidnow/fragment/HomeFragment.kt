package com.example.covidnow.fragment

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
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
import com.example.covidnow.fragment.alert_dialogs.HotspotAlertDialogFragment
import com.example.covidnow.helpers.Utils
import com.example.covidnow.models.Article
import com.example.covidnow.models.Location
import com.example.covidnow.receivers.LocationUpdatesBroadcastReceiver
import com.example.covidnow.repository.ParseRepository
import com.example.covidnow.viewmodels.HomeViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.parse.ParseUser
import com.parse.SaveCallback
import org.json.JSONObject
import org.parceler.Parcels
import java.util.*


class HomeFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private val chartDescription = "Cases for the Past Week"
    private var rvArticles: RecyclerView? = null
    private var mLocationRequest: LocationRequest? = null
    private val fragment: Fragment = this
    private var tvCases: TextView? = null
    private var btnQuickReview: FloatingActionButton? = null
    private var chart: LineChart? = null
    private var pbLoading: ProgressBar? = null
    private var mViewModel: HomeViewModel? = null
    private val permissionFineLocation= Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionBackgroundLocation= Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private val permissionCoarseLocation= Manifest.permission.ACCESS_COARSE_LOCATION
    // TODO: Times shortened for demo ONLY
    private val UPDATE_INTERVAL: Long = 30000  // Every 30 seconds.
    private val FASTEST_UPDATE_INTERVAL: Long = 10000 // Every 10 seconds
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 2 // Every 2 minutes.

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "OnViewCreated")
        super.onViewCreated(view, savedInstanceState)

        if (validatePermissionsLocation()) {
            // Retrieve user's current location with permission
            Log.i(TAG, "Getting current location")
            getMyLocation()
        } else {
            Toast.makeText(context, "Permission needed to get news for your area", Toast.LENGTH_SHORT).show()
            requestPermissions()
        }
        // Assign view model class
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        rvArticles = view.findViewById(R.id.rvArticles)
        tvCases = view.findViewById(R.id.tvCases)
        pbLoading = view.findViewById(R.id.pbLoading)
        btnQuickReview = view.findViewById(R.id.btnQuickReview)
        chart = view.findViewById(R.id.chart)
        chart?.setNoDataText("Chart loading")

        // Initialize recyclerview
        initializeRvArticles(rvArticles)

        // Show progress bar while loading
        pbLoading?.visibility = View.VISIBLE

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
            //mViewModel?.getCovidNews(newLocation, getString(R.string.covid_news_key));
        }

        // Listen for JSON location to be put
        mViewModel?.getJsonLocation()?.observe(fragment.viewLifecycleOwner, locObserver)

        // Listen for case count from news API
        val caseCountObserver: Observer<String> = Observer<String> { caseCount -> // Case count is ready to be shown
            Log.i(TAG, "Case count received from View Model")
            tvCases?.text = caseCount
        }
        // Listen for case count to be put by NewsRepo
        mViewModel?.getCaseCount()?.observe(viewLifecycleOwner, caseCountObserver)

        // Listen for case history from news API
        val caseHistoryObserver: Observer<List<Pair<String, Int>>> = Observer<List<Pair<String, Int>>> { caseHistory -> // Case count is ready to be shown
            Log.i(TAG, "case history received from View Model")
            // Chart past cases
            setupChart(caseHistory)
        }
        // Listen for case count to be put by NewsRepo
        mViewModel?.getCaseHistory()?.observe(viewLifecycleOwner, caseHistoryObserver)

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
            if (mViewModel?.getFinalLocation()?.value?.isHotspot == true) {
                // Show user alert dialog that this is a hotspot
                showAlertDialog(mViewModel?.getFinalLocation()?.value as Location)
            }
            btnQuickReview?.setOnClickListener(View.OnClickListener {
                Log.i(TAG, "Quick Review button clicked!")
                goQuickReview()
            })
        }
        // Listen for news to be ready to post on home screen
        mViewModel?.getFinalLocation()?.observe(viewLifecycleOwner, finalLocationObserver)
    }

    private fun setupChart(caseHistory: List<Pair<String, Int>>) {
        if (pbLoading?.visibility == View.GONE) {
            pbLoading?.visibility = View.VISIBLE
        }
        val dates = ArrayList<String>()
        val entries = ArrayList<Entry>()
        // Loop through case history
        for (i in caseHistory.indices) {
            // TODO add date instead of index
            caseHistory[i].first?.let { dates.add(it) }
            caseHistory[i].second?.toFloat()?.let { Entry(i.toFloat(), it) }?.let { entries.add(it) }
        }

        // Make dates the X axis label
        val formatter: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String? {
                return dates.get(value.toInt())
            }
        }
        val xAxis: XAxis = chart?.xAxis as XAxis
        xAxis.granularity = 1f // minimum axis-step (interval) is 1
        // Style chart
        chart?.description?.text = chartDescription

        chart?.setDrawGridBackground(false)
        chart?.axisRight?.setDrawLabels(false)

        xAxis.valueFormatter = formatter

        // Assign data to chart
        val dataSet = LineDataSet(entries, "Label")
        dataSet.valueTextSize = 0f
        val lineData = LineData(dataSet)
        chart?.data = lineData
        // Refresh chart
        chart?.invalidate()
        pbLoading?.visibility = View.GONE
    }

    private fun showAlertDialog(location: Location) {
        val alertDialog: HotspotAlertDialogFragment = HotspotAlertDialogFragment.newInstance("Warning", location)
        fragmentManager?.let { alertDialog.show(it, "fragment_alert") };
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
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


    //@NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    @SuppressWarnings("MissingPermission")
    fun getMyLocation() {
        Log.i(TAG, "Getting user's current location ")
        val mFusedLocationClient = activity?.applicationContext?.let { LocationServices.getFusedLocationProviderClient(it) }
        Log.i(TAG, "LOCATION CLIENT $mFusedLocationClient")
        getLastlocation(mFusedLocationClient, getString(R.string.google_maps_key))

        // Only do background updates with permission
        if (validatePermissionsBackground()) {
             Log.i(TAG, "Background permissions granted")
                createLocationRequest()
            // TODO uncomment if you want location updates
            //mFusedLocationClient?.let { requestLocationUpdates(it) }
        }
    }

    //@NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    @SuppressWarnings("MissingPermission")
    fun getLastlocation(locationClient: FusedLocationProviderClient?, apiKey: String) {
        Log.i(TAG, "In getMyLocation")
        locationClient?.lastLocation
                ?.addOnSuccessListener { location ->
                    onLocationChanged(location, apiKey)
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "Exception", e)
                }
    }

    private fun onLocationChanged(location: android.location.Location, apiKey: String) {
        // GPS may be turned off
        mViewModel?.getAddress(apiKey, Pair.create(location.latitude, location.longitude))
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        // Sets the desired interval for active location updates.
        mLocationRequest?.interval = UPDATE_INTERVAL
        // Sets the fastest rate for active location updates.
        mLocationRequest?.fastestInterval = FASTEST_UPDATE_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // Sets the maximum time when batched location updates are delivered.
        mLocationRequest?.maxWaitTime = MAX_WAIT_TIME
    }

    private fun getPendingIntent(): PendingIntent? {
        Log.i(TAG, "Creating pending intent")
        val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun requestLocationUpdates(locationClient: FusedLocationProviderClient) {
        try {
            Log.i(TAG, "Starting location updates")
            Utils.setRequestingLocationUpdates(context, true)
            locationClient.requestLocationUpdates(mLocationRequest, getPendingIntent())
        } catch (e: SecurityException) {
            Utils.setRequestingLocationUpdates(context, false)
            e.printStackTrace()
        }
    }

    private fun requestPermissions(){
        Log.i(TAG, "Requesting permissions")
        val contextProvider = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissionFineLocation) }

        if (contextProvider != null) {
            if(contextProvider){
                Toast.makeText(activity?.applicationContext, "Permission is required to obtain location", Toast.LENGTH_SHORT).show()

            }
            permissionRequest()
        } else {
            Log.i(TAG, "Context provider was null!")
        }

    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == Utils.KEY_LOCATION_UPDATES_RESULT) {
            Log.i(TAG, "Location updates result")
        } else if (s == Utils.KEY_LOCATION_UPDATES_REQUESTED) {
            Log.i(TAG, "Location updates requested");
        }
    }


    private fun permissionRequest(){
        Log.i(TAG, "In permission request")
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permissionFineLocation, permissionCoarseLocation, permissionBackgroundLocation), REQUEST_CODE_LOCATION) }
    }
    private fun validatePermissionsLocation():Boolean{
        val fineLocationAvailable = activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionFineLocation) } == PackageManager.PERMISSION_GRANTED
        val coarseLocationAvailable = activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionCoarseLocation) } ==PackageManager.PERMISSION_GRANTED
        return fineLocationAvailable && coarseLocationAvailable
    }

    private fun validatePermissionsBackground():Boolean{

        return activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionBackgroundLocation) } == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 100
        private const val TAG = "HomeFragment"
        private var adapterArticles: MutableList<Article>? = null
        private var adapter: ArticlesAdapter? = null

    }
}
