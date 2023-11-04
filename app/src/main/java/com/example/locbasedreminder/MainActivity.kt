package com.example.locbasedreminder
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
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
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var reminderAdapter: ArrayAdapter<Reminder>
    lateinit var remindersListView : ListView
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private  val LOCATION_PERMISSION_REQUEST_CODE = 42
    private val MAP_REQUEST_CODE = 123
    private val EXTRA_CHOSEN_LOCATION = "chosen_location"
    lateinit var reminderLocation:Location
    lateinit var taskAtLocation:String
    lateinit var reminders:MutableList<Reminder>
    lateinit var Database:ReminderDatabaseHelper
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        remindersListView = findViewById(R.id.remindersListView)
        val pickonmap = findViewById<FloatingActionButton>(R.id.pickonmap)
        Database = ReminderDatabaseHelper(this)
         reminders = Database.getReminders()
//        reminderAdapter = ReminderAdapter(this, reminders)
        reminderAdapter = ReminderAdapter(this, reminders) { position ->
            val reminder = reminders[position]
            Database.removeReminderByTask(reminder.task)
            reminders.removeAt(position)
            reminderAdapter.notifyDataSetChanged()
        }

        remindersListView.adapter = reminderAdapter

        pickonmap.setOnClickListener {
            if(isLocationEnabled()) {
                startActivityForResult(Intent(this, MapActivity2::class.java), MAP_REQUEST_CODE)
            }
            else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }


    }


    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            // Handle location updates
            locationResult.lastLocation?.let { isUserNearReminderLocation(it) }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

//    private fun onLocationChanged(location: Location) {
//        if (isUserNearReminderLocation(location)) {
//            showReminderNotification()
//        }
//    }
    fun setReminderLocation1(newLocation: Location, task: String) {

        Database.addReminder(newLocation.latitude, newLocation.longitude, task)
    }
    private fun isUserNearReminderLocation(location: Location): Boolean {
        var reminderShown = false

        for ((reminderLocation, task) in reminders) {
            val proximityRadius = 10.0
            val reminderLatLng = Location("").apply {
                latitude = reminderLocation.latitude
                longitude = reminderLocation.longitude
            }
            if (location.distanceTo(reminderLatLng) <= proximityRadius) {
                 showReminderNotification(task)
            }
        }

        return reminderShown
    }

    private fun onMapLocationPicked(chosenLocation: LatLng, task:String) {
        val chosenReminderLocation = Location("").apply {
            latitude = chosenLocation.latitude
            longitude = chosenLocation.longitude
        }
        Database.addReminder(chosenLocation.latitude, chosenLocation.longitude, task)
        val newReminder = Reminder(chosenLocation, task)

        reminderAdapter.add(newReminder)
        reminderAdapter.notifyDataSetChanged()
        setReminderLocation1(chosenReminderLocation, task)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val chosenLocation = data?.getParcelableExtra<LatLng>(EXTRA_CHOSEN_LOCATION)
            val task = data?.getStringExtra("task")
            if (chosenLocation != null && task != null) {
                onMapLocationPicked(chosenLocation, task)
            }
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
    private fun showReminderNotification(task: String) {
        Toast.makeText(this, task, Toast.LENGTH_LONG).show()
    }

}


