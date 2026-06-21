package com.pokeshophub.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberDocumentOpener(): (ByteArray, String) -> Unit {
    return remember {
        { bytes, fileName ->
            try {
                // Escribir bytes a un archivo temporal
                val file = File(System.getProperty("java.io.tmpdir"), fileName)
                file.writeBytes(bytes)
                
                // Abrir con el visor predeterminado del sistema operativo
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                } else {
                    println("Desktop no soportado en este entorno.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
