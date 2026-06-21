package com.pokeshophub.util

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap.asImageBitmap()
}

actual fun combineFrontBackImages(frontBytes: ByteArray, backBytes: ByteArray): ByteArray {
    val bmp1 = BitmapFactory.decodeByteArray(frontBytes, 0, frontBytes.size)
    val bmp2 = BitmapFactory.decodeByteArray(backBytes, 0, backBytes.size)
    
    val width = maxOf(bmp1.width, bmp2.width)
    val height = bmp1.height + bmp2.height
    
    val combined = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(combined)
    
    canvas.drawBitmap(bmp1, 0f, 0f, null)
    canvas.drawBitmap(bmp2, 0f, bmp1.height.toFloat(), null)
    
    val stream = java.io.ByteArrayOutputStream()
    combined.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
    
    bmp1.recycle()
    bmp2.recycle()
    combined.recycle()
    
    return stream.toByteArray()
}
