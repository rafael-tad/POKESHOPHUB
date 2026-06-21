package com.pokeshophub.data.network

private const val IS_PRODUCTION = true
private const val PRODUCTION_URL = "https://pokeshophub-backend.onrender.com"

actual fun getBaseUrl(): String {
    return if (IS_PRODUCTION) PRODUCTION_URL else "http://localhost:8080"
}
