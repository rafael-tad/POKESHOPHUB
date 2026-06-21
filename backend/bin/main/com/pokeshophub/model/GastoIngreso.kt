package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

enum class TipoMovimiento { GASTO, INGRESO }

@Entity
@Table(name = "gastos_ingresos")
data class GastoIngreso(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clienteId: Long = 0,

    @Enumerated(EnumType.STRING)
    var tipo: TipoMovimiento = TipoMovimiento.GASTO,

    @Column(nullable = false)
    var descripcion: String = "",

    @Column(nullable = false)
    var importe: Double = 0.0,

    var categoria: String = "",
    var fecha: LocalDate = LocalDate.now(),

    /** Ruta de la imagen de factura (Scan & Go) */
    var imagenFactura: String? = null,

    val fechaRegistro: LocalDateTime = LocalDateTime.now()
)
