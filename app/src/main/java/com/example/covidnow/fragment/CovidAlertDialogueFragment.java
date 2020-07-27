package com.example.covidnow.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.covidnow.R;
import com.example.covidnow.viewmodels.ProfileViewModel;

import org.parceler.Parcels;

public class CovidAlertDialogueFragment extends DialogFragment {
    private static ProfileViewModel mViewModel;

    public CovidAlertDialogueFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CovidAlertDialogueFragment newInstance(String title, ProfileViewModel profileViewModel) {
        CovidAlertDialogueFragment frag = new CovidAlertDialogueFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        mViewModel = profileViewModel;
        frag.setArguments(args);
        return frag;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage("This includes anyone who was at the same location on the same day as you. Are you sure?");
        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                // Trace who may have come in contact with infected user
                Toast.makeText(getContext(), "Users will be notified", Toast.LENGTH_SHORT).show();
                mViewModel.contactTracing();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO check if showing?
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        });

        return alertDialogBuilder.create();
    }
}

