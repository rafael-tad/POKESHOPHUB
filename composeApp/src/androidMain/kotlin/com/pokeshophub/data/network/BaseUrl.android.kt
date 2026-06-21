package com.pokeshophub.data.network

import android.os.Build

/**
 * Configuración de producción para Render.
 * - Cambia IS_PRODUCTION a true para que la app móvil se conecte al servidor en la nube.
 * - Coloca la URL de tu servicio web de Render en PRODUCTION_URL (ej: "https://pokeshop-backend.onrender.com").
 */
private const val IS_PRODUCTION = false
private const val PRODUCTION_URL = "https://pokeshop-backend.onrender.com" 

actual fun getBaseUrl(): String {
    return if (IS_PRODUCTION) PRODUCTION_URL else "http://127.0.0.1:8080"
}
