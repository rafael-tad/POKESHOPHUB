package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Embeddable
data class SolicitudCompraItem(
    var productoId: Long = 0,
    var productoNombre: String = "",
    var cantidad: Int = 0,
    var precioUnitario: Double = 0.0
)

@Entity
@Table(name = "solicitudes_compra")
data class SolicitudCompra(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clienteId: Long = 0,

    @Column(nullable = false)
    val clienteNombre: String = "",

    @Column(nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var estado: String = "PENDIENTE", // "PENDIENTE", "APROBADA", "RECHAZADA"

    @Column(nullable = false)
    val total: Double = 0.0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "solicitud_compra_items", joinColumns = [JoinColumn(name = "solicitud_id")])
    val items: List<SolicitudCompraItem> = mutableListOf()
)
