package com.example.covidnow.activity

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
import com.example.covidnow.models.Article
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.parceler.Parcels

class MainActivity : AppCompatActivity() {
    private val fragmentManager = supportFragmentManager
    private var bottomNavigationView: BottomNavigationView? = null
    private var homeFragment: HomeFragment? = null
    private var mapsFragment: MapsFragment? = null
    private var profileFragment: ProfileFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            // Create new fragments, otherwise use saved ones
            Log.i(TAG, "Creating new frags")
            homeFragment = HomeFragment()
            mapsFragment = MapsFragment()
            profileFragment = ProfileFragment()
        }
        // Setup bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView?.setupWithNavController(this.findNavController(R.id.nav_host_fragment))
        initializeBottomNavigationView(bottomNavigationView, this.fragmentManager, this.profileFragment, mapsFragment, homeFragment)
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
        fun initializeBottomNavigationView(bottomNavigationView: BottomNavigationView?, fManager: FragmentManager, profFrag: ProfileFragment?, mapsFrag: MapsFragment?, homeFrag: HomeFragment?) {
            bottomNavigationView?.setOnNavigationItemSelectedListener { menuItem ->
                mapsFragment = mapsFrag
                homeFragment = homeFrag
                profileFragment = profFrag
                ft = fManager.beginTransaction()
                fragmentManager = fManager
                when (menuItem.itemId) {
                    R.id.action_profile -> {
                        ft?.replace(R.id.flContainer, profileFragment as ProfileFragment)
                        ft?.addToBackStack("ProfileFragment")
                    }
                    R.id.action_map -> {
                        ft?.replace(R.id.flContainer, mapsFragment as MapsFragment)
                        ft?.addToBackStack("MapsFragment")
                    }
                    else ->{
                        // Go to home fragment
                        ft?.replace(R.id.flContainer, homeFragment as HomeFragment)
                        ft?.addToBackStack("HomeFragment")
                    }

                }

                ft?.commit()
                true
            }
        }

        private fun displayMaps() {
            Log.i(TAG, "Fragments: " + fragmentManager?.fragments.toString())
            var flag = true
            if (mapsFragment?.isAdded == true) {
                Log.i(TAG, "Home already in container")
                // Maps already in container
                ft?.show(mapsFragment as MapsFragment)
            } else {
                (ft as FragmentTransaction).add(R.id.flContainer, mapsFragment as MapsFragment)
            }
            if ( homeFragment?.isAdded == true) {
                flag = false
                Log.i(TAG, "Home was added")
                ft?.hide(homeFragment as HomeFragment)
            }
            if ( profileFragment?.isAdded == true) {
                flag = false
                Log.i(TAG, "Profile was added")
                ft?.hide(profileFragment as ProfileFragment)
            }
            if (flag) {
                Log.i(TAG, "Don't need to save old fragment")
                ft?.replace(R.id.flContainer, mapsFragment as MapsFragment)
                ft?.show(mapsFragment as MapsFragment)
            }
            ft?.commit()
        }

        private fun displayHome() {
            var flag = true
            Log.i(TAG, "Fragments: " + fragmentManager?.fragments.toString())
            if (homeFragment?.isAdded == true) {
                Log.i(TAG, "Maps already in container")
                // Maps already in container
                ft?.show(homeFragment as HomeFragment)
            } else {
                (ft as FragmentTransaction).add(R.id.flContainer, homeFragment as HomeFragment)
            }
            if ( mapsFragment?.isAdded == true) {
                Log.i(TAG, "Maps was added")
                ft?.hide(mapsFragment as MapsFragment)
                flag = false
            }
            if ( profileFragment?.isAdded == true) {
                Log.i(TAG, "Profile was added")
                ft?.hide(profileFragment as ProfileFragment)
                flag = false
            }
            if (flag) {
                Log.i(TAG, "Don't need to save old fragment")
                ft?.replace(R.id.flContainer, homeFragment as HomeFragment)
                ft?.show(homeFragment as HomeFragment)
            }
            ft?.commit()
        }

        private fun displayProfile() {
            var flag = true
            Log.i(TAG, "Fragments: " + fragmentManager?.fragments.toString())
            if (profileFragment?.isAdded == true) {
                // Profile already in container
                Log.i(TAG, "Profile already in container")
                ft?.show(profileFragment as ProfileFragment)
            } else{
                // Add profile to container
                (ft as FragmentTransaction).add(R.id.flContainer, profileFragment as ProfileFragment)
            }
            // Hide any other fragments already added
            if ( mapsFragment?.isAdded == true) {
                Log.i(TAG, "Maps was added")
                ft?.hide(mapsFragment as MapsFragment)
                flag = false
            }
            if ( homeFragment?.isAdded == true) {
                Log.i(TAG, "Home was added")
                ft?.hide(homeFragment as HomeFragment)
            }
            if (flag) {
                flag = false
                Log.i(TAG, "Don't need to save old fragment")
                ft?.replace(R.id.flContainer, profileFragment as ProfileFragment)
                ft?.show(profileFragment as ProfileFragment)
            }
            ft?.commit()

        }
    }
}