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
    }
}