package com.example.covidnow.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.covidnow.repository.ParseRepository
import com.parse.ParseUser
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    fun getNumReviews(user: ParseUser): Int {
        val numReviews = user.getNumber(ParseRepository.KEY_NUM_REVIEWS)
        return numReviews?.toInt() ?: 0
    }

    fun logout(): ParseUser {
        // Logout user
        ParseUser.logOut()
        return ParseUser.getCurrentUser()
    }
}