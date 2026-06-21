package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

enum class EstadoCita { PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA }

@Entity
@Table(name = "citas_calendario")
data class CitaCalendario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clienteId: Long = 0,

    val trabajadorId: Long? = null,

    @Column(nullable = false)
    var fecha: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var hora: LocalTime = LocalTime.of(9, 0),

    var motivo: String = "",
    var notas: String = "",

    @Enumerated(EnumType.STRING)
    var estado: EstadoCita = EstadoCita.PENDIENTE
)
