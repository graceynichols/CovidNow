package com.example.covidnow.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.covidnow.models.Location;
import com.example.covidnow.repository.ParseRepository;
import com.parse.Parse;
import com.parse.ParseFile;

import java.io.File;

public class ComposeReviewViewModel extends AndroidViewModel {
    private ParseRepository parseRepository;

    public ComposeReviewViewModel(@NonNull Application application) {
        super(application);
        this.parseRepository = new ParseRepository();
    }

    public void saveReview(Location location, File photoFile, boolean checked) {
        if (photoFile != null) {
            // They added a photo
            location.setImage(new ParseFile(photoFile));
        }
        location.setIsHotspot(checked);
        parseRepository.saveLocation(location);
    }
}
