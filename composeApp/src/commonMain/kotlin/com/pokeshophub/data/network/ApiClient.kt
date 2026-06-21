package com.pokeshophub.data.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.header

/**
 * URL base del backend.
 * - Desktop (Windows): http://localhost:8080
 * - Emulador Android: http://10.0.2.2:8080  (10.0.2.2 = tu PC desde el emulador)
 * - Dispositivo fÃ­sico: http://TU_IP_LOCAL:8080  (ej: http://192.168.1.50:8080)
 *
 * En desarrollo, usa la funciÃ³n expect/actual para que cada plataforma use su URL correcta.
 */
val BASE_URL = getBaseUrl()

/**
 * Proveedor de token JWT â€” se configura tras el login.
 * Usamos un singleton mutable para actualizar el token en runtime.
 */
object TokenStore {
    var token: String? = null
}

/**
 * Cliente HTTP Ktor compartido para toda la app.
 * El motor (OkHttp en Android, CIO en Desktop) se inyecta por expect/actual.
 */
val httpClient = HttpClient(createHttpClientEngine()) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
        logger = Logger.SIMPLE
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
    }
    defaultRequest {
        url(BASE_URL)
        TokenStore.token?.let {
            header("Authorization", "Bearer $it")
        }
    }
}
