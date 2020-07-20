package com.example.covidnow.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.covidnow.R
import com.example.covidnow.fragment.HomeFragment
import com.example.covidnow.fragment.MapsFragment
import com.example.covidnow.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val fragmentManager = supportFragmentManager
    private var bottomNavigationView: BottomNavigationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        initializeBottomNavigationView(bottomNavigationView, this.fragmentManager)
        bottomNavigationView?.selectedItemId = R.id.action_home
    }

    companion object {
        const val TAG = "MainActivity"
        private val lastPage: MenuItem? = null
        fun initializeBottomNavigationView(bottomNavigationView: BottomNavigationView?, fManager: FragmentManager) {
            bottomNavigationView?.setOnNavigationItemSelectedListener { menuItem ->
                val fragment: Fragment = when (menuItem.itemId) {
                    R.id.action_profile -> ProfileFragment()
                    R.id.action_map -> MapsFragment()
                    else ->
                        // Go to home fragment
                        HomeFragment()
                }
                fManager.beginTransaction().replace(R.id.flContainer, fragment).commit()
                true
            }
        }
    }
}