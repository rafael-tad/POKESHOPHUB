package com.pokeshophub.config

import com.pokeshophub.dto.MensajeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<MensajeResponse> {
        val errors = ex.bindingResult.fieldErrors
        val errorMsg = errors.firstOrNull()?.let { error ->
            val field = when (error.field) {
                "email" -> "Correo electrónico"
                "password" -> "Contraseña"
                "nombre" -> "Nombre"
                "apellidos" -> "Apellidos"
                "dni" -> "DNI/NIE"
                else -> error.field
            }
            val reason = when (error.code) {
                "Email" -> "debe ser una dirección de correo válida"
                "Size" -> "debe tener al menos 8 caracteres"
                "NotBlank" -> "no puede estar vacío"
                else -> error.defaultMessage ?: "no es válido"
            }
            "$field: $reason."
        } ?: "Campos del formulario incorrectos."

        return ResponseEntity.badRequest().body(MensajeResponse(errorMsg, false))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<MensajeResponse> {
        val msg = ex.localizedMessage ?: "Ocurrió un error inesperado en el servidor."
        return ResponseEntity.status(500).body(MensajeResponse("Error del servidor: $msg", false))
    }
}
