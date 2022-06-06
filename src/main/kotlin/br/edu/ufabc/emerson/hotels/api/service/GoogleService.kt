package br.edu.ufabc.emerson.hotels.api.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.FindPlaceFromTextRequest
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.internal.ApiResponse
import com.google.maps.model.FindPlaceFromText
import org.springframework.stereotype.Service

@Service
class GoogleService {
    fun start(): String? {
        val context = GeoApiContext.Builder()
            .apiKey("AIza...")
            .build()
        context.use {
            val results: FindPlaceFromText = PlacesApi.findPlaceFromText(
                it,
                "1600 Amphitheatre Parkway Mountain View, CA 94043",
                FindPlaceFromTextRequest.InputType.TEXT_QUERY
            ).await()
            val gson: Gson = GsonBuilder().setPrettyPrinting().create()
            println(gson.toJson(results.candidates))

            return@start gson.toJson(results.candidates)
        }
    }
}