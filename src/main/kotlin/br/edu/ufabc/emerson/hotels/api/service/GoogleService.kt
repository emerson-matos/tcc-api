package br.edu.ufabc.emerson.hotels.api.service

import br.edu.ufabc.emerson.hotels.api.models.PlaceDetailsInternal
import br.edu.ufabc.emerson.hotels.api.models.PlacesSearchResultInternal
import br.edu.ufabc.emerson.hotels.api.repositories.PlacesDetailsRepository
import br.edu.ufabc.emerson.hotels.api.repositories.PlacesSearchResultRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.maps.FindPlaceFromTextRequest
import com.google.maps.GeoApiContext
import com.google.maps.PlaceDetailsRequest
import com.google.maps.PlacesApi
import com.google.maps.TextSearchRequest
import com.google.maps.model.FindPlaceFromText
import com.google.maps.model.PlaceDetails
import com.google.maps.model.PlacesSearchResponse
import com.google.maps.model.PlacesSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GoogleService(
    private val log: Logger = LoggerFactory.getLogger(GoogleService::class.java),
    private val placesDetailsRepository: PlacesDetailsRepository,
    private val placesSearchResultRepository: PlacesSearchResultRepository,
) {


    suspend fun makeRequestToDetailsAPI(
        apiContext: GeoApiContext,
        placesSearchResult: PlacesSearchResult,
        om: ObjectMapper
    ) {
        val await =
            PlacesApi.placeDetails(apiContext, placesSearchResult.placeId)
                .region("br")
                .language("pt-BR")
                .fields(*PlaceDetailsRequest.FieldMask.values())
                .await()
        log.info(om.writeValueAsString(await))
        log.info("${await.name} result ${await.placeId}")
        await?.let {
            placesDetailsRepository.save(
                PlaceDetailsInternal(
                    UUID.nameUUIDFromBytes(it.placeId.toByteArray(Charsets.UTF_8)),
                    it
                )
            )
        }
    }

    fun apiCall(apiContext: GeoApiContext, candidates: Array<PlacesSearchResult>, om: ObjectMapper) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // or CoroutineScope(Dispatchers.IO)
        candidates.forEach {
            scope.launch {
                try {
                    makeRequestToDetailsAPI(apiContext, it, om)
                } catch (e: Exception) {
                    log.error(e.stackTraceToString())
                }
            }
        }
    }

    fun start(apiKey: String, online: Boolean, textToSearch: String): List<PlaceDetails> {
        val om = jacksonMapperBuilder().findAndAddModules().build()
        val resultList = mutableListOf<PlaceDetails>()
        log.warn("online: $online, string: $textToSearch, apikey: $apiKey")
        when (online) {
            true -> {
                val context = GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build()

                context.use {
                    val result: PlacesSearchResponse =
                        PlacesApi
                            .textSearchQuery(it,textToSearch)
                            .language("pt-BR")
                            .region("br")
                            .await()

                    apiCall(context, result.results, om)
                    // or CoroutineScope(Dispatchers.IO)
                    runBlocking {
                        val list = result.results.map { place ->
                            PlacesSearchResultInternal(
                                UUID.nameUUIDFromBytes(
                                    place.placeId.toByteArray(
                                        Charsets.UTF_8
                                    )
                                ), place
                            )
                        }.toList()
                        launch {
                            withContext(Dispatchers.IO) {
                                log.info(om.writeValueAsString(list))
                                placesSearchResultRepository.saveAll(list)
                            }
                        }
                    }
                }
            }
            false -> {
                val response =
                    placesDetailsRepository.findAll()
//                    om.readValue<PlaceDetails>(mock)
                resultList.addAll(response.map { it.placeDetails })
            }
        }

        return resultList
    }

}

val resultListMock =
    """[{"formattedAddress":null,"geometry":null,"name":null,"icon":null,"placeId":"ChIJSVmU2M9pzpQR5YMIPZZZl-k","scope":null,"rating":0.0,"types":null,"openingHours":null,"photos":null,"vicinity":null,"permanentlyClosed":false,"userRatingsTotal":0,"businessStatus":null}]"""
val mock =
    """{"addressComponents":[{"longName":"279","shortName":"279","types":["STREET_NUMBER"]},{"longName":"Rua Giovanni Battista Pirelli","shortName":"Rua Giovanni Battista Pirelli","types":["ROUTE"]},{"longName":"Torre I","shortName":"Torre I","types":["SUBLOCALITY_LEVEL_1","SUBLOCALITY","POLITICAL"]},{"longName":"Santo André","shortName":"Santo André","types":["ADMINISTRATIVE_AREA_LEVEL_2","POLITICAL"]},{"longName":"São Paulo","shortName":"SP","types":["ADMINISTRATIVE_AREA_LEVEL_1","POLITICAL"]},{"longName":"Brazil","shortName":"BR","types":["COUNTRY","POLITICAL"]},{"longName":"09111-340","shortName":"09111-340","types":["POSTAL_CODE"]}],"adrAddress":"<span class=\"street-address\">Rua Giovanni Battista Pirelli, 279</span> - <span class=\"extended-address\">Torre I</span>, <span class=\"locality\">Santo André</span> - <span class=\"region\">SP</span>, <span class=\"postal-code\">09111-340</span>, <span class=\"country-name\">Brazil</span>","formattedAddress":"Rua Giovanni Battista Pirelli, 279 - Torre I, Santo André - SP, 09111-340, Brazil","formattedPhoneNumber":"(11) 3500-4370","geometry":{"bounds":null,"location":{"lat":-23.6637937,"lng":-46.506303},"locationType":null,"viewport":{"northeast":{"lat":-23.6623483197085,"lng":-46.5048567697085},"southwest":{"lat":-23.6650462802915,"lng":-46.5075547302915}}},"icon":"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/lodging-71.png","internationalPhoneNumber":"+55 11 3500-4370","name":"Go Inn Santo André","openingHours":{"openNow":true,"periods":[{"open":{"day":"SUNDAY","time":[0,0]},"close":null}],"weekdayText":["Monday: Open 24 hours","Tuesday: Open 24 hours","Wednesday: Open 24 hours","Thursday: Open 24 hours","Friday: Open 24 hours","Saturday: Open 24 hours","Sunday: Open 24 hours"],"permanentlyClosed":null},"photos":[{"photoReference":"Aap_uEA6s-mXGK4fdze5rRrWEEsPkANKsHXFbeFQCzTHAp7igcpyzxItNEwv1fUTV51o-xyXCo58X6348JLPOr131QzpTf4UdDkic-B5BsfWtV9Uf_OMt25L1qsq1W1jK7Hk3L4rfX4QJ8lqCVD7hwfOtHsDQ0CWwWZ-WeH-6LRw16vm-VMj","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uECvB8PsJolzNhiJPO--H1ffjleIR1Vaod1Oifeav_up_4RiGAvj9FwcR64WUNfuIFW23xLcyQP21Ux6ABIWHK6BAaibIlnh_9FCA2S1RyFsbOCAegJpJEkJlwpCIR09Zp26HhtXiqKnEoYbo_gw5i-NJv4gIxHgEx1byliaaHYOmLwM","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEAI5EPoCBJlSLoLSlowaj6rFgydTkPviwKstg2dcdftm8yRBfa9y9cmBlKHSAzTVZmaWCbHzsTI2WC6_YcSDnRafM8c5o0kCFzXw5cOsH5RgTHIPiyp6O7-WdQ7brRHk4icoSv_FgNHK9n0k-uUXQxUounAGawuvTXNj0SRPRA8Qy4m","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEBvw7elu15ydxnSMCEtHQefW0AVeBlXC0sCKNadiiusDfpbTM0K0j139NlwqkHHimKC1Nx6fbp9CovPIpMyUGeYfLAXYPWjFn63ZGJYERxnaFbP0aT9QfKV_2vPKJMlD4j8IWMNYJVMmGujZi240FBX-hwVOegX-PTARTiN68NITNvJ","height":3554,"width":5331,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEAKa1CDDerwKJwbeV9P3Bv0UBAxN4-e3ACulaUbgWsWMWgP4uz_irVsj89WRJ4Xunx9f4F72B22HzKEj26JEzQMOdhlYX1Edug8TYL1aV_5f1gaSpCR2_UTX40YuPnjeBK-O0SJ4IeStJPpWFqJ9BRR4m9Wdo5NUw1uVB4vbLPYBxS0","height":3823,"width":5735,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEC8YVfHB0R3FMmRxsy2V_fFJ52pOHpd6nSrYcZKc3YhXO3v4LCZLYMXCwxpgK-Ek6TxLhLB1ZFDg0GO2E_Dk5gQvis9_cMEiam7RF_yM0kL-Or8xd6r4LTDNhk2ic__sTynsEom9OlfXBChWen-sfsdbMvattaQpZIbcjyiNlf4p0XM","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEAx1KRLxOcO1oqqCn92TZUL-AC445G7OegliVsEtVPyJtg6FlkqBpc90zEJJRPoDMz_iKmr3ZibVIHo_7BWMdX4cC_rghjjr-Z7SJqAomd3HRsQcBl0smay-X7utFoiqFWyx31FQpIl1vLZySv9rsjbqQmLqAvGHqhuf9brB4sTiXJN","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEA2q2NBRpoYs9JHYtVWZBB_Hje3-8OUm_BMsuIHDGjI6qdSl3Z_RwuA-kay7G_q9goU9XvfdvCTKM9zpReEE5Ax2vMpYQWDzLShGKcV6B5UWDUrrrI86goXv_Ei9OoWduXF_0P7QhCCUPs48VnQPAaCIxyNoV9hoQqtSB8qW0JrI6Kv","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEC3FOlfCGzTpId0jRE0E07n4RPte7eXtVtcw-0u95MMqamB2GFImYscReUKvF91bfFmvIV8526dWK10-wupcJs1pHMTHh-0escW0bscVZKpv8e5odoWalue73PFmgAX5AIc1HGjNtAiodjRdoVnLrPXWHDxUnWQAxNAl5zGtd_KnwCR","height":5760,"width":3840,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]},{"photoReference":"Aap_uEB9Qg_BzDIv-fweCjAAJKDrPpoFHxXEhBOnHJ5C-D6psnpbpLtflQS9xQVr_GYDG9ynS7uwLCHuA2uQp2OU_MM2T5uJTYRY9jSSJLnrK2xLK8r8ZCqLNRM7hjRPja8Ei4UAKDWQ3wAc_3kv74DduLUPz_IbVDrvmvs3N4cN_0MfEfmf","height":3840,"width":5760,"htmlAttributions":["<a href=\"https://maps.google.com/maps/contrib/105831719710314549451\">Go Inn Santo André</a>"]}],"placeId":"ChIJSVmU2M9pzpQR5YMIPZZZl-k","scope":null,"plusCode":{"globalCode":"588M8FPV+FF","compoundCode":"8FPV+FF Santo André - State of São Paulo, Brazil"},"permanentlyClosed":false,"userRatingsTotal":1583,"altIds":null,"priceLevel":null,"rating":4.4,"reviews":[{"aspects":null,"authorName":"O respeitador","authorUrl":"https://www.google.com/maps/contrib/102369626412066838323/reviews","language":"en","profilePhotoUrl":"https://lh3.googleusercontent.com/a-/AOh14Gi9FzLBp31g0EoktrjodnhxbmdeVjci5seBUrHBqQ=s128-c0x00000000-cc-rp-mo-ba4","rating":5,"relativeTimeDescription":"a year ago","text":"Td ok.","time":1621077275.000000000},{"aspects":null,"authorName":"A Greer","authorUrl":"https://www.google.com/maps/contrib/115458223073029717298/reviews","language":"en","profilePhotoUrl":"https://lh3.googleusercontent.com/a-/AOh14GhmIkmZtC3w4WUPzqsm7d3UiIaRk2nUl2bKKfCEtw=s128-c0x00000000-cc-rp-mo-ba6","rating":5,"relativeTimeDescription":"4 years ago","text":"New hotel in a good location, and all needed within the hotel or in the shopping mall attached to it. Wheelchair accessible to all areas including the swimming pool. Breakfast buffet comes with a cook ready to accept your order. Rooms are modern, and comfortable. Weekend rate are a value plus, weekday rates too expensive. At the time of the review came with free parking and 24 hour security.","time":1506131088.000000000},{"aspects":null,"authorName":"Gustavo Vasques","authorUrl":"https://www.google.com/maps/contrib/113051078750622541227/reviews","language":"en","profilePhotoUrl":"https://lh3.googleusercontent.com/a/AATXAJyvnY1IzbwTgVVBmFzl5K3x4z6E5fG_FUV0JgqniQ=s128-c0x00000000-cc-rp-mo-ba2","rating":4,"relativeTimeDescription":"3 years ago","text":"There is a simple place without any luxuous, but its ok. They have a good and kindness attendants. The rooms are so small too.","time":1536164201.000000000},{"aspects":null,"authorName":"Liam Terblanche","authorUrl":"https://www.google.com/maps/contrib/115476204888573463908/reviews","language":"en","profilePhotoUrl":"https://lh3.googleusercontent.com/a-/AOh14GhU0frPAZmKBW8XZzeajiRBBE8od6-eCJv_ftlLjCE=s128-c0x00000000-cc-rp-mo-ba4","rating":4,"relativeTimeDescription":"4 years ago","text":"I really love this hotel. Brand new. Not everything is working 100% yet, but the staff are exceptionally friendly and helpful, facilities are good,I love the fitness centre and pool area, and the rooms are spacious and well equipped.","time":1509099213.000000000},{"aspects":null,"authorName":"Leonardo Bellan","authorUrl":"https://www.google.com/maps/contrib/104065125697550255328/reviews","language":"en","profilePhotoUrl":"https://lh3.googleusercontent.com/a/AATXAJzX8bhQxUJMGx2zkKjXDds93n_qfti_MbiwQY_Y=s128-c0x00000000-cc-rp-mo","rating":3,"relativeTimeDescription":"3 years ago","text":"It is stay together with shopping mall ATRIUM next a intense avenue strret. There isn't rom service for foods you call and to go a take you food in reception. There is a simple breakfast but it is tasty. The Aircondition was with smelling bad and the water in the shower takes too long heat. Pets accepted plus extra charge for 50 reais per Pets and they aren't stay alone in the room for long time. There is a parking in the shopping 20 reais charged by the hotel.","time":1543496877.000000000}],"types":["LODGING","POINT_OF_INTEREST","ESTABLISHMENT"],"url":"https://maps.google.com/?cid=16832020634124452837","utcOffset":-180,"vicinity":"Rua Giovanni Battista Pirelli, 279 - Torre I, Santo André","website":"https://www.reserveatlantica.com.br/hotel/go-inn-santo-andre/?utm_source=gmb&utm_medium=organic&utm_campaign=google-my-business","htmlAttributions":[],"businessStatus":"OPERATIONAL"}
"""