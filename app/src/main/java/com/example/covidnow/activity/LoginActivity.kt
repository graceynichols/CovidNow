package com.example.covidnow.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.covidnow.databinding.ActivityLoginBinding
import com.example.covidnow.viewmodels.LoginViewModel
import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseUser
import com.parse.SignUpCallback
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var mViewModel: LoginViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: RelativeLayout? = binding?.root
        setContentView(view)
        mViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        if (ParseUser.getCurrentUser() != null) {
            Log.i(TAG, "Logging in: " + ParseUser.getCurrentUser().username)
            goMainActivity()
        }
        binding?.btnLogin?.setOnClickListener {
            Log.i(TAG, "onClick login button")
            binding?.pbLoading?.visibility = ProgressBar.VISIBLE
            val username = binding?.etUsername?.text.toString()
            val password = binding?.etPassword?.text.toString()
            val loginCallback: LogInCallback? = LogInCallback { user, e ->
                if (e != null) {
                    Log.e(TAG, "Issue with login", e)
                    binding?.pbLoading?.visibility = View.GONE
                } else {
                    binding?.pbLoading?.visibility = View.GONE
                    // Put keyboard away automatically
                    view?.hideKeyboard()
                    goMainActivity()
                }
            }
            mViewModel?.loginUser(username, password, loginCallback)

        }
        // On click listener for sign up button
        binding?.btnSignup?.setOnClickListener(View.OnClickListener {
            binding?.btnLogin?.visibility = View.GONE
            binding?.etEmail?.visibility = View.VISIBLE
            Log.i(TAG, "onClick signup button")
            binding?.btnSignup?.setOnClickListener(View.OnClickListener {
                val username = binding?.etUsername?.text.toString()
                val password = binding?.etPassword?.text.toString()
                val email = binding?.etEmail?.text.toString()
                binding?.pbLoading?.visibility = ProgressBar.VISIBLE
                // Make sure username and password pass basic requirements
                if (username == "") {
                    Toast.makeText(applicationContext, "Username missing!", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                if (password == "") {
                    Toast.makeText(applicationContext, "Password missing!", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                // Signup user
                val signupCallback = SignUpCallback {  e: ParseException? ->
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        binding?.pbLoading?.visibility = ProgressBar.INVISIBLE
                        Toast.makeText(applicationContext, "Successful sign up!", Toast.LENGTH_SHORT).show()
                        view?.hideKeyboard()
                        goMainActivity()
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        binding?.pbLoading?.visibility = ProgressBar.INVISIBLE
                        if (e.code == ParseException.USERNAME_TAKEN) {
                            Toast.makeText(applicationContext, "Username already taken", Toast.LENGTH_SHORT).show()
                        } else if (e.code == ParseException.INVALID_EMAIL_ADDRESS) {
                            Toast.makeText(applicationContext, "Invalid Email", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "Error while signing up", Toast.LENGTH_SHORT).show()
                        }
                        Log.i(TAG, e.toString())
                    }
                }
                mViewModel?.signupUser(username, password, email, signupCallback)
            })
        })
    }

    private fun goMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        const val TAG = "LoginActivity"
        private var binding: ActivityLoginBinding? = null
    }
}