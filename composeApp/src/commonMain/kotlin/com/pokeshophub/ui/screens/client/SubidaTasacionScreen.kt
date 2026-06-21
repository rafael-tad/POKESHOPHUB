package com.pokeshophub.ui.screens.client

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
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
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*
import com.pokeshophub.util.*
import com.pokeshophub.ui.screens.shared.VisorDocumentoScreen
import androidx.compose.ui.layout.ContentScale

class SubidaTasacionScreen(val sesion: SesionUsuario, @field:Transient val onBack: (() -> Unit)? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var tasaciones by remember { mutableStateOf<List<Tasacion>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        var descripcion by remember { mutableStateOf("") }
        var photoFrontalBytes by remember { mutableStateOf<ByteArray?>(null) }
        var photoTraseraBytes by remember { mutableStateOf<ByteArray?>(null) }
        var subiendo by remember { mutableStateOf(false) }

        var showNuevaTasacionDialog by remember { mutableStateOf(false) }

        val cameraLauncherFront = rememberCameraLauncher { bytes ->
            if (bytes != null) {
                photoFrontalBytes = bytes
            }
        }

        val cameraLauncherBack = rememberCameraLauncher { bytes ->
            if (bytes != null) {
                photoTraseraBytes = bytes
            }
        }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    tasaciones = httpClient.get("/api/tasaciones/cliente/${sesion.userId}").body()
                } catch (e: Exception) {
                    errorMsg = "Error al cargar tasaciones: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }

        // Diálogo de Nueva Tasación
        if (showNuevaTasacionDialog) {
            AlertDialog(
                onDismissRequest = { if (!subiendo) showNuevaTasacionDialog = false },
                title = { Text("Tasar nueva carta", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Sube dos fotos (frontal y trasera) de tu carta en orientación VERTICAL para recibir una valoración de Store Credit.",
                            color = TextoSecundario,
                            fontSize = 13.sp
                        )
                        
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (nombre, estado, edición)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeoBorde,
                                unfocusedBorderColor = NeoBorde.copy(alpha = 0.6f),
                                focusedLabelColor = NeoBorde,
                                unfocusedLabelColor = TextoSecundario
                            )
                        )

                        // Advertencia de orientación vertical
                        Surface(
                            color = AmarilloAlerta.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NeoBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = AmarilloAlerta, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "IMPORTANTE: Ambas fotos deben tomarse con la carta colocada en vertical (formato retrato).",
                                    fontSize = 11.sp,
                                    color = TextoPrimario,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Fila de fotos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Panel Frontal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.75f) // Relación de aspecto vertical (3:4)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SuperficieVariante)
                                    .border(BorderStroke(1.dp, NeoBorde), RoundedCornerShape(16.dp))
                                    .clickable { cameraLauncherFront.launch() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoFrontalBytes == null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, null, tint = AzulPrimario, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.height(6.dp))
                                        Text("Foto Frontal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoSecundario)
                                    }
                                } else {
                                    val bitmap = remember(photoFrontalBytes) { photoFrontalBytes?.let { decodeImageBitmap(it) } }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Parte frontal",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    // Indicador de completado
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .background(VerdeConfirmacion, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Panel Trasero
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.75f) // Relación de aspecto vertical (3:4)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SuperficieVariante)
                                    .border(BorderStroke(1.dp, NeoBorde), RoundedCornerShape(16.dp))
                                    .clickable { cameraLauncherBack.launch() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoTraseraBytes == null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, null, tint = AzulPrimario, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.height(6.dp))
                                        Text("Foto Trasera", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoSecundario)
                                    }
                                } else {
                                    val bitmap = remember(photoTraseraBytes) { photoTraseraBytes?.let { decodeImageBitmap(it) } }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Parte trasera",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    // Indicador de completado
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .background(VerdeConfirmacion, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (descripcion.isBlank() || photoFrontalBytes == null || photoTraseraBytes == null) return@Button
                            scope.launch {
                                subiendo = true
                                try {
                                    val combinedBytes = combineFrontBackImages(photoFrontalBytes!!, photoTraseraBytes!!)
                                    httpClient.submitFormWithBinaryData(
                                        url = "/api/tasaciones",
                                        formData = formData {
                                            append("clienteId", sesion.userId.toString())
                                            append("descripcion", descripcion)
                                            append("foto", combinedBytes, Headers.build {
                                                append(HttpHeaders.ContentType, "image/jpeg")
                                                append(HttpHeaders.ContentDisposition, "filename=\"carta.jpg\"")
                                            })
                                        }
                                    )
                                    showNuevaTasacionDialog = false
                                    descripcion = ""
                                    photoFrontalBytes = null
                                    photoTraseraBytes = null
                                    cargarDatos() // Refrescar
                                } catch (e: Exception) {
                                    errorMsg = "Error al subir tasación: ${e.message}"
                                } finally {
                                    subiendo = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulPrimario,
                            contentColor = TextoSobrePrimario,
                            disabledContainerColor = SuperficieVariante
                        ),
                        shape = RoundedCornerShape(20.dp),
                        enabled = descripcion.isNotBlank() && photoFrontalBytes != null && photoTraseraBytes != null && !subiendo
                    ) {
                        if (subiendo) {
                            CircularProgressIndicator(color = TextoSobrePrimario, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Enviar a Tasar", color = TextoSobrePrimario)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showNuevaTasacionDialog = false },
                        enabled = !subiendo
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
                        title = { Text("Mis Tasaciones", color = TextoSobrePrimario) },
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNuevaTasacionDialog = true },
                    containerColor = AzulPrimario,
                    contentColor = TextoSobrePrimario,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Nueva Tasación", tint = TextoSobrePrimario)
                }
            },
            containerColor = FondoApp
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when {
                    cargando && tasaciones.isEmpty() -> {
                        CircularProgressIndicator(
                            color = AzulPrimario,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMsg != null && tasaciones.isEmpty() -> {
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
                    tasaciones.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(24.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(64.dp), tint = TextoSecundario.copy(alpha = 0.3f))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No tienes cartas enviadas a tasación.",
                                color = TextoSecundario,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Pulsa el botón + para enviar tu primera foto.",
                                color = TextoSecundario.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(tasaciones) { tas ->
                                TasacionRow(tas) {
                                    // Ver foto en el visor
                                    val downloadUrl = "/api/tasaciones/foto/${tas.id}"
                                    navigator.push(VisorDocumentoScreen(tas.id, "Foto de Carta #${tas.id}", downloadUrl))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TasacionRow(tas: Tasacion, onVerFotoClick: () -> Unit) {
        val esValorada = tas.estado == "VALORADA"
        
        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
            backgroundColor = Superficie,
            shape = RoundedCornerShape(20.dp),
            shadowOffset = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tasación #${tas.id}",
                        fontWeight = FontWeight.Bold,
                        color = TextoSecundario,
                        fontSize = 12.sp
                    )

                    Surface(
                        color = if (esValorada) VerdeConfirmacion.copy(alpha = 0.15f) else AmarilloAlerta.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tas.estado,
                            color = if (esValorada) VerdeConfirmacion else AmarilloAlerta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = tas.descripcion,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                    style = MaterialTheme.typography.titleMedium
                )

                if (esValorada) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VerdeConfirmacion.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NeoBorde)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Valor Estimado de Compra:",
                                fontSize = 12.sp,
                                color = TextoSecundario
                            )
                            Text(
                                "${tas.valorEstimado ?: 0.0} €",
                                fontWeight = FontWeight.ExtraBold,
                                color = VerdeConfirmacion,
                                fontSize = 20.sp
                            )
                            if (tas.notasAdmin?.isNotBlank() == true) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Notas del tasador: ${tas.notasAdmin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Subida: ${tas.fecha.take(10)}",
                        fontSize = 11.sp,
                        color = TextoSecundario
                    )

                    TextButton(
                        onClick = onVerFotoClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Visibility, null, tint = AzulPrimario, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ver Foto", color = AzulPrimario, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
