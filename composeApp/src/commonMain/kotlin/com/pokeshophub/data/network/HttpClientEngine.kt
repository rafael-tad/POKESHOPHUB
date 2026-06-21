package com.pokeshophub.data.network

import io.ktor.client.engine.*

/** expect declaration â€” implementado en androidMain y jvmMain */
expect fun createHttpClientEngine(): HttpClientEngine
