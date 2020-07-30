package com.example.covidnow.helpers

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class PermissionsRequestHelper(activity: Activity) {
    private val REQUEST_CODE_LOCATION = 100

    private var activity: Activity? = null

    fun requestPermissions() {
        val contextProvider = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissionFineLocation) }
        if (contextProvider != null) {
            if (contextProvider) {
                Toast.makeText(activity?.applicationContext, "Permission is required to obtain location", Toast.LENGTH_SHORT).show()
            }
            permissionRequest()
        }
    }

    fun validatePermissionsLocation():Boolean{
        val fineLocationAvailable = activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, PermissionsRequestHelper.permissionFineLocation) } == PackageManager.PERMISSION_GRANTED
        val coarseLocationAvailable = activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, PermissionsRequestHelper.permissionCoarseLocation) } == PackageManager.PERMISSION_GRANTED
        return fineLocationAvailable && coarseLocationAvailable
    }

    fun validatePermissionsBackground():Boolean{
        return activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, PermissionsRequestHelper.permissionBackgroundLocation) } == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionRequest() {
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permissionFineLocation, permissionCoarseLocation, permissionBackgroundLocation), REQUEST_CODE_LOCATION) }
    }

    companion object {
        const val permissionFineLocation= Manifest.permission.ACCESS_FINE_LOCATION
        const val permissionBackgroundLocation= Manifest.permission.ACCESS_BACKGROUND_LOCATION
        const val permissionCoarseLocation= Manifest.permission.ACCESS_COARSE_LOCATION
    }
}

