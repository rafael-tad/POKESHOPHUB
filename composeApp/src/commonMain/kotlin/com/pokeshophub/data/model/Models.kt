package com.pokeshophub.data.model

import kotlinx.serialization.Serializable

// ── Auth ────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val token: String,
    val rol: String,
    val userId: Long,
    val nombre: String
)

@Serializable
data class RegistroRequest(
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    val password: String
)

// ── Cliente ──────────────────────────────────────────────────────

@Serializable
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

@Serializable
data class ActualizarClienteAdminRequest(
    val nombre: String? = null,
    val apellidos: String? = null,
    val dni: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
)

@Serializable
data class ActualizarPerfilClienteRequest(
    val telefono: String? = null,
    val direccion: String? = null
)

// ── Sesión de usuario activa ─────────────────────────────────────

@Serializable
data class SesionUsuario(
    val token: String,
    val rol: String,
    val userId: Long,
    val nombre: String
) : com.pokeshophub.util.PlatformSerializable {
    val esAdmin: Boolean get() = rol == "ADMIN" || rol == "TRABAJADOR"
    val esCliente: Boolean get() = rol == "CLIENTE"
}

// ── Tarea ───────────────────────────────────────────────────────

@Serializable
data class Tarea(
    val id: Long = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val clienteId: Long? = null,
    val trabajadorId: Long? = null,
    val estado: String = "PENDIENTE",
    val prioridad: String = "MEDIA",
    val fechaVencimiento: String? = null,
    val fechaCreacion: String = "",
    val fechaActualizacion: String = ""
)

@Serializable
data class CrearTareaRequest(
    val titulo: String,
    val descripcion: String = "",
    val clienteId: Long? = null,
    val trabajadorId: Long? = null,
    val prioridad: String = "MEDIA",
    val fechaVencimiento: String? = null
)

@Serializable
data class ActualizarEstadoTareaRequest(
    val estado: String
)

// ── Interaccion ──────────────────────────────────────────────────

@Serializable
data class Interaccion(
    val id: Long = 0,
    val clienteId: Long? = null,
    val trabajadorId: Long? = null,
    val usuarioNombre: String? = null,
    val tipo: String = "SISTEMA",
    val nota: String = "",
    val fecha: String = ""
)

@Serializable
data class CrearInteraccionRequest(
    val clienteId: Long,
    val trabajadorId: Long,
    val tipo: String,
    val nota: String
)

// ── Tienda (Productos) ───────────────────────────────────────────

@Serializable
data class Producto(
    val id: Long = 0,
    var nombre: String = "",
    var descripcion: String = "",
    var precio: Double = 0.0,
    var stock: Int = 0,
    var imagenUrl: String? = null,
    var categoria: String = "Cartas"
)

@Serializable
data class CrearProductoRequest(
    val nombre: String,
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val imagenUrl: String? = null,
    val categoria: String = "Cartas"
)

@Serializable
data class CompraRequest(
    val productoId: Long,
    val cantidad: Int = 1
)

// ── Torneos ──────────────────────────────────────────────────────

@Serializable
data class Torneo(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val maxParticipantes: Int = 16,
    val precioInscripcion: Double = 0.0,
    val participantesCount: Int = 0,
    val inscrito: Boolean = false,
    val estado: String = "ABIERTO"
)

@Serializable
data class CrearTorneoRequest(
    val nombre: String,
    val descripcion: String = "",
    val fecha: String,
    val hora: String,
    val maxParticipantes: Int = 16,
    val precioInscripcion: Double = 0.0
)

@Serializable
data class ReservaTorneo(
    val id: Long = 0,
    val torneoId: Long,
    val clienteId: Long,
    val fechaReserva: String
)

// ── Tasación ─────────────────────────────────────────────────────

@Serializable
data class Tasacion(
    val id: Long = 0,
    val clienteId: Long,
    val nombreCliente: String = "",
    val descripcion: String = "",
    val rutaFoto: String = "",
    val valorEstimado: Double? = null,
    val notasAdmin: String? = null,
    val estado: String = "PENDIENTE",
    val fecha: String = ""
)

@Serializable
data class EnviarTasacionRequest(
    val descripcion: String,
    val rutaFoto: String
)

@Serializable
data class ValorarTasacionRequest(
    val valorEstimado: Double,
    val notasAdmin: String,
    val estado: String = "VALORADA"
)

// ── Wallet / Monedero ────────────────────────────────────────────

@Serializable
data class AjustarSaldoRequest(
    val importe: Double,
    val descripcion: String = "Ajuste de saldo manual"
)

@Serializable
data class GastoIngreso(
    val id: Long = 0,
    val clienteId: Long,
    val tipo: String,
    val descripcion: String,
    val importe: Double,
    val categoria: String,
    val fecha: String,
    val imagenFactura: String? = null
)

@Serializable
data class CrearGastoIngresoRequest(
    val clienteId: Long,
    val tipo: String,
    val descripcion: String,
    val importe: Double,
    val categoria: String,
    val fecha: String = ""
)

// ── Notificacion ─────────────────────────────────────────────────

@Serializable
data class Notificacion(
    val id: Long = 0,
    val titulo: String = "",
    val mensaje: String = "",
    val destinatarioClienteId: Long? = null,
    val leida: Boolean = false,
    val fecha: String = ""
)

@Serializable
data class CrearNotificacionRequest(
    val titulo: String,
    val mensaje: String,
    val destinatarioClienteId: Long? = null
)

@Serializable
data class EditarNotificacionRequest(
    val titulo: String? = null,
    val mensaje: String? = null,
    val destinatarioClienteId: Long? = null
)

// ── Reseñas ──────────────────────────────────────────────────────

@Serializable
data class ResenaDto(
    val id: Long = 0,
    val clienteId: Long = 0,
    val nombreCliente: String = "",
    val estrellas: Int = 5,
    val comentario: String = "",
    val fecha: String = ""
)

@Serializable
data class CrearResenaRequest(
    val clienteId: Long,
    val estrellas: Int = 5,
    val comentario: String
)

// ── Enlace de chat ───────────────────────────────────────────────

@Serializable
data class Mensaje(
    val id: Long = 0,
    val clienteId: Long = 0,
    val remitente: String = "CLIENTE",
    val texto: String = "",
    val fechaHora: String = ""
)

@Serializable
data class EnviarMensajeRequest(
    val clienteId: Long,
    val texto: String,
    val remitente: String = "CLIENTE"
)

@Serializable
data class ChatResumenDto(
    val clienteId: Long,
    val nombreCliente: String,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)

// ── Respuesta genérica ───────────────────────────────────────────

@Serializable
data class MensajeResponse(val mensaje: String, val success: Boolean = true)

@Serializable
data class AdminResumenDto(
    val totalProductos: Int = 0,
    val totalClientes: Int = 0,
    val torneosActivos: Int = 0,
    val tasacionesPendientes: Int = 0
)
