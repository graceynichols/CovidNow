package com.example.covidnow.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.covidnow.models.Location;
import com.example.covidnow.R;
import com.example.covidnow.viewmodels.ComposeReviewViewModel;
import com.example.covidnow.viewmodels.MapsViewModel;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class ComposeReviewFragment extends Fragment {

    public static final String TAG = "ComposeReviewFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 40;
    private String photoFileName = "photo.jpg";
    private Button btnCaptureImage;
    private ImageView ivPostImage;
    private ImageView ivHotspot;
    private Switch switchHotspot;
    private Button btnSubmit;
    private Location location;
    private File photoFile;
    private ProgressBar pb;
    private boolean photoFlag = false;
    private ComposeReviewViewModel mViewModel;

    public ComposeReviewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_review, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set view model
        mViewModel = ViewModelProviders.of(this).get(ComposeReviewViewModel.class);

        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        pb = view.findViewById(R.id.pbLoading);
        ivHotspot = view.findViewById(R.id.ivHotspot);
        switchHotspot = view.findViewById(R.id.switchHotspot);

        location = Parcels.unwrap(getArguments().getParcelable("location"));

        // Make switch reflect whether this was a hotspot already or not
        if (location.isHotspot()) {
            switchHotspot.setChecked(true);
            ivHotspot.setVisibility(View.VISIBLE);
        }
        // Show caution symbol if marked as hotspot
        switchHotspot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ivHotspot.setVisibility(View.GONE);
                } else {
                    ivHotspot.setVisibility(View.VISIBLE);
                }
            }
        });

        // Button to take a picture
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        // Clicking submit saves image and hotspot status to
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start progress bar
                pb.setVisibility(ProgressBar.VISIBLE);

                mViewModel.saveReview(location, photoFile, switchHotspot.isChecked());
                pb.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Review Saved!", Toast.LENGTH_SHORT).show();

                // Return to details activity
                goDetailsActivity();
            }
        });

        // Handle back button pressed event
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                goDetailsActivity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goDetailsActivity() {
        Fragment newFrag = new LocationDetailsFragment();
        Bundle result = new Bundle();
        // Send this location to the compose fragment
        result.putParcelable("location", Parcels.wrap(location));
        newFrag.setArguments(result);
        // Start compose review fragment
        getFragmentManager().beginTransaction().replace(R.id.flContainer,
                newFrag).commit();
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
                photoFlag = true;
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
}