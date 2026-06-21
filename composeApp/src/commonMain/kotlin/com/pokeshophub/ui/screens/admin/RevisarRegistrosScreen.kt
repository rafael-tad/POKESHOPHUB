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
import com.pokeshophub.data.model.ClienteDto
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.model.MensajeResponse
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class RevisarRegistrosScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var solicitudes by remember { mutableStateOf<List<ClienteDto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        // Estados para diálogos de acción
        var usuarioParaAdmin by remember { mutableStateOf<ClienteDto?>(null) }
        var cargandoAccion by remember { mutableStateOf(false) }

        fun cargarSolicitudes() {
            scope.launch {
                cargando = true
                errorMsg = null
                try {
                    val response = httpClient.get("/api/admin/registros-pendientes")
                    if (response.status.isSuccess()) {
                        solicitudes = response.body()
                    } else {
                        errorMsg = "Error del servidor al obtener solicitudes"
                    }
                } catch (e: Exception) {
                    errorMsg = "Error de conexión: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        fun procesarAprobacion(clienteId: Long, esAdmin: Boolean) {
            scope.launch {
                cargandoAccion = true
                try {
                    val endpoint = if (esAdmin) {
                        "/api/admin/clientes/$clienteId/aprobar-admin"
                    } else {
                        "/api/admin/clientes/$clienteId/aprobar-cliente"
                    }
                    val response = httpClient.post(endpoint)
                    if (response.status.isSuccess()) {
                        cargarSolicitudes()
                    } else {
                        val err: MensajeResponse = response.body()
                        errorMsg = err.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al procesar: ${e.message}"
                } finally {
                    cargandoAccion = false
                    usuarioParaAdmin = null
                }
            }
        }

        fun rechazarSolicitud(clienteId: Long) {
            scope.launch {
                cargandoAccion = true
                try {
                    val response = httpClient.post("/api/admin/clientes/$clienteId/rechazar")
                    if (response.status.isSuccess()) {
                        cargarSolicitudes()
                    } else {
                        val err: MensajeResponse = response.body()
                        errorMsg = err.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al rechazar: ${e.message}"
                } finally {
                    cargandoAccion = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarSolicitudes()
        }

        // Diálogo de advertencia para rol Administrador
        usuarioParaAdmin?.let { cliente ->
            AlertDialog(
                onDismissRequest = { if (!cargandoAccion) usuarioParaAdmin = null },
                modifier = Modifier.border(1.dp, NeoBorde, RoundedCornerShape(24.dp)),
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Advertencia de Seguridad",
                        tint = NaranjaAccento,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        "Permisos de Administrador",
                        fontWeight = FontWeight.Bold,
                        color = TextoPrimario,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas conceder permisos de administrador a ${cliente.nombre} ${cliente.apellidos}?\n\nEsto le dará acceso completo a todas las funciones de administración y auditoría del sistema.",
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { procesarAprobacion(cliente.id, esAdmin = true) },
                        enabled = !cargandoAccion,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargandoAccion) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Confirmar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { usuarioParaAdmin = null },
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
                TopAppBar(
                    title = { Text("Solicitudes de Registro", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrimario),
                    actions = {
                        IconButton(onClick = { cargarSolicitudes() }, enabled = !cargando) {
                            Icon(Icons.Default.Refresh, "Actualizar", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = FondoApp
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AzulPrimario
                    )
                } else if (errorMsg != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Error, null, tint = RojoError, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(errorMsg!!, color = RojoError, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { cargarSolicitudes() },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                        ) {
                            Text("Reintentar", color = Color.White)
                        }
                    }
                } else if (solicitudes.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.HowToReg,
                            null,
                            modifier = Modifier.size(72.dp),
                            tint = AzulPrimario.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay solicitudes pendientes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextoPrimario
                        )
                        Text(
                            "Todos los registros de usuarios han sido revisados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(solicitudes) { cliente ->
                            SolicitudRegistroCard(
                                cliente = cliente,
                                onAprobarCliente = { procesarAprobacion(cliente.id, esAdmin = false) },
                                onAprobarAdmin = { usuarioParaAdmin = cliente },
                                onRechazar = { rechazarSolicitud(cliente.id) },
                                habilitado = !cargandoAccion
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SolicitudRegistroCard(
        cliente: ClienteDto,
        onAprobarCliente: () -> Unit,
        onAprobarAdmin: () -> Unit,
        onRechazar: () -> Unit,
        habilitado: Boolean
    ) {
        val iniciales = "${cliente.nombre.firstOrNull() ?: ' '}${cliente.apellidos.firstOrNull() ?: ' '}".trim().uppercase()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Superficie),
            elevation = CardDefaults.cardElevation(3.dp),
            border = BorderStroke(1.dp, NeoBorde)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Info Cabecera
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(AzulPrimario, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iniciales, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cliente.nombre} ${cliente.apellidos}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextoPrimario
                        )
                        Text(
                            text = cliente.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }

                    // Badge de pendiente
                    Surface(
                        color = NaranjaAccento.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Pendiente",
                            color = NaranjaAccento,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = NeoBorde, thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                // Detalles adicionales
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DNI/NIE", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        Text(cliente.dni, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario, fontWeight = FontWeight.SemiBold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Teléfono", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        Text(cliente.telefono.ifEmpty { "No provisto" }, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (cliente.direccion.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Dirección", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    Text(cliente.direccion, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                }

                Spacer(Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Aprobar como Cliente
                    Button(
                        onClick = onAprobarCliente,
                        enabled = habilitado,
                        modifier = Modifier.weight(1.2f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario, contentColor = Color.White)
                    ) {
                        Text("Rol Cliente", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Botón Aprobar como Admin
                    Button(
                        onClick = onAprobarAdmin,
                        enabled = habilitado,
                        modifier = Modifier.weight(1.2f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White) // Indigo
                    ) {
                        Text("Rol Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Botón Rechazar (Icono)
                    OutlinedButton(
                        onClick = onRechazar,
                        enabled = habilitado,
                        modifier = Modifier.size(width = 54.dp, height = 40.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoError),
                        border = BorderStroke(1.dp, RojoError.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, "Rechazar", tint = RojoError)
                    }
                }
            }
        }
    }
}
