package com.pokeshophub.controller

import com.pokeshophub.config.JwtService
import com.pokeshophub.dto.*
import com.pokeshophub.model.Rol
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.TrabajadorRepository
import com.pokeshophub.model.Cliente
import com.pokeshophub.service.AuditoriaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val clienteRepository: ClienteRepository,
    private val trabajadorRepository: TrabajadorRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val auditoriaService: AuditoriaService
) {

    /**
     * Login unificado para clientes y trabajadores.
     * Busca primero en la tabla de clientes, luego en trabajadores.
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any> {
        println("AUTH: Intento de login para email: ${request.email}")
        // Intentar como cliente
        val cliente = clienteRepository.findByEmail(request.email)
        if (cliente != null) {
            println("AUTH: Cliente encontrado. Activo: ${cliente.activo}")
            if (!cliente.aprobado) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(MensajeResponse("Tu cuenta está pendiente de revisión por un administrador.", false))
            }
            if (!passwordEncoder.matches(request.password, cliente.password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensajeResponse("Credenciales incorrectas", false))
            }
            val token = jwtService.generateToken(cliente.email, Rol.CLIENTE.name, cliente.id)
            auditoriaService.registrar(
                clienteId = cliente.id,
                trabajadorId = null,
                tipo = "SISTEMA",
                nota = "Cliente ${cliente.nombre} ${cliente.apellidos} inició sesión"
            )
            return ResponseEntity.ok(LoginResponse(token, Rol.CLIENTE.name, cliente.id, "${cliente.nombre} ${cliente.apellidos}"))
        }

        // Intentar como trabajador
        val trabajador = trabajadorRepository.findByEmail(request.email)
        if (trabajador != null) {
            println("AUTH: Trabajador encontrado. Activo: ${trabajador.activo}")
            if (!trabajador.activo) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(MensajeResponse("Cuenta de trabajador desactivada.", false))
            }
            if (!passwordEncoder.matches(request.password, trabajador.password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensajeResponse("Credenciales incorrectas", false))
            }
            val token = jwtService.generateToken(trabajador.email, trabajador.rol.name, trabajador.id)
            auditoriaService.registrar(
                clienteId = null,
                trabajadorId = trabajador.id,
                tipo = "SISTEMA",
                nota = "Personal ${trabajador.nombre} ${trabajador.apellidos} (${trabajador.rol}) inició sesión"
            )
            return ResponseEntity.ok(LoginResponse(token, trabajador.rol.name, trabajador.id, "${trabajador.nombre} ${trabajador.apellidos}"))
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(MensajeResponse("No existe ninguna cuenta con ese email", false))
    }

    /**
     * Registro de nuevos clientes (público).
     */
    @PostMapping("/registro")
    fun registro(@Valid @RequestBody request: RegistroClienteRequest): ResponseEntity<Any> {
        if (clienteRepository.findByEmail(request.email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(MensajeResponse("Ya existe una cuenta con ese email", false))
        }
        if (clienteRepository.findByDni(request.dni) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(MensajeResponse("Ya existe una cuenta con ese DNI", false))
        }

        val cliente = Cliente(
            nombre = request.nombre,
            apellidos = request.apellidos,
            dni = request.dni,
            email = request.email,
            telefono = request.telefono,
            direccion = request.direccion,
            password = passwordEncoder.encode(request.password),
            aprobado = false
        )
        val saved = clienteRepository.save(cliente)
        auditoriaService.registrar(
            clienteId = saved.id,
            trabajadorId = null,
            tipo = "SISTEMA",
            nota = "Nuevo cliente registrado (pendiente de aprobación): ${saved.nombre} ${saved.apellidos}"
        )
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(MensajeResponse("Registro realizado con éxito. Tu cuenta está pendiente de revisión por un administrador.", true))
    }

    /**
     * Endpoint temporal para crear un administrador y poder probar la app.
     * WARNING: Este endpoint es inseguro y debe eliminarse en producción.
     */
    @GetMapping("/crear-admin")
    fun crearAdminDePrueba(): ResponseEntity<String> {
        val email = "admin@pokeshophub.com"
        if (trabajadorRepository.findByEmail(email) != null) {
            return ResponseEntity.ok("El administrador ya existe. Email: $email | Password: admin")
        }
        val admin = com.pokeshophub.model.Trabajador(
            nombre = "Administrador",
            apellidos = "Principal",
            email = email,
            password = passwordEncoder.encode("admin"),
            rol = com.pokeshophub.model.Rol.ADMIN
        )
        trabajadorRepository.save(admin)
        return ResponseEntity.ok("✅ Administrador creado con éxito.<br>Email: <b>$email</b><br>Contraseña: <b>admin</b>")
    }
}
