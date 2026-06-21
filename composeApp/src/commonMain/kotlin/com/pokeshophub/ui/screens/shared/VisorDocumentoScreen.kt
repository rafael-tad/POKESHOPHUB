package com.pokeshophub.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.NaranjaAccento
import com.pokeshophub.util.DocumentViewer
import com.pokeshophub.util.decodeImageBitmap
import com.pokeshophub.util.rememberDocumentSaver

class VisorDocumentoScreen(
    private val documentoId: Long,
    private val nombreArchivo: String,
    private val downloadUrl: String? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val saver = rememberDocumentSaver()

        var bytes by remember { mutableStateOf<ByteArray?>(null) }
        var firmaBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(documentoId, downloadUrl) {
            try {
                cargando = true
                errorMsg = null
                val urlToFetch = downloadUrl ?: "/api/documentos/$documentoId/descargar"
                val response = httpClient.get(urlToFetch)
                if (response.status.value !in 200..299) {
                    errorMsg = if (response.status.value == 404) {
                        "El archivo no existe en el servidor. Debido al almacenamiento temporal de la demo, se ha eliminado. Por favor, vuelve a subirlo."
                    } else {
                        "No se pudo descargar el documento: Código de respuesta ${response.status.value}"
                    }
                } else {
                    bytes = response.body()
                    // Intentar cargar la firma si es un documento de cliente habitual (no curso)
                    if (downloadUrl == null) {
                        try {
                            val fbResponse = httpClient.get("/api/documentos/$documentoId/firma")
                            if (fbResponse.status.value in 200..299) {
                                firmaBitmap = decodeImageBitmap(fbResponse.body())
                            }
                        } catch (_: Exception) { }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "No se pudo descargar el documento: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = nombreArchivo,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    actions = {
                        bytes?.let { fileBytes ->
                            IconButton(onClick = {
                                val mimeType = when {
                                    nombreArchivo.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                                    nombreArchivo.endsWith(".jpg", ignoreCase = true) ||
                                    nombreArchivo.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                    nombreArchivo.endsWith(".png", ignoreCase = true) -> "image/png"
                                    else -> "application/octet-stream"
                                }
                                saver(fileBytes, nombreArchivo, mimeType)
                            }) {
                                Icon(Icons.Default.Download, "Guardar en dispositivo", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NaranjaAccento,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                when {
                    cargando -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = NaranjaAccento)
                            Spacer(Modifier.height(16.dp))
                            Text("Descargando vista previa...", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    errorMsg != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        cargando = true
                                        errorMsg = null
                                        try {
                                            val urlToFetch = downloadUrl ?: "/api/documentos/$documentoId/descargar"
                                            val response = httpClient.get(urlToFetch)
                                            if (response.status.value !in 200..299) {
                                                errorMsg = if (response.status.value == 404) {
                                                    "El archivo no existe en el servidor. Debido al almacenamiento temporal de la demo, se ha eliminado. Por favor, vuelve a subirlo."
                                                } else {
                                                    "No se pudo descargar el documento: Código de respuesta ${response.status.value}"
                                                }
                                            } else {
                                                bytes = response.body()
                                            }
                                        } catch (e: Exception) {
                                            errorMsg = "No se pudo descargar el documento: ${e.localizedMessage}"
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NaranjaAccento)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                    bytes != null -> {
                        // DocumentViewer ya incluye su propia LazyColumn y el bloque de firma al final
                        DocumentViewer(
                            bytes = bytes!!,
                            fileName = nombreArchivo,
                            modifier = Modifier.fillMaxSize(),
                            firmaBitmap = firmaBitmap,
                            mostrarFirma = (downloadUrl == null)
                        )
                    }
                }
            }
        }
    }
}
