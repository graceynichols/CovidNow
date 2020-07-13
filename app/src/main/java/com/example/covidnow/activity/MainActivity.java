package com.example.covidnow.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.covidnow.R;
import com.example.covidnow.fragment.HomeFragment;
import com.example.covidnow.fragment.MapsFragment;
import com.example.covidnow.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static MenuItem lastPage;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        initializeBottomNavigationView(bottomNavigationView, fragmentManager);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    public static void initializeBottomNavigationView(final BottomNavigationView bottomNavigationView, final FragmentManager fManager) {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    // Go to the selected fragment
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    case R.id.action_map:
                        fragment = new MapsFragment();
                        break;
                    default:
                        // Go to home fragment
                        fragment = new HomeFragment();
                        break;
                }
                fManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
    }
}