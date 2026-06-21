package com.pokeshophub.util

import androidx.compose.ui.graphics.Path

/**
 * Platform-specific function to encode a Compose [Path] as PNG bytes.
 * Width/height define the canvas size used during drawing.
 */
expect fun encodePathAsPng(path: Path, width: Int, height: Int): ByteArray
