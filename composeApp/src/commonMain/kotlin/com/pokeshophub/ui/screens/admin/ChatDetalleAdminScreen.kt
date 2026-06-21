package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.EnviarMensajeRequest
import com.pokeshophub.data.model.Mensaje
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class ChatDetalleAdminScreen(
    val sesion: SesionUsuario,
    val clienteIdChat: Long,
    val nombreCliente: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val snackbarHostState = remember { SnackbarHostState() }

        var mensajes by remember { mutableStateOf<List<Mensaje>>(emptyList()) }
        var nuevoMensaje by remember { mutableStateOf("") }
        var enviando by remember { mutableStateOf(false) }

        // Polling loop
        LaunchedEffect(Unit) {
            while (true) {
                try {
                    val lista: List<Mensaje> = httpClient.get("/api/mensajes/$clienteIdChat").body()
                    if (lista != mensajes) {
                        val scroll = mensajes.isEmpty() || listState.isScrollInProgress.not()
                        mensajes = lista
                        if (scroll && mensajes.isNotEmpty()) {
                            listState.animateScrollToItem(mensajes.size - 1)
                        }
                    }
                } catch (e: Exception) {
                    println("Error loading messages: ${e.message}")
                }
                delay(3000)
            }
        }

        fun enviar() {
            if (nuevoMensaje.isBlank()) return
            val texto = nuevoMensaje
            nuevoMensaje = "" // Clear optimistic
            scope.launch {
                enviando = true
                try {
                    val req = EnviarMensajeRequest(
                        clienteId = clienteIdChat,
                        texto = texto,
                        remitente = "ADMIN"
                    )
                    val response = httpClient.post("/api/mensajes") {
                        contentType(ContentType.Application.Json)
                        setBody(req)
                    }
                    if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                        val enviado: Mensaje = response.body()
                        mensajes = mensajes + enviado
                        listState.animateScrollToItem(mensajes.size - 1)
                    } else {
                        nuevoMensaje = texto // Restore text
                        snackbarHostState.showSnackbar("Error al enviar el mensaje: ${response.status}")
                    }
                } catch (e: Exception) {
                    nuevoMensaje = texto // Restore text
                    snackbarHostState.showSnackbar("Error de conexión con el servidor")
                    println("Error enviando: ${e.message}")
                }
                enviando = false
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(nombreCliente, color = Color.White)
                            Text("Chat con el cliente", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.7f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrimario)
                )
            },
            containerColor = FondoApp
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Lista de Mensajes
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mensajes) { msg ->
                        // Para el admin, "esMio" es cuando remitente == "ADMIN"
                        val esMio = msg.remitente == "ADMIN"
                        val alignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart
                        val backgroundColor = if (esMio) MoradoSecundario else SuperficieVariante
                        val textColor = if (esMio) Color.White else TextoPrimario

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = alignment
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f) // max 80% width
                                    .background(
                                        color = backgroundColor,
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (esMio) 16.dp else 4.dp,
                                            bottomEnd = if (esMio) 4.dp else 16.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.texto,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.fechaHora.take(16).replace("T", " "),
                                    color = textColor.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                // Input Area
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Superficie,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoMensaje,
                            onValueChange = { nuevoMensaje = it },
                            placeholder = { Text("Escribe una respuesta...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MoradoSecundario,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FloatingActionButton(
                            onClick = { enviar() },
                            containerColor = if (nuevoMensaje.isNotBlank()) MoradoSecundario else Color.Gray,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Enviar")
                        }
                    }
                }
            }
        }
    }
}
