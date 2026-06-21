package com.pokeshophub.util

import androidx.compose.ui.graphics.Path

actual fun encodePathAsPng(path: Path, width: Int, height: Int): ByteArray {
    // Desktop: return empty placeholder (signature saving not needed on desktop)
    return ByteArray(0)
}
