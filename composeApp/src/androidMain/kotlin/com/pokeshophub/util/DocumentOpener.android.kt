package com.pokeshophub.util

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberDocumentOpener(): (ByteArray, String) -> Unit {
    val context = LocalContext.current
    return remember {
        { bytes, fileName ->
            try {
                // Escribir bytes a un archivo temporal en caché
                val file = File(context.cacheDir, fileName)
                file.writeBytes(bytes)
                
                // Obtener URI segura mediante FileProvider
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                
                // Determinar el MIME type
                val mimeType = when {
                    fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                    fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                    else -> "*/*"
                }
                
                // Crear y lanzar el Intent para abrir el visor nativo
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
