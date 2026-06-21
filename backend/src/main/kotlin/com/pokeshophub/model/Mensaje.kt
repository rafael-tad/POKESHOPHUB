package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mensajes")
data class Mensaje(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clienteId: Long = 0,

    @Column(nullable = false)
    val remitente: String = "", // "CLIENTE" o "ADMIN"

    @Column(columnDefinition = "TEXT", nullable = false)
    val texto: String = "",

    val fechaHora: LocalDateTime = LocalDateTime.now()
)
