package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "torneos")
data class Torneo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var nombre: String = "",

    @Column(length = 1000)
    var descripcion: String = "",

    @Column(nullable = false)
    var fecha: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var hora: LocalTime = LocalTime.NOON,

    @Column(nullable = false)
    var maxParticipantes: Int = 16,

    @Column(nullable = false)
    var participantesActuales: Int = 0,

    @Column(nullable = false)
    var precioInscripcion: Double = 0.0,

    @Column(nullable = false)
    var estado: String = "ABIERTO" // "ABIERTO", "FINALIZADO", "CANCELADO"
)
