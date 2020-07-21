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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            // Create new fragments, otherwise use saved ones
            homeFragment = HomeFragment()
            mapsFragment = MapsFragment()
        }
        // Setup bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView?.setupWithNavController(this.findNavController(R.id.nav_host_fragment))
        initializeBottomNavigationView(bottomNavigationView, this.fragmentManager, this.findNavController(R.id.nav_host_fragment), mapsFragment, homeFragment)
        bottomNavigationView?.selectedItemId = R.id.action_home

    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    companion object {
        const val TAG = "MainActivity"
        private val lastPage: MenuItem? = null
        var mapsFragment: MapsFragment? = null
        var homeFragment: HomeFragment? = null
        private var ft: FragmentTransaction? = null
        fun initializeBottomNavigationView(bottomNavigationView: BottomNavigationView?, fManager: FragmentManager, navController: NavController, mapsFrag: MapsFragment?, homeFrag: HomeFragment?) {
            bottomNavigationView?.setOnNavigationItemSelectedListener { menuItem ->
                mapsFragment = mapsFrag
                homeFragment = homeFrag
                ft = fManager.beginTransaction()
                when (menuItem.itemId) {
                    R.id.action_profile -> displayProfile()
                    R.id.action_map -> displayMaps()
                    else ->
                        // Go to home fragment
                        displayHome()
                }
                //navController.navigate(R.id.nav_host_fragment)
                //fManager.beginTransaction().replace(R.id.flContainer, fragment).commit()
                true
            }
        }

        fun displayMaps() {
            if (mapsFragment?.isAdded == true) {
                // Maps already in container
                ft?.show(mapsFragment as MapsFragment)
            } else {
                (ft as FragmentTransaction).add(R.id.flContainer, mapsFragment as MapsFragment)
            }
            if (homeFragment?.isAdded == true) {
                ft?.hide(homeFragment as HomeFragment)
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
            ft?.commit()
        }

        private fun displayProfile() {
            if (mapsFragment?.isAdded == true) {
                ft?.hide(mapsFragment as MapsFragment)
            }
            if (homeFragment?.isAdded == true) {
                ft?.hide(homeFragment as HomeFragment)
            }
            ft?.replace(R.id.flContainer, ProfileFragment())?.commit()

        }
    }
}