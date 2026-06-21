package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.BorderStroke
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

class PanelControlSaldoScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var clientes by remember { mutableStateOf<List<ClienteDto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var refreshTrigger by remember { mutableStateOf(0) }

        // Diálogo de ajuste de saldo
        var clienteAJustar by remember { mutableStateOf<ClienteDto?>(null) }
        var importeStr by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("Ajuste de saldo manual") }
        var procesandoAjuste by remember { mutableStateOf(false) }
        var dialogStatusMsg by remember { mutableStateOf<String?>(null) }

        fun cargarClientes() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    val endpoint = if (searchQuery.isBlank()) "/api/clientes" else "/api/clientes/buscar?q=$searchQuery"
                    clientes = httpClient.get(endpoint).body()
                } catch (e: Exception) {
                    errorMsg = "Error al cargar los clientes: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(refreshTrigger, searchQuery) {
            cargarClientes()
        }

        fun ajustarSaldo(clienteId: Long) {
            val valor = importeStr.replace(",", ".").toDoubleOrNull()
            if (valor == null || valor == 0.0) {
                dialogStatusMsg = "Introduce un importe válido (positivo o negativo)"
                return
            }

            scope.launch {
                procesandoAjuste = true
                dialogStatusMsg = "Procesando ajuste de saldo..."
                try {
                    val req = AjustarSaldoRequest(
                        importe = valor,
                        descripcion = descripcion
                    )
                    val response = httpClient.post("/api/wallet/admin/ajustar-saldo/$clienteId") {
                        contentType(ContentType.Application.Json)
                        setBody(req)
                    }

                    if (response.status.value in 200..299) {
                        clienteAJustar = null
                        importeStr = ""
                        descripcion = "Ajuste de saldo manual"
                        refreshTrigger++
                    } else {
                        val res = response.body<MensajeResponse>()
                        dialogStatusMsg = res.mensaje
                    }
                } catch (e: Exception) {
                    dialogStatusMsg = "Error de conexión: ${e.message}"
                } finally {
                    procesandoAjuste = false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel Control de Saldo", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
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
                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar cliente por nombre o DNI...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPrimario,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                if (cargando && clientes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulPrimario)
                    }
                } else {
                    errorMsg?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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

                    if (clientes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron entrenadores.", color = TextoSecundario)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(clientes) { cli ->
                                ClienteSaldoItem(
                                    cli = cli,
                                    onAjustarClick = {
                                        clienteAJustar = cli
                                        importeStr = ""
                                        descripcion = "Ajuste de saldo manual"
                                        dialogStatusMsg = null
                                    }
                                )
                            }
                        }
                    }
                }

                // Modal Ajustar Saldo
                clienteAJustar?.let { cli ->
                    AlertDialog(
                        onDismissRequest = { if (!procesandoAjuste) clienteAJustar = null },
                        title = { Text("Ajustar Saldo manual", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Entrenador: ${cli.nombre} ${cli.apellidos}", fontSize = 14.sp, color = TextoPrimario)
                                Text("Saldo Actual: ${cli.saldo} €", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VerdeConfirmacion)
                                Divider()
                                OutlinedTextField(
                                    value = importeStr,
                                    onValueChange = { importeStr = it },
                                    label = { Text("Importe a Sumar/Restar") },
                                    placeholder = { Text("Ej: 15.0 o -10.0") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = descripcion,
                                    onValueChange = { descripcion = it },
                                    label = { Text("Concepto / Descripción") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                dialogStatusMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.contains("correctamente") || msg.contains("Procesando")) VerdeConfirmacion else RojoError
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { ajustarSaldo(cli.id) },
                                enabled = !procesandoAjuste && importeStr.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                            ) {
                                Text("Aceptar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { clienteAJustar = null }, enabled = !procesandoAjuste) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ClienteSaldoItem(cli: ClienteDto, onAjustarClick: () -> Unit) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Superficie,
            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${cli.nombre} ${cli.apellidos}", fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text("DNI: ${cli.dni} | Correo: ${cli.email}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${cli.saldo} €",
                        fontWeight = FontWeight.ExtraBold,
                        color = VerdeConfirmacion,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    IconButton(
                        onClick = onAjustarClick,
                        modifier = Modifier.background(AzulPrimario.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, "Editar Saldo", tint = AzulPrimario)
                    }
                }
            }
        }
    }
}
