package com.hm.admanagerx.adsanalysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.admanagerx.R

class AdAnalyticsTrackerAdapter(
    private val trackerList: ArrayList<AdAnalyticsTracker>,
) : RecyclerView.Adapter<AdAnalyticsTrackerAdapter.TrackerViewHolder>() {

    class TrackerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trackerName: TextView = itemView.findViewById(R.id.tvTrackerName)
        val trackerDetails: TextView = itemView.findViewById(R.id.tvTrackerDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ad_tracker, parent, false)
        return TrackerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
        val currentTracker = trackerList[position]
        holder.trackerName.text = currentTracker.adsTitle
        holder.trackerDetails.text = currentTracker.totalRequests.toString()
    }

    override fun getItemCount() = trackerList.size
}
