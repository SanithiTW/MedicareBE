package com.example.donorlk.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.donorlk.R
import com.example.donorlk.controllers.MakeReservationActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class MakeReservationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var db: FirebaseFirestore
    private lateinit var makeReservationButton: MaterialButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedCenterId: String? = null
    private var selectedCenterName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_make_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        // Initialize views
        makeReservationButton = view.findViewById(R.id.makeReservationButton)

        // Setup map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Setup button click listener
        makeReservationButton.setOnClickListener {
            if (selectedCenterId != null && selectedCenterName != null) {
                val intent = Intent(requireContext(), MakeReservationActivity::class.java)
                intent.putExtra("centerId", selectedCenterId)
                intent.putExtra("centerName", selectedCenterName)
                startActivity(intent)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        // Check location permission and enable location features
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            googleMap.isMyLocationEnabled = true
            // Load centers after map is ready
            loadDonationCenters()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            // Still load centers even without permission (will show all)
            loadDonationCenters()
        }

        // Set marker click listener
        googleMap.setOnMarkerClickListener { marker ->
            selectedCenterId = marker.tag as? String
            selectedCenterName = marker.title

            // Show the reservation button
            makeReservationButton.visibility = View.VISIBLE
            makeReservationButton.text = "Make Reservation at ${marker.title}"

            // Zoom to selected marker
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(marker.position, 15f),
                1000,
                null
            )

            // Return false to show default info window
            false
        }

        // Hide button when map is clicked (not on marker)
        googleMap.setOnMapClickListener {
            makeReservationButton.visibility = View.GONE
            selectedCenterId = null
            selectedCenterName = null
        }
    }

    private fun loadDonationCenters() {
        // First get user's location, then load nearby centers
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = LatLng(location.latitude, location.longitude)
                        loadNearbyCenters(userLocation)
                    } else {
                        // If no location, load all centers
                        loadAllCenters()
                    }
                }
                .addOnFailureListener {
                    // If location fails, load all centers
                    loadAllCenters()
                }
        } else {
            // If no permission, load all centers
            loadAllCenters()
        }
    }

    private fun loadNearbyCenters(userLocation: LatLng) {
        db.collection("donation_centers")
            .get()
            .addOnSuccessListener { documents ->
                val allMarkers = mutableListOf<LatLng>()
                val nearbyMarkersFor25kmZoom = mutableListOf<LatLng>()
                val maxDistance = 25.0 // 25 km radius for zoom calculation
                var centersWithin25km = 0

                // Clear any existing markers first
                googleMap.clear()

                // Add ALL centers to the map
                for (document in documents) {
                    val centerName = document.getString("centerName")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    if (centerName != null && latitude != null && longitude != null) {
                        val centerLocation = LatLng(latitude, longitude)

                        // Calculate distance from user location
                        val distance = calculateDistance(userLocation, centerLocation)

                        // Add marker for ALL centers (not just nearby ones)
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(centerLocation)
                                .title(centerName)
                                .snippet("Distance: ${String.format("%.1f", distance)} km")
                        )

                        marker?.tag = document.id
                        allMarkers.add(centerLocation)

                        // Keep track of centers within 25km for zoom calculation
                        if (distance <= maxDistance) {
                            nearbyMarkersFor25kmZoom.add(centerLocation)
                            centersWithin25km++
                        }
                    }
                }

                // Always zoom to show 25km radius around user, regardless of markers
                zoomToShow25kmRadius(userLocation, nearbyMarkersFor25kmZoom)

                if (centersWithin25km > 0) {
                    Toast.makeText(requireContext(), "Showing all ${allMarkers.size} centers. $centersWithin25km centers within 25km", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Showing all ${allMarkers.size} centers. No centers within 25km of your location", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load donation centers: ${e.message}", Toast.LENGTH_LONG).show()
                // Fallback to show area around user location
                zoomToShow25kmRadius(userLocation, emptyList())
            }
    }

    private fun zoomToShow25kmRadius(userLocation: LatLng, nearbyMarkers: List<LatLng>) {
        // Create a bounding box that represents approximately 25km radius
        val radiusInDegrees = 0.225 // Approximately 25km in degrees (latitude/longitude)

        val bounds = LatLngBounds.Builder()

        // Add points around the user location to create a 25km radius view
        bounds.include(LatLng(userLocation.latitude + radiusInDegrees, userLocation.longitude + radiusInDegrees))
        bounds.include(LatLng(userLocation.latitude + radiusInDegrees, userLocation.longitude - radiusInDegrees))
        bounds.include(LatLng(userLocation.latitude - radiusInDegrees, userLocation.longitude + radiusInDegrees))
        bounds.include(LatLng(userLocation.latitude - radiusInDegrees, userLocation.longitude - radiusInDegrees))

        // Also include the nearby markers if any
        for (marker in nearbyMarkers) {
            bounds.include(marker)
        }

        // Include user location
        bounds.include(userLocation)

        try {
            val padding = 100 // Padding in pixels
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds.build(), padding),
                2000,
                null
            )
        } catch (e: Exception) {
            // Fallback to a standard zoom level that shows ~25km area
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(userLocation, 11f), // 11f shows roughly 25-30km area
                2000,
                null
            )
        }
    }

    private fun loadAllCenters() {
        db.collection("donation_centers")
            .get()
            .addOnSuccessListener { documents ->
                val markersList = mutableListOf<LatLng>()

                for (document in documents) {
                    val centerName = document.getString("centerName")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    if (centerName != null && latitude != null && longitude != null) {
                        val position = LatLng(latitude, longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(centerName)
                                .snippet("Tap to make a reservation")
                        )

                        marker?.tag = document.id
                        markersList.add(position)
                    }
                }

                if (markersList.isNotEmpty()) {
                    zoomToShowAllMarkers(markersList)
                    Toast.makeText(requireContext(), "${documents.size()} donation centers loaded", Toast.LENGTH_SHORT).show()
                } else {
                    val sriLankaCenter = LatLng(7.8731, 80.7718)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 8f))
                    Toast.makeText(requireContext(), "No donation centers found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load donation centers: ${e.message}", Toast.LENGTH_LONG).show()
                val sriLankaCenter = LatLng(7.8731, 80.7718)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 8f))
            }
    }

    // Calculate distance between two points in kilometers
    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return (results[0] / 1000).toDouble() // Convert meters to kilometers
    }

    private fun zoomToShowNearbyMarkers(markerPositions: List<LatLng>) {
        if (markerPositions.isEmpty()) return

        val builder = LatLngBounds.Builder()
        for (position in markerPositions) {
            builder.include(position)
        }

        val bounds = builder.build()
        val padding = 150 // More padding for nearby view

        try {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding),
                2000,
                null
            )
        } catch (e: Exception) {
            // Fallback to first marker with closer zoom
            if (markerPositions.isNotEmpty()) {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(markerPositions[0], 13f),
                    1500,
                    null
                )
            }
        }
    }

    private fun zoomToShowAllMarkers(markerPositions: List<LatLng>) {
        if (markerPositions.isEmpty()) return

        val builder = LatLngBounds.Builder()
        for (position in markerPositions) {
            builder.include(position)
        }

        val bounds = builder.build()
        val padding = 100 // Standard padding for all markers view

        try {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding),
                2000,
                null
            )
        } catch (e: Exception) {
            // Fallback to first marker with standard zoom
            if (markerPositions.isNotEmpty()) {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(markerPositions[0], 10f),
                    1500,
                    null
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        googleMap.isMyLocationEnabled = true

                        // Move to user's location
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    val userLatLng = LatLng(location.latitude, location.longitude)
                                    googleMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(userLatLng, 12f),
                                        2000,
                                        null
                                    )
                                    Toast.makeText(requireContext(), "Showing your current location", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } catch (se: SecurityException) {
                        Toast.makeText(requireContext(), "Location access denied", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Location permission denied. You can still view donation centers.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}