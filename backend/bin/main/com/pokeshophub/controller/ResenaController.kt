package com.pokeshophub.controller

import com.pokeshophub.model.Resena
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.ResenaRepository
import com.pokeshophub.dto.CrearResenaRequest
import com.pokeshophub.dto.ResenaDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/resenas")
class ResenaController(
    private val resenaRepository: ResenaRepository,
    private val clienteRepository: ClienteRepository
) {

    @PostMapping
    fun crearResena(@RequestBody request: CrearResenaRequest): ResponseEntity<Any> {
        val cliente = clienteRepository.findById(request.clienteId).orElse(null)
            ?: return ResponseEntity.badRequest().body(mapOf("mensaje" to "Cliente no encontrado"))

        val resena = Resena(
            cliente = cliente,
            estrellas = request.estrellas,
            comentario = request.comentario,
            fecha = LocalDateTime.now()
        )
        resenaRepository.save(resena)
        return ResponseEntity.ok(mapOf("mensaje" to "Reseña guardada con éxito", "success" to true))
    }

    @GetMapping
    fun obtenerResenas(@RequestParam(required = false) estrellas: Int?): ResponseEntity<List<ResenaDto>> {
        val resenas = if (estrellas != null) {
            resenaRepository.findByEstrellas(estrellas)
        } else {
            resenaRepository.findAllByOrderByFechaDesc()
        }

        val dtos = resenas.map { 
            ResenaDto(
                id = it.id,
                clienteId = it.cliente?.id ?: 0,
                nombreCliente = "${it.cliente?.nombre} ${it.cliente?.apellidos}",
                estrellas = it.estrellas,
                comentario = it.comentario,
                fecha = it.fecha.toString()
            )
        }
        return ResponseEntity.ok(dtos)
    }
}
