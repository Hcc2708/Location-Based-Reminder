package com.example.locbasedreminder

// MapActivity.kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View

import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class ReminderLocation : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    lateinit var inflater:LayoutInflater


    var receivedValue:LatLng? = null
    lateinit var receivedTask:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reminder_location)
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


//

        receivedValue = intent.getParcelableExtra("chosen_location1")
        receivedTask=intent.getStringExtra("task").toString()


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient.lastLocation?.addOnSuccessListener { location: Location? ->
                if(receivedValue == null) {
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

                        mMap.addMarker(
                            MarkerOptions().position(currentLocation).title(receivedTask).visible(true)
                        )
                    }
                }
                else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(receivedValue, 15f))

                    mMap.addMarker(
                        MarkerOptions().position(receivedValue).title(receivedTask)
                    )

                }
            }
        } else {

        }

//      setLocation(googleMap)



    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED)) {
            }
        }
    }

}
