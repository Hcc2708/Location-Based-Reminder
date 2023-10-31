package com.example.locbasedreminder
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

//import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var reminderAdapter: ArrayAdapter<Reminder>
    lateinit var remindersListView : ListView
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private  val LOCATION_PERMISSION_REQUEST_CODE = 42
    private val MAP_REQUEST_CODE = 123
    private val EXTRA_CHOSEN_LOCATION = "chosen_location"
    lateinit var reminderLocation:Location
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val reminders = mutableListOf<Reminder>()
        remindersListView = findViewById(R.id.remindersListView)
        val openMapButton = findViewById<Button>(R.id.mapbutton1)
        val pickonmap = findViewById<Button>(R.id.pickonmap)
        reminders.add(Reminder("Meeting", 37.7749, -122.4194))
        reminders.add(Reminder("Grocery Shopping", 37.7749, -122.4324))

        reminderAdapter = ReminderAdapter(this, reminders)
        remindersListView.adapter = reminderAdapter

        // Handle adding new reminders and opening the map
//        addButton.setOnClickListener {
//            // TODO: Implement logic to add new reminders
//        }
        pickonmap.setOnClickListener {
            // Start the MapActivity to pick a location
            startActivityForResult(Intent(this, MapActivity2::class.java), MAP_REQUEST_CODE)
        }
        openMapButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, start location updates
            startLocationUpdates()
        }


    }


    // Initialize the LocationRequest
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Create a LocationCallback
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            // Handle location updates
            locationResult.lastLocation?.let { onLocationChanged(it) }
        }
    }

    // Start location updates
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Handle location changes
    private fun onLocationChanged(location: Location) {
        // Check if the user is near the predefined location and trigger a reminder
        if (isUserNearReminderLocation(location)) {
            // Show a reminder notification or perform the desired action
            showReminderNotification()
        }
    }
    fun setReminderLocation1(newLocation: Location) {
        reminderLocation = newLocation
    }
    // Check if the user is near the predefined location
    private fun isUserNearReminderLocation(location: Location): Boolean {
        // Replace with the coordinates of your reminder location
        return if (::reminderLocation.isInitialized) {
            // Set the radius for proximity (in meters)
            val proximityRadius = 10.0

            // Check if the user is within the specified radius of the reminder location
            location.distanceTo(reminderLocation) <= proximityRadius
        } else {
            false
        }
    }

    private fun onMapLocationPicked(chosenLocation: LatLng) {
        // Create a Location object from the chosen LatLng
        val chosenReminderLocation = Location("").apply {
            latitude = chosenLocation.latitude
            longitude = chosenLocation.longitude
        }

        // Set the chosen location as the reminder location
        setReminderLocation1(chosenReminderLocation)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get the chosen location from the MapActivity
            val chosenLocation = data?.getParcelableExtra<LatLng>(EXTRA_CHOSEN_LOCATION)

            // Use the chosen location as needed
            if (chosenLocation != null) {
                onMapLocationPicked(chosenLocation)
            }
        }
    }
    // Show a reminder notification
    private fun showReminderNotification() {
        // Implement the logic to show a notification
        Toast.makeText(this, "You have task at this destination", Toast.LENGTH_LONG).show()
    }

}


