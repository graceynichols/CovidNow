package com.example.covidnow.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.covidnow.R
import com.example.covidnow.adapter.PlacesAdapter
import com.example.covidnow.helpers.RecyclerViewSwipeListener
import com.example.covidnow.viewmodels.MapsViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.parceler.Parcels
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.*

@RuntimePermissions
class MapsFragment : Fragment() {
    private val fragment = this
    private var etSearch: EditText? = null
    private var btnSearch: ImageButton? = null
    private var btnQuickReview: FloatingActionButton? = null
    private var card: CardView? = null
    private var rvPlaces: RecyclerView? = null
    private var ivArrow: ImageView? = null
    private var coordinates: Pair<Double, Double>? = null
    private var pbLoading: ProgressBar? = null
    private var mCurrentLocation: Location? = null
    private var map: GoogleMap? = null
    private var mViewModel: MapsViewModel? = null
    private val permissionFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
    private val REQUEST_CODE_LOCATION = 100
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         */
        map = googleMap
        if (map != null) {
            // Map is ready
            // Get users current location with permission
            if (validatePermissionsLocation()){
                Log.i(TAG, "permission")
                getMyLocation()
            }
            else{
                requestPermissions()
            }
        } else {
            Toast.makeText(fragment.context, "Error - Map was null!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pbLoading = view.findViewById(R.id.pbLoading)
        etSearch = view.findViewById(R.id.etSearch)
        btnSearch = view.findViewById(R.id.btnSearch)
        rvPlaces = view.findViewById(R.id.rvPlaces)
        card = view.findViewById(R.id.card)
        btnQuickReview = view.findViewById(R.id.btnQuickReview)
        ivArrow = view.findViewById(R.id.ivArrow)

        var flag = true

        if (validatePermissionsLocation()){
            Log.i(TAG, "Permission granted")
            getMyLocation()
        }
        else{
            Log.i(TAG, "Permission not granted")
            //requestPermissions()
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            Log.i(TAG, "mCurrentLocation not null")
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION)
        }
        // Set view model
        mViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)

        // Setup recyclerview of places
        initializeRvPlaces(rvPlaces)

        // Setup map view
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // Listen for location to be retrieved for quick review
        val coordinatesObserver: Observer<Pair<Double, Double>> = Observer { it ->
            Log.i(TAG, "Coordinates received from view model")
            // Move camera to current location
            coordinates = it
            it.first?.let { it1 -> it.second?.let { it2 -> moveCameraToCoordinates(it1, it2) } }
        }
        mViewModel?.getCoordinates()?.observe(viewLifecycleOwner, coordinatesObserver)

        // Listen for searches
        btnSearch?.setOnClickListener(View.OnClickListener {
            // Show progress bar
            if (coordinates == null) {
                Toast.makeText(context, "Error, current location not found yet", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            // Get user's query
            val search = etSearch?.text.toString()
            // Clear search box
            etSearch?.setText("")
            if (search.isEmpty()) {
                Toast.makeText(context, "Must provide search query", Toast.LENGTH_SHORT).show()
            } else {
                // Automatically put keyboard away
                val mgr = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                mgr.hideSoftInputFromWindow(etSearch?.windowToken, 0)

                // Show progress bar
                pbLoading?.visibility = View.VISIBLE

                // Retrieve nearby places
                if (coordinates != null) {
                    mViewModel?.getPlaces(coordinates as Pair<Double, Double>, search, getString(R.string.google_maps_key))
                }

                // Listen for nearby places JSON from PlacesRepository
                val placesJSONObserver: Observer<JSONArray> = Observer<JSONArray> { jArray -> // Places JSON received from view model
                    Log.i(TAG, "Places JSON received from PlacesRepo")
                    mViewModel?.getSavedPlaces(jArray as JSONArray)
                }
                // Listen for Places API call to finish
                mViewModel?.getNearbyPlacesJson()?.observe(viewLifecycleOwner, placesJSONObserver)
                val placesListObserver: Observer<List<com.example.covidnow.models.Location>> = Observer<List<com.example.covidnow.models.Location>> { newPlaces -> // List of places ready to be given to recyclerview
                    Log.i(TAG, "Places list received from ParseRepo")
                    adapter?.addAll(newPlaces as MutableList<com.example.covidnow.models.Location>)
                    // Hide progress bar
                    pbLoading?.visibility = View.GONE
                    // Make places list slide up
                    if (flag) {
                        showPlaces()
                    }
                    flag = false

                }
                // Listen for List<covidnow.location> of saved places received from ParseRepo
                mViewModel?.getNearbyPlacesList()?.observe(viewLifecycleOwner, placesListObserver)
            }
        })

        // Listen for arrow button to show rvPlaces
        ivArrow?.setOnClickListener(View.OnClickListener { showPlaces() })

        // Listen for location to be retrieved for quick review
        val finalLocationObserver: Observer<com.example.covidnow.models.Location> = Observer<com.example.covidnow.models.Location> {
            Log.i(TAG, "Location received from view model as Location")
            // Listen for user to press quick review button
            btnQuickReview?.setOnClickListener(View.OnClickListener {
                Log.i(TAG, "Quick Review button clicked!")
                // Location ready to be used for quick review
                goQuickReview(mViewModel?.getFinalLocation()?.value)
            })
        }
        // Listen for location to be ready to be reviewed
        mViewModel?.getFinalLocation()?.observe(viewLifecycleOwner, finalLocationObserver)
    }

    private fun initializeRvPlaces(rvPlaces: RecyclerView?) {
        // Initialize adapter
        adapterPlaces = ArrayList()
        adapter = PlacesAdapter(fragment, adapterPlaces as ArrayList<com.example.covidnow.models.Location>)

        // Initialize recyclerview
        rvPlaces?.adapter = adapter
        val layoutManager = LinearLayoutManager(context)
        rvPlaces?.layoutManager = layoutManager

        // Add lines between recycler view
        val itemDecoration: ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvPlaces?.addItemDecoration(itemDecoration)

        // Initialize swipe for details on rvPlaces
        val touchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val background = ColorDrawable(resources.getColor(R.color.light_blue))
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter?.goToDetails(viewHolder.adapterPosition)
            }

            // Show blue background on swipe
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                val itemView = viewHolder.itemView
                if (dX > 0) {
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else if (dX < 0) {
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)
            }
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvPlaces)

        // Swipe up to show places, swipe down to hide
        rvPlaces?.onFlingListener = object : RecyclerViewSwipeListener(true) {
            override fun onSwipeDown() {
                hidePlaces()
            }

            override fun onSwipeUp() {
                //showPlaces()
            }
        }
    }

    private fun goQuickReview(location: com.example.covidnow.models.Location?) {
        val newFrag: Fragment = ComposeReviewFragment()
        val result = Bundle()
        // Send this location to the compose fragment
        result.putParcelable("location", Parcels.wrap(location))
        newFrag.arguments = result
        // Start compose review fragment
        fragmentManager?.beginTransaction()?.replace(R.id.flContainer,
                newFrag)?.addToBackStack("MapsFragment")?.commit()
    }

    private fun showPlaces() {
        // Show rvPlaces with animation
        Log.i(TAG, "Showing places")
        card?.visibility = View.VISIBLE
        card?.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_up))
        ivArrow?.visibility = View.GONE
    }

    fun addMarker(location: com.example.covidnow.models.Location) {
        // Add marker at current location
        val point = LatLng(location.latitude, location.longitude)
        val marker = map?.addMarker(mViewModel?.createMarker(point, location))
        // Move camera to new marker
        moveCameraToLatLng(point)
        marker?.let { mViewModel?.dropPinEffect(it) }
    }

    private fun hidePlaces() {
        // Hide rvPlaces with animation
        Log.i(TAG, "Hiding places")
        card?.visibility = View.GONE
        card?.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_down))
        ivArrow?.visibility = View.VISIBLE
    }

    private fun requestPermissions(){
        val contextProvider= activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissionFineLocation) }

        if(contextProvider == true){
            Toast.makeText(activity?.applicationContext, "Permission is required to obtain location", Toast.LENGTH_SHORT).show()
        }
        permissionRequest()
    }
    private fun permissionRequest(){
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permissionFineLocation, permissionCoarseLocation), REQUEST_CODE_LOCATION) }
    }
    private fun validatePermissionsLocation():Boolean{
        val fineLocationAvailable= activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionFineLocation) } == PackageManager.PERMISSION_GRANTED
        val coarseLocationAvailable= activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, permissionCoarseLocation) } ==PackageManager.PERMISSION_GRANTED

        return fineLocationAvailable && coarseLocationAvailable
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun getMyLocation() {
        Log.i(TAG, "GETTING  LIOCATION ")
        if (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED && context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } != PackageManager.PERMISSION_GRANTED) {
            permissionRequest()
            return
        }
        map?.isMyLocationEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
        val locationClient = activity?.applicationContext?.let { LocationServices.getFusedLocationProviderClient(it) }
        Log.i(TAG, "LOCATION CLIENT " + locationClient)
        mViewModel?.getMyLocation(locationClient, getString(R.string.google_maps_key))
    }

    private fun moveCameraToCoordinates(x: Double, y: Double) {
        // Set camera to user's current location
        val currLocationLatLng = LatLng(x, y)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currLocationLatLng, 17f)
        map?.animateCamera(cameraUpdate)
    }

    private fun moveCameraToLatLng(point: LatLng) {
        // Set camera to this point
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 17f)
        map?.animateCamera(cameraUpdate)
    }

    companion object {
        private const val TAG = "MapsFragment"
        private const val KEY_LOCATION = "location"
        private var adapterPlaces: List<com.example.covidnow.models.Location> = ArrayList()
        private var adapter: PlacesAdapter? = null
    }
}