package com.example.covidnow.adapter

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.covidnow.R
import com.example.covidnow.models.Location
import com.example.covidnow.repository.ParseRepository
import com.parse.GetCallback
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

internal class HistoryAdapter(private val fragment: Fragment, private val exposures: MutableList<JSONObject>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(element: JSONObject) {
            Log.i(TAG, "Current date " + Calendar.getInstance().time.toString())
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

            //tvDate.text = ParseRepository.jsonObjectToDate(element).toString()
            // TODO make this a better formatted date
            tvDate.text = element.getJSONObject("date").getString("iso")
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

    companion object {
        private const val TAG = "HistoryAdapter"

        @JvmStatic
        fun getRelativeTimeAgo(date: Date): String {
            // User twitter format date
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
