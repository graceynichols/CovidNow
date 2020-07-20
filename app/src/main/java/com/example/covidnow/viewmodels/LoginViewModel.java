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

        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("numReviews", 0);
        // Invoke signUpInBackground
        user.signUpInBackground(signUpCallback);
    }

    private boolean passwordValidityCheck(Context context, String username, String password) {
        if (username.equals("")) {
            Toast.makeText(context, "Username missing!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.equals("")) {
            Toast.makeText(context, "Password missing!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(context, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.equals(username)) {
            Toast.makeText(context, "Password cannot be the same as username", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
