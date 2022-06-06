package br.edu.ufabc.emerson.hotels.api.controllers

import br.edu.ufabc.emerson.hotels.api.service.GoogleService
import br.edu.ufabc.emerson.hotels.api.service.InfoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class Entry(
    val getInfoService: InfoService,
    val getGoogle: GoogleService
) {
    @GetMapping
    fun get() = getInfoService.getInfo()

    @GetMapping("/231")
    fun get2() = getGoogle.start()
}

