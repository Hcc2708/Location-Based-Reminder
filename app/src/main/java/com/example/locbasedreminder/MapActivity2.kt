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
import android.util.Log
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
    private val EXTRA_CHOSEN_LOCATION = "chosen_location"
    private val TASK_AT_THE_LOCATION = "task"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private var chosenLocation: LatLng? = null
    private var yourTask:String = ""
    lateinit var inflater:LayoutInflater
    lateinit var popupView:View
    lateinit var taskEditText:EditText
    lateinit var saveButton:Button
    lateinit var popupWindow:PopupWindow
     var receivedValue:LatLng? = null
     var  xOffset:Int = 0
     var  yOffset:Int = 0
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
        xOffset = -popupView.width / 2
        yOffset = -popupView.height - 50
        receivedValue = intent.getParcelableExtra("chosen_location1")

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        saveButton.setOnClickListener {
            val task = taskEditText.text.toString()
            if (task.isNotEmpty()) {
                yourTask = task
                popupWindow.dismiss()
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_CHOSEN_LOCATION, chosenLocation)
                resultIntent.putExtra(TASK_AT_THE_LOCATION, yourTask)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }

        }


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
                                MarkerOptions().position(currentLocation).title("You are here")
                            )
                        }
                    }
                    else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(receivedValue, 15f))

                        mMap.addMarker(
                            MarkerOptions().position(receivedValue).title("You are here")
                        )
                        val markerScreenPosition = mMap.projection.toScreenLocation(receivedValue)


                        popupWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, markerScreenPosition.x + xOffset, markerScreenPosition.y + yOffset)
                        chosenLocation = receivedValue
                    }
                }
            } else {

            }

            setLocation(googleMap)



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
    fun setLocation(googleMap: GoogleMap){
        mMap = googleMap

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            if(receivedValue == null)
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

            val markerScreenPosition = mMap.projection.toScreenLocation(latLng)



            popupWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, markerScreenPosition.x + xOffset, markerScreenPosition.y + yOffset)
            chosenLocation = latLng

        }
    }

}
