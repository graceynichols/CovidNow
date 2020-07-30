package com.example.covidnow.activity

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {
    private val fragmentManager = supportFragmentManager
    private var bottomNavigationView: BottomNavigationView? = null
    private var homeFragment: HomeFragment? = null
    private var mapsFragment: MapsFragment? = null
    private var profileFragment: ProfileFragment? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up custom action bar
        this.supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setDisplayShowCustomEnabled(true);
        supportActionBar?.setCustomView(R.layout.custom_action_bar);

        // Setup bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView?.setupWithNavController(this.findNavController(R.id.nav_host_fragment))
        initializeBottomNavigationView(bottomNavigationView, this.fragmentManager)
        bottomNavigationView?.selectedItemId = R.id.action_home

    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    companion object {
        const val TAG = "MainActivity"
        private var mapsFragment: MapsFragment? = null
        private var homeFragment: HomeFragment? = null
        private var profileFragment: ProfileFragment? = null
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
            if (homeFragment != null) {
                if ((homeFragment as HomeFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(homeFragment as HomeFragment)
                }
            }
            if (mapsFragment != null) {
                if ((mapsFragment as MapsFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(mapsFragment as MapsFragment)
                }
            }
            if (profileFragment == null) {
                Log.i(TAG, "Creating profile fragment")
                // Instantiate maps fragment only once
                profileFragment = ProfileFragment()

            } else {
                if ((profileFragment as ProfileFragment).isDetached) {
                    Log.i(TAG, "Attaching Profile fragment")
                    ft?.attach(profileFragment as ProfileFragment)
                }
            }
            ft?.replace(R.id.flContainer, profileFragment as ProfileFragment)
            ft?.addToBackStack("ProfileFragment")
        }

        private fun displayMaps() {
            if (profileFragment != null) {
                if ((profileFragment as ProfileFragment).isAdded) {
                    Log.i(TAG, "Detaching Profile fragment")
                    ft?.detach(profileFragment as ProfileFragment)
                }
            }
            if (homeFragment != null) {
                if ((homeFragment as HomeFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(homeFragment as HomeFragment)
                }
            }
            if (Companion.mapsFragment == null) {
                Log.i(TAG, "Creating maps fragment")
                // Instantiate maps fragment only once
                Companion.mapsFragment = MapsFragment()
            } else {
                if ((Companion.mapsFragment as MapsFragment).isDetached) {
                    Log.i(TAG, "Attaching Maps fragment")
                    Companion.ft?.attach(Companion.mapsFragment as MapsFragment)
                }
            }
            Companion.ft?.replace(R.id.flContainer, Companion.mapsFragment as MapsFragment)

            ft?.addToBackStack("MapsFragment")
        }

        private fun displayHome() {
            if (profileFragment != null) {
                if ((profileFragment as ProfileFragment).isAdded) {
                    Log.i(TAG, "Detaching Profile fragment")
                    ft?.detach(profileFragment as ProfileFragment)
                }
            }
            if (mapsFragment != null) {
                if ((mapsFragment as MapsFragment).isAdded) {
                    Log.i(TAG, "Detaching home fragment")
                    ft?.detach(mapsFragment as MapsFragment)
                }
            }
            // Add home fragment
            if (homeFragment == null) {
                // Instantiate home fragment only once
                homeFragment = HomeFragment()
            } else {
                if ((homeFragment as HomeFragment).isDetached) {
                    Log.i(TAG, "Attaching home fragment")
                    ft?.attach(homeFragment as HomeFragment)
                }
            }
            ft?.replace(R.id.flContainer, homeFragment as HomeFragment)

            ft?.addToBackStack("HomeFragment")
        }
    }
}