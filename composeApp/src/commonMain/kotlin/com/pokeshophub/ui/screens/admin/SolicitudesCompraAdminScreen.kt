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
import io.ktor.http.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class SolicitudesCompraAdminScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var solicitudes by remember { mutableStateOf<List<SolicitudCompra>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var cargandoAccion by remember { mutableStateOf(false) }

        // Diálogos de confirmación
        var solicitudAProcesar by remember { mutableStateOf<SolicitudCompra?>(null) }
        var esAprobacion by remember { mutableStateOf(true) }

        fun cargarSolicitudes() {
            scope.launch {
                cargando = true
                errorMsg = null
                try {
                    val response = httpClient.get("/api/tienda/admin/solicitudes-compra")
                    if (response.status.isSuccess()) {
                        solicitudes = response.body()
                    } else {
                        errorMsg = "Error al obtener las solicitudes"
                    }
                } catch (e: Exception) {
                    errorMsg = "Error de conexión: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        fun resolverSolicitud(id: Long, aceptar: Boolean) {
            scope.launch {
                cargandoAccion = true
                errorMsg = null
                try {
                    val response = httpClient.post("/api/tienda/admin/solicitudes-compra/$id/resolver") {
                        contentType(ContentType.Application.Json)
                        setBody(ResolverSolicitudCompraRequest(aceptar = aceptar))
                    }
                    val res: MensajeResponse = response.body()
                    if (response.status.isSuccess() && res.success) {
                        cargarSolicitudes()
                    } else {
                        errorMsg = res.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al resolver la solicitud: ${e.message}"
                } finally {
                    cargandoAccion = false
                    solicitudAProcesar = null
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarSolicitudes()
        }

        // Diálogo de Confirmación
        solicitudAProcesar?.let { sol ->
            AlertDialog(
                onDismissRequest = { if (!cargandoAccion) solicitudAProcesar = null },
                modifier = Modifier.border(1.dp, NeoBorde, RoundedCornerShape(24.dp)),
                icon = {
                    Icon(
                        if (esAprobacion) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (esAprobacion) VerdeConfirmacion else RojoError,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        if (esAprobacion) "Aprobar Compra" else "Rechazar Compra",
                        fontWeight = FontWeight.Bold,
                        color = TextoPrimario
                    )
                },
                text = {
                    Text(
                        if (esAprobacion) {
                            "¿Estás seguro de que deseas APROBAR la solicitud de compra de ${sol.clienteNombre} por un total de ${sol.total} €?\n\nSe restará el stock de los productos y el saldo correspondiente de su monedero virtual."
                        } else {
                            "¿Estás seguro de que deseas RECHAZAR la solicitud de compra de ${sol.clienteNombre} por un total de ${sol.total} €?"
                        },
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { resolverSolicitud(sol.id, esAprobacion) },
                        enabled = !cargandoAccion,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esAprobacion) VerdeConfirmacion else RojoError,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargandoAccion) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Confirmar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { solicitudAProcesar = null },
                        enabled = !cargandoAccion
                    ) {
                        Text("Cancelar", color = TextoSecundario)
                    }
                },
                containerColor = Superficie,
                shape = RoundedCornerShape(24.dp)
            )
        }

        Scaffold(
            topBar = {
                Surface(
                    color = AzulPrimario,
                    shadowElevation = 8.dp
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                "Solicitudes de Compra",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            containerColor = FondoApp
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (cargando && solicitudes.isEmpty()) {
                    CircularProgressIndicator(
                        color = AzulPrimario,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (solicitudes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            null,
                            tint = TextoSecundario.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay solicitudes de compra registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (errorMsg != null) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = RojoError.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, RojoError)
                                ) {
                                    Text(
                                        errorMsg ?: "",
                                        color = RojoError,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        items(solicitudes) { sol ->
                            SolicitudCompraCard(
                                solicitud = sol,
                                onAprobar = {
                                    solicitudAProcesar = sol
                                    esAprobacion = true
                                },
                                onRechazar = {
                                    solicitudAProcesar = sol
                                    esAprobacion = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SolicitudCompraCard(
        solicitud: SolicitudCompra,
        onAprobar: () -> Unit,
        onRechazar: () -> Unit
    ) {
        val estadoColor = when (solicitud.estado) {
            "APROBADA" -> VerdeConfirmacion
            "RECHAZADA" -> RojoError
            else -> NaranjaAccento
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Superficie,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, NeoBorde)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Cabecera: Cliente y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            solicitud.clienteNombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextoPrimario
                        )
                        Text(
                            solicitud.fecha.replace("T", " ").take(19),
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }

                    Surface(
                        color = estadoColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, estadoColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            solicitud.estado,
                            color = estadoColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = NeoBorde.copy(alpha = 0.5f))

                // Items list
                Text(
                    "Artículos:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextoPrimario,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    solicitud.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "• ${item.cantidad}x ${item.productoNombre}",
                                fontSize = 13.sp,
                                color = TextoSecundario,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${(item.precioUnitario * item.cantidad).formatTwoDecimals()} €",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextoPrimario
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = NeoBorde.copy(alpha = 0.5f))

                // Pie: Total y Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total: ", fontWeight = FontWeight.Bold, color = TextoPrimario)
                        Text(
                            "${solicitud.total.formatTwoDecimals()} €",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = AzulPrimario
                        )
                    }

                    if (solicitud.estado == "PENDIENTE") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onRechazar,
                                modifier = Modifier
                                    .background(RojoError.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, RojoError, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.Clear, "Rechazar", tint = RojoError, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = onAprobar,
                                modifier = Modifier
                                    .background(VerdeConfirmacion.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, VerdeConfirmacion, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.Check, "Aprobar", tint = VerdeConfirmacion, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Double.formatTwoDecimals(): String {
        val s = this.toString()
        val parts = s.split(".")
        if (parts.size == 1) return s + ".00"
        val dec = parts[1]
        return if (dec.length >= 2) {
            parts[0] + "." + dec.substring(0, 2)
        } else {
            parts[0] + "." + dec + "0"
        }
    }
}
