package com.example.covidnow.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.covidnow.R;
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
}