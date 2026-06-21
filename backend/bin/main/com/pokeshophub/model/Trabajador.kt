package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trabajadores")
data class Trabajador(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var nombre: String = "",

    @Column(nullable = false)
    var apellidos: String = "",

    @Column(unique = true, nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Enumerated(EnumType.STRING)
    var rol: Rol = Rol.TRABAJADOR,

    var activo: Boolean = true,

    val fechaAlta: LocalDateTime = LocalDateTime.now()
)
