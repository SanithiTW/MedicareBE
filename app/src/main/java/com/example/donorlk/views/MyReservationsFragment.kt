package com.example.donorlk.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.adapters.ReservationAdapter
import com.example.donorlk.models.Reservation

class MyReservationsFragment : Fragment() {

    private lateinit var adapter: ReservationAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_reservations, container, false)
        recyclerView = view.findViewById(R.id.reservationsRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReservationAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Load dummy data
        loadDummyReservations()
    }

    private fun loadDummyReservations() {
        val dummyReservations = listOf(
            Reservation(
                time = "19:00 AM",
                date = "July 5, 2025",
                place = "Central Blood Bank"
            ),
            Reservation(
                time = "02:30 PM",
                date = "July 10, 2025",
                place = "City Hospital"
            ),
            Reservation(
                time = "11:15 AM",
                date = "July 15, 2025",
                place = "Community Health Center"
            ),
            Reservation(
                time = "04:00 PM",
                date = "July 20, 2025",
                place = "Regional Medical Center"
            )
        )

        adapter.updateReservations(dummyReservations)
    }
}
