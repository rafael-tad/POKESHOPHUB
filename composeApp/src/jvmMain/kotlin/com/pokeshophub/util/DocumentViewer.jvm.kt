package com.pokeshophub.util

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import com.pokeshophub.ui.theme.TextoSecundario

@Composable
actual fun DocumentViewer(
    bytes: ByteArray,
    fileName: String,
    modifier: Modifier,
    firmaBitmap: ImageBitmap?,
    mostrarFirma: Boolean
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Vista previa no disponible para Escritorio.\nUsa el botÃ³n de descarga para ver el archivo '$fileName'.",
            textAlign = TextAlign.Center,
            color = TextoSecundario
        )
    }
}

