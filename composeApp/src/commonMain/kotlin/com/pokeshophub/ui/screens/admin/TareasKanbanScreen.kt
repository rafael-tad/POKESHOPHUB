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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

class TareasKanbanScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var kanban by remember { mutableStateOf<Map<String, List<Tarea>>>(emptyMap()) }
        var cargando by remember { mutableStateOf(true) }
        
        // Estado de diálogos
        var mostrarDialogoCrear by remember { mutableStateOf(false) }
        var tareaSeleccionada by remember { mutableStateOf<Tarea?>(null) }

        fun cargarTareas() {
            scope.launch {
                cargando = true
                try {
                    kanban = httpClient.get("/api/tareas/kanban").body()
                } catch (e: Exception) {
                    println("Error cargando tareas: ${e.message}")
                }
                cargando = false
            }
        }

        LaunchedEffect(Unit) {
            cargarTareas()
        }

        var selectedTabIndex by remember { mutableStateOf(0) }
        var filtroBusqueda by remember { mutableStateOf("") }
        val titulosTabs = listOf("Pendiente", "En Proceso", "Pte. Cliente", "Finalizada")
        val keysTabs = listOf("PENDIENTE", "EN_PROCESO", "PENDIENTE_CLIENTE", "FINALIZADA")

        fun filtrarLista(lista: List<Tarea>): List<Tarea> {
            if (filtroBusqueda.isBlank()) return lista
            return lista.filter { 
                it.titulo.contains(filtroBusqueda, ignoreCase = true) || 
                it.clienteId?.toString()?.contains(filtroBusqueda) == true 
            }
        }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Kanban de Tareas", color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulPrimario)
                    )
                    
                    // Barra de Búsqueda
                    Box(modifier = Modifier.fillMaxWidth().background(AzulPrimario).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = filtroBusqueda,
                            onValueChange = { filtroBusqueda = it },
                            placeholder = { Text("Buscar tarea o cliente...", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                cursorColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = AzulPrimario,
                        contentColor = Color.White,
                        edgePadding = 16.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color.White
                            )
                        }
                    ) {
                        titulosTabs.forEachIndexed { index, title ->
                            val key = keysTabs[index]
                            val list = kanban[key] ?: emptyList()
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            title, 
                                            color = if (selectedTabIndex == index) Color.White else Color.White.copy(alpha = 0.7f)
                                        )
                                        if (list.isNotEmpty()) {
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                color = Color.White.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    list.size.toString(), 
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarDialogoCrear = true },
                    containerColor = MoradoSecundario,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Nueva Tarea")
                }
            },
            containerColor = FondoApp
        ) { padding ->
            if (cargando && kanban.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
            } else {
                val key = keysTabs[selectedTabIndex]
                val tareasMostrar = filtrarLista(kanban[key] ?: emptyList())

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tareasMostrar.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = TextoSecundario.copy(alpha = 0.3f))
                                    Spacer(Modifier.height(16.dp))
                                    Text("No hay tareas en esta sección", color = TextoSecundario)
                                }
                            }
                        }
                    } else {
                        items(tareasMostrar) { tarea ->
                            TareaCard(tarea, onClick = { tareaSeleccionada = tarea })
                        }
                    }
                }
            }
        }

        // Diálogo de Crear Tarea
        if (mostrarDialogoCrear) {
            CrearTareaDialog(
                onDismiss = { mostrarDialogoCrear = false },
                onCrear = {
                    cargarTareas()
                    mostrarDialogoCrear = false
                }
            )
        }

        // Diálogo de Detalle / Mover Tarea
        tareaSeleccionada?.let { tarea ->
            DetalleTareaDialog(
                tarea = tarea,
                onDismiss = { tareaSeleccionada = null },
                onModificada = {
                    cargarTareas()
                    tareaSeleccionada = null
                }
            )
        }
    }

    @Composable
    private fun TareaCard(tarea: Tarea, onClick: () -> Unit) {
        val prioridadColor = when (tarea.prioridad) {
            "ALTA" -> RojoError
            "MEDIA" -> NaranjaAccento
            else -> AzulPrimario
        }

        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Badge(containerColor = prioridadColor.copy(alpha = 0.15f)) {
                        val prioridadLabel = when (tarea.prioridad) {
                            "BAJA"  -> "Baja"
                            "MEDIA" -> "Media"
                            "ALTA"  -> "Alta"
                            else    -> tarea.prioridad
                        }
                        Text(prioridadLabel, color = prioridadColor, style = MaterialTheme.typography.labelSmall)
                    }
                    if (tarea.fechaVencimiento != null) {
                        Text(
                            text = "📅 ${tarea.fechaVencimiento.take(10)}", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = TextoSecundario
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextoPrimario
                )
                
                if (tarea.clienteId != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = TextoSecundario)
                        Spacer(Modifier.width(4.dp))
                        Text("Cliente #${tarea.clienteId}", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                }
            }
        }
    }

    @Composable
    private fun CrearTareaDialog(onDismiss: () -> Unit, onCrear: () -> Unit) {
        val scope = rememberCoroutineScope()
        var titulo by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var clienteIdStr by remember { mutableStateOf("") }
        var prioridad by remember { mutableStateOf("MEDIA") }
        var cargando by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nueva Tarea", color = AzulPrimario, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = clienteIdStr,
                        onValueChange = { clienteIdStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("ID Cliente (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Prioridad", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BAJA", "MEDIA", "ALTA").forEach { p ->
                            val label = when (p) {
                                "BAJA" -> "Baja"
                                "MEDIA" -> "Media"
                                "ALTA" -> "Alta"
                                else -> p
                            }
                            FilterChip(
                                selected = prioridad == p,
                                onClick = { prioridad = p },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titulo.isBlank() || cargando) return@Button
                        scope.launch {
                            cargando = true
                            try {
                                val clienteId = clienteIdStr.toLongOrNull()
                                val req = CrearTareaRequest(
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    clienteId = clienteId,
                                    prioridad = prioridad
                                )
                                httpClient.post("/api/tareas") {
                                    contentType(ContentType.Application.Json)
                                    setBody(req)
                                }
                                onCrear()
                            } catch (e: Exception) {
                                println("Error creando tarea: ${e.message}")
                            }
                            cargando = false
                        }
                    },
                    enabled = !cargando && titulo.isNotBlank()
                ) {
                    if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Crear Tarea")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    }

    @Composable
    private fun DetalleTareaDialog(tarea: Tarea, onDismiss: () -> Unit, onModificada: () -> Unit) {
        val scope = rememberCoroutineScope()
        var cargando by remember { mutableStateOf(false) }

        fun moverTarea(nuevoEstado: String) {
            scope.launch {
                cargando = true
                try {
                    httpClient.patch("/api/tareas/${tarea.id}/estado") {
                        contentType(ContentType.Application.Json)
                        setBody(ActualizarEstadoTareaRequest(nuevoEstado))
                    }
                    onModificada()
                } catch (e: Exception) {
                    println("Error actualizando: ${e.message}")
                }
                cargando = false
            }
        }

        fun borrarTarea() {
            scope.launch {
                cargando = true
                try {
                    httpClient.delete("/api/tareas/${tarea.id}")
                    onModificada()
                } catch (e: Exception) {
                    println("Error borrando: ${e.message}")
                }
                cargando = false
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(tarea.titulo, color = TextoPrimario, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Detalles", style = MaterialTheme.typography.labelSmall, color = AzulPrimario)
                    Text(tarea.descripcion.ifBlank { "Sin descripción detallada." })
                    Divider()
                    Text("Cambiar Estado a:", style = MaterialTheme.typography.labelSmall, color = AzulPrimario)
                    
                    val estados = listOf("PENDIENTE", "EN_PROCESO", "PENDIENTE_CLIENTE", "FINALIZADA")
                    estados.forEach { estado ->
                        if (tarea.estado != estado) {
                            val label = when (estado) {
                                "PENDIENTE"         -> "Pendiente"
                                "EN_PROCESO"        -> "En Proceso"
                                "PENDIENTE_CLIENTE" -> "Pendiente Cliente"
                                "FINALIZADA"        -> "Finalizada"
                                else                -> estado
                            }
                            OutlinedButton(
                                onClick = { moverTarea(estado) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !cargando
                            ) {
                                Text("Mover a $label")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(onClick = { borrarTarea() }, enabled = !cargando) {
                    Text("Eliminar Tarea", color = RojoError)
                }
            }
        )
    }
}
