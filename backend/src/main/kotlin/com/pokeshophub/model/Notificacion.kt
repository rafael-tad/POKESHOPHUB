package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notificaciones")
data class Notificacion(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var titulo: String = "",

    @Column(columnDefinition = "TEXT", nullable = false)
    var mensaje: String = "",

    /** null = para todos los clientes; valor = clienteId específico */
    var destinatarioClienteId: Long? = null,

    var leida: Boolean = false,

    val fecha: LocalDateTime = LocalDateTime.now()
)
