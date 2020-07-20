package com.example.covidnow.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.covidnow.models.Location;
import com.example.covidnow.repository.ParseRepository;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;

import static com.example.covidnow.repository.ParseRepository.KEY_NUM_REVIEWS;

public class ComposeReviewViewModel extends AndroidViewModel {
    private static final String TAG = "ComposeReviewViewModel";
    private ParseRepository parseRepository;


    public ComposeReviewViewModel(@NonNull Application application) {
        super(application);
        this.parseRepository = new ParseRepository();
    }

    public void saveReview(Location location, ParseFile photoFile, ParseUser user, boolean checked) {
        if (photoFile != null) {
            // They added a photo
            location.setImage(photoFile);
        }
        location.setIsHotspot(checked);
        parseRepository.saveLocation(location);
        // Add one to this user's review count
        if (user.getNumber(KEY_NUM_REVIEWS) != null) {
            user.put(KEY_NUM_REVIEWS, user.getNumber(KEY_NUM_REVIEWS).intValue() + 1);
        } else {
            // User didn't previously have a numReviews
            user.put(KEY_NUM_REVIEWS, 1);
        }

        user.saveInBackground();
    }


}
