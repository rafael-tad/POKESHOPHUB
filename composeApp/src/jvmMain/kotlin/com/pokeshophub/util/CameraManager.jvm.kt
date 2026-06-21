package com.pokeshophub.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher {
    return remember {
        object : CameraLauncher {
            override fun launch() {
                // En Desktop podrÃ­amos abrir un selector de archivos como alternativa
                // Por ahora devolvemos null o un placeholder
                onResult(null)
            }
        }
    }
}
