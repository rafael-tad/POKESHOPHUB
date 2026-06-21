package com.pokeshophub.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun DocumentViewer(
    bytes: ByteArray,
    fileName: String,
    modifier: Modifier = Modifier,
    firmaBitmap: ImageBitmap? = null,
    mostrarFirma: Boolean = true
)
