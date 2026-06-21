package com.pokeshophub.config

import com.pokeshophub.service.AuditoriaService
import com.pokeshophub.repository.TasacionRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuditoriaInterceptor(
    private val auditoriaService: AuditoriaService,
    private val jwtService: JwtService,
    private val tasacionRepository: TasacionRepository
) : HandlerInterceptor {

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // Solo auditar métodos de modificación exitosos
        val method = request.method
        if (method != "POST" && method != "PUT" && method != "DELETE" && method != "PATCH") {
            return
        }

        // Solo auditar endpoints de la API
        val uri = request.requestURI
        if (!uri.startsWith("/api")) {
            return
        }

        // Ignorar endpoints de login/registro (estos los maneja el AuthController manualmente para mayor precisión)
        if (uri.contains("/auth/login") || uri.contains("/auth/registro")) {
            return
        }

        // Ignorar el propio endpoint de registro de auditorías si se hace POST
        if (uri.contains("/interacciones") && method == "POST") {
            return
        }

        // Si la respuesta no fue exitosa (código 2xx), no la auditamos como movimiento completado
        val status = response.status
        if (status !in 200..299) {
            return
        }

        // Obtener usuario autenticado
        val principal = SecurityContextHolder.getContext().authentication?.principal as? UserPrincipal
        val (userId, role) = if (principal != null) {
            Pair(principal.userId, principal.rol)
        } else {
            // Reintentar extraer del header si el contexto de Spring no se cargó completamente
            val authHeader = request.getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                Pair(jwtService.extractUserId(token), jwtService.extractRol(token))
            } else {
                Pair(null, null)
            }
        }

        if (userId == null || role == null) {
            return
        }

        val clienteId = if (role == "CLIENTE") userId else null
        val trabajadorId = if (role == "ADMIN" || role == "TRABAJADOR") userId else null

        // Determinar tipo de interacción
        val tipo = when {
            uri.contains("/wallet") -> "SISTEMA"
            uri.contains("/mensajes") || uri.contains("/chat") -> "CHAT"
            uri.contains("/notificaciones") -> "NOTIFICACION"
            uri.contains("/tasaciones") -> "DOCUMENTO"
            uri.contains("/citas") || uri.contains("/calendario") -> "CITA"
            else -> "SISTEMA"
        }

        // Intentar obtener ID de cliente objetivo (target/destinatario)
        var targetClienteId: Long? = null
        val pathParts = uri.split("/")

        // Casos comunes de URIs:
        // /api/wallet/admin/ajustar-saldo/{clienteId}
        if (uri.contains("/wallet/admin/ajustar-saldo/")) {
            targetClienteId = pathParts.lastOrNull()?.toLongOrNull()
        }
        // /api/tienda/comprar/{clienteId}
        else if (uri.contains("/tienda/comprar/")) {
            targetClienteId = pathParts.lastOrNull()?.toLongOrNull()
        }
        // /api/tasaciones/admin/{id}/valorar -> buscar tasación para asociar al cliente
        else if (uri.contains("/tasaciones/admin/") && uri.endsWith("/valorar")) {
            val tasacionIdIndex = pathParts.indexOf("admin") + 1
            if (tasacionIdIndex < pathParts.size) {
                val tasacionId = pathParts[tasacionIdIndex].toLongOrNull()
                if (tasacionId != null) {
                    val tas = tasacionRepository.findById(tasacionId).orElse(null)
                    if (tas != null) {
                        targetClienteId = tas.clienteId
                    }
                }
            }
        }
        // /api/clientes/perfil/{id} o /api/clientes/admin/{id}
        else if (uri.contains("/clientes/perfil/") || uri.contains("/clientes/admin/")) {
            targetClienteId = pathParts.lastOrNull()?.toLongOrNull()
        }

        // Si el que realiza la acción es el cliente, asociar su propio clienteId
        val finalClienteId = clienteId ?: targetClienteId

        // Construir nota descriptiva
        val descripcionNota = generarNotaDeMovimiento(method, uri, role, userId, targetClienteId)

        // Registrar auditoría
        auditoriaService.registrar(
            clienteId = finalClienteId,
            trabajadorId = trabajadorId,
            tipo = tipo,
            nota = descripcionNota
        )
    }

    private fun generarNotaDeMovimiento(
        method: String,
        uri: String,
        role: String,
        userId: Long,
        targetClienteId: Long?
    ): String {
        val userLabel = if (role == "ADMIN" || role == "TRABAJADOR") "Administrador/Staff (ID: $userId)" else "Cliente (ID: $userId)"

        return when {
            uri.contains("/wallet/admin/ajustar-saldo") -> {
                "$userLabel ajustó el saldo del monedero del cliente (ID: $targetClienteId)"
            }
            uri.contains("/tienda/comprar") -> {
                "$userLabel realizó una compra de producto"
            }
            uri.contains("/tienda/admin/productos") -> {
                if (method == "POST") "$userLabel creó un nuevo producto en la tienda"
                else if (method == "PUT") "$userLabel actualizó un producto en la tienda"
                else "$userLabel eliminó un producto de la tienda"
            }
            uri.contains("/tasaciones/admin/") && uri.endsWith("/valorar") -> {
                "$userLabel procesó y valoró la tasación de carta del cliente (ID: $targetClienteId)"
            }
            uri.contains("/tasaciones") && method == "POST" -> {
                "$userLabel solicitó una nueva tasación de carta"
            }
            uri.contains("/torneos/reservar") -> {
                "$userLabel reservó plaza en un torneo"
            }
            uri.contains("/torneos/cancelar") -> {
                "$userLabel canceló su reserva en un torneo"
            }
            uri.contains("/torneos/admin") || uri.contains("/torneos") && (method == "POST" || method == "DELETE") -> {
                if (method == "POST") "$userLabel creó un nuevo torneo"
                else "$userLabel eliminó un torneo"
            }
            uri.contains("/notificaciones") -> {
                if (method == "POST") "$userLabel creó una nueva notificación"
                else if (method == "PUT" || uri.endsWith("/leida")) "$userLabel actualizó una notificación"
                else "$userLabel eliminó una notificación"
            }
            uri.contains("/resenas") -> {
                if (method == "POST") "$userLabel publicó una reseña"
                else "$userLabel eliminó una reseña"
            }
            uri.contains("/clientes/perfil") -> {
                "$userLabel actualizó su información de perfil"
            }
            uri.contains("/clientes/admin") -> {
                "$userLabel modificó los datos de la ficha del cliente (ID: $targetClienteId)"
            }
            uri.contains("/tareas") -> {
                if (method == "POST") "$userLabel creó una nueva tarea/cita"
                else if (method == "PUT" || method == "PATCH") "$userLabel modificó una tarea/cita"
                else "$userLabel eliminó una tarea/cita"
            }
            uri.contains("/mensajes/enviar") -> {
                "$userLabel envió un mensaje de chat"
            }
            else -> {
                "Acción $method realizada por $userLabel en $uri"
            }
        }
    }
}
