package com.pokeshophub.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser

@Composable
actual fun rememberDocumentSaver(): (ByteArray, String, String) -> Unit {
    return remember {
        { bytes, fileName, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fileChooser = JFileChooser().apply {
                        selectedFile = File(fileName)
                        dialogTitle = "Guardar documento"
                    }
                    val userSelection = fileChooser.showSaveDialog(null)
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        val fileToSave = fileChooser.selectedFile
                        fileToSave.writeBytes(bytes)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
