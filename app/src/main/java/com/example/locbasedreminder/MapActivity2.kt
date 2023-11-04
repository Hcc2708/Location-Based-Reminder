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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
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
    private val TASK_AT_THE_LOCATION = "task"
    private var longitude:Double = 0.0
    private var latitude:Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private var chosenLocation: LatLng? = null
    private var yourTask:String = ""
    lateinit var inflater:LayoutInflater
    lateinit var popupView:View
    lateinit var taskEditText:EditText
    lateinit var saveButton:Button
    lateinit var popupWindow:PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map2)
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.ltask_input_dialog, null)

        taskEditText = popupView.findViewById(R.id.taskEditText)
        saveButton = popupView.findViewById(R.id.saveButton)

        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        saveButton.setOnClickListener {
            val task = taskEditText.text.toString()
            if (task.isNotEmpty()) {
                // Handle the task input, e.g., save it along with the location
                yourTask = task
                popupWindow.dismiss()
            } else {
                // Notify the user that the task cannot be empty
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_CHOSEN_LOCATION, chosenLocation)
            resultIntent.putExtra(TASK_AT_THE_LOCATION, yourTask)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set a default location (e.g., your city or any location you prefer)
//        val defaultLocation = LatLng(latitude, longitude)
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
//
//        // Set a marker at the default location
//        mMap.addMarker(MarkerOptions().position(defaultLocation).title("You are here"))

        if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mFusedLocationClient.lastLocation?.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

                        mMap.addMarker(
                            MarkerOptions().position(currentLocation).title("You are here")
                        )
                    }
                }
            } else {

            }


        mMap.setOnMapClickListener { latLng ->
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
            mMap.addMarker(MarkerOptions().position(latLng).title("Chosen Location"))
//            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            val markerScreenPosition = mMap.projection.toScreenLocation(latLng)

            // Set the pop-up window position relative to the marker
            val xOffset = -popupView.width / 2
            val yOffset = -popupView.height - 50  // Adjust this value based on your preference

            popupWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, markerScreenPosition.x + xOffset, markerScreenPosition.y + yOffset)
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

}
