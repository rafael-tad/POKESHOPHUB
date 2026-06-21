package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasaciones")
data class Tasacion(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clienteId: Long = 0,

    @Column(length = 1000)
    var descripcion: String = "",

    @Column(nullable = false)
    var rutaFoto: String = "",

    @Column(nullable = false)
    var estado: String = "PENDIENTE", // "PENDIENTE", "VALORADA", "RECHAZADA"

    var valorEstimado: Double? = null,

    @Column(length = 1000)
    var notasAdmin: String? = null,

    @Column(nullable = false)
    val fechaSubida: LocalDateTime = LocalDateTime.now(),

    var subCentrado: Double? = null,
    var subBordes: Double? = null,
    var subEsquinas: Double? = null,
    var subSuperficie: Double? = null
)
