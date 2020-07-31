package com.example.covidnow.fragment.alert_dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.covidnow.R
import com.example.covidnow.viewmodels.ProfileViewModel

class CovidAlertDialogueFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString("title")
        val alertDialogBuilder = AlertDialog.Builder(activity, R.style.AlertDialogCustom)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage("This includes anyone who was at the same location on the same day as you. Are you sure?")
        alertDialogBuilder.setPositiveButton("OK") { dialog, which -> // on success
            // Trace who may have come in contact with infected user
            Toast.makeText(context, "Users will be notified", Toast.LENGTH_SHORT).show()
            mViewModel?.contactTracing()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which -> dialog?.dismiss() }
        return alertDialogBuilder.create()
    }

    companion object {
        private var mViewModel: ProfileViewModel? = null
        fun newInstance(title: String?, profileViewModel: ProfileViewModel?): CovidAlertDialogueFragment {
            val frag = CovidAlertDialogueFragment()
            val args = Bundle()
            args.putString("title", title)
            mViewModel = profileViewModel
            frag.arguments = args
            return frag
        }
    }
}