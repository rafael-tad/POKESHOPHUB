package com.pokeshophub.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

/** Motor HTTP para Desktop (JVM) */
actual fun createHttpClientEngine(): HttpClientEngine = CIO.create()
