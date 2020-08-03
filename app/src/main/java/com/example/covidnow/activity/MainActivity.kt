package com.example.covidnow.activity

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.covidnow.R
import com.example.covidnow.fragment.ArticleDetailsFragment
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.fragment.MapsFragment
import com.example.covidnow.fragment.ProfileFragment
import com.example.covidnow.helpers.PermissionsRequestHelper
import com.example.covidnow.models.Article
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.parceler.Parcels

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val fragmentManager = supportFragmentManager
    private var bottomNavigationView: BottomNavigationView? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up custom action bar
        this.supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setDisplayShowCustomEnabled(true);
        supportActionBar?.setCustomView(R.layout.custom_action_bar);
        mapsFlag = false
        homeFlag = false
        profileFlag = false
        // Setup bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        //bottomNavigationView?.setupWithNavController(this.findNavController(R.id.nav_host_fragment))
        initializeBottomNavigationView(bottomNavigationView, this.fragmentManager)
        bottomNavigationView?.selectedItemId = R.id.action_home

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionsResult")
        if (requestCode == HomeFragment.REQUEST_CODE_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Location permission request.")

            // Check if the only required permission has been granted
            if ((grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "Location permission has now been granted.")
                getHomeFragLocation()
            }
        }  else {
            super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }


    companion object {
        const val TAG = "MainActivity"
        const val MAPS_TAG = "MapsFragment"
        const val HOME_TAG = "HomeFragment"
        const val PROFILE_TAG = "ProfileFragment"
        private var mapsFragment: MapsFragment? = null
        private var homeFragment: HomeFragment? = null
        private var profileFragment: ProfileFragment? = null
        private var profileFlag: Boolean = false
        private var mapsFlag: Boolean = false
        private var homeFlag: Boolean = false
        private var fragmentManager: FragmentManager? = null
        private var ft: FragmentTransaction? = null

        fun initializeBottomNavigationView(bottomNavigationView: BottomNavigationView?, fManager: FragmentManager) {
            bottomNavigationView?.setOnNavigationItemSelectedListener { menuItem ->
                ft = fManager.beginTransaction()
                fragmentManager = fManager
                when (menuItem.itemId) {
                    R.id.action_profile -> {
                        displayProfile()
                    }
                    R.id.action_map -> {
                        // Go to maps fragment
                        displayMaps()
                    }
                    else ->{
                        // Go to home fragment
                        displayHome()
                    }
                }
                ft?.commit()
                true
            }
        }

        private fun displayProfile() {
            if (homeFlag) {
                homeFragment = fragmentManager?.findFragmentByTag(HOME_TAG) as HomeFragment
                if ((homeFragment as HomeFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(homeFragment as HomeFragment)
                }
            }
            if (mapsFlag) {
                mapsFragment = fragmentManager?.findFragmentByTag(MAPS_TAG) as MapsFragment
                if ((mapsFragment as MapsFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(mapsFragment as MapsFragment)
                }
            }
            if (!profileFlag) {
                Log.i(TAG, "Creating profile fragment")
                // Instantiate profile fragment only once
                profileFlag = true
                ft?.replace(R.id.flContainer, ProfileFragment(), PROFILE_TAG)
            } else {
                profileFragment = fragmentManager?.findFragmentByTag(PROFILE_TAG) as ProfileFragment
                if ((profileFragment as ProfileFragment).isDetached) {
                    Log.i(TAG, "Attaching Profile fragment")
                    ft?.attach(profileFragment as ProfileFragment)
                }
                ft?.replace(R.id.flContainer, profileFragment as ProfileFragment, PROFILE_TAG)
            }
            ft?.addToBackStack("ProfileFragment")
        }

        private fun displayMaps() {
            if (profileFlag) {
                profileFragment = fragmentManager?.findFragmentByTag(PROFILE_TAG) as ProfileFragment
                if (profileFragment?.isAdded == true) {
                    Log.i(TAG, "Detaching Profile fragment")
                    ft?.detach(profileFragment as ProfileFragment)
                }
            }
            if (homeFlag) {
                homeFragment = fragmentManager?.findFragmentByTag(HOME_TAG) as HomeFragment
                if ((homeFragment as HomeFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(homeFragment as HomeFragment)
                }
            }
            if (!mapsFlag) {
                Log.i(TAG, "Creating maps fragment")
                // Instantiate maps fragment only once
                mapsFlag = true
                ft?.replace(R.id.flContainer, MapsFragment(), MAPS_TAG)
            } else {
                mapsFragment = fragmentManager?.findFragmentByTag(MAPS_TAG) as MapsFragment
                if ((mapsFragment as MapsFragment).isDetached) {
                    Log.i(TAG, "Attaching Maps fragment")
                    ft?.attach(mapsFragment as MapsFragment)
                }
                ft?.replace(R.id.flContainer, mapsFragment as MapsFragment, MAPS_TAG)
            }
            ft?.addToBackStack("MapsFragment")
        }

        @JvmStatic
        fun displayHome() {
            if (profileFlag) {
                profileFragment = fragmentManager?.findFragmentByTag(PROFILE_TAG) as ProfileFragment
                if (profileFragment?.isAdded == true) {
                    Log.i(TAG, "Detaching Profile fragment")
                    ft?.detach(profileFragment as ProfileFragment)
                }
            }
            if (mapsFlag) {
                mapsFragment = fragmentManager?.findFragmentByTag(MAPS_TAG) as MapsFragment
                if ((mapsFragment as MapsFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(mapsFragment as MapsFragment)
                }
            }
            // Add home fragment
            if (!homeFlag) {
                // Instantiate home fragment only once
                Log.i(TAG, "Creating home fragment")
                homeFlag = true
                ft?.replace(R.id.flContainer, HomeFragment(), HOME_TAG)

            } else {
                Log.i(TAG, "HomeFragment is not null")
                homeFragment = fragmentManager?.findFragmentByTag(HOME_TAG) as HomeFragment
                if ((homeFragment as HomeFragment).isDetached) {
                    Log.i(TAG, "Attaching home fragment")
                    ft?.attach(homeFragment as HomeFragment)
                }
                ft?.replace(R.id.flContainer, homeFragment as HomeFragment, HOME_TAG)

            }
            ft?.addToBackStack("HomeFragment")

        }

        fun getHomeFragLocation() {
            Log.i(TAG, "in get home frag location")
            homeFragment = fragmentManager?.findFragmentByTag(HOME_TAG) as HomeFragment
            if (homeFragment == null) {
                Log.i(TAG, "Home fragment is null, this shouldn't happen")
            } else {
                (fragmentManager?.findFragmentByTag(HOME_TAG) as HomeFragment).getMyLocation()
            }
        }
    }
}