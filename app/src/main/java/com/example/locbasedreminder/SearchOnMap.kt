package com.example.locbasedreminder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class SearchOnMap : AppCompatActivity() {

    private lateinit var searchAutoCompleteTextView: AutoCompleteTextView
    private lateinit var placesClient: PlacesClient

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_on_map)

        // Initialize Places API
        Places.initialize(applicationContext, "AIzaSyAiPxiLmZ6o-OTpTm_Gx0tkn-3nRTT1gnA")
        placesClient = Places.createClient(this)

        // Initialize AutoCompleteTextView
        searchAutoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        val placesAutocompleteAdapter = PlacesAutocompleteAdapter(this, placesClient)
        searchAutoCompleteTextView.setAdapter(placesAutocompleteAdapter)

        // Handle item click in the AutoCompleteTextView
        searchAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val prediction = placesAutocompleteAdapter.getItem(position)
            prediction?.let { handlePlaceSelection(it) }
        }
    }

    private fun handlePlaceSelection(prediction: AutocompletePrediction) {
        val placeId = prediction.placeId

        val placeRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build()
        placesClient.fetchPlace(placeRequest)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng

                // Open MapActivity and pass the LatLng
                val mapIntent = Intent(this, MapActivity2::class.java)
                mapIntent.putExtra("chosen_location", latLng)
                startActivity(mapIntent)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
