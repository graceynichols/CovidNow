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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.covidnow.R
import com.example.covidnow.activity.LoginActivity
import com.example.covidnow.adapter.HistoryAdapter
import com.example.covidnow.viewmodels.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.parse.ParseUser
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class ProfileFragment : Fragment() {
    private var tvUsername: TextView? = null
    private var tvReviewCount: TextView? = null
    // RecyclerView showing exposure history
    private var rvHistory: RecyclerView? = null
    private var btnLogout: Button? = null
    private var btnCovid: Button? = null
    private var mViewModel: ProfileViewModel? = null
    private var adapterHistory: List<JSONObject>?  = null
    private var adapter: HistoryAdapter?  = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up ProfileViewModel
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvReviewCount = view.findViewById(R.id.tvReviewCount)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnCovid = view.findViewById(R.id.btnCovid)
        rvHistory = view.findViewById(R.id.rvHistory)

        // Set review number
        val reviews = "" + mViewModel?.getNumReviews(ParseUser.getCurrentUser())
        tvReviewCount?.text = reviews
        tvUsername?.text = ParseUser.getCurrentUser().username

        // Set up location history adapter
        adapterHistory = ArrayList()
        adapter = HistoryAdapter(this, adapterHistory as ArrayList<JSONObject>)
        rvHistory?.adapter = adapter

        // Set recyclerview layoutmanager
        val layoutManager = LinearLayoutManager(context)
        rvHistory?.layoutManager = layoutManager
        // Add lines between recycler view
        val itemDecoration: ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvHistory?.addItemDecoration(itemDecoration)

        // Get this user's exposure messages for rvHistory
        mViewModel?.getMessages()?.let { adapter?.addAll(it) }

        // Listen for logout button
        btnLogout?.setOnClickListener(View.OnClickListener { view ->
            val myOnClickListener = View.OnClickListener {
                // Logout user
                if (mViewModel?.logout() != null) {
                    // Logout was unsuccessful
                    Log.i(TAG, "Error logging out")
                    Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                } else {
                    // logout was successful
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
                    // Only get logged out if they click yes
                    .setAction("Yes", myOnClickListener)
                    .show()
        })

        // If someone hits the "I have COVID-19" button
        btnCovid?.setOnClickListener(View.OnClickListener { view ->
            // Start compose review fragment
            showAlertDialog();
        })

    }

    private fun showAlertDialog() {
        val alertDialog: CovidAlertDialogueFragment = CovidAlertDialogueFragment.newInstance("Users who you may have exposed will be notified", mViewModel)
        fragmentManager?.let { alertDialog.show(it, "fragment_alert") };
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}