package com.example.covidnow.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.covidnow.R
import com.example.covidnow.activity.MainActivity

object Utils {
    private const val TAG = "Utils"
    const val KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested"
    const val KEY_LOCATION_UPDATES_RESULT = "location-update-result"
    const val CHANNEL_ID = "channel_01"
    fun setRequestingLocationUpdates(context: Context?, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply()
    }

    fun getRequestingLocationUpdates(context: Context?): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false)
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    @JvmStatic
    fun sendNotification(context: Context, notificationDetails: String) {
        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.putExtra("from_notification", true)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(context)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = makeNotification(context, notificationDetails, notificationPendingIntent)

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Get an instance of the Notification manager
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)

            // Channel ID
            builder.setChannelId(CHANNEL_ID)
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }

    private fun makeNotification(context: Context, notificationDetails: String, notificationPendingIntent: PendingIntent): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context)

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher) // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Warning")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent)
        return builder
    }

    /**
     * Returns te text for reporting about a list of  [Location] objects.
     *
     * @param locations List of [Location]s.
     */
    private fun getLocationResultText(context: Context, locations: List<Location>): String {
        if (locations.isEmpty()) {
            return ""
        }
        val sb = StringBuilder()
        for (location in locations) {
            sb.append("(")
            sb.append(location.latitude)
            sb.append(", ")
            sb.append(location.longitude)
            sb.append(")")
            sb.append("\n")
        }
        Log.i(TAG, sb.toString())
        return sb.toString()
    }

    @JvmStatic
    fun setLocationUpdatesResult(context: Context, locations: List<Location>) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, """
     Location Results:
     ${getLocationResultText(context, locations)}
     """.trimIndent())
                .apply()
    }

    @JvmStatic
    fun getLocationUpdatesResult(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "")
    }
}