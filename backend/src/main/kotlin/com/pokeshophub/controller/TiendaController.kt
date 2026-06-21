package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.Producto
import com.pokeshophub.model.GastoIngreso
import com.pokeshophub.repository.ProductoRepository
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.GastoIngresoRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.springframework.http.MediaType
import java.util.UUID

@RestController
@RequestMapping("/api/tienda")
class TiendaController(
    private val productoRepository: ProductoRepository,
    private val clienteRepository: ClienteRepository,
    private val gastoIngresoRepository: GastoIngresoRepository
) {

    @Value("\${app.upload.dir}")
    private lateinit var uploadDir: String

    @GetMapping("/productos")
    fun listarProductos(): List<Producto> = productoRepository.findAll()

    @GetMapping("/productos/{id}")
    fun obtenerProducto(@PathVariable id: Long): ResponseEntity<Producto> {
        val producto = productoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(producto)
    }

    // Comprar producto usando saldo del monedero virtual
    @PostMapping("/comprar/{clienteId}")
    fun comprarProducto(
        @PathVariable clienteId: Long,
        @RequestBody request: CompraRequest
    ): ResponseEntity<MensajeResponse> {
        val cliente = clienteRepository.findById(clienteId).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("Cliente no encontrado", false))

        val producto = productoRepository.findById(request.productoId).orElse(null)
            ?: return ResponseEntity.badRequest().body(MensajeResponse("Producto no encontrado", false))

        if (producto.stock < request.cantidad) {
            return ResponseEntity.badRequest().body(MensajeResponse("Stock insuficiente", false))
        }

        val costeTotal = producto.precio * request.cantidad
        if (cliente.saldo < costeTotal) {
            return ResponseEntity.badRequest().body(MensajeResponse("Saldo insuficiente en el monedero virtual", false))
        }

        // Restar stock y restar saldo
        producto.stock -= request.cantidad
        cliente.saldo -= costeTotal

        productoRepository.save(producto)
        clienteRepository.save(cliente)

        // Registrar transacción
        gastoIngresoRepository.save(
            GastoIngreso(
                clienteId = clienteId,
                tipo = com.pokeshophub.model.TipoMovimiento.GASTO,
                descripcion = "Compra: ${request.cantidad}x ${producto.nombre}",
                importe = costeTotal,
                categoria = "TIENDA",
                fecha = java.time.LocalDate.now()
            )
        )

        return ResponseEntity.ok(MensajeResponse("Compra realizada con éxito", true))
    }

    // ADMIN: Crear producto
    @PostMapping("/admin/productos")
    fun crearProducto(@RequestBody request: CrearProductoRequest): Producto {
        val producto = Producto(
            nombre = request.nombre,
            descripcion = request.descripcion,
            precio = request.precio,
            stock = request.stock,
            imagenUrl = request.imagenUrl,
            categoria = request.categoria
        )
        return productoRepository.save(producto)
    }

    // ADMIN: Actualizar producto/stock
    @PutMapping("/admin/productos/{id}")
    fun actualizarProducto(
        @PathVariable id: Long,
        @RequestBody request: CrearProductoRequest
    ): ResponseEntity<Producto> {
        val producto = productoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        producto.nombre = request.nombre
        producto.descripcion = request.descripcion
        producto.precio = request.precio
        producto.stock = request.stock
        producto.imagenUrl = request.imagenUrl
        producto.categoria = request.categoria

        return ResponseEntity.ok(productoRepository.save(producto))
    }

    // ADMIN: Eliminar producto
    @DeleteMapping("/admin/productos/{id}")
    fun eliminarProducto(@PathVariable id: Long): ResponseEntity<MensajeResponse> {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        productoRepository.deleteById(id)
        return ResponseEntity.ok(MensajeResponse("Producto eliminado correctamente"))
    }

    // ADMIN: Subir foto de un producto
    @PostMapping("/admin/productos/{id}/foto", consumes = [org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE])
    fun subirFoto(
        @PathVariable id: Long,
        @RequestParam foto: MultipartFile
    ): ResponseEntity<Producto> {
        val producto = productoRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        
        val uploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()
        Files.createDirectories(uploadPath)

        val uniqueName = "${UUID.randomUUID()}_${foto.originalFilename ?: "producto.jpg"}"
        val dest = uploadPath.resolve(uniqueName)
        foto.transferTo(dest)

        producto.imagenUrl = uniqueName
        val saved = productoRepository.save(producto)
        return ResponseEntity.ok(saved)
    }

    // Servir imagen de producto
    @GetMapping("/productos/foto/{id}")
    fun obtenerFoto(@PathVariable id: Long, response: jakarta.servlet.http.HttpServletResponse) {
        val producto = productoRepository.findById(id).orElse(null) ?: return
        val imgUrl = producto.imagenUrl ?: return
        val path = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(imgUrl)

        if (!Files.exists(path)) {
            response.status = 404
            return
        }

        response.contentType = MediaType.IMAGE_JPEG_VALUE
        Files.copy(path, response.outputStream)
        response.outputStream.flush()
    }
}
