package com.pokeshophub.util

import androidx.compose.ui.graphics.ImageBitmap

/** Decodes raw PNG/JPG bytes into a Compose [ImageBitmap]. */
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap

/** Combines front and back image bytes vertically into a single JPEG byte array. */
expect fun combineFrontBackImages(frontBytes: ByteArray, backBytes: ByteArray): ByteArray
