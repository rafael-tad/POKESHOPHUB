package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.Cliente
import com.pokeshophub.repository.ClienteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clientes")
class ClienteController(
    private val clienteRepository: ClienteRepository
) {

    /** Buscar clientes por nombre, apellidos o DNI — admin/trabajador */
    @GetMapping("/buscar")
    fun buscar(@RequestParam q: String): List<ClienteDto> {
        return clienteRepository
            .findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(q, q, q)
            .filter { it.activo && it.aprobado }
            .map { it.toDto() }
    }

    /** Listar todos los clientes activos — admin/trabajador */
    @GetMapping
    fun listar(): List<ClienteDto> = clienteRepository.findByActivoAndAprobado(true, true).map { it.toDto() }

    /** Obtener ficha de un cliente */
    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): ResponseEntity<ClienteDto> {
        val cliente = clienteRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(cliente.toDto())
    }

    /** El propio cliente actualiza sus datos de contacto */
    @PatchMapping("/perfil/{id}")
    fun actualizarPerfil(
        @PathVariable id: Long,
        @RequestBody request: ActualizarPerfilClienteRequest
    ): ResponseEntity<ClienteDto> {
        val cliente = clienteRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        request.telefono?.let { cliente.telefono = it }
        request.direccion?.let { cliente.direccion = it }

        val saved = clienteRepository.save(cliente).toDto()
        return ResponseEntity.ok(saved)
    }

    /** El administrador actualiza cualquier dato del cliente */
    @PatchMapping("/admin/{id}")
    fun actualizarAdmin(
        @PathVariable id: Long,
        @RequestBody request: ActualizarClienteAdminRequest
    ): ResponseEntity<ClienteDto> {
        val cliente = clienteRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        request.nombre?.let { cliente.nombre = it }
        request.apellidos?.let { cliente.apellidos = it }
        request.dni?.let { cliente.dni = it }
        request.email?.let { cliente.email = it }
        request.telefono?.let { cliente.telefono = it }
        request.direccion?.let { cliente.direccion = it }

        val saved = clienteRepository.save(cliente).toDto()
        return ResponseEntity.ok(saved)
    }

    /** Desactivar cliente (soft delete) — solo admin */
    @DeleteMapping("/{id}")
    fun desactivar(@PathVariable id: Long): ResponseEntity<MensajeResponse> {
        val cliente = clienteRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        cliente.activo = false
        clienteRepository.save(cliente)
        return ResponseEntity.ok(MensajeResponse("Cliente desactivado correctamente"))
    }

    private fun Cliente.toDto() = ClienteDto(
        id = id, nombre = nombre, apellidos = apellidos, dni = dni,
        email = email, telefono = telefono, direccion = direccion,
        activo = activo, fechaAlta = fechaAlta.toString(), saldo = saldo,
        aprobado = aprobado
    )
}
