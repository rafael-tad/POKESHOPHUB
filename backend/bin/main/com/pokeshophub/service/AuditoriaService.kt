package com.pokeshophub.service

import com.pokeshophub.model.Interaccion
import com.pokeshophub.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuditoriaService(
    private val interaccionRepository: InteraccionRepository,
    private val clienteRepository: ClienteRepository,
    private val trabajadorRepository: TrabajadorRepository
) {

    fun registrar(clienteId: Long?, trabajadorId: Long?, tipo: String, nota: String) {
        try {
            var usuarioNombre: String? = null
            if (clienteId != null) {
                val cliente = clienteRepository.findById(clienteId).orElse(null)
                if (cliente != null) {
                    usuarioNombre = "${cliente.nombre} ${cliente.apellidos}"
                }
            } else if (trabajadorId != null) {
                val trabajador = trabajadorRepository.findById(trabajadorId).orElse(null)
                if (trabajador != null) {
                    usuarioNombre = "${trabajador.nombre} ${trabajador.apellidos}"
                }
            }

            val interaccion = Interaccion(
                clienteId = clienteId,
                trabajadorId = trabajadorId,
                usuarioNombre = usuarioNombre,
                tipo = tipo,
                nota = nota,
                fecha = LocalDateTime.now()
            )
            interaccionRepository.save(interaccion)
            println("AUDITORIA: Evento registrado -> [$tipo] $nota (Usuario: $usuarioNombre)")
        } catch (e: Exception) {
            println("ERROR AUDITORIA: No se pudo guardar el log: ${e.message}")
        }
    }
}
