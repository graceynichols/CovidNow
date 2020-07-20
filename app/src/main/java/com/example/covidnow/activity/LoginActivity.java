package com.example.covidnow.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.covidnow.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final int USERNAME_ERROR_CODE = 202;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private Button btnLogin;
    private Button btnSignup;
    private ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (ParseUser.getCurrentUser() != null) {
            Log.i(TAG, "Logging in: " + ParseUser.getCurrentUser().getUsername());
            goMainActivity();
        }
        pb = findViewById(R.id.pbLoading);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        etEmail = findViewById(R.id.etEmail);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick login button");
                pb.setVisibility(ProgressBar.VISIBLE);
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });
        // On click listener for sign up button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLogin.setVisibility(View.GONE);
                etEmail.setVisibility(View.VISIBLE);
                Log.i(TAG, "onClick signup button");

                btnSignup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String username = etUsername.getText().toString();
                        String password = etPassword.getText().toString();
                        String email = etEmail.getText().toString();
                        pb.setVisibility(ProgressBar.VISIBLE);
                        signupUser(username, password, email);
                    }
                });
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, "Issue with login", Toast.LENGTH_SHORT).show();
                    pb.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }
                pb.setVisibility(ProgressBar.INVISIBLE);
                goMainActivity();
            }
        });
    }

    private void signupUser(String username, String password, String email) {
        Log.i(TAG, "Attempting to signup user " + username);
        if (username.equals("")) {
            Toast.makeText(getApplicationContext(), "Username missing!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "Password missing!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.equals(username)) {
            Toast.makeText(getApplicationContext(), "Password cannot be the same as username", Toast.LENGTH_SHORT).show();
            return;
        }
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("numReviews", 0);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    pb.setVisibility(ProgressBar.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Successful sign up!", Toast.LENGTH_SHORT).show();
                    goMainActivity();
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    pb.setVisibility(ProgressBar.INVISIBLE);
                    if (e.getCode() == ParseException.USERNAME_TAKEN) {
                        Toast.makeText(getApplicationContext(), "Username already taken", Toast.LENGTH_SHORT).show();
                    } else if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS){
                        Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error while signing up", Toast.LENGTH_SHORT).show();
                    }
                    Log.i(TAG, e.toString());
                }
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}