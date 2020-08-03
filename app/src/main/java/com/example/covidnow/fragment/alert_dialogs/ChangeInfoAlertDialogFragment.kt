package com.example.covidnow.fragment.alert_dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.covidnow.R
import com.example.covidnow.viewmodels.ProfileViewModel

class ChangeInfoAlertDialogFragment : DialogFragment() {
    private var etInput: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val isUsername = arguments?.getBoolean("isUsername")
        val alertDialogBuilder = AlertDialog.Builder(activity, R.style.AlertDialogCustom)
        // Set the custom dialog layout
        var dialogText: String? = null
        var toastText: String? = null
        val customLayout = layoutInflater.inflate(R.layout.fragment_change_info_alert_dialog, null)
        alertDialogBuilder.setView(customLayout)
        etInput = customLayout.findViewById(R.id.etInput)
        if (isUsername as Boolean) {
            // User is changing their username
            alertDialogBuilder.setTitle("Please enter new username")
            dialogText = "Change Username"
            toastText = "Your username has been changed"
        } else {
            // User is changing their password
            alertDialogBuilder.setTitle("Please enter new password")
            dialogText = "Change my password"
            toastText = "Your password has been changed"
        }

        alertDialogBuilder.setPositiveButton(dialogText) { dialog, which -> // on success

            val userInput = etInput?.text.toString()
            if (isUsername) {
                if (userInput != "") {
                    (mViewModel as ProfileViewModel).changeUsername(userInput)

                } else {
                    toastText = "Username cannot be empty"
                }
            } else {
                if (userInput != "") {
                    // Reset password
                    (mViewModel as ProfileViewModel).resetPassword(userInput)
                } else {
                    toastText = "Password cannot be empty"
                }
            }
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which -> dialog?.dismiss() }
        return alertDialogBuilder.create()
    }

    companion object {
        private var mViewModel: ViewModel? = null
        fun newInstance(profileViewModel: ViewModel?, isUsername: Boolean): ChangeInfoAlertDialogFragment {
            val frag = ChangeInfoAlertDialogFragment()
            val args = Bundle()
            args.putBoolean("isUsername", isUsername)
            mViewModel = profileViewModel
            frag.arguments = args
            return frag
        }
    }
}