package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class Rol { CLIENTE, TRABAJADOR, ADMIN }

@Entity
@Table(name = "clientes")
data class Cliente(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var nombre: String = "",

    @Column(nullable = false)
    var apellidos: String = "",

    @Column(unique = true, nullable = false)
    var dni: String = "",

    @Column(unique = true, nullable = false)
    var email: String = "",

    var telefono: String = "",
    var direccion: String = "",

    @Column(nullable = false)
    var password: String = "",

    var activo: Boolean = true,

    var aprobado: Boolean = true,

    var saldo: Double = 0.0,

    val fechaAlta: LocalDateTime = LocalDateTime.now()
)
