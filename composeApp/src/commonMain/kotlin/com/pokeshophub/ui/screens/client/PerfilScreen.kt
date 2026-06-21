package com.pokeshophub.ui.screens.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class PerfilScreen(val sesion: SesionUsuario, @field:Transient val onBack: (() -> Unit)? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var cliente by remember { mutableStateOf<ClienteDto?>(null) }
        var cargando by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var editando by remember { mutableStateOf(false) }
        var guardando by remember { mutableStateOf(false) }

        // Form fields
        var nombre by remember { mutableStateOf("") }
        var apellidos by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            try {
                cargando = true
                val response = httpClient.get("/api/clientes/${sesion.userId}")
                if (response.status.value in 200..299) {
                    val c = response.body<ClienteDto>()
                    cliente = c
                    nombre = c.nombre
                    apellidos = c.apellidos
                    telefono = c.telefono
                    direccion = c.direccion
                } else {
                    error = "Error al cargar perfil"
                }
            } catch (e: Exception) {
                error = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }

        fun guardarCambios() {
            scope.launch {
                guardando = true
                error = null
                try {
                    val response = httpClient.patch("/api/clientes/perfil/${sesion.userId}") {
                        contentType(ContentType.Application.Json)
                        setBody(ActualizarPerfilClienteRequest(
                            telefono = telefono,
                            direccion = direccion
                        ))
                    }
                    if (response.status.value in 200..299) {
                        cliente = response.body<ClienteDto>()
                        editando = false
                    } else {
                        error = "Error al guardar cambios"
                    }
                } catch (e: Exception) {
                    error = "Error de red: ${e.message}"
                } finally {
                    guardando = false
                }
            }
        }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Mi Perfil", color = TextoSobrePrimario) },
                        navigationIcon = {
                            IconButton(onClick = { onBack?.invoke() ?: navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, "Volver", tint = TextoSobrePrimario)
                            }
                        },
                        actions = {
                            if (!cargando && cliente != null) {
                                if (editando) {
                                    IconButton(onClick = { guardarCambios() }, enabled = !guardando) {
                                        if (guardando) {
                                            CircularProgressIndicator(color = TextoSobrePrimario, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Save, "Guardar", tint = TextoSobrePrimario)
                                        }
                                    }
                                } else {
                                    IconButton(onClick = { editando = true }) {
                                        Icon(Icons.Default.Edit, "Editar", tint = TextoSobrePrimario)
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AzulPrimario,
                            titleContentColor = TextoSobrePrimario,
                            navigationIconContentColor = TextoSobrePrimario,
                            actionIconContentColor = TextoSobrePrimario
                        )
                    )
                    HorizontalDivider(color = NeoBorde, thickness = 1.dp)
                }
            },
            containerColor = FondoApp
        ) { padding ->
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
            } else if (cliente != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Sin Canvas difusos decorativos para mantener la estética Neo-brutalista limpia

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Header con Avatar
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
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    if (error != null) {
                        Text(error ?: "", color = RojoError, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    // Datos personales
                    NeoBrutalistCard(
                        modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                        backgroundColor = Superficie,
                        shape = RoundedCornerShape(16.dp),
                        shadowOffset = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Información Personal", style = MaterialTheme.typography.titleLarge, color = AzulPrimario, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = NeoBorde, thickness = 1.dp)

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                enabled = false,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = NeoBorde.copy(alpha = 0.4f),
                                    disabledLabelColor = TextoSecundario.copy(alpha = 0.5f),
                                    disabledTextColor = TextoPrimario.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = apellidos,
                                onValueChange = { apellidos = it },
                                label = { Text("Apellidos") },
                                enabled = false,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = NeoBorde.copy(alpha = 0.4f),
                                    disabledLabelColor = TextoSecundario.copy(alpha = 0.5f),
                                    disabledTextColor = TextoPrimario.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = cliente!!.dni,
                                onValueChange = { },
                                label = { Text("DNI/NIE") },
                                enabled = false,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = NeoBorde.copy(alpha = 0.4f),
                                    disabledLabelColor = TextoSecundario.copy(alpha = 0.5f),
                                    disabledTextColor = TextoPrimario.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = cliente!!.email,
                                onValueChange = { },
                                label = { Text("Email") },
                                enabled = false,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = NeoBorde.copy(alpha = 0.4f),
                                    disabledLabelColor = TextoSecundario.copy(alpha = 0.5f),
                                    disabledTextColor = TextoPrimario.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Datos de contacto
                    NeoBrutalistCard(
                        modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                        backgroundColor = Superficie,
                        shape = RoundedCornerShape(16.dp),
                        shadowOffset = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Contacto", style = MaterialTheme.typography.titleLarge, color = AzulPrimario, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = NeoBorde, thickness = 1.dp)

                            OutlinedTextField(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = { Text("Teléfono") },
                                enabled = editando,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AzulPrimario,
                                    unfocusedBorderColor = NeoBorde,
                                    disabledBorderColor = NeoBorde.copy(alpha = 0.4f),
                                    focusedLabelColor = AzulPrimario,
                                    unfocusedLabelColor = TextoSecundario
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = direccion,
                                onValueChange = { direccion = it },
                                label = { Text("Dirección") },
                                enabled = editando,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AzulPrimario,
                                    unfocusedBorderColor = NeoBorde,
                                    disabledBorderColor = NeoBorde.copy(alpha = 0.4f),
                                    focusedLabelColor = AzulPrimario,
                                    unfocusedLabelColor = TextoSecundario
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Card de Configuración (Modo Oscuro Toggle)
                    NeoBrutalistCard(
                        modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                        backgroundColor = Superficie,
                        shape = RoundedCornerShape(16.dp),
                        shadowOffset = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Preferencias", style = MaterialTheme.typography.titleLarge, color = AzulPrimario, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = NeoBorde, thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Modo Oscuro", style = MaterialTheme.typography.bodyLarge, color = TextoPrimario, fontWeight = FontWeight.Bold)
                                    Text("Activar interfaz oscura para toda la app", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                }
                                Switch(
                                    checked = isAppDarkTheme.value,
                                    onCheckedChange = { isAppDarkTheme.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AzulPrimario,
                                        uncheckedThumbColor = TextoSecundario,
                                        uncheckedTrackColor = SuperficieVariante,
                                        checkedBorderColor = NeoBorde,
                                        uncheckedBorderColor = NeoBorde
                                    )
                                )
                            }
                        }
                    }

                    if (editando) {
                        Button(
                            onClick = { editando = false },
                            modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RojoError, contentColor = Color.White)
                        ) {
                            Text("Cancelar Cambios", color = Color.White)
                        }
                    }
                }
            }
        } else {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(error ?: "Error desconocido al cargar el perfil")
                }
            }
        }
    }
}
