package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.data.network.AuthRepository
import com.pokeshophub.ui.screens.auth.LoginScreen
import com.pokeshophub.ui.theme.*

class DashboardAdminScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val authRepo = remember { AuthRepository() }

        var resumen by remember { mutableStateOf<AdminResumenDto?>(null) }
        var cargando by remember { mutableStateOf(true) }
        var errorMensaje by remember { mutableStateOf<String?>(null) }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMensaje = null
                    val response = httpClient.get("/api/admin/resumen")
                    if (response.status.value in 200..299) {
                        resumen = response.body()
                    } else {
                        errorMensaje = "Error del servidor: ${response.status}"
                    }
                } catch (e: Exception) {
                    errorMensaje = "Error de conexión: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }

        Scaffold(
            topBar = {
                Surface(
                    color = AzulPrimario,
                    shadowElevation = 8.dp
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    "PokeShop Hub Admin",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    sesion.nombre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        actions = {
                            IconButton(onClick = {
                                authRepo.cerrarSesion()
                                navigator.replaceAll(LoginScreen())
                            }) {
                                Icon(Icons.Default.PowerSettingsNew, "Cerrar sesión", tint = Color.White)
                            }
                        }
                    )
                }
            },
            containerColor = FondoApp
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── KPI Cards ──────────────────────────────────────────
                item {
                    Text(
                        "Estado del Centro Pokemon",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextoPrimario
                    )
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                icono = Icons.Default.Inventory,
                                titulo = resumen?.totalProductos?.toString() ?: "0",
                                subtitulo = "Cartas/Stock",
                                color = AzulPrimario,
                                onClick = { navigator.push(GestionStockScreen(sesion)) }
                            )
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                icono = Icons.Default.Groups,
                                titulo = resumen?.totalClientes?.toString() ?: "0",
                                subtitulo = "Entrenadores",
                                color = AzulVariante,
                                onClick = { navigator.push(BuscadorClientesScreen(sesion)) }
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                icono = Icons.Default.EmojiEvents,
                                titulo = resumen?.torneosActivos?.toString() ?: "0",
                                subtitulo = "Torneos",
                                color = MoradoSecundario,
                                onClick = { navigator.push(OrganizacionTorneosScreen(sesion)) }
                            )
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                icono = Icons.Default.GppMaybe,
                                titulo = resumen?.tasacionesPendientes?.toString() ?: "0",
                                subtitulo = "Tasaciones Pend.",
                                color = if ((resumen?.tasacionesPendientes ?: 0) > 0) NaranjaAccento else VerdeConfirmacion,
                                onClick = { navigator.push(ValoracionCartasScreen(sesion)) }
                            )
                        }
                    }
                }

                if (errorMensaje != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = RojoError.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, RojoError)
                        ) {
                            Text(
                                "Error de conexión:\n$errorMensaje",
                                color = RojoError,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // ── Accesos rápidos admin ──────────────────────────────
                item {
                    Text(
                        "Herramientas de Administración",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextoPrimario
                    )
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val items = listOf(
                            Triple(Icons.Default.ManageSearch, "Buscador de Clientes", AzulPrimario),
                            Triple(Icons.Default.PersonAdd, "Revisar Registros", Color(0xFF8B5CF6)),
                            Triple(Icons.Default.Inventory, "Gestión de Stock TCG", AzulVariante),
                            Triple(Icons.Default.GppMaybe, "Valoración de Cartas", NaranjaAccento),
                            Triple(Icons.Default.EmojiEvents, "Organización de Torneos", MoradoSecundario),
                            Triple(Icons.Default.AccountBalanceWallet, "Panel Control de Saldo", VerdeConfirmacion),
                            Triple(Icons.Default.DashboardCustomize, "Kanban de Tareas", Color(0xFFD94F8A)),
                            Triple(Icons.Default.NotificationsActive, "Notificaciones Globales", Color(0xFFE2893B)),
                            Triple(Icons.Default.QuestionAnswer, "Centro de Mensajes", Color(0xFF10B981)),
                            Triple(Icons.Default.Star, "Reseñas de Clientes", NaranjaAccento),
                            Triple(Icons.Default.HistoryEdu, "Registro de Auditoría", AzulVariante)
                        )
                        
                        items.forEach { (icono, titulo, color) ->
                            AccesoRapidoCard(icono, titulo, color, onClick = {
                                when (titulo) {
                                    "Buscador de Clientes" -> navigator.push(BuscadorClientesScreen(sesion))
                                    "Revisar Registros" -> navigator.push(RevisarRegistrosScreen(sesion))
                                    "Gestión de Stock TCG" -> navigator.push(GestionStockScreen(sesion))
                                    "Valoración de Cartas" -> navigator.push(ValoracionCartasScreen(sesion))
                                    "Organización de Torneos" -> navigator.push(OrganizacionTorneosScreen(sesion))
                                    "Panel Control de Saldo" -> navigator.push(PanelControlSaldoScreen(sesion))
                                    "Kanban de Tareas" -> navigator.push(TareasKanbanScreen(sesion))
                                    "Notificaciones Globales" -> navigator.push(NotificacionesAdminScreen(sesion))
                                    "Centro de Mensajes" -> navigator.push(MensajesAdminScreen(sesion))
                                    "Reseñas de Clientes" -> navigator.push(ResenasAdminScreen(sesion))
                                    "Registro de Auditoría" -> navigator.push(AuditoriaAdminScreen(sesion))
                                }
                            })
                        }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Superficie,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(titulo, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextoPrimario)
            Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
        }
    }
}

@Composable
private fun AccesoRapidoCard(icono: ImageVector, titulo: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Superficie,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextoPrimario,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowForwardIos, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
        }
    }
}
