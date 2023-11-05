package com.example.locbasedreminder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

class SearchViaGeocoder : AppCompatActivity() {

    private lateinit var searchAutoCompleteTextView: AutoCompleteTextView
    private val MAP_REQUEST_CODE = 123
    private val EXTRA_CHOSEN_LOCATION = "chosen_location"
    private val TASK_AT_THE_LOCATION = "task"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_via_geocoder)
        val search1 = findViewById<Button>(R.id.search1)
        searchAutoCompleteTextView = findViewById(R.id.autoCompleteTextView)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        searchAutoCompleteTextView.setAdapter(adapter)

        val suggestedLocations = listOf("Location1", "Location2", "Location3")
        adapter.clear()
        adapter.addAll(suggestedLocations)
        adapter.notifyDataSetChanged()
        searchAutoCompleteTextView.setOnItemClickListener { _, _, _, _ ->
            handlePlaceSelection()
        }
        search1.setOnClickListener{
            handlePlaceSelection()
        }

    }

    private fun handlePlaceSelection() {
        val locationName = searchAutoCompleteTextView.text.toString()

        val geocoder = Geocoder(this)
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses?.get(0)
                    val latLng = address?.let { LatLng(address.latitude, address.longitude) }

                    // Open MapActivity and pass the LatLng
                    val mapIntent = Intent(this, MapActivity2::class.java)
                    mapIntent.putExtra("chosen_location1", latLng)
                   startActivityForResult(mapIntent, MAP_REQUEST_CODE)
                } else {
                    Toast.makeText(this, "No suggestion found for the location", Toast.LENGTH_SHORT).show()
                    searchAutoCompleteTextView.text.clear()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error while fetching location", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val chosenLocation = data?.getParcelableExtra<LatLng>(EXTRA_CHOSEN_LOCATION)
            val task = data?.getStringExtra("task")
            if (chosenLocation != null && task != null) {
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_CHOSEN_LOCATION, chosenLocation)
                resultIntent.putExtra(TASK_AT_THE_LOCATION, task)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
