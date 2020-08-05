package com.example.covidnow.adapter

import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.covidnow.R
import com.example.covidnow.activity.LoginActivity
import com.example.covidnow.fragment.ProfileFragment
import com.example.covidnow.models.Location
import com.example.covidnow.repository.ParseRepository
import com.google.android.material.snackbar.Snackbar
import com.parse.GetCallback
import com.parse.ParseUser
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class HistoryAdapter(private val fragment: Fragment, private val exposures: MutableList<JSONObject>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    public val context = fragment.context
    private var mRecentlyDeletedItem: JSONObject? = null
    private var mRecentlyDeletedItemPosition: Int? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Display each exposure's location and date
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(element: JSONObject) {
            Log.i(TAG, "Current date " + Calendar.getInstance().time.toString())
            // Search for place name in Parse
            ParseRepository().searchPlace(element.getString(Location.KEY_PLACE_ID), GetCallback { `object`, e ->
                if (`object` == null) {
                    // no location saved, this shouldn't ever happen
                    Log.i(TAG, "Location in history not previously saved")
                    Log.i(TAG, e.toString())
                } else {
                    // The location was saved in parse
                    if (`object`.name != null) {
                        tvName.text = `object`.name
                    } else {
                        // No name saved, just show address
                        tvName.text = `object`.address
                    }

                }
            })
            // TODO make this a better formatted date
            tvDate.text = getRelativeTimeAgo(ParseRepository.jsonObjectToDate(element))
            //tvDate.text = getRelativeTimeAgo(element.getJSONObject("date").getString("iso"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(fragment.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = exposures[position]
        holder.bind(element)
    }

    override fun getItemCount(): Int {
        return exposures.size
    }

    fun addAll(newPlaces: Collection<JSONObject>) {
        exposures.addAll(newPlaces)
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        mRecentlyDeletedItem = exposures[position];
        mRecentlyDeletedItemPosition = position;
        exposures.removeAt(position);
        notifyItemRemoved(position);
        showUndoSnackbar();

    }

    private fun showUndoSnackbar() {
        val myOnClickListener = View.OnClickListener {
            // User wants to undo delete
            undoDelete()
        }
        // Make the snackbar
        fragment.view?.let {
            Snackbar.make(it, "Undo?", Snackbar.LENGTH_SHORT)
                // Only get logged out if they click yes
                .setAction("Yes", myOnClickListener)
                .show()
        }
    }

    private fun undoDelete() {
        mRecentlyDeletedItemPosition?.let {
            mRecentlyDeletedItem?.let { it1 ->
                exposures.add(it,
                        it1)
            }
        }
        mRecentlyDeletedItemPosition?.let { notifyItemInserted(it) }
    }

    companion object {
        private const val TAG = "HistoryAdapter"

        @JvmStatic
        fun getRelativeTimeAgo(date: Date): String {
            // Turn Date into relative time format
            val format = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"
            val sf = SimpleDateFormat(format, Locale.ENGLISH)
            sf.isLenient = true
            var relativeDate = ""
            val dateMillis = date.time
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString()
            return relativeDate
        }
    }
}
