package com.example.donorlk.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.adapters.ReservationAdapter
import com.example.donorlk.models.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyReservationsFragment : Fragment() {

    private lateinit var adapter: ReservationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = ReservationAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Load user's reservations from Firebase
        loadUserReservations()
    }

    private fun loadUserReservations() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("reservations")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val reservations = mutableListOf<Reservation>()

                for (document in documents) {
                    val reservation = Reservation(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        centerId = document.getString("centerId") ?: "",
                        centerName = document.getString("centerName") ?: "",
                        reservationDate = document.getString("reservationDate") ?: "",
                        reservationTime = document.getString("reservationTime") ?: "",
                        notes = document.getString("notes") ?: "",
                        status = document.getString("status") ?: "pending",
                        createdAt = document.getTimestamp("createdAt")
                    )
                    reservations.add(reservation)
                }

                // Sort reservations by date (most recent first) in the app
                reservations.sortByDescending { it.createdAt?.toDate() }

                adapter.updateReservations(reservations)

                if (reservations.isEmpty()) {
                    Toast.makeText(requireContext(), "No reservations found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Log the actual error for debugging
                android.util.Log.e("MyReservationsFragment", "Error loading reservations", e)
                Toast.makeText(requireContext(), "Error loading reservations: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
