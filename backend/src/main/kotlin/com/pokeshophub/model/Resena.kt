package com.pokeshophub.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "resenas")
data class Resena(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    var cliente: Cliente? = null,

    @Column(nullable = false)
    var estrellas: Int = 0,

    @Column(columnDefinition = "TEXT")
    var comentario: String = "",

    val fecha: LocalDateTime = LocalDateTime.now()
)
