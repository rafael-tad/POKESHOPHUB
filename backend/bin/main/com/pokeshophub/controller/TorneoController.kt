package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.Torneo
import com.pokeshophub.model.ReservaTorneo
import com.pokeshophub.model.GastoIngreso
import com.pokeshophub.model.TipoMovimiento
import com.pokeshophub.repository.TorneoRepository
import com.pokeshophub.repository.ReservaTorneoRepository
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.GastoIngresoRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/torneos")
class TorneoController(
    private val torneoRepository: TorneoRepository,
    private val reservaTorneoRepository: ReservaTorneoRepository,
    private val clienteRepository: ClienteRepository,
    private val gastoIngresoRepository: GastoIngresoRepository
) {

    @GetMapping
    fun listarTorneos(): List<Torneo> = torneoRepository.findAll()

    @GetMapping("/inscritos/{clienteId}")
    fun listarInscritosPorCliente(@PathVariable clienteId: Long): List<Torneo> {
        val reservas = reservaTorneoRepository.findByClienteId(clienteId)
            .filter { it.estado == "CONFIRMADA" }
        return reservas.mapNotNull { torneoRepository.findById(it.torneoId).orElse(null) }
    }

    // Inscribirse en torneo
    @PostMapping("/{id}/inscribir/{clienteId}")
    fun inscribirCliente(
        @PathVariable id: Long,
        @PathVariable clienteId: Long
    ): ResponseEntity<MensajeResponse> {
        val torneo = torneoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("Torneo no encontrado", false))

        val cliente = clienteRepository.findById(clienteId).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("Cliente no encontrado", false))

        if (torneo.estado != "ABIERTO") {
            return ResponseEntity.badRequest().body(MensajeResponse("El torneo no está abierto para inscripciones", false))
        }

        if (torneo.participantesActuales >= torneo.maxParticipantes) {
            return ResponseEntity.badRequest().body(MensajeResponse("El torneo ya está completo", false))
        }

        // Verificar si ya está inscrito
        val reservaExistente = reservaTorneoRepository.findByTorneoIdAndClienteId(id, clienteId)
        if (reservaExistente != null && reservaExistente.estado == "CONFIRMADA") {
            return ResponseEntity.badRequest().body(MensajeResponse("Ya estás inscrito en este torneo", false))
        }

        // Cobrar la inscripción si tiene coste
        if (torneo.precioInscripcion > 0.0) {
            if (cliente.saldo < torneo.precioInscripcion) {
                return ResponseEntity.badRequest().body(MensajeResponse("Saldo insuficiente en el monedero virtual", false))
            }
            cliente.saldo -= torneo.precioInscripcion
            clienteRepository.save(cliente)

            // Registrar gasto
            gastoIngresoRepository.save(
                GastoIngreso(
                    clienteId = clienteId,
                    tipo = TipoMovimiento.GASTO,
                    descripcion = "Inscripción torneo: ${torneo.nombre}",
                    importe = torneo.precioInscripcion,
                    categoria = "TORNEO",
                    fecha = LocalDate.now()
                )
            )
        }

        // Inscribir o reactivar reserva cancelada
        if (reservaExistente != null) {
            reservaExistente.estado = "CONFIRMADA"
            reservaTorneoRepository.save(reservaExistente)
        } else {
            reservaTorneoRepository.save(
                ReservaTorneo(
                    torneoId = id,
                    clienteId = clienteId
                )
            )
        }

        torneo.participantesActuales += 1
        torneoRepository.save(torneo)

        return ResponseEntity.ok(MensajeResponse("Inscripción confirmada con éxito", true))
    }

    // Cancelar inscripción en torneo
    @PostMapping("/{id}/desapuntar/{clienteId}")
    fun desapuntarCliente(
        @PathVariable id: Long,
        @PathVariable clienteId: Long
    ): ResponseEntity<MensajeResponse> {
        val torneo = torneoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("Torneo no encontrado", false))

        val cliente = clienteRepository.findById(clienteId).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("Cliente no encontrado", false))

        val reserva = reservaTorneoRepository.findByTorneoIdAndClienteId(id, clienteId)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("No estás inscrito en este torneo", false))

        if (reserva.estado == "CANCELADA") {
            return ResponseEntity.badRequest().body(MensajeResponse("Tu inscripción ya estaba cancelada", false))
        }

        reserva.estado = "CANCELADA"
        reservaTorneoRepository.save(reserva)

        torneo.participantesActuales = (torneo.participantesActuales - 1).coerceAtLeast(0)
        torneoRepository.save(torneo)

        // Devolver saldo si tenía costo
        if (torneo.precioInscripcion > 0.0) {
            cliente.saldo += torneo.precioInscripcion
            clienteRepository.save(cliente)

            // Registrar devolución
            gastoIngresoRepository.save(
                GastoIngreso(
                    clienteId = clienteId,
                    tipo = TipoMovimiento.INGRESO,
                    descripcion = "Devolución inscripción torneo: ${torneo.nombre}",
                    importe = torneo.precioInscripcion,
                    categoria = "TORNEO",
                    fecha = LocalDate.now()
                )
            )
        }

        return ResponseEntity.ok(MensajeResponse("Inscripción cancelada y saldo reembolsado si correspondía", true))
    }

    // ADMIN: Crear torneo
    @PostMapping("/admin")
    fun crearTorneo(@RequestBody request: CrearTorneoRequest): Torneo {
        val torneo = Torneo(
            nombre = request.nombre,
            descripcion = request.descripcion,
            fecha = LocalDate.parse(request.fecha),
            hora = LocalTime.parse(request.hora),
            maxParticipantes = request.maxParticipantes,
            precioInscripcion = request.precioInscripcion,
            estado = "ABIERTO"
        )
        return torneoRepository.save(torneo)
    }

    // ADMIN: Modificar estado de torneo
    @PatchMapping("/admin/{id}/estado")
    fun cambiarEstadoTorneo(
        @PathVariable id: Long,
        @RequestParam estado: String
    ): ResponseEntity<Torneo> {
        val torneo = torneoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        torneo.estado = estado
        return ResponseEntity.ok(torneoRepository.save(torneo))
    }

    // ADMIN: Eliminar torneo
    @DeleteMapping("/admin/{id}")
    fun eliminarTorneo(@PathVariable id: Long): ResponseEntity<MensajeResponse> {
        if (!torneoRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        torneoRepository.deleteById(id)
        return ResponseEntity.ok(MensajeResponse("Torneo eliminado correctamente"))
    }
}
