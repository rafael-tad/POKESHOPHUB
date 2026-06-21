package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class OrganizacionTorneosScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var torneos by remember { mutableStateOf<List<Torneo>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var refreshTrigger by remember { mutableStateOf(0) }

        // Diálogos de control
        var mostrarCrearDialogo by remember { mutableStateOf(false) }
        var torneoAEditarEstado by remember { mutableStateOf<Torneo?>(null) }

        // Formulario nuevo torneo
        var nombre by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var fecha by remember { mutableStateOf("") }
        var hora by remember { mutableStateOf("") }
        var maxPart by remember { mutableStateOf("16") }
        var precio by remember { mutableStateOf("5.0") }
        var guardando by remember { mutableStateOf(false) }
        var dialogStatusMsg by remember { mutableStateOf<String?>(null) }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    torneos = httpClient.get("/api/torneos").body()
                } catch (e: Exception) {
                    errorMsg = "Error al obtener torneos: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(refreshTrigger) {
            cargarDatos()
        }

        fun crearTorneo() {
            val iMaxPart = maxPart.toIntOrNull() ?: 16
            val dPrecio = precio.replace(",", ".").toDoubleOrNull() ?: 0.0
            if (nombre.isBlank() || fecha.isBlank() || hora.isBlank() || iMaxPart <= 0) {
                dialogStatusMsg = "Introduce datos válidos"
                return
            }

            scope.launch {
                guardando = true
                dialogStatusMsg = "Creando torneo..."
                try {
                    val req = CrearTorneoRequest(
                        nombre = nombre,
                        descripcion = descripcion,
                        fecha = fecha,
                        hora = hora,
                        maxParticipantes = iMaxPart,
                        precioInscripcion = dPrecio
                    )
                    val response = httpClient.post("/api/torneos/admin") {
                        contentType(ContentType.Application.Json)
                        setBody(req)
                    }
                    if (response.status.value in 200..299) {
                        mostrarCrearDialogo = false
                        nombre = ""
                        descripcion = ""
                        fecha = ""
                        hora = ""
                        maxPart = "16"
                        precio = "5.0"
                        refreshTrigger++
                    } else {
                        dialogStatusMsg = "Error al crear torneo en el servidor"
                    }
                } catch (e: Exception) {
                    dialogStatusMsg = "Error de conexión: ${e.message}"
                } finally {
                    guardando = false
                }
            }
        }

        fun cambiarEstado(torneoId: Long, nuevoEstado: String) {
            scope.launch {
                try {
                    val response = httpClient.patch("/api/torneos/admin/$torneoId/estado") {
                        parameter("estado", nuevoEstado)
                    }
                    if (response.status.value in 200..299) {
                        torneoAEditarEstado = null
                        refreshTrigger++
                    } else {
                        errorMsg = "Error al cambiar estado"
                    }
                } catch (e: Exception) {
                    errorMsg = "Error de conexión: ${e.message}"
                }
            }
        }

        fun eliminarTorneo(torneoId: Long) {
            scope.launch {
                try {
                    val response = httpClient.delete("/api/torneos/admin/$torneoId")
                    if (response.status.value in 200..299) {
                        torneoAEditarEstado = null
                        refreshTrigger++
                    } else {
                        errorMsg = "Error al eliminar torneo"
                    }
                } catch (e: Exception) {
                    errorMsg = "Error de conexión: ${e.message}"
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Organización de Torneos", color = Color.White, fontWeight = FontWeight.Bold) },
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
                        nombre = ""
                        descripcion = ""
                        fecha = ""
                        hora = ""
                        maxPart = "16"
                        precio = "5.0"
                        dialogStatusMsg = null
                        mostrarCrearDialogo = true
                    },
                    containerColor = AzulPrimario,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Crear Torneo")
                }
            },
            containerColor = FondoApp
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (cargando && torneos.isEmpty()) {
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

                        if (torneos.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay torneos registrados. Añade uno con el botón +", color = TextoSecundario, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(torneos) { torneo ->
                                    val (estadoLabel, colorLabel) = when (torneo.estado) {
                                        "CANCELADO" -> "Cancelado" to RojoError
                                        "FINALIZADO" -> "Finalizado" to Color.Gray
                                        "CERRADO" -> "Inscripciones Cerradas" to AmarilloAlerta
                                        else -> "Inscripciones Abiertas" to VerdeConfirmacion
                                    }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Superficie),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        onClick = { torneoAEditarEstado = torneo }
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    torneo.nombre,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextoPrimario
                                                )
                                                Surface(
                                                    color = colorLabel.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = estadoLabel,
                                                        color = colorLabel,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(8.dp))
                                            Text(torneo.descripcion, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                            Spacer(Modifier.height(8.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Fecha: ${torneo.fecha} • ${torneo.hora}", fontSize = 12.sp, color = TextoSecundario)
                                                Text("Precio: ${torneo.precioInscripcion} €", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MoradoSecundario)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text("Participantes: ${torneo.participantesCount} / ${torneo.maxParticipantes}", fontSize = 12.sp, color = TextoSecundario)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Modal Crear Torneo
                if (mostrarCrearDialogo) {
                    AlertDialog(
                        onDismissRequest = { if (!guardando) mostrarCrearDialogo = false },
                        title = { Text("Crear Nuevo Torneo", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre del Torneo") },
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
                                    value = fecha,
                                    onValueChange = { fecha = it },
                                    label = { Text("Fecha (AAAA-MM-DD)") },
                                    placeholder = { Text("2026-06-25") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = hora,
                                    onValueChange = { hora = it },
                                    label = { Text("Hora (HH:MM)") },
                                    placeholder = { Text("17:00") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = maxPart,
                                    onValueChange = { maxPart = it },
                                    label = { Text("Máx Participantes") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = precio,
                                    onValueChange = { precio = it },
                                    label = { Text("Precio Inscripción (€)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                dialogStatusMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.contains("Creando")) VerdeConfirmacion else RojoError
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { crearTorneo() },
                                enabled = !guardando && nombre.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                            ) {
                                Text("Crear", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarCrearDialogo = false }, enabled = !guardando) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Modal Administrar Torneo
                torneoAEditarEstado?.let { torneo ->
                    AlertDialog(
                        onDismissRequest = { torneoAEditarEstado = null },
                        title = { Text("Administrar Torneo", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Nombre: ${torneo.nombre}", fontSize = 14.sp, color = TextoPrimario)
                                Text("Estado actual: ${torneo.estado}", fontSize = 14.sp, color = TextoSecundario)
                                Divider()
                                Text("Cambiar Estado a:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoSecundario)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (torneo.estado != "ABIERTO") {
                                        Button(
                                            onClick = { cambiarEstado(torneo.id, "ABIERTO") },
                                            colors = ButtonDefaults.buttonColors(containerColor = VerdeConfirmacion),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Abrir", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                    if (torneo.estado != "CERRADO") {
                                        Button(
                                            onClick = { cambiarEstado(torneo.id, "CERRADO") },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmarilloAlerta),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cerrar", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (torneo.estado != "FINALIZADO") {
                                        Button(
                                            onClick = { cambiarEstado(torneo.id, "FINALIZADO") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Finalizar", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                    if (torneo.estado != "CANCELADO") {
                                        Button(
                                            onClick = { cambiarEstado(torneo.id, "CANCELADO") },
                                            colors = ButtonDefaults.buttonColors(containerColor = RojoError),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancelar", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { eliminarTorneo(torneo.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = RojoError)
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Eliminar Torneo")
                                }
                                TextButton(onClick = { torneoAEditarEstado = null }) {
                                    Text("Cerrar")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
