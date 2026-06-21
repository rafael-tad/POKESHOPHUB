package com.pokeshophub.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberDocumentSaver(): (ByteArray, String, String) -> Unit // (bytes, fileName, mimeType)
