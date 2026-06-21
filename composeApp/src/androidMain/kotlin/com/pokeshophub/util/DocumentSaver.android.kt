package com.pokeshophub.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
actual fun rememberDocumentSaver(): (ByteArray, String, String) -> Unit {
    val context = LocalContext.current
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    // Usamos CreateDocument con "*/*" para abarcar cualquier tipo de archivo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val bytes = pendingBytes
        if (uri != null && bytes != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(bytes)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingBytes = null
                }
            }
        } else {
            pendingBytes = null
        }
    }
    
    return remember(launcher) {
        { bytes, fileName, _ ->
            pendingBytes = bytes
            launcher.launch(fileName)
        }
    }
}
