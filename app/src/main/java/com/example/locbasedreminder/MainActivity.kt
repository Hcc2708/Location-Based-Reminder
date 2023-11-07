package com.example.locbasedreminder
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
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
    var enable = true
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val toolbar:Toolbar?  = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
        val option = findViewById<FloatingActionButton>(R.id.options)
        val pickonmap = findViewById<FloatingActionButton>(R.id.pickonmap)
        val searchOnMap = findViewById<FloatingActionButton>(R.id.searchOnMap)
        val openMap = findViewById<FloatingActionButton>(R.id.openMap)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        startForegroundService(Intent(this, ReminderService::class.java))
        startForegroundService(Intent(this, ReminderService::class.java))
        remindersListView = findViewById(R.id.remindersListView)

        Database = ReminderDatabaseHelper(this)
        reminders = Database.getReminders()
        reminderAdapter = ReminderAdapter(this, reminders) { position ->
            val reminder = reminders[position]
            Log.d("ReminderAdapter", "Before removal - Size: ${reminders.size}")
            Database.removeReminderByTask(reminder.task)
            reminders.removeAt(position)
            Log.d("ReminderAdapter", "After removal - Size: ${reminders.size}")
////            stopService(Intent(this, ReminderService::class.java))
//            startForegroundService(Intent(this, ReminderService::class.java))
            reminderAdapter.notifyDataSetChanged()
        }

        remindersListView.adapter = reminderAdapter

        remindersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedItem = reminders[position].toString() // Get the selected item
            // Handle the item click, e.g., open a new activity or show details
            Toast.makeText(this, "You clicked: $selectedItem", Toast.LENGTH_SHORT).show()
        }



        option.setOnClickListener{
            if(enable){
                option.setImageResource(R.drawable.baseline_add_task_24)
                pickonmap.show()
                searchOnMap.show()
                openMap.show()
                enable = false
            }
            else {
                option.setImageResource(R.drawable.baseline_playlist_add_circle_24)
                pickonmap.hide()
                searchOnMap.hide()
                openMap.hide()
                enable = true
            }
        }

        pickonmap.setOnClickListener {
            if(isLocationEnabled()) {
                startActivityForResult(Intent(this, MapActivity2::class.java), MAP_REQUEST_CODE)
            }
            else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                if(isLocationEnabled()) {
                    startActivityForResult(Intent(this, MapActivity2::class.java), MAP_REQUEST_CODE)
                }
            }
        }
        searchOnMap.setOnClickListener {
            if(isLocationEnabled()) {
                startActivityForResult(Intent(this, SearchViaGeocoder::class.java), MAP_REQUEST_CODE)
            }
            else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                if(isLocationEnabled()) {
                    startActivityForResult(Intent(this, SearchViaGeocoder::class.java), MAP_REQUEST_CODE)

                }
            }
        }
        openMap.setOnClickListener{
            if(isLocationEnabled()){
//                val uri  = Uri.parse("geo:")
                val mapIntent = Intent()
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
            else{
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                if(isLocationEnabled()) {
                    val mapIntent = Intent()
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onMapLocationPicked(chosenLocation: LatLng, task:String) {
        val chosenReminderLocation = Location("").apply {
            latitude = chosenLocation.latitude
            longitude = chosenLocation.longitude
        }
        Database.addReminder(chosenLocation.latitude, chosenLocation.longitude, task)
////        stopService(Intent(this, ReminderService::class.java))
//        startForegroundService(Intent(this, ReminderService::class.java))
        val newReminder = Reminder(chosenLocation, task)

        reminderAdapter.add(newReminder)
        reminderAdapter.notifyDataSetChanged()
    }
    @RequiresApi(Build.VERSION_CODES.O)
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
}


