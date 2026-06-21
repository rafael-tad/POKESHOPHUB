package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

class DetalleClienteAdminScreen(val sesion: SesionUsuario, val clienteId: Long) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var cliente by remember { mutableStateOf<ClienteDto?>(null) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var editando by remember { mutableStateOf(false) }
        var guardando by remember { mutableStateOf(false) }
        var mostrarDialogoConfirmacionDesactivar by remember { mutableStateOf(false) }

        // Form fields
        var nombre by remember { mutableStateOf("") }
        var apellidos by remember { mutableStateOf("") }
        var dni by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }

        // Diálogo de ajuste de saldo rápido
        var mostrarAjustarSaldo by remember { mutableStateOf(false) }
        var importeStr by remember { mutableStateOf("") }
        var descripcionAjuste by remember { mutableStateOf("Ajuste de saldo manual") }
        var procesandoAjuste by remember { mutableStateOf(false) }
        var dialogStatusMsg by remember { mutableStateOf<String?>(null) }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    val response = httpClient.get("/api/clientes/$clienteId")
                    if (response.status.value in 200..299) {
                        val c = response.body<ClienteDto>()
                        cliente = c
                        nombre = c.nombre
                        apellidos = c.apellidos
                        dni = c.dni
                        email = c.email
                        telefono = c.telefono.orEmpty()
                        direccion = c.direccion.orEmpty()
                    } else {
                        errorMsg = "Error al obtener ficha de cliente"
                    }
                } catch (e: Exception) {
                    errorMsg = "Error de conexión: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }

        fun guardarCambios() {
            scope.launch {
                guardando = true
                errorMsg = null
                try {
                    val response = httpClient.patch("/api/clientes/admin/$clienteId") {
                        contentType(ContentType.Application.Json)
                        setBody(ActualizarClienteAdminRequest(
                            nombre = nombre,
                            apellidos = apellidos,
                            dni = dni,
                            email = email,
                            telefono = telefono,
                            direccion = direccion
                        ))
                    }
                    if (response.status.value in 200..299) {
                        cliente = response.body()
                        editando = false
                    } else {
                        errorMsg = "Error al guardar cambios"
                    }
                } catch (e: Exception) {
                    errorMsg = "Error de conexión: ${e.message}"
                } finally {
                    guardando = false
                }
            }
        }

        fun ajustarSaldo() {
            val valor = importeStr.replace(",", ".").toDoubleOrNull()
            if (valor == null || valor == 0.0) {
                dialogStatusMsg = "Introduce un importe válido"
                return
            }

            scope.launch {
                procesandoAjuste = true
                dialogStatusMsg = "Procesando ajuste..."
                try {
                    val req = AjustarSaldoRequest(
                        importe = valor,
                        descripcion = descripcionAjuste
                    )
                    val response = httpClient.post("/api/wallet/admin/ajustar-saldo/$clienteId") {
                        contentType(ContentType.Application.Json)
                        setBody(req)
                    }

                    if (response.status.value in 200..299) {
                        mostrarAjustarSaldo = false
                        cargarDatos()
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
                    title = { Text("Ficha de Entrenador", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    actions = {
                        if (!cargando && cliente != null) {
                            if (editando) {
                                IconButton(onClick = { guardarCambios() }, enabled = !guardando) {
                                    if (guardando) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Save, "Guardar", tint = Color.White)
                                    }
                                }
                            } else {
                                IconButton(onClick = { editando = true }) {
                                    Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrimario)
                )
            },
            containerColor = FondoApp
        ) { padding ->
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
            } else if (cliente != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(AzulPrimario, shape = RoundedCornerShape(40.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${nombre.firstOrNull() ?: ""}${apellidos.firstOrNull() ?: ""}".uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    errorMsg?.let { msg ->
                        Text(msg, color = RojoError, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Saldo Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Superficie),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Monedero Virtual", fontSize = 12.sp, color = TextoSecundario)
                                Text("${cliente?.saldo} €", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VerdeConfirmacion)
                            }
                            if (!editando) {
                                Button(
                                    onClick = {
                                        importeStr = ""
                                        descripcionAjuste = "Ajuste de saldo manual"
                                        dialogStatusMsg = null
                                        mostrarAjustarSaldo = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ajustar Saldo", color = Color.White)
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Superficie)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Datos del Entrenador", style = MaterialTheme.typography.titleMedium, color = AzulPrimario, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                enabled = editando,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = apellidos,
                                onValueChange = { apellidos = it },
                                label = { Text("Apellidos") },
                                enabled = editando,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = dni,
                                onValueChange = { dni = it },
                                label = { Text("DNI") },
                                enabled = editando,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                enabled = editando,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Superficie)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Información de Contacto", style = MaterialTheme.typography.titleMedium, color = AzulPrimario, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = { Text("Teléfono") },
                                enabled = editando,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = direccion,
                                onValueChange = { direccion = it },
                                label = { Text("Dirección") },
                                enabled = editando,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (editando) {
                        Button(
                            onClick = { editando = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = Color.White)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { mostrarDialogoConfirmacionDesactivar = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoError),
                            border = BorderStroke(1.dp, RojoError),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Desactivar Cuenta Cliente")
                        }
                    }
                }

                // Modal Confirmar Desactivación
                if (mostrarDialogoConfirmacionDesactivar) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoConfirmacionDesactivar = false },
                        title = { Text("¿Desactivar entrenador?", fontWeight = FontWeight.Bold) },
                        text = { Text("¿Estás seguro de que deseas desactivar permanentemente a este cliente? Se desactivarán todas sus actividades registradas en PokeShop Hub.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    mostrarDialogoConfirmacionDesactivar = false
                                    scope.launch {
                                        try {
                                            httpClient.delete("/api/clientes/$clienteId")
                                            navigator.pop()
                                        } catch (_: Exception) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RojoError)
                            ) {
                                Text("Desactivar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoConfirmacionDesactivar = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Modal Ajustar Saldo manual
                if (mostrarAjustarSaldo) {
                    AlertDialog(
                        onDismissRequest = { if (!procesandoAjuste) mostrarAjustarSaldo = false },
                        title = { Text("Ajustar Saldo", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = importeStr,
                                    onValueChange = { importeStr = it },
                                    label = { Text("Importe a Sumar/Restar") },
                                    placeholder = { Text("Ej: 20.0 o -5.0") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = descripcionAjuste,
                                    onValueChange = { descripcionAjuste = it },
                                    label = { Text("Concepto") },
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
                                onClick = { ajustarSaldo() },
                                enabled = !procesandoAjuste && importeStr.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                            ) {
                                Text("Aceptar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarAjustarSaldo = false }, enabled = !procesandoAjuste) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}