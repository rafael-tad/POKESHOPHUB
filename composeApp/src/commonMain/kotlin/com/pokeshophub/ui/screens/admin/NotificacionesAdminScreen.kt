package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.CrearNotificacionRequest
import com.pokeshophub.data.model.Notificacion
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.data.model.EditarNotificacionRequest
import com.pokeshophub.ui.theme.*

class NotificacionesAdminScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        
        var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var mostrarDialogoCrear by remember { mutableStateOf(false) }

        val ColorNotif = Color(0xFFD94F8A)

        fun cargarNotificaciones() {
            scope.launch {
                cargando = true
                try {
                    val lista: List<Notificacion> = httpClient.get("/api/notificaciones/0").body()
                    notificaciones = lista.filter { it.destinatarioClienteId == null }.sortedByDescending { it.id }
                } catch (e: Exception) {
                    println("Error al cargar notificaciones: ${e.message}")
                }
                cargando = false
            }
        }

        LaunchedEffect(Unit) {
            cargarNotificaciones()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notificaciones Globales", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorNotif)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarDialogoCrear = true },
                    containerColor = ColorNotif,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Campaign, "Enviar Notificación")
                }
            },
            containerColor = FondoApp
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (cargando) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorNotif)
                    }
                } else if (notificaciones.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            null,
                            modifier = Modifier.size(72.dp),
                            tint = TextoSecundario.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No has enviado notificaciones globales",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextoSecundario
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notificaciones) { n ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Superficie),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                var showEditDialog by remember { mutableStateOf(false) }
                                var editTitulo by remember { mutableStateOf(n.titulo) }
                                var editMensaje by remember { mutableStateOf(n.mensaje) }
                                var showDeleteConfirm by remember { mutableStateOf(false) }

                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(ColorNotif.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Campaign, null, tint = ColorNotif)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                n.titulo,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextoPrimario,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        IconButton(onClick = { showEditDialog = true }) {
                                            Icon(Icons.Default.Edit, "Editar", tint = Color.Black)
                                        }
                                        IconButton(onClick = { showDeleteConfirm = true }) {
                                            Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        n.mensaje,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextoSecundario
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = n.fecha.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextoSecundario.copy(alpha = 0.7f)
                                    )
                                }

                                if (showEditDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showEditDialog = false },
                                        title = { Text("Editar Notificación") },
                                        text = {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = editTitulo,
                                                    onValueChange = { editTitulo = it },
                                                    label = { Text("Título") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = editMensaje,
                                                    onValueChange = { editMensaje = it },
                                                    label = { Text("Mensaje") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    minLines = 3
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(onClick = {
                                                scope.launch {
                                                    try {
                                                        val req = EditarNotificacionRequest(titulo = editTitulo, mensaje = editMensaje)
                                                        httpClient.put("/api/notificaciones/${n.id}") {
                                                            contentType(ContentType.Application.Json)
                                                            setBody(req)
                                                        }
                                                        cargarNotificaciones()
                                                    } catch (e: Exception) { println("Error editando: ${e.message}") }
                                                }
                                                showEditDialog = false
                                            }) { Text("Guardar") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                                        }
                                    )
                                }

                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = { Text("Eliminar Notificación") },
                                        text = { Text("¿Estás seguro de eliminar esta notificación?") },
                                        confirmButton = {
                                            Button(onClick = {
                                                scope.launch {
                                                    try {
                                                        httpClient.delete("/api/notificaciones/${n.id}")
                                                        cargarNotificaciones()
                                                    } catch (e: Exception) { println("Error eliminando: ${e.message}") }
                                                }
                                                showDeleteConfirm = false
                                            }) { Text("Eliminar") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogoCrear) {
            var titulo by remember { mutableStateOf("") }
            var mensaje by remember { mutableStateOf("") }
            var enviando by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { mostrarDialogoCrear = false },
                icon = { Icon(Icons.Default.Campaign, null, tint = ColorNotif) },
                title = { Text("Nueva Notificación Global", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Se enviará a todos los clientes de la aplicación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = mensaje,
                            onValueChange = { mensaje = it },
                            label = { Text("Mensaje o Novedad *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (titulo.isBlank() || mensaje.isBlank()) return@Button
                            scope.launch {
                                enviando = true
                                try {
                                    val req = CrearNotificacionRequest(titulo, mensaje)
                                    httpClient.post("/api/notificaciones") {
                                        contentType(ContentType.Application.Json)
                                        setBody(req)
                                    }
                                    cargarNotificaciones()
                                    mostrarDialogoCrear = false
                                } catch (e: Exception) {
                                    println("Error enviando notificación: ${e.message}")
                                }
                                enviando = false
                            }
                        },
                        enabled = !enviando && titulo.isNotBlank() && mensaje.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorNotif)
                    ) {
                        if (enviando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Enviar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoCrear = false }) { Text("Cancelar") }
                }
            )
        }
    }
}
