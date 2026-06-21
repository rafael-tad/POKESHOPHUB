package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
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
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.ChatResumenDto
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class MensajesAdminScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var chats by remember { mutableStateOf<List<ChatResumenDto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            cargando = true
            try {
                chats = httpClient.get("/api/mensajes/admin/chats").body()
            } catch (e: Exception) {
                println("Error cargando chats: ${e.message}")
            }
            cargando = false
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mensajes de Clientes", color = Color.White) },
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
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AzulPrimario)
                } else if (chats.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(64.dp), tint = TextoSecundario.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay conversaciones activas",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextoSecundario
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chats) { chat ->
                            Card(
                                onClick = { navigator.push(ChatDetalleAdminScreen(sesion, chat.clienteId, chat.nombreCliente)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Superficie),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(AzulPrimario.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, null, tint = AzulPrimario)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                chat.nombreCliente,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextoPrimario,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                chat.fechaUltimoMensaje.take(16).replace("T", " "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextoSecundario
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            chat.ultimoMensaje,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextoSecundario,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
