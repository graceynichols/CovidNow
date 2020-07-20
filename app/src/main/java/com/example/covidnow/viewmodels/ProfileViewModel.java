package com.example.covidnow.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.covidnow.activity.LoginActivity;
import com.example.covidnow.repository.ParseRepository;
import com.parse.Parse;
import com.parse.ParseUser;

import java.util.Objects;

public class ProfileViewModel extends AndroidViewModel {

    public ProfileViewModel(@NonNull Application application) {
        super(application);
    }

    public int getNumReviews(ParseUser user) {
        return Objects.requireNonNull(user.getNumber(ParseRepository.KEY_NUM_REVIEWS)).intValue();
    }

    public ParseUser logout() {
        // Logout user
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser;
    }
}
