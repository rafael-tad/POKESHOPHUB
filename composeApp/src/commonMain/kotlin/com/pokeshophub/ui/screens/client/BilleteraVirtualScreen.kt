package com.pokeshophub.ui.screens.client

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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*
import com.pokeshophub.util.formatTwoDecimals

class BilleteraVirtualScreen(val sesion: SesionUsuario, @field:Transient val onBack: (() -> Unit)? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var movimientos by remember { mutableStateOf<List<GastoIngreso>>(emptyList()) }
        var saldoCliente by remember { mutableStateOf(0.0) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        var showRecargaDialog by remember { mutableStateOf(false) }
        var importeRecarga by remember { mutableStateOf("") }
        var recargando by remember { mutableStateOf(false) }
        var errorRecarga by remember { mutableStateOf<String?>(null) }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    // Obtener historial de wallet
                    movimientos = httpClient.get("/api/wallet/historial/${sesion.userId}").body()
                    // Obtener saldo
                    val cliente: ClienteDto = httpClient.get("/api/clientes/${sesion.userId}").body()
                    saldoCliente = cliente.saldo
                } catch (e: Exception) {
                    errorMsg = "Error al cargar el monedero: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }

        // Diálogo para Recarga
        if (showRecargaDialog) {
            AlertDialog(
                onDismissRequest = { if (!recargando) showRecargaDialog = false },
                modifier = Modifier.border(1.dp, NeoBorde, RoundedCornerShape(24.dp)),
                title = { Text("Recargar Monedero", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Introduce el importe que deseas añadir a tu cuenta de PokeShop Hub:", color = TextoSecundario)
                        OutlinedTextField(
                            value = importeRecarga,
                            onValueChange = { importeRecarga = it; errorRecarga = null },
                            label = { Text("Importe (€)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulPrimario,
                                unfocusedBorderColor = NeoBorde,
                                focusedLabelColor = AzulPrimario,
                                unfocusedLabelColor = TextoSecundario
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        errorRecarga?.let { msg ->
                            Text(msg, color = RojoError, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val valor = importeRecarga.toDoubleOrNull()
                            if (valor == null || valor <= 0.0) {
                                errorRecarga = "Por favor, introduce un importe válido."
                                return@Button
                            }
                            scope.launch {
                                recargando = true
                                errorRecarga = null
                                try {
                                    val response = httpClient.post("/api/wallet/admin/ajustar-saldo/${sesion.userId}") {
                                        contentType(ContentType.Application.Json)
                                        setBody(AjustarSaldoRequest(importe = valor, descripcion = "Recarga de monedero (Simulada)"))
                                    }
                                    if (response.status.value in 200..299) {
                                        showRecargaDialog = false
                                        importeRecarga = ""
                                        cargarDatos() // Refrescar
                                    } else {
                                        val error = response.body<MensajeResponse>()
                                        errorRecarga = error.mensaje
                                    }
                                } catch (e: Exception) {
                                    errorRecarga = "Error en la recarga: ${e.message}"
                                } finally {
                                    recargando = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulPrimario,
                            contentColor = TextoSobrePrimario,
                            disabledContainerColor = SuperficieVariante
                        ),
                        enabled = !recargando
                    ) {
                        if (recargando) {
                            CircularProgressIndicator(color = TextoSobrePrimario, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Recargar", color = TextoSobrePrimario)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRecargaDialog = false },
                        enabled = !recargando
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
                Column {
                    TopAppBar(
                        title = { Text("Monedero Virtual", color = TextoSobrePrimario) },
                        navigationIcon = {
                            IconButton(onClick = {
                                onBack?.invoke() ?: navigator.pop()
                            }) {
                                Icon(Icons.Default.ArrowBack, "Volver", tint = TextoSobrePrimario)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Tarjeta de Balance
                NeoBrutalistCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    backgroundColor = AzulPrimario,
                    shape = RoundedCornerShape(24.dp),
                    shadowOffset = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Saldo Disponible (Store Credit)", color = TextoSobrePrimario.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${saldoCliente.formatTwoDecimals()} €",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextoSobrePrimario,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))
                        
                        Button(
                            onClick = { showRecargaDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TextoSobrePrimario, contentColor = AzulPrimario),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.AddCard, "Recargar", tint = AzulPrimario)
                            Spacer(Modifier.width(8.dp))
                            Text("Recargar Saldo", color = AzulPrimario, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    "Historial de transacciones",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        cargando && movimientos.isEmpty() -> {
                            CircularProgressIndicator(
                                color = AzulPrimario,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        errorMsg != null && movimientos.isEmpty() -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                Text(errorMsg!!, color = RojoError, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { cargarDatos() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario, contentColor = TextoSobrePrimario)
                                ) {
                                    Text("Reintentar", color = TextoSobrePrimario)
                                }
                            }
                        }
                        movimientos.isEmpty() -> {
                            Text(
                                text = "Aún no tienes transacciones en tu cuenta.",
                                modifier = Modifier.align(Alignment.Center),
                                color = TextoSecundario
                            )
                        }
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(movimientos) { mov ->
                                    MovimientoRow(mov)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MovimientoRow(mov: GastoIngreso) {
        val esIngreso = mov.tipo == "INGRESO"
        
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
            backgroundColor = Superficie,
            shape = RoundedCornerShape(16.dp),
            shadowOffset = 4.dp,
            borderWidth = 1.5.dp
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (esIngreso) VerdeConfirmacion.copy(alpha = 0.15f) else MoradoSecundario.copy(alpha = 0.15f),
                                CircleShape
                            )
                            .border(1.dp, if (esIngreso) VerdeConfirmacion else MoradoSecundario, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (esIngreso) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (esIngreso) VerdeConfirmacion else MoradoSecundario,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mov.descripcion, fontWeight = FontWeight.Bold, color = TextoPrimario, fontSize = 14.sp)
                        Text(
                            text = "${mov.fecha} • ${mov.categoria}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "${if (esIngreso) "+" else "-"}${mov.importe.formatTwoDecimals()} €",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (esIngreso) VerdeConfirmacion else MoradoSecundario,
                    fontSize = 16.sp
                )
            }
        }
    }
}
