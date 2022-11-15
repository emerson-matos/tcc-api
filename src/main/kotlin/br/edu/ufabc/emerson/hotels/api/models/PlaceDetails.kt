package br.edu.ufabc.emerson.hotels.api.models

import com.google.maps.model.PlaceDetails
import com.google.maps.model.PlacesSearchResult
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class PlaceDetailsInternal(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val uuid: UUID?,
    val placeDetails: PlaceDetails,
) {
}

@Entity
data class PlacesSearchResultInternal(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val uuid: UUID?,
    val placeSearchResult: PlacesSearchResult,
) {
}