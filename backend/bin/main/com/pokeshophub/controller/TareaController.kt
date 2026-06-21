package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.Tarea
import com.pokeshophub.model.EstadoTarea
import com.pokeshophub.model.PrioridadTarea
import com.pokeshophub.repository.TareaRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/tareas")
class TareaController(private val tareaRepository: TareaRepository) {

    @GetMapping("/kanban")
    fun obtenerKanban(): Map<String, List<Tarea>> {
        val todas = tareaRepository.findAll()
        return mapOf(
            "PENDIENTE" to todas.filter { it.estado == EstadoTarea.PENDIENTE },
            "EN_PROCESO" to todas.filter { it.estado == EstadoTarea.EN_PROCESO },
            "PENDIENTE_CLIENTE" to todas.filter { it.estado == EstadoTarea.PENDIENTE_CLIENTE },
            "FINALIZADA" to todas.filter { it.estado == EstadoTarea.FINALIZADA }
        )
    }

    @PostMapping
    fun crearTarea(@RequestBody request: CrearTareaRequest): ResponseEntity<Tarea> {
        val tarea = Tarea(
            titulo = request.titulo,
            descripcion = request.descripcion,
            clienteId = request.clienteId,
            trabajadorId = request.trabajadorId,
            prioridad = PrioridadTarea.valueOf(request.prioridad),
            estado = EstadoTarea.PENDIENTE,
            fechaVencimiento = request.fechaVencimiento?.let { LocalDate.parse(it) }
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaRepository.save(tarea))
    }

    @PatchMapping("/{id}/estado")
    fun actualizarEstado(
        @PathVariable id: Long,
        @RequestBody request: ActualizarEstadoTareaRequest
    ): ResponseEntity<Tarea> {
        val tarea = tareaRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        tarea.estado = EstadoTarea.valueOf(request.estado)
        tarea.fechaActualizacion = LocalDateTime.now()
        return ResponseEntity.ok(tareaRepository.save(tarea))
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long): ResponseEntity<MensajeResponse> {
        if (!tareaRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        tareaRepository.deleteById(id)
        return ResponseEntity.ok(MensajeResponse("Tarea eliminada correctamente"))
    }
}
