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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.Interaccion
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class AuditoriaAdminScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var auditoria by remember { mutableStateOf<List<Interaccion>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }

        fun cargarAuditoria() {
            scope.launch {
                cargando = true
                try {
                    auditoria = httpClient.get("/api/admin/auditoria").body()
                } catch (e: Exception) {
                    println("Error cargando auditoría: ${e.message}")
                }
                cargando = false
            }
        }

        LaunchedEffect(Unit) {
            cargarAuditoria()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registro de Auditoría", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { cargarAuditoria() }) {
                            Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
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
                } else if (auditoria.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HistoryToggleOff, null, modifier = Modifier.size(64.dp), tint = TextoSecundario.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay registros de actividad recientes.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextoSecundario
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(auditoria.withIndex().toList()) { (index, evento) ->
                            val isLast = index == auditoria.lastIndex
                            TimelineItem(evento, isLast)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TimelineItem(evento: Interaccion, isLast: Boolean) {
        val uiConf = getEventoUI(evento.tipo)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(40.dp).fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(uiConf.second.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(uiConf.first, null, tint = uiConf.second, modifier = Modifier.size(18.dp))
                }
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(Color.LightGray.copy(alpha=0.5f))
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Superficie),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val esUrgente = evento.nota.contains("[URGENTE]")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = if (esUrgente) RojoError.copy(alpha = 0.15f) else uiConf.second.copy(alpha = 0.15f)) {
                                Text(evento.tipo, color = if (esUrgente) RojoError else uiConf.second, style = MaterialTheme.typography.labelSmall)
                            }
                            if (esUrgente) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Bolt, null, tint = RojoError, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            evento.fecha.take(16).replace("T", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        evento.nota,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esUrgente) RojoError else TextoPrimario,
                        fontWeight = if (esUrgente) FontWeight.Bold else FontWeight.Medium
                    )
                    if (evento.usuarioNombre != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Usuario: ${evento.usuarioNombre} (ID: ${evento.clienteId ?: evento.trabajadorId})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario.copy(alpha=0.7f)
                        )
                    } else if (evento.clienteId != null && evento.clienteId > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Referencia a Cliente ID: ${evento.clienteId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario.copy(alpha=0.7f)
                        )
                    }
                }
            }
        }
    }

    private fun getEventoUI(tipo: String): Pair<ImageVector, Color> {
        return when (tipo) {
            "TAREA" -> Pair(Icons.Default.TaskAlt, VerdeConfirmacion)
            "PRODUCTO", "TIENDA" -> Pair(Icons.Default.Inventory, AzulPrimario)
            "TORNEO" -> Pair(Icons.Default.EmojiEvents, MoradoSecundario)
            "TASACION" -> Pair(Icons.Default.GppMaybe, NaranjaAccento)
            "NOTIFICACION" -> Pair(Icons.Default.Campaign, Color(0xFFD94F8A))
            "CHAT" -> Pair(Icons.Default.Chat, Color(0xFF10B981))
            "SISTEMA" -> Pair(Icons.Default.Settings, Color.Gray)
            "MONEDERO", "WALLET" -> Pair(Icons.Default.AccountBalanceWallet, VerdeConfirmacion)
            else -> Pair(Icons.Default.Info, TextoSecundario)
        }
    }
}
