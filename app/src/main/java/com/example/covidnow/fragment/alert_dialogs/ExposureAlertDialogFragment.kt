package com.example.covidnow.fragment.alert_dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.covidnow.R
import com.example.covidnow.activity.MainActivity
import com.example.covidnow.viewmodels.ProfileViewModel

class ExposureAlertDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity, R.style.AlertDialogCustom)
        alertDialogBuilder.setTitle("Warning")
        alertDialogBuilder.setMessage("You have recently been exposed to a user who has COVID-19")
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which -> dialog?.dismiss()}
        alertDialogBuilder.setPositiveButton("See exposure history") { dialog, which ->
            // Show profile fragment
            MainActivity.showProfileFromAlert()
        }
        return alertDialogBuilder.create()
    }

    companion object {
        fun newInstance(): ExposureAlertDialogFragment {
            val frag = ExposureAlertDialogFragment()
            return frag
        }
    }
}