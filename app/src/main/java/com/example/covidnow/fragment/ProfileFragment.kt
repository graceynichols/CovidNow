package com.example.covidnow.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.covidnow.R
import com.example.covidnow.activity.LoginActivity
import com.example.covidnow.viewmodels.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.parse.ParseUser

class ProfileFragment : Fragment() {
    private var tvUsername: TextView? = null
    private var tvReviewCount: TextView? = null
    private var btnLogout: Button? = null
    private var btnCovid: Button? = null
    private var mViewModel: ProfileViewModel? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvReviewCount = view.findViewById(R.id.tvReviewCount)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnCovid = view.findViewById(R.id.btnCovid)

        // Set information
        val reviews = "" + mViewModel?.getNumReviews(ParseUser.getCurrentUser())
        tvReviewCount?.text = reviews
        tvUsername?.text = ParseUser.getCurrentUser().username

        // Listen for logout button
        btnLogout?.setOnClickListener(View.OnClickListener { view ->
            val myOnClickListener = View.OnClickListener {
                // Logout user
                if (mViewModel?.logout() != null) {
                    Log.i(TAG, "Error logging out")
                    Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(TAG, "Logout successful")
                    // Take user back to login screen
                    Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
            // Make the snackbar
            Snackbar.make(view, "Are you sure you want to logout?", Snackbar.LENGTH_LONG)
                    .setAction("Yes", myOnClickListener)
                    .show()
        })

        // If someone hits the "I have COVID-19" button
        btnCovid?.setOnClickListener(View.OnClickListener { view ->
            val myOnClickListener = View.OnClickListener {
                // Trace people who overlapped with this person's location history
                mViewModel?.contactTracing()
                Toast.makeText(context, "People you may have had contact with have been notified", Toast.LENGTH_SHORT).show()
            }
            // Snackbar - user must confirm they have covid before contact tracing begins
            Snackbar.make(view, "Are you sure you are positive for COVID-19?", Snackbar.LENGTH_LONG)
                    .setAction("Yes", myOnClickListener)
                    .show()
        })

    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}