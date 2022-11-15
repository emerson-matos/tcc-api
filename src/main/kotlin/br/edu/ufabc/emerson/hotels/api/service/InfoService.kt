package br.edu.ufabc.emerson.hotels.api.service

import br.edu.ufabc.emerson.hotels.api.models.Place
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class InfoService {
    fun getInfo(): List<Place> = listOf(Place(), Place())
}
