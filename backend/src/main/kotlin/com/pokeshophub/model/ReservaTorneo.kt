package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reserva_torneos")
data class ReservaTorneo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val torneoId: Long = 0,

    @Column(nullable = false)
    val clienteId: Long = 0,

    @Column(nullable = false)
    val fechaReserva: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var estado: String = "CONFIRMADA" // "CONFIRMADA", "CANCELADA"
)
