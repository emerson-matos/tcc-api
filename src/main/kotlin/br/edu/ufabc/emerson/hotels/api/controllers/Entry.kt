package br.edu.ufabc.emerson.hotels.api.controllers

import br.edu.ufabc.emerson.hotels.api.models.PlaceDetailsInternal
import br.edu.ufabc.emerson.hotels.api.models.PlacesSearchResultInternal
import br.edu.ufabc.emerson.hotels.api.models.Pokemon
import br.edu.ufabc.emerson.hotels.api.repositories.PlacesDetailsRepository
import br.edu.ufabc.emerson.hotels.api.repositories.PlacesSearchResultRepository
import br.edu.ufabc.emerson.hotels.api.repositories.PokemonRepository
import br.edu.ufabc.emerson.hotels.api.service.GoogleService
import br.edu.ufabc.emerson.hotels.api.service.InfoService
import com.google.maps.model.PlaceDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.server.ResponseStatusException
import java.net.URL
import javax.transaction.Transactional
import kotlin.random.Random

@RestController
@CrossOrigin
@RequestMapping("/")
class Entry(
    val getInfoService: InfoService,
    val getGoogle: GoogleService,
    val repository: PokemonRepository,
    val searchResultRepository: PlacesSearchResultRepository,
    val detailsRepository: PlacesDetailsRepository,
    private val log: Logger = LoggerFactory.getLogger(Entry::class.java),
) {
    @GetMapping
    fun getPokemons(): ResponseEntity<List<Pokemon>> {
        return ResponseEntity.ok(repository.findAll().toList())
    }
    @GetMapping("/hotels/search")
    fun getSearchResults(): ResponseEntity<List<PlacesSearchResultInternal>> {
        return ResponseEntity.ok(searchResultRepository.findAll().toList())
    }

    @GetMapping("/hotels/detailed")
    fun getDetailedResults(): ResponseEntity<List<PlaceDetailsInternal>> {
        return ResponseEntity.ok(detailsRepository.findAll().toList())
    }

    @GetMapping("/hotels/url")
    fun getURLResults(): ResponseEntity<List<Triple<URL, Int, String>>> {
        val hoteisInfo = detailsRepository.findAll().map { Triple(it.placeDetails.url, it.placeDetails.userRatingsTotal, it.placeDetails.name) }.toList()
        log.info(hoteisInfo.joinToString(separator= ",", postfix = "\n"))
        return ResponseEntity.ok(hoteisInfo)
    }

    @GetMapping("/google")
    fun google(
        @RequestHeader("X-Google-Authorization") apikey: String?,
        @RequestParam(name = "search", defaultValue = "Hotels em Santo André") textToSearch: String,
        @RequestParam(name = "online", defaultValue = "FALSE") online: Boolean,
    ): List<PlaceDetails> {
        log.info("started")
        apikey?.let {
            val result = getGoogle.start(apikey, online, textToSearch)
            log.info("finished")
            return result.sortedBy { - it.rating }
        } ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "X-Google-Authorization is not present", null)
    }

    private
    val scope =
        CoroutineScope(Dispatchers.IO + SupervisorJob()) // or CoroutineScope(Dispatchers.IO + SupervisorJob())


    suspend fun longJob(i: Int) {
        val restTemplate = RestTemplate()
        val entity = restTemplate.getForEntity<Pokemon>("https://pokeapi.co/api/v2/pokemon/$i/")
        log.info("${entity.statusCodeValue} $i result ${entity.body?.name}")
        entity.body?.let {
            repository.save(it)
        }
    }

    @GetMapping("/async")
    @Transactional
    fun apiCall(): Int {
        for (i in 1..Random.nextInt(1155)) scope.launch {
            try {
                longJob(i)
            } catch (e: Exception) {
                log.error(e.stackTraceToString())
            }
        }
        return 204
    }
}
