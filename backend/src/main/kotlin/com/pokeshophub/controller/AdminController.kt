package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.*
import com.pokeshophub.repository.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api")
class AdminController(
    private val notificacionRepository: NotificacionRepository,
    private val clienteRepository: ClienteRepository,
    private val productoRepository: ProductoRepository,
    private val torneoRepository: TorneoRepository,
    private val tasacionRepository: TasacionRepository,
    private val interaccionRepository: InteraccionRepository,
    private val trabajadorRepository: TrabajadorRepository
) {

    // ── Resumen KPI para PokeShop Hub ────────────────────────────────────────
    
    @GetMapping("/admin/resumen")
    fun obtenerResumen(): AdminResumenDto {
        val totalProductos = productoRepository.count().toInt()
        val totalClientes = clienteRepository.count().toInt()
        val torneosActivos = torneoRepository.findByEstado("ABIERTO").size
        val tasacionesPendientes = tasacionRepository.findByEstado("PENDIENTE").size
        
        return AdminResumenDto(totalProductos, totalClientes, torneosActivos, tasacionesPendientes)
    }

    // ── Auditoría (Interacciones) ────────────────────────────────────────────

    @GetMapping("/admin/auditoria")
    fun auditoriaGlobal(): List<Interaccion> {
        return interaccionRepository.findTop100ByOrderByFechaDesc()
    }

    @GetMapping("/interacciones/{clienteId}")
    fun historialCliente(@PathVariable clienteId: Long): List<Interaccion> {
        return interaccionRepository.findByClienteIdOrderByFechaDesc(clienteId)
    }

    @PostMapping("/interacciones")
    fun registrarInteraccion(@RequestBody req: CrearInteraccionRequest): ResponseEntity<Interaccion> {
        var usuarioNombre: String? = null
        if (req.clienteId > 0) {
            val cliente = clienteRepository.findById(req.clienteId).orElse(null)
            if (cliente != null) {
                usuarioNombre = "${cliente.nombre} ${cliente.apellidos}"
            }
        }
        if (req.trabajadorId > 0) {
            val trabajador = trabajadorRepository.findById(req.trabajadorId).orElse(null)
            if (trabajador != null) {
                usuarioNombre = "${trabajador.nombre} ${trabajador.apellidos}"
            }
        }
        val inter = Interaccion(
            clienteId = req.clienteId,
            trabajadorId = req.trabajadorId,
            usuarioNombre = usuarioNombre,
            tipo = req.tipo,
            nota = req.nota
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(interaccionRepository.save(inter))
    }

    // ── Notificaciones ───────────────────────────────────────────

    @GetMapping("/notificaciones/{clienteId}")
    fun listarNotificaciones(@PathVariable clienteId: Long): List<Notificacion> =
        notificacionRepository.findByDestinatarioClienteIdIsNullOrDestinatarioClienteId(clienteId)

    @PostMapping("/notificaciones")
    fun crearNotificacion(@RequestBody req: CrearNotificacionRequest): ResponseEntity<Notificacion> {
        val notif = Notificacion(
            titulo = req.titulo,
            mensaje = req.mensaje,
            destinatarioClienteId = req.destinatarioClienteId
        )
        val saved = notificacionRepository.save(notif)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }

    @PatchMapping("/notificaciones/{id}/leida")
    fun marcarLeida(@PathVariable id: Long): ResponseEntity<Notificacion> {
        val notif = notificacionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        notif.leida = true
        val saved = notificacionRepository.save(notif)
        return ResponseEntity.ok(saved)
    }

    @PutMapping("/notificaciones/{id}")
    fun editarNotificacion(
        @PathVariable id: Long,
        @RequestBody req: EditarNotificacionRequest
    ): ResponseEntity<Notificacion> {
        val notif = notificacionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        req.titulo?.let { notif.titulo = it }
        req.mensaje?.let { notif.mensaje = it }
        req.destinatarioClienteId?.let { notif.destinatarioClienteId = it }
        val saved = notificacionRepository.save(notif)
        return ResponseEntity.ok(saved)
    }

    @DeleteMapping("/notificaciones/{id}")
    fun eliminarNotificacion(@PathVariable id: Long): ResponseEntity<MensajeResponse> {
        val notif = notificacionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        notificacionRepository.delete(notif)
        return ResponseEntity.ok(MensajeResponse("Notificación eliminada con éxito", true))
    }

    // ── Gestión de Aprobación de Registros ───────────────────────────────

    @GetMapping("/admin/registros-pendientes")
    fun obtenerRegistrosPendientes(): List<ClienteDto> {
        return clienteRepository.findByAprobado(false).map {
            ClienteDto(
                id = it.id, nombre = it.nombre, apellidos = it.apellidos, dni = it.dni,
                email = it.email, telefono = it.telefono, direccion = it.direccion,
                activo = it.activo, fechaAlta = it.fechaAlta.toString(), saldo = it.saldo,
                aprobado = it.aprobado
            )
        }
    }

    @PostMapping("/admin/clientes/{id}/aprobar-cliente")
    fun aprobarCliente(@PathVariable id: Long): ResponseEntity<Any> {
        val cliente = clienteRepository.findById(id).orElse(null) 
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse("Usuario no encontrado", false))
        cliente.aprobado = true
        cliente.activo = true
        clienteRepository.save(cliente)
        
        val inter = Interaccion(
            clienteId = cliente.id,
            trabajadorId = null,
            usuarioNombre = "${cliente.nombre} ${cliente.apellidos}",
            tipo = "SISTEMA",
            nota = "Administrador aprobó la cuenta del cliente ${cliente.nombre} ${cliente.apellidos}"
        )
        interaccionRepository.save(inter)
        
        return ResponseEntity.ok(MensajeResponse("Cliente aprobado con éxito", true))
    }

    @PostMapping("/admin/clientes/{id}/aprobar-admin")
    fun aprobarAdmin(@PathVariable id: Long): ResponseEntity<Any> {
        val cliente = clienteRepository.findById(id).orElse(null) 
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse("Usuario no encontrado", false))
        
        val admin = Trabajador(
            nombre = cliente.nombre,
            apellidos = cliente.apellidos,
            email = cliente.email,
            password = cliente.password, // password ya encriptada
            rol = Rol.ADMIN,
            activo = true
        )
        val savedAdmin = trabajadorRepository.save(admin)
        
        clienteRepository.delete(cliente)
        
        val inter = Interaccion(
            clienteId = null,
            trabajadorId = savedAdmin.id,
            usuarioNombre = "${admin.nombre} ${admin.apellidos}",
            tipo = "SISTEMA",
            nota = "Administrador aprobó e integró la cuenta como Administrador para ${admin.nombre} ${admin.apellidos}"
        )
        interaccionRepository.save(inter)
        
        return ResponseEntity.ok(MensajeResponse("Administrador aprobado con éxito", true))
    }

    @PostMapping("/admin/clientes/{id}/rechazar")
    fun rechazarRegistro(@PathVariable id: Long): ResponseEntity<Any> {
        val cliente = clienteRepository.findById(id).orElse(null) 
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse("Usuario no encontrado", false))
        
        clienteRepository.delete(cliente)
        
        return ResponseEntity.ok(MensajeResponse("Registro rechazado y eliminado con éxito", true))
    }
}
