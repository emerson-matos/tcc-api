package br.edu.ufabc.emerson.hotels.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HotelsApiApplication

fun main(args: Array<String>) {
    runApplication<HotelsApiApplication>(*args)
}
