package com.pokeshophub.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

/** Motor HTTP para Android (OkHttp) — expect/actual pattern */
actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()
