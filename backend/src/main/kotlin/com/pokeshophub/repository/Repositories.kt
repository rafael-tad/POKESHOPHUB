package com.pokeshophub.repository

import com.pokeshophub.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ClienteRepository : JpaRepository<Cliente, Long> {
    fun findByEmail(email: String): Cliente?
    fun findByDni(dni: String): Cliente?
    fun findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(
        nombre: String, apellidos: String, dni: String
    ): List<Cliente>
    fun findByActivo(activo: Boolean): List<Cliente>
    fun findByAprobado(aprobado: Boolean): List<Cliente>
    fun findByActivoAndAprobado(activo: Boolean, aprobado: Boolean): List<Cliente>
}

@Repository
interface TrabajadorRepository : JpaRepository<Trabajador, Long> {
    fun findByEmail(email: String): Trabajador?
    fun findByActivo(activo: Boolean): List<Trabajador>
}

@Repository
interface ProductoRepository : JpaRepository<Producto, Long> {
    fun findByCategoria(categoria: String): List<Producto>
    fun findByNombreContainingIgnoreCase(nombre: String): List<Producto>
}

@Repository
interface TorneoRepository : JpaRepository<Torneo, Long> {
    fun findByEstado(estado: String): List<Torneo>
    fun findByFechaAfter(fecha: LocalDate): List<Torneo>
}

@Repository
interface ReservaTorneoRepository : JpaRepository<ReservaTorneo, Long> {
    fun findByClienteId(clienteId: Long): List<ReservaTorneo>
    fun findByTorneoId(torneoId: Long): List<ReservaTorneo>
    fun findByTorneoIdAndClienteId(torneoId: Long, clienteId: Long): ReservaTorneo?
}

@Repository
interface TasacionRepository : JpaRepository<Tasacion, Long> {
    fun findByClienteId(clienteId: Long): List<Tasacion>
    fun findByEstado(estado: String): List<Tasacion>
}

@Repository
interface GastoIngresoRepository : JpaRepository<GastoIngreso, Long> {
    fun findByClienteIdOrderByFechaDesc(clienteId: Long): List<GastoIngreso>
}

@Repository
interface MensajeRepository : JpaRepository<Mensaje, Long> {
    fun findByClienteIdOrderByFechaHoraAsc(clienteId: Long): List<Mensaje>

    @Query("SELECT DISTINCT m.clienteId FROM Mensaje m")
    fun findDistinctClienteIds(): List<Long>
}

@Repository
interface ResenaRepository : JpaRepository<Resena, Long> {
    fun findByEstrellas(estrellas: Int): List<Resena>
    fun findAllByOrderByFechaDesc(): List<Resena>
}

@Repository
interface NotificacionRepository : JpaRepository<Notificacion, Long> {
    fun findByDestinatarioClienteIdIsNullOrDestinatarioClienteId(clienteId: Long): List<Notificacion>
}

@Repository
interface TareaRepository : JpaRepository<Tarea, Long> {
    fun findByClienteId(clienteId: Long): List<Tarea>
    fun findByTrabajadorId(trabajadorId: Long): List<Tarea>
    fun findByEstado(estado: EstadoTarea): List<Tarea>
    fun findByClienteIdAndEstado(clienteId: Long, estado: EstadoTarea): List<Tarea>
}

@Repository
interface InteraccionRepository : JpaRepository<Interaccion, Long> {
    fun findByClienteIdOrderByFechaDesc(clienteId: Long): List<Interaccion>
    fun findTop100ByOrderByFechaDesc(): List<Interaccion>
}

@Repository
interface SolicitudCompraRepository : JpaRepository<SolicitudCompra, Long> {
    fun findByClienteIdOrderByFechaDesc(clienteId: Long): List<SolicitudCompra>
    fun findAllByOrderByFechaDesc(): List<SolicitudCompra>
}

