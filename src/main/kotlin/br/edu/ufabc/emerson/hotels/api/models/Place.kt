package br.edu.ufabc.emerson.hotels.api.models

import java.math.BigDecimal

data class Place(
    val name: String = "Teste",
    val price: BigDecimal = BigDecimal.ONE,
    val reviews: List<String> = listOf("Aval", "RUims")
)
