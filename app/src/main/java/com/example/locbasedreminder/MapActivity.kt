package com.example.locbasedreminder

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Sample marker for each reminder
//        val reminders = // TODO: Retrieve your list of reminders

                val location = LatLng(31.255992, 75.70514349999999)
                mMap.addMarker(MarkerOptions().position(location).title("Lovely"))

        // Move camera to the first reminder (if available)
//        if (reminders.isNotEmpty()) {
//            val firstReminder = reminders[0]
//            val firstLocation = LatLng(firstReminder.latitude, firstReminder.longitude)
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(firstLocation))
//        }
    }
}
