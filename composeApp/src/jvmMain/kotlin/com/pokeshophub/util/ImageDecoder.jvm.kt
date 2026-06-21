package com.pokeshophub.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

actual fun combineFrontBackImages(frontBytes: ByteArray, backBytes: ByteArray): ByteArray {
    val img1 = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(frontBytes))
    val img2 = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(backBytes))
    
    val width = maxOf(img1.width, img2.width)
    val height = img1.height + img2.height
    
    val combined = java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB)
    val g2d = combined.createGraphics()
    g2d.drawImage(img1, 0, 0, null)
    g2d.drawImage(img2, 0, img1.height, null)
    g2d.dispose()
    
    val stream = java.io.ByteArrayOutputStream()
    javax.imageio.ImageIO.write(combined, "jpg", stream)
    return stream.toByteArray()
}
