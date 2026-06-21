package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

enum class EstadoTarea { PENDIENTE, EN_PROCESO, PENDIENTE_CLIENTE, FINALIZADA }
enum class PrioridadTarea { BAJA, MEDIA, ALTA, CRITICA }

@Entity
@Table(name = "tareas")
data class Tarea(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var titulo: String = "",

    @Column(length = 1000)
    var descripcion: String = "",

    var clienteId: Long? = null,
    var trabajadorId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var estado: EstadoTarea = EstadoTarea.PENDIENTE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var prioridad: PrioridadTarea = PrioridadTarea.MEDIA,

    var fechaVencimiento: LocalDate? = null,

    val fechaCreacion: LocalDateTime = LocalDateTime.now(),
    var fechaActualizacion: LocalDateTime = LocalDateTime.now()
)
