package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.Tasacion
import com.pokeshophub.model.Cliente
import com.pokeshophub.model.GastoIngreso
import com.pokeshophub.model.TipoMovimiento
import com.pokeshophub.repository.TasacionRepository
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.GastoIngresoRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/tasaciones")
class TasacionController(
    private val tasacionRepository: TasacionRepository,
    private val clienteRepository: ClienteRepository,
    private val gastoIngresoRepository: GastoIngresoRepository
) {
    @Value("\${app.upload.dir}")
    private lateinit var uploadDir: String

    @GetMapping("/cliente/{clienteId}")
    fun listarPorCliente(@PathVariable clienteId: Long): List<Tasacion> {
        return tasacionRepository.findByClienteId(clienteId)
    }

    @GetMapping("/admin/pendientes")
    fun listarPendientes(): List<Tasacion> {
        return tasacionRepository.findByEstado("PENDIENTE")
    }

    @GetMapping("/admin/todas")
    fun listarTodas(): List<Tasacion> {
        return tasacionRepository.findAll()
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun enviarTasacion(
        @RequestParam clienteId: Long,
        @RequestParam descripcion: String,
        @RequestParam foto: MultipartFile
    ): ResponseEntity<Tasacion> {
        val uploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()
        Files.createDirectories(uploadPath)

        val uniqueName = "${UUID.randomUUID()}_${foto.originalFilename ?: "carta.jpg"}"
        val dest = uploadPath.resolve(uniqueName)
        foto.transferTo(dest)

        val tasacion = Tasacion(
            clienteId = clienteId,
            descripcion = descripcion,
            rutaFoto = uniqueName,
            estado = "PENDIENTE",
            fechaSubida = LocalDateTime.now()
        )

        val saved = tasacionRepository.save(tasacion)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }

    // ADMIN: Valorar o Rechazar tasación
    @PostMapping("/admin/{id}/valorar")
    fun valorarTasacion(
        @PathVariable id: Long,
        @RequestBody request: ValorarTasacionRequest
    ): ResponseEntity<MensajeResponse> {
        val tasacion = tasacionRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (tasacion.estado != "PENDIENTE") {
            return ResponseEntity.badRequest().body(MensajeResponse("Esta tasación ya fue procesada", false))
        }

        val cliente = clienteRepository.findById(tasacion.clienteId).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("El cliente asociado no existe", false))

        tasacion.estado = request.estado
        tasacion.notasAdmin = request.notasAdmin

        if (request.estado == "VALORADA") {
            tasacion.valorEstimado = request.valorEstimado
            // Sumar al monedero virtual del cliente (Store Credit) si es valorada
            cliente.saldo += request.valorEstimado
            clienteRepository.save(cliente)

            // Registrar ingreso
            gastoIngresoRepository.save(
                GastoIngreso(
                    clienteId = tasacion.clienteId,
                    tipo = TipoMovimiento.INGRESO,
                    descripcion = "Ingreso por tasación: ${tasacion.descripcion}",
                    importe = request.valorEstimado,
                    categoria = "TASACION",
                    fecha = LocalDate.now()
                )
            )
        }

        tasacionRepository.save(tasacion)
        return ResponseEntity.ok(MensajeResponse("Tasación procesada correctamente", true))
    }

    // Servir imágenes de tasación
    @GetMapping("/foto/{id}")
    fun obtenerFoto(@PathVariable id: Long, response: jakarta.servlet.http.HttpServletResponse) {
        val tasacion = tasacionRepository.findById(id).orElse(null) ?: return
        val path = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(tasacion.rutaFoto)

        if (!Files.exists(path)) {
            response.status = 404
            return
        }

        response.contentType = MediaType.IMAGE_JPEG_VALUE
        Files.copy(path, response.outputStream)
        response.outputStream.flush()
    }
}
