package com.pokeshophub.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.pokeshophub.ui.theme.TextoSecundario
import com.pokeshophub.ui.theme.VerdeConfirmacion
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun DocumentViewer(
    bytes: ByteArray,
    fileName: String,
    modifier: Modifier,
    firmaBitmap: ImageBitmap?,
    mostrarFirma: Boolean
) {
    val context = LocalContext.current
    val isPdf = fileName.endsWith(".pdf", ignoreCase = true)

    // ── Imágenes ─────────────────────────────────────────────────
    if (!isPdf) {
        var bitmap by remember(bytes) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(bytes) {
            withContext(Dispatchers.IO) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = fileName,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                } ?: Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            if (mostrarFirma) {
                item { FirmaBlock(firmaBitmap) }
            }
        }
        return
    }

    // ── PDF ───────────────────────────────────────────────────────
    var pages by remember(bytes) { mutableStateOf<List<Bitmap>?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bytes) {
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            val tempFile = File(context.cacheDir, "temp_preview_${System.currentTimeMillis()}.pdf")
            try {
                FileOutputStream(tempFile).use { it.write(bytes) }
                pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                val list = mutableListOf<Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    list.add(bmp)
                    page.close()
                }
                pages = list
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "Error al renderizar el PDF: ${e.localizedMessage}"
            } finally {
                try { renderer?.close() } catch (_: Exception) {}
                try { pfd?.close() } catch (_: Exception) {}
                try { tempFile.delete() } catch (_: Exception) {}
            }
        }
    }

    when {
        errorMsg != null -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(errorMsg!!, modifier = Modifier.padding(16.dp))
        }
        pages == null -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pages!!) { bmp ->
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Página PDF",
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                }
            }
            // Bloque de firma al final del PDF
            if (mostrarFirma) {
                item { FirmaBlock(firmaBitmap) }
            }
        }
    }
}

@Composable
private fun FirmaBlock(firmaBitmap: ImageBitmap?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Draw, null,
                    tint = if (firmaBitmap != null) VerdeConfirmacion else TextoSecundario
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (firmaBitmap != null) "Firma Digital" else "Sin firma registrada",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (firmaBitmap != null) VerdeConfirmacion else TextoSecundario
                )
            }
            if (firmaBitmap != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = firmaBitmap,
                        contentDescription = "Firma del cliente",
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Firmado digitalmente por el titular del documento",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario
                )
            }
        }
    }
}
