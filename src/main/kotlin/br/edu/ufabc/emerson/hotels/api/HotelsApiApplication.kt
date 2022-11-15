package br.edu.ufabc.emerson.hotels.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class HotelsApiApplication

fun main(args: Array<String>) {
    runApplication<HotelsApiApplication>(*args)
}
