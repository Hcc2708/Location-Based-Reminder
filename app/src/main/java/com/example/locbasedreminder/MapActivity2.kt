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
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapActivity2 : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val MAP_REQUEST_CODE = 123
    private val EXTRA_CHOSEN_LOCATION = "chosen_location"
    private var longitude:Double = 0.0
    private var latitude:Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private var chosenLocation: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map2)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            // Pass the chosen location back to the calling activity
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_CHOSEN_LOCATION, chosenLocation)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set a default location (e.g., your city or any location you prefer)
        val defaultLocation = LatLng(latitude, longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        // Set a marker at the default location
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("You are here"))

        if(isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mFusedLocationClient.lastLocation?.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

                        // Set a marker at the default location (current location)
                        mMap.addMarker(
                            MarkerOptions().position(currentLocation).title("You are here")
                        )
                    }
                }
            } else {
                // Handle the case when location permission is not granted
                // You may request permission here or handle it in another way
            }
        }
        else {
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        // Set a click listener for the map
        mMap.setOnMapClickListener { latLng ->
            // When the user clicks on the map, send back the chosen location
            mMap.clear()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mFusedLocationClient.lastLocation?.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("You are here"))
                    }
                }
            }
            // Add a marker at the clicked location
            mMap.addMarker(MarkerOptions().position(latLng).title("Chosen Location"))

            // Save the chosen location
            chosenLocation = latLng

        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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
//    Make sure to define MAP_REQUEST_CODE and EXTRA_CHOSEN_LOCATION as constants in your code.

}
