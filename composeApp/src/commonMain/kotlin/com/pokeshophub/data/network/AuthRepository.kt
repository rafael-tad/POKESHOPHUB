package com.pokeshophub.data.network

import com.russhwolf.settings.Settings
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.pokeshophub.data.model.*

/**
 * Repositorio de autenticaciÃ³n.
 * Gestiona login, registro y persistencia local del JWT.
 */
class AuthRepository(private val settings: Settings = Settings()) {
        companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_ROL = "user_rol"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NOMBRE = "user_nombre"
    }

    /** Inicia sesiÃ³n. Devuelve la sesiÃ³n si tiene Ã©xito, null si las credenciales son incorrectas. */
    suspend fun login(email: String, password: String): Result<SesionUsuario> {
        return try {
            val response = httpClient.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            if (response.status.isSuccess()) {
                val loginResp = response.body<LoginResponse>()
                val sesion = loginResp.toSesion()
                guardarSesion(sesion)
                TokenStore.token = sesion.token
                Result.success(sesion)
            } else {
                val error = response.body<MensajeResponse>()
            }
            if (response.status.isSuccess()) {
                val loginResp = response.body<LoginResponse>()
                val sesion = loginResp.toSesion()
                guardarSesion(sesion)
                TokenStore.token = sesion.token
                Result.success(sesion)
            } else {
                val error = response.body<MensajeResponse>()
                Result.failure(Exception(error.mensaje))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /** Registra un nuevo cliente. Retorna mensaje de éxito. */
    suspend fun registro(
        nombre: String,
        apellidos: String,
        dni: String,
        email: String,
        telefono: String,
        direccion: String,
        password: String
    ): Result<String> {
        return try {
            val response = httpClient.post("/api/auth/registro") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nombre, apellidos, dni, email, telefono, direccion, password))
            }
            if (response.status.isSuccess()) {
                val res = response.body<MensajeResponse>()
                Result.success(res.mensaje)
            } else {
                val error = response.body<MensajeResponse>()
                Result.failure(Exception(error.mensaje))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /** Recupera la sesión guardada localmente (para auto-login). */
    fun sesionGuardada(): SesionUsuario? {
        val token = settings.getStringOrNull(KEY_TOKEN) ?: return null
        val rol = settings.getStringOrNull(KEY_ROL) ?: return null
        val userId = settings.getLongOrNull(KEY_USER_ID) ?: return null
        val nombre = settings.getStringOrNull(KEY_NOMBRE) ?: return null
        TokenStore.token = token
        return SesionUsuario(token, rol, userId, nombre)
    }

    /** Cierra sesiÃ³n limpiando el almacenamiento local. */
    fun cerrarSesion() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_ROL)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_NOMBRE)
        TokenStore.token = null
    }

    private fun guardarSesion(sesion: SesionUsuario) {
        settings.putString(KEY_TOKEN, sesion.token)
        settings.putString(KEY_ROL, sesion.rol)
        settings.putLong(KEY_USER_ID, sesion.userId)
        settings.putString(KEY_NOMBRE, sesion.nombre)
    }

    private fun LoginResponse.toSesion() = SesionUsuario(token, rol, userId, nombre)
}
