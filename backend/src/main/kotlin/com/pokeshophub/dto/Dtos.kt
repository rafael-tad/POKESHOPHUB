package com.pokeshophub.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// ── Auth ────────────────────────────────────────────────────────

data class LoginRequest(
    @field:NotBlank val email: String = "",
    @field:NotBlank val password: String = ""
)

data class LoginResponse(
    val token: String,
    val rol: String,
    val userId: Long,
    val nombre: String
)

data class RegistroClienteRequest(
    @field:NotBlank val nombre: String = "",
    @field:NotBlank val apellidos: String = "",
    @field:NotBlank val dni: String = "",
    @field:Email @field:NotBlank val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    @field:Size(min = 8) val password: String = ""
)

// ── Cliente ──────────────────────────────────────────────────────

data class ClienteDto(
    val id: Long,
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    val activo: Boolean,
    val fechaAlta: String,
    val saldo: Double = 0.0,
    val aprobado: Boolean = true
)

data class ActualizarClienteAdminRequest(
    val nombre: String? = null,
    val apellidos: String? = null,
    val dni: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
)

data class ActualizarPerfilClienteRequest(
    val telefono: String? = null,
    val direccion: String? = null
)

// ── Tarea ───────────────────────────────────────────────────────

data class CrearTareaRequest(
    val titulo: String,
    val descripcion: String = "",
    val clienteId: Long? = null,
    val trabajadorId: Long? = null,
    val prioridad: String = "MEDIA",
    val fechaVencimiento: String? = null
)

data class ActualizarEstadoTareaRequest(
    val estado: String
)

// ── Tienda (Productos) ───────────────────────────────────────────

data class CrearProductoRequest(
    @field:NotBlank val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val imagenUrl: String? = null,
    val categoria: String = "Cartas"
)

data class CompraRequest(
    val productoId: Long = 0,
    val cantidad: Int = 1
)

// ── Torneos ──────────────────────────────────────────────────────

data class CrearTorneoRequest(
    @field:NotBlank val nombre: String = "",
    val descripcion: String = "",
    @field:NotBlank val fecha: String = "", // "yyyy-MM-dd"
    @field:NotBlank val hora: String = "",  // "HH:mm"
    val maxParticipantes: Int = 16,
    val precioInscripcion: Double = 0.0
)

// ── Tasación ─────────────────────────────────────────────────────

data class EnviarTasacionRequest(
    val descripcion: String = "",
    val rutaFoto: String = ""
)

data class ValorarTasacionRequest(
    val valorEstimado: Double = 0.0,
    val notasAdmin: String = "",
    @field:NotBlank val estado: String = "VALORADA"
)

// ── Wallet / Monedero ────────────────────────────────────────────

data class AjustarSaldoRequest(
    val importe: Double = 0.0,
    val descripcion: String = "Ajuste de saldo manual"
)

data class CrearGastoIngresoRequest(
    val clienteId: Long = 0,
    @field:NotBlank val tipo: String = "",
    @field:NotBlank val descripcion: String = "",
    val importe: Double = 0.0,
    val categoria: String = "",
    val fecha: String = ""
)

// ── Notificacion ─────────────────────────────────────────────────

data class CrearInteraccionRequest(
    val clienteId: Long = 0,
    val trabajadorId: Long = 0,
    @field:NotBlank val tipo: String = "",
    @field:NotBlank val nota: String = ""
)

data class CrearNotificacionRequest(
    @field:NotBlank val titulo: String = "",
    @field:NotBlank val mensaje: String = "",
    val destinatarioClienteId: Long? = null
)

data class EditarNotificacionRequest(
    val titulo: String? = null,
    val mensaje: String? = null,
    val destinatarioClienteId: Long? = null
)

// ── Mensajes ───────────────────────────────────────────────────────

data class EnviarMensajeRequest(
    val clienteId: Long = 0,
    @field:NotBlank val texto: String = "",
    val remitente: String = "CLIENTE"
)

data class ChatResumenDto(
    val clienteId: Long,
    val nombreCliente: String,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)

// ── Reseñas ───────────────────────────────────────────────────────

data class CrearResenaRequest(
    val clienteId: Long = 0,
    val estrellas: Int = 5,
    val comentario: String = ""
)

data class ResenaDto(
    val id: Long = 0,
    val clienteId: Long = 0,
    val nombreCliente: String = "",
    val estrellas: Int = 5,
    val comentario: String = "",
    val fecha: String = ""
)

// ── Respuesta genérica ───────────────────────────────────────────

data class MensajeResponse(val mensaje: String, val success: Boolean = true)

data class AdminResumenDto(
    val totalProductos: Int = 0,
    val totalClientes: Int = 0,
    val torneosActivos: Int = 0,
    val tasacionesPendientes: Int = 0
)
