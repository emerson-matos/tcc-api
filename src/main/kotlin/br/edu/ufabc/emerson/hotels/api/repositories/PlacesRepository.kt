package br.edu.ufabc.emerson.hotels.api.repositories

import br.edu.ufabc.emerson.hotels.api.models.PlaceDetailsInternal
import br.edu.ufabc.emerson.hotels.api.models.PlacesSearchResultInternal
import com.google.maps.model.PlaceDetails
import com.google.maps.model.PlacesSearchResult
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface PlacesDetailsRepository : PagingAndSortingRepository<PlaceDetailsInternal, UUID>

interface PlacesSearchResultRepository : PagingAndSortingRepository<PlacesSearchResultInternal, UUID>