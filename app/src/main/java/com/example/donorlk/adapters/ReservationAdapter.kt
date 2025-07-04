package com.example.donorlk.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.Reservation

class ReservationAdapter : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    private val reservations = mutableListOf<Reservation>()

    class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        private val placeTextView: TextView = view.findViewById(R.id.placeTextView)

        fun bind(reservation: Reservation) {
            timeTextView.text = reservation.time
            dateTextView.text = reservation.date
            placeTextView.text = reservation.place
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation_card, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    override fun getItemCount() = reservations.size

    fun updateReservations(newReservations: List<Reservation>) {
        reservations.clear()
        reservations.addAll(newReservations)
        notifyDataSetChanged()
    }
}
