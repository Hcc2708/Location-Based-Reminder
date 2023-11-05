package com.example.locbasedreminder

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import java.util.concurrent.TimeUnit

class PlacesAutocompleteAdapter(context: Context, private val placesClient: PlacesClient) :
    ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_dropdown_item_1line) {

    private val token = AutocompleteSessionToken.newInstance()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                val predictions = if (constraint != null) {
                    getAutocomplete(constraint.toString())
                } else {
                    emptyList()
                }

                results.values = predictions
                results.count = predictions.size

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    clear()
                    addAll(results.values as List<AutocompletePrediction>)
                    notifyDataSetChanged()
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null) ?: ""
                } else {
                    super.convertResultToString(resultValue)
                }
            }
        }
    }

    private fun getAutocomplete(query: String): List<AutocompletePrediction> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        try {
            val response: FindAutocompletePredictionsResponse = placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    // Handle successful response
                }
                .addOnFailureListener { exception ->
                    Log.e("PlacesAutocompleteAdapter", "Error getting autocomplete predictions: ${exception.localizedMessage}")
                }
                .getResult()

            return if (response.autocompletePredictions.isNotEmpty()) {
                response.autocompletePredictions
            } else {
                emptyList()
            }
        } catch (e: ApiException) {
            Log.e("PlacesAutocompleteAdapter", "Error getting autocomplete predictions: ${e.localizedMessage}")
            return emptyList()
        }
    }
}
