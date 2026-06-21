package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*
import com.pokeshophub.util.rememberCameraLauncher

class GestionStockScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var refreshTrigger by remember { mutableStateOf(0) }

        // Diálogo de agregar/editar
        var mostrarDialogo by remember { mutableStateOf(false) }
        var esEdicion by remember { mutableStateOf(false) }
        var productoEditadoId by remember { mutableStateOf<Long?>(null) }
        var nombre by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        var stock by remember { mutableStateOf("") }
        var categoria by remember { mutableStateOf("Cartas") }
        var photoBytes by remember { mutableStateOf<ByteArray?>(null) }
        var guardando by remember { mutableStateOf(false) }
        var dialogStatusMsg by remember { mutableStateOf<String?>(null) }

        val cameraLauncher = rememberCameraLauncher { bytes ->
            if (bytes != null) {
                photoBytes = bytes
            }
        }

        fun cargarProductos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    productos = httpClient.get("/api/tienda/productos").body()
                } catch (e: Exception) {
                    errorMsg = "Error al obtener stock: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(refreshTrigger) {
            cargarProductos()
        }

        fun eliminarProducto(id: Long) {
            scope.launch {
                try {
                    val response = httpClient.delete("/api/tienda/admin/productos/$id")
                    if (response.status.value in 200..299) {
                        refreshTrigger++
                    } else {
                        val res = response.body<MensajeResponse>()
                        errorMsg = res.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al eliminar producto: ${e.message}"
                }
            }
        }

        fun guardarProducto() {
            val dPrecio = precio.replace(",", ".").toDoubleOrNull() ?: 0.0
            val iStock = stock.toIntOrNull() ?: 0
            if (nombre.isBlank() || dPrecio <= 0.0 || iStock < 0) {
                dialogStatusMsg = "Introduce datos válidos"
                return
            }

            scope.launch {
                guardando = true
                dialogStatusMsg = "Guardando producto..."
                try {
                    val req = CrearProductoRequest(
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = dPrecio,
                        stock = iStock,
                        categoria = categoria
                    )

                    val savedProd: Producto = if (esEdicion) {
                        httpClient.put("/api/tienda/admin/productos/$productoEditadoId") {
                            contentType(ContentType.Application.Json)
                            setBody(req)
                        }.body()
                    } else {
                        httpClient.post("/api/tienda/admin/productos") {
                            contentType(ContentType.Application.Json)
                            setBody(req)
                        }.body()
                    }

                    // Si hay foto que subir
                    val bytes = photoBytes
                    if (bytes != null) {
                        dialogStatusMsg = "Subiendo imagen del producto..."
                        httpClient.post("/api/tienda/admin/productos/${savedProd.id}/foto") {
                            setBody(MultiPartFormDataContent(
                                formData {
                                    append("foto", bytes, Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"producto.jpg\"")
                                    })
                                }
                            ))
                        }
                    }

                    mostrarDialogo = false
                    photoBytes = null
                    nombre = ""
                    descripcion = ""
                    precio = ""
                    stock = ""
                    categoria = "Cartas"
                    refreshTrigger++
                } catch (e: Exception) {
                    dialogStatusMsg = "Error al guardar: ${e.message}"
                } finally {
                    guardando = false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestión de Stock TCG", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrimario)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        esEdicion = false
                        productoEditadoId = null
                        nombre = ""
                        descripcion = ""
                        precio = ""
                        stock = ""
                        categoria = "Cartas"
                        photoBytes = null
                        dialogStatusMsg = null
                        mostrarDialogo = true
                    },
                    containerColor = AzulPrimario,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Crear Producto")
                }
            },
            containerColor = FondoApp
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (cargando && productos.isEmpty()) {
                    CircularProgressIndicator(
                        color = AzulPrimario,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        errorMsg?.let { msg ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = RojoError.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, RojoError.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, null, tint = RojoError)
                                    Spacer(Modifier.width(10.dp))
                                    Text(msg, color = TextoPrimario, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { errorMsg = null }) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (productos.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay productos en el inventario. Añade uno con el botón +", color = TextoSecundario, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(productos) { prod ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Superficie),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    prod.nombre,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextoPrimario
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text("Precio: ${prod.precio} € | Stock: ${prod.stock} uds", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                                Text("Categoría: ${prod.categoria}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario.copy(alpha = 0.8f))
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = {
                                                    esEdicion = true
                                                    productoEditadoId = prod.id
                                                    nombre = prod.nombre
                                                    descripcion = prod.descripcion
                                                    precio = prod.precio.toString()
                                                    stock = prod.stock.toString()
                                                    categoria = prod.categoria
                                                    photoBytes = null
                                                    dialogStatusMsg = null
                                                    mostrarDialogo = true
                                                }) {
                                                    Icon(Icons.Default.Edit, "Editar", tint = AzulPrimario)
                                                }
                                                IconButton(onClick = { eliminarProducto(prod.id) }) {
                                                    Icon(Icons.Default.Delete, "Eliminar", tint = RojoError)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Modal Crear/Editar Producto
                if (mostrarDialogo) {
                    AlertDialog(
                        onDismissRequest = { if (!guardando) mostrarDialogo = false },
                        title = { Text(if (esEdicion) "Editar Producto" else "Crear Producto", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = descripcion,
                                    onValueChange = { descripcion = it },
                                    label = { Text("Descripción") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = precio,
                                    onValueChange = { precio = it },
                                    label = { Text("Precio (€)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = stock,
                                    onValueChange = { stock = it },
                                    label = { Text("Stock inicial") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = categoria,
                                    onValueChange = { categoria = it },
                                    label = { Text("Categoría (ej: Cartas, Sobres, Accesorios)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { cameraLauncher.launch() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Tomar Foto", fontSize = 12.sp)
                                    }
                                    if (photoBytes != null) {
                                        Icon(Icons.Default.CheckCircle, "Foto cargada", tint = VerdeConfirmacion, modifier = Modifier.size(18.dp))
                                        Text("Foto lista", fontSize = 11.sp, color = TextoSecundario)
                                    }
                                }

                                dialogStatusMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.contains("correctamente") || msg.contains("Guardando") || msg.contains("Subiendo")) VerdeConfirmacion else RojoError
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { guardarProducto() },
                                enabled = !guardando && nombre.isNotBlank() && precio.isNotBlank() && stock.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                            ) {
                                Text("Guardar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogo = false }, enabled = !guardando) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}