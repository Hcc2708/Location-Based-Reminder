package com.example.locbasedreminder

import com.google.android.gms.maps.model.LatLng

data class Reminder(val location: LatLng, val task: String)

