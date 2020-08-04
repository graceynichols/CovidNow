package com.example.covidnow.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.covidnow.repository.ParseRepository
import com.parse.LogInCallback
import com.parse.ParseUser
import com.parse.SignUpCallback

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val parseRepository: ParseRepository = ParseRepository()
    fun loginUser(username: String, password: String?, logInCallback: LogInCallback?) {
        Log.i(TAG, "Attempting to login user $username")
        ParseUser.logInInBackground(username, password, logInCallback)
    }

    fun signupUser(username: String, password: String?, email: String?, signUpCallback: SignUpCallback?) {
        Log.i(TAG, "Attempting to signup user $username")
        parseRepository.createNewUser(username, password, email, signUpCallback)
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }

}