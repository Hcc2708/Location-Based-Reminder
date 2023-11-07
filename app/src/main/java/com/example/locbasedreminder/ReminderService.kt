package com.example.locbasedreminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.locbasedreminder.MainActivity
import com.example.locbasedreminder.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class ReminderService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "ReminderNotificationChannel"
    private var NOTIFICATION_ID = 1
    private var task:String = "Task Reminder"
    private lateinit var  fusedLocationClient:FusedLocationProviderClient
    val Database = ReminderDatabaseHelper(this)
    lateinit var  reminders:MutableList<Reminder>
    override fun onCreate() {
        super.onCreate()
         fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ReminderService)
        reminders = Database.getReminders()
        // Create notification channel (for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Logic for monitoring location and triggering reminders

        // Start the service as a foreground service
        startForeground(NOTIFICATION_ID, createNotification(task))
        startLocationUpdates()
//        stopSelf()
        return START_STICKY
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

    private val shownNotifications = HashSet<String>()

    private fun isUserNearReminderLocation(location: Location) {
        for ((reminderLocation, task) in Database.getReminders()) {
            val proximityRadius = 10.0
            val reminderLatLng = Location("").apply {
                latitude = reminderLocation.latitude
                longitude = reminderLocation.longitude
            }
            if (location.distanceTo(reminderLatLng) <= proximityRadius && !shownNotifications.contains(task)) {
                showReminderNotification(task)
                shownNotifications.add(task)
            } else if (location.distanceTo(reminderLatLng) > proximityRadius && shownNotifications.contains(task)) {
                // If the user is no longer in the proximity, remove the task from the shown notifications set
                shownNotifications.remove(task)
            }
        }
    }
    private var notificationIdCounter = 0
    private fun showReminderNotification(task: String) {
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent =
//            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setContentTitle("Reminder Notification")
//            .setContentText(task)
//            .setSmallIcon(R.drawable.baseline_edit_notifications_24)
//            .setContentIntent(pendingIntent)
//            .setSound(notificationSoundUri)  // Set notification sound
//
//        val notification = notificationBuilder.build()
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Use a unique notification ID to ensure each notification is shown
//        val notificationId = notificationIdCounter++
//        notificationManager.notify(notificationId, notification)
//        Toast.makeText(this, task, Toast.LENGTH_SHORT).show()
           updateNotification(task)
    }
    private fun updateNotification(task: String) {

            if(task != null)
            startForeground(NOTIFICATION_ID, createNotification(task))

    }
    private fun createNotification(task: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val stopServiceIntent = Intent(this, StopServiceReceiver::class.java) // Create a new Intent for stopping the service
        val stopServicePendingIntent = PendingIntent.getBroadcast(this, 0, stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Reminder Service")
            .setContentText(task)
            .setSmallIcon(R.drawable.baseline_edit_notifications_24)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.baseline_stop_circle_24, "Stop Service", stopServicePendingIntent) // Add stop action to the notification
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop any ongoing tasks or cleanup
    }
}
