package br.edu.ufabc.emerson.hotels.api.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.data.jpa.domain.AbstractAuditable
import org.springframework.data.jpa.domain.AbstractPersistable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id


@Entity
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Pokemon(
    @Id val id: Int,
    val name: String,
    val height: Int,
    val isDefault: Boolean,
    val weight: Int,
    @JsonProperty("order") val orderAttr: Int,
    val baseExperince: Int,
    val locationAreaEncounters: String,
)

@Entity
data class Usuario(@Id val id: UUID)