package com.example.donorlk.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.model.DonationModel

class DonationAdapter(private val donations: List<DonationModel>) :
    RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    class DonationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.donationTitle)
        val dateTextView: TextView = view.findViewById(R.id.donationDate)
        val placeTextView: TextView = view.findViewById(R.id.donationPlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.donation_card, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donations[position]
        holder.titleTextView.text = donation.title
        holder.dateTextView.text = donation.date
        holder.placeTextView.text = donation.place
    }

    override fun getItemCount() = donations.size
}
