package com.example.covidnow.activity

import android.app.Activity
import android.os.Bundle
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
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.fragment.MapsFragment
import com.example.covidnow.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

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
        private var ft: FragmentTransaction? = null
        fun initializeBottomNavigationView(bottomNavigationView: BottomNavigationView?, fManager: FragmentManager, profFrag: ProfileFragment?, mapsFrag: MapsFragment?, homeFrag: HomeFragment?) {
            bottomNavigationView?.setOnNavigationItemSelectedListener { menuItem ->
                mapsFragment = mapsFrag
                homeFragment = homeFrag
                profileFragment = profFrag
                ft = fManager.beginTransaction()
                when (menuItem.itemId) {
                    R.id.action_profile -> displayProfile()
                    R.id.action_map -> displayMaps()
                    else ->
                        // Go to home fragment
                        displayHome()
                }
                true
            }
        }

        private fun displayMaps() {
            if (mapsFragment?.isAdded == true) {
                // Maps already in container
                ft?.show(mapsFragment as MapsFragment)
            } else {
                (ft as FragmentTransaction).add(R.id.flContainer, mapsFragment as MapsFragment)
            }
            if (homeFragment?.isAdded == true) {
                ft?.hide(homeFragment as HomeFragment)
            }
            if (profileFragment?.isAdded == true) {
                ft?.hide(profileFragment as ProfileFragment)
            }
            ft?.commit()
        }

        private fun displayHome() {
            if (homeFragment?.isAdded == true) {
                // Maps already in container
                ft?.show(homeFragment as HomeFragment)
            } else {
                (ft as FragmentTransaction).add(R.id.flContainer, homeFragment as HomeFragment)
            }
            if (mapsFragment?.isAdded == true) {
                ft?.hide(mapsFragment as MapsFragment)
            }
            if (profileFragment?.isAdded == true) {
                ft?.hide(profileFragment as ProfileFragment)
            }
            ft?.commit()
        }

        private fun displayProfile() {
            if (profileFragment?.isAdded == true) {
                // Profile already in container
                ft?.show(profileFragment as ProfileFragment)
            } else{
                // Add profile to container
                (ft as FragmentTransaction).add(R.id.flContainer, profileFragment as ProfileFragment)
            }
            // Hide any other fragments already added
            if (mapsFragment?.isAdded == true) {
                ft?.hide(mapsFragment as MapsFragment)
            }
            if (homeFragment?.isAdded == true) {
                ft?.hide(homeFragment as HomeFragment)
            }
            ft?.commit()

        }
    }
}