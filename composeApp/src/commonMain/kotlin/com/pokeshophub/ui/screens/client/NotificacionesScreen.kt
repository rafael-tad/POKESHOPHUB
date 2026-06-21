package com.pokeshophub.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
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
import com.pokeshophub.data.model.Notificacion
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class NotificacionesScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val lista: List<Notificacion> = httpClient.get("/api/notificaciones/${sesion.userId}").body()
                    notificaciones = lista.sortedByDescending { it.id }
                } catch (e: Exception) {
                    println("Error loading notifications: ${e.message}")
                }
                cargando = false
            }
        }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Todas mis notificaciones", color = TextoSobrePrimario) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoSobrePrimario)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AzulPrimario,
                            titleContentColor = TextoSobrePrimario,
                            navigationIconContentColor = TextoSobrePrimario
                        )
                    )
                    HorizontalDivider(color = NeoBorde, thickness = 1.dp)
                }
            },
            containerColor = FondoApp
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = NaranjaAccento
                    )
                } else if (notificaciones.isEmpty()) {
                    Text(
                        text = "No tienes notificaciones en tu historial.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoSecundario,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notificaciones) { notif ->
                            NeoBrutalistCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = if (!notif.leida) Superficie else SuperficieVariante,
                                shadowOffset = 4.dp,
                                borderWidth = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = if (!notif.leida) NaranjaAccento.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .border(1.dp, if (!notif.leida) NaranjaAccento else NeoBorde.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = if (!notif.leida) NaranjaAccento else TextoSecundario,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.titulo,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextoPrimario,
                                            fontWeight = if (!notif.leida) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = notif.mensaje,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (!notif.leida) TextoPrimario.copy(alpha = 0.9f) else TextoSecundario
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = notif.fecha.take(16).replace("T", " "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSecundario
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
