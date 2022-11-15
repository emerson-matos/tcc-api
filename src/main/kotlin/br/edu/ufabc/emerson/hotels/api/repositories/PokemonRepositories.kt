package br.edu.ufabc.emerson.hotels.api.repositories

import br.edu.ufabc.emerson.hotels.api.models.Pokemon
import br.edu.ufabc.emerson.hotels.api.models.Usuario
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface PokemonRepository : PagingAndSortingRepository<Pokemon, Long>

interface UsuarioRepository : PagingAndSortingRepository<Usuario, UUID>