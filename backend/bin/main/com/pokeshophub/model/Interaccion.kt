package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class TipoInteraccion { SISTEMA, CHAT, DOCUMENTO, CITA, NOTIFICACION }

@Entity
@Table(name = "interacciones")
data class Interaccion(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var clienteId: Long? = null,
    var trabajadorId: Long? = null,
    var usuarioNombre: String? = null,

    @Column(nullable = false)
    var tipo: String = "SISTEMA",

    @Column(length = 2000, nullable = false)
    var nota: String = "",

    val fecha: LocalDateTime = LocalDateTime.now()
)
