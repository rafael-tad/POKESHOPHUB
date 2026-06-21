package com.pokeshophub.model

import jakarta.persistence.*

@Entity
@Table(name = "productos")
data class Producto(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var nombre: String = "",

    @Column(length = 1000)
    var descripcion: String = "",

    @Column(nullable = false)
    var precio: Double = 0.0,

    @Column(nullable = false)
    var stock: Int = 0,

    var imagenUrl: String? = null,

    var categoria: String = "Cartas" // "Sobres", "Cajas", "Accesorios", "Cartas"
)
