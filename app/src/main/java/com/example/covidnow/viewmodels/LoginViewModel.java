package com.example.covidnow.viewmodels;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.covidnow.activity.LoginActivity;
import com.example.covidnow.repository.ParseRepository;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginViewModel extends AndroidViewModel {
    private static final String TAG = "LoginViewModel";
    private ParseRepository parseRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.parseRepository = new ParseRepository();
    }

    public void loginUser(String username, String password, LogInCallback logInCallback) {
        Log.i(TAG, "Attempting to login user " + username);

        ParseUser.logInInBackground(username, password, logInCallback);
    }

    public void signupUser(Context context, String username, String password, String email, SignUpCallback signUpCallback) {
        Log.i(TAG, "Attempting to signup user " + username);
        parseRepository.createNewUser(username, password, email, signUpCallback);

    }


}
