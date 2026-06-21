package com.pokeshophub.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberDocumentOpener(): (ByteArray, String) -> Unit
