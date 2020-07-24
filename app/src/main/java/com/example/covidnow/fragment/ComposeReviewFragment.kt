package com.example.covidnow.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.covidnow.R
import com.example.covidnow.models.Location
import com.example.covidnow.viewmodels.ComposeReviewViewModel
import com.parse.ParseFile
import com.parse.ParseUser
import com.parse.SaveCallback
import org.parceler.Parcels
import java.io.File

class ComposeReviewFragment : Fragment() {
    private val photoFileName = "photo.jpg"
    private var btnCaptureImage: Button? = null
    private var ivPostImage: ImageView? = null
    private var switchHotspot: Switch? = null
    private var tvName: TextView? = null
    private var btnSubmit: Button? = null
    private var location: Location? = null
    private var photoFile: File? = null
    private var pb: ProgressBar? = null
    private var photoParseFile: ParseFile? = null
    private var mViewModel: ComposeReviewViewModel? = null
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_compose_review, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set view model
        mViewModel = ViewModelProviders.of(this).get(ComposeReviewViewModel::class.java)
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage)
        ivPostImage = view.findViewById(R.id.ivPostImage)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        pb = view.findViewById(R.id.pbLoading)
        switchHotspot = view.findViewById(R.id.switchHotspot)
        tvName = view.findViewById(R.id.tvName)

        // Get location info from parcel
        location = Parcels.unwrap<Location>(arguments?.getParcelable("location"))

        //Display location name
        tvName?.text = location?.name

        // Make switch reflect whether this was a hotspot already or not
        if (location?.isHotspot == true) {
            switchHotspot?.isChecked = true
        }

        // Button to take a picture
        btnCaptureImage?.setOnClickListener(View.OnClickListener { launchCamera() })

        // Clicking submit saves image and hotspot status to
        btnSubmit?.setOnClickListener(View.OnClickListener { // Start progress bar
            pb?.visibility = ProgressBar.VISIBLE
            if (photoFile != null) {
                Log.i(TAG, "We have a photo file")
                photoParseFile = ParseFile(photoFile)
            }


            switchHotspot?.isChecked?.let { it1 -> mViewModel?.saveReview(location as Location, photoParseFile, ParseUser.getCurrentUser(), it1) }
            pb?.visibility = View.GONE
            Log.i(TAG, "User saved")
            Toast.makeText(context, "Review Saved!", Toast.LENGTH_SHORT).show()

            // Return to details activity
            goDetailsActivity()


        })
    }

    private fun goDetailsActivity() {
        val newFrag: Fragment = LocationDetailsFragment()
        val result = Bundle()
        // Send this location to the compose fragment
        result.putParcelable("location", Parcels.wrap(location))
        newFrag.arguments = result
        // Start compose review fragment
        fragmentManager?.beginTransaction()?.replace(R.id.flContainer,
                newFrag)?.commit()
    }

    private fun launchCamera() {
        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName)

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        val fileProvider = context?.let { FileProvider.getUriForFile(it, "com.codepath.fileprovider", photoFile as File) }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (context?.packageManager?.let { intent.resolveActivity(it) } != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // by this point we have the camera photo on disk
                val takenImage = BitmapFactory.decodeFile(photoFile?.absolutePath)
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPostImage?.setImageBitmap(takenImage)
            } else { // Result was a failure
                Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private fun getPhotoFileUri(fileName: String): File {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    companion object {
        const val TAG = "ComposeReviewFragment"
        const val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 40
    }
}