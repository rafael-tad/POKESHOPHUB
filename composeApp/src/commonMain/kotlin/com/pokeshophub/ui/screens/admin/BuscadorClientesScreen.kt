package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.ClienteDto
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class BuscadorClientesScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        
        var query by remember { mutableStateOf("") }
        var clientes by remember { mutableStateOf<List<ClienteDto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var searchJob by remember { mutableStateOf<Job?>(null) }

        // Función para cargar clientes
        fun buscarClientes(q: String) {
            searchJob?.cancel()
            searchJob = scope.launch {
                cargando = true
                try {
                    // Simular debounce
                    delay(300)
                    val url = if (q.isBlank()) "/api/clientes" else "/api/clientes/buscar?q=$q"
                    clientes = httpClient.get(url).body()
                } catch (e: Exception) {
                    println("Error buscando clientes: ${e.message}")
                }
                cargando = false
            }
        }

        // Carga inicial
        LaunchedEffect(Unit) {
            buscarClientes("")
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestión de Clientes", color = Color.White, fontWeight = FontWeight.Bold) },
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
                // Barra de Búsqueda
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            buscarClientes(it)
                        },
                        placeholder = { Text("Buscar por nombre, apellidos o DNI...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { 
                                    query = ""
                                    buscarClientes("")
                                }) {
                                    Icon(Icons.Default.Clear, "Borrar")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPrimario,
                            focusedLeadingIconColor = AzulPrimario
                        )
                    )
                }

                // Lista de Clientes
                Box(modifier = Modifier.fillMaxSize()) {
                    if (cargando && clientes.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AzulPrimario
                        )
                    } else if (clientes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PersonOff, null, modifier = Modifier.size(64.dp), tint = TextoSecundario)
                            Spacer(Modifier.height(16.dp))
                            Text("No se encontraron clientes", style = MaterialTheme.typography.titleMedium, color = TextoSecundario)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(clientes) { cliente ->
                                ClienteCard(cliente, onClick = {
                                    navigator.push(DetalleClienteAdminScreen(sesion, cliente.id))
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteCard(cliente: ClienteDto, onClick: () -> Unit) {
    val iniciales = "${cliente.nombre.firstOrNull() ?: ' '}${cliente.apellidos.firstOrNull() ?: ' '}".trim().uppercase()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(AzulPrimario, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${cliente.nombre} ${cliente.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoPrimario
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, null, modifier = Modifier.size(16.dp), tint = TextoSecundario)
                    Spacer(Modifier.width(4.dp))
                    Text(cliente.dni, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp), tint = TextoSecundario)
                    Spacer(Modifier.width(4.dp))
                    Text(cliente.telefono.orEmpty().ifEmpty { "Sin teléfono" }, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                }
            }
            
            if (!cliente.activo) {
                Badge(containerColor = RojoError) {
                    Text("Inactivo")
                }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = TextoSecundario)
        }
    }
}
