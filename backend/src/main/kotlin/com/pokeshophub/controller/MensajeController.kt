package com.pokeshophub.controller

import com.pokeshophub.dto.ChatResumenDto
import com.pokeshophub.dto.EnviarMensajeRequest
import com.pokeshophub.model.Mensaje
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.MensajeRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/mensajes")
class MensajeController(
    private val mensajeRepository: MensajeRepository,
    private val clienteRepository: ClienteRepository
) {

    @GetMapping("/{clienteId}")
    fun obtenerHistorial(@PathVariable clienteId: Long): List<Mensaje> {
        return mensajeRepository.findByClienteIdOrderByFechaHoraAsc(clienteId)
    }

    @PostMapping
    fun enviarMensaje(@RequestBody req: EnviarMensajeRequest): ResponseEntity<Mensaje> {
        val mensaje = Mensaje(
            clienteId = req.clienteId,
            remitente = req.remitente,
            texto = req.texto,
            fechaHora = LocalDateTime.now()
        )
        val saved = mensajeRepository.save(mensaje)
        return ResponseEntity.ok(saved)
    }

    @GetMapping("/admin/chats")
    fun listarChatsAdmin(): List<ChatResumenDto> {
        val clienteIds = mensajeRepository.findDistinctClienteIds()
        val resumen = clienteIds.mapNotNull { cId ->
            val cliente = clienteRepository.findById(cId).orElse(null) ?: return@mapNotNull null
            val ultimosMensajes = mensajeRepository.findByClienteIdOrderByFechaHoraAsc(cId)
            val ultimo = ultimosMensajes.lastOrNull() ?: return@mapNotNull null
            ChatResumenDto(
                clienteId = cId,
                nombreCliente = "${cliente.nombre} ${cliente.apellidos}",
                ultimoMensaje = ultimo.texto,
                fechaUltimoMensaje = ultimo.fechaHora.toString()
            )
        }
        return resumen.sortedByDescending { it.fechaUltimoMensaje }
    }
}
