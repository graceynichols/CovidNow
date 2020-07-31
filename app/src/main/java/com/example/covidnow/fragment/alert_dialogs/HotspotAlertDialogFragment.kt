package com.example.covidnow.fragment.alert_dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.covidnow.R
import com.example.covidnow.models.Location
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Use the [HotspotAlertDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HotspotAlertDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString("title")
        val location = Parcels.unwrap<Location>(arguments?.getParcelable("location"))
        val alertDialogBuilder = AlertDialog.Builder(activity, R.style.AlertDialogCustom)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage("Your current location had been marked as a hotspot, is it still a hotspot?")
        alertDialogBuilder.setPositiveButton("Yes") { dialog, which -> // on success
            // Mark this location as hotspot (so updated at is current)
            Log.i(TAG, "Marking location " + location.placeId + " as hotspot")
            location.isHotspot = true
            location.saveInBackground()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, which ->
            Log.i(TAG, "Marking location " + location.placeId + " as not a hotspot")
            location.isHotspot = false
            location.saveInBackground()
        }
        alertDialogBuilder.setNeutralButton("Cancel") { dialog, which -> dialog?.dismiss()}
        return alertDialogBuilder.create()
    }

    companion object {
        const val TAG = "HotspotAlertDialog"
        fun newInstance(title: String?, location: Location): HotspotAlertDialogFragment {
            val frag = HotspotAlertDialogFragment()
            val args = Bundle()
            args.putParcelable("location", Parcels.wrap(location))
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }
}