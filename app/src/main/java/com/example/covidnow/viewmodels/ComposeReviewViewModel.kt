package com.example.covidnow.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.covidnow.models.Location
import com.example.covidnow.repository.ParseRepository
import com.parse.ParseFile
import com.parse.ParseUser
import com.parse.SaveCallback

class ComposeReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val parseRepository: ParseRepository = ParseRepository()
    fun saveReview(location: Location, photoFile: ParseFile?, user: ParseUser, checked: Boolean) {
        if (photoFile != null) {
            // They added a photo
            location.image = photoFile
        }
        location.isHotspot = checked
        parseRepository.saveLocation(location)
        // Add one to this user's review count
        val numberReviews = user.getNumber(ParseRepository.KEY_NUM_REVIEWS);
        if (numberReviews != null) {
            val numRev: Int ? = user.getNumber(ParseRepository.KEY_NUM_REVIEWS)?.toInt()
            if (numRev != null) {
                user.put(ParseRepository.KEY_NUM_REVIEWS, numRev + 1)
            } else {
                user.put(ParseRepository.KEY_NUM_REVIEWS, 1)
            }
        } else {
            // User didn't previously have a numReviews
            user.put(ParseRepository.KEY_NUM_REVIEWS, 1)
        }
        user.saveInBackground()
    }

    companion object {
        private const val TAG = "ComposeReviewViewModel"
    }

}