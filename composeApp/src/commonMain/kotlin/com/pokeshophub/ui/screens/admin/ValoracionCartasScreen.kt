package com.pokeshophub.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.pokeshophub.util.decodeImageBitmap

class ValoracionCartasScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var tasaciones by remember { mutableStateOf<List<Tasacion>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var refreshTrigger by remember { mutableStateOf(0) }

        // Estado para valorar
        var tasacionAValorar by remember { mutableStateOf<Tasacion?>(null) }
        var valorStr by remember { mutableStateOf("") }
        var notasAdmin by remember { mutableStateOf("") }
        var procesandoVal by remember { mutableStateOf(false) }

        // Subnotas PSA (0-10 cada una)
        var subCentrado by remember { mutableStateOf("") }
        var subBordes by remember { mutableStateOf("") }
        var subEsquinas by remember { mutableStateOf("") }
        var subSuperficie by remember { mutableStateOf("") }

        // Media y nota PSA calculada en tiempo real
        val mediaSubnotas: Double? = remember(subCentrado, subBordes, subEsquinas, subSuperficie) {
            val vals = listOf(subCentrado, subBordes, subEsquinas, subSuperficie)
                .mapNotNull { it.replace(",", ".").toDoubleOrNull() }
            if (vals.size == 4) vals.average() else null
        }
        val notaFinalLabel: String = remember(mediaSubnotas) {
            when {
                mediaSubnotas == null -> "-"
                mediaSubnotas >= 9.5 -> "PSA 10 — GEM-MT"
                mediaSubnotas >= 9.0 -> "PSA 9 — MINT"
                mediaSubnotas >= 8.5 -> "PSA 8 — NM-MT"
                mediaSubnotas >= 8.0 -> "PSA 8 — NM-MT"
                mediaSubnotas >= 7.5 -> "PSA 7 — NM"
                mediaSubnotas >= 7.0 -> "PSA 7 — NM"
                mediaSubnotas >= 6.0 -> "PSA 6 — EX-MT"
                mediaSubnotas >= 5.0 -> "PSA 5 — EX"
                mediaSubnotas >= 4.0 -> "PSA 4 — VG-EX"
                mediaSubnotas >= 3.0 -> "PSA 3 — VG"
                mediaSubnotas >= 2.0 -> "PSA 2 — GOOD"
                else -> "PSA 1 — POOR"
            }
        }

        // Estado para ver foto ampliada
        var fotoAmpliadaTasacionId by remember { mutableStateOf<Long?>(null) }
        var fotoBytes by remember { mutableStateOf<ByteArray?>(null) }
        var cargandoFoto by remember { mutableStateOf(false) }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    tasaciones = httpClient.get("/api/tasaciones/admin/pendientes").body()
                } catch (e: Exception) {
                    errorMsg = "Error al cargar tasaciones pendientes: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(refreshTrigger) {
            cargarDatos()
        }

        LaunchedEffect(fotoAmpliadaTasacionId) {
            val id = fotoAmpliadaTasacionId
            if (id != null) {
                scope.launch {
                    try {
                        cargandoFoto = true
                        fotoBytes = httpClient.get("/api/tasaciones/foto/$id").body()
                    } catch (e: Exception) {
                        fotoBytes = null
                    } finally {
                        cargandoFoto = false
                    }
                }
            } else {
                fotoBytes = null
            }
        }

        fun valorar(tasacionId: Long, estado: String) {
            scope.launch {
                procesandoVal = true
                try {
                    val valor = valorStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val response = httpClient.post("/api/tasaciones/admin/$tasacionId/valorar") {
                        contentType(ContentType.Application.Json)
                        setBody(ValorarTasacionRequest(
                            valorEstimado = if (estado == "VALORADA") valor else 0.0,
                            notasAdmin = notasAdmin,
                            estado = estado,
                            subCentrado = if (estado == "VALORADA") subCentrado.replace(",", ".").toDoubleOrNull() else null,
                            subBordes = if (estado == "VALORADA") subBordes.replace(",", ".").toDoubleOrNull() else null,
                            subEsquinas = if (estado == "VALORADA") subEsquinas.replace(",", ".").toDoubleOrNull() else null,
                            subSuperficie = if (estado == "VALORADA") subSuperficie.replace(",", ".").toDoubleOrNull() else null
                        ))
                    }
                    if (response.status.value in 200..299) {
                        tasacionAValorar = null
                        valorStr = ""
                        notasAdmin = ""
                        subCentrado = ""
                        subBordes = ""
                        subEsquinas = ""
                        subSuperficie = ""
                        refreshTrigger++
                    } else {
                        val res = response.body<MensajeResponse>()
                        errorMsg = res.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al procesar tasación: ${e.message}"
                } finally {
                    procesandoVal = false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Valoración de Cartas", color = Color.White, fontWeight = FontWeight.Bold) },
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
                                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                    tasaciones.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(24.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(64.dp), tint = VerdeConfirmacion.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No hay tasaciones pendientes de revisión.",
                                color = TextoSecundario,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            errorMsg?.let { msg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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

                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(tasaciones) { tas ->
                                    TasacionPendienteCard(
                                        tas = tas,
                                        onVerFoto = { fotoAmpliadaTasacionId = tas.id },
                                        onProcesar = {
                                            tasacionAValorar = tas
                                            valorStr = ""
                                            notasAdmin = ""
                                            subCentrado = ""
                                            subBordes = ""
                                            subEsquinas = ""
                                            subSuperficie = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Modal para procesar Valoración
                tasacionAValorar?.let { tas ->
                    Dialog(
                        onDismissRequest = { if (!procesandoVal) tasacionAValorar = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(20.dp),
                            color = Superficie,
                            tonalElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Cabecera
                                Text("Valorar Carta #${tas.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoPrimario)
                                Text("Cliente: ${tas.nombreCliente}", fontSize = 13.sp, color = TextoSecundario)
                                Text("Descripción: ${tas.descripcion}", fontSize = 13.sp, color = TextoPrimario)

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // Subnotas PSA
                                Text(
                                    "Subnotas (estilo PSA)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AzulPrimario
                                )
                                Text(
                                    "Introduce una nota del 0 al 10 para cada criterio:",
                                    fontSize = 12.sp,
                                    color = TextoSecundario
                                )

                                val subnotaLabels = listOf(
                                    Triple("🎯 Centrado", subCentrado, { v: String -> subCentrado = v }),
                                    Triple("📏 Bordes", subBordes, { v: String -> subBordes = v }),
                                    Triple("🔲 Esquinas", subEsquinas, { v: String -> subEsquinas = v }),
                                    Triple("✨ Superficie", subSuperficie, { v: String -> subSuperficie = v })
                                )

                                subnotaLabels.forEach { (label, value, onValueChange) ->
                                    OutlinedTextField(
                                        value = value,
                                        onValueChange = onValueChange,
                                        label = { Text(label) },
                                        placeholder = { Text("0.0 – 10.0") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AzulPrimario,
                                            focusedLabelColor = AzulPrimario
                                        )
                                    )
                                }

                                // Resultado de media y nota PSA
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (mediaSubnotas != null) AzulPrimario.copy(alpha = 0.08f) else Color(0xFFF1F5F9)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Nota Media: ${if (mediaSubnotas != null) String.format("%.2f", mediaSubnotas) else "—"} / 10",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (mediaSubnotas != null) AzulPrimario else TextoSecundario
                                        )
                                        Text(
                                            notaFinalLabel,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = when {
                                                mediaSubnotas == null -> TextoSecundario
                                                mediaSubnotas >= 9.0 -> VerdeConfirmacion
                                                mediaSubnotas >= 7.0 -> AmarilloAlerta
                                                else -> RojoError
                                            }
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // Valor en euros
                                OutlinedTextField(
                                    value = valorStr,
                                    onValueChange = { valorStr = it },
                                    label = { Text("Valor de Tasación (€)") },
                                    placeholder = { Text("Ej: 45.00") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VerdeConfirmacion,
                                        focusedLabelColor = VerdeConfirmacion
                                    )
                                )

                                // Notas opcionales
                                OutlinedTextField(
                                    value = notasAdmin,
                                    onValueChange = { notasAdmin = it },
                                    label = { Text("Notas o Comentarios (opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )

                                // Botones de acción
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { tasacionAValorar = null },
                                        enabled = !procesandoVal,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Cancelar")
                                    }

                                    Button(
                                        onClick = { valorar(tas.id, "RECHAZADA") },
                                        enabled = !procesandoVal,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RojoError)
                                    ) {
                                        Text("Rechazar", color = Color.White)
                                    }
                                }

                                val subnotasCompletas = mediaSubnotas != null
                                val notasFinal = if (subnotasCompletas) "[${notaFinalLabel}] " else ""
                                Button(
                                    onClick = {
                                        // Añadir nota PSA a las notas del admin automáticamente si hay subnotas
                                        if (subnotasCompletas && notasAdmin.isNotBlank().not()) {
                                            notasAdmin = "$notasFinal${notasAdmin}"
                                        } else if (subnotasCompletas) {
                                            notasAdmin = "$notasFinal$notasAdmin"
                                        }
                                        valorar(tas.id, "VALORADA")
                                    },
                                    enabled = !procesandoVal && valorStr.isNotBlank() && subnotasCompletas,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeConfirmacion)
                                ) {
                                    if (procesandoVal) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Abonar y Valorar", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Modal para ver foto ampliada con zoom
                if (fotoAmpliadaTasacionId != null) {
                    Dialog(
                        onDismissRequest = { fotoAmpliadaTasacionId = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        ) {
                            if (cargandoFoto) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                val bitmap = remember(fotoBytes) {
                                    fotoBytes?.let { decodeImageBitmap(it) }
                                }
                                if (bitmap != null) {
                                    ZoomableImage(bitmap = bitmap)
                                } else {
                                    Text(
                                        "Error al cargar la foto",
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { fotoAmpliadaTasacionId = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasacionPendienteCard(
    tas: Tasacion,
    onVerFoto: () -> Unit,
    onProcesar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ID Tasación: #${tas.id}",
                    fontWeight = FontWeight.Bold,
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
                Surface(
                    color = NaranjaAccento.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PENDIENTE",
                        color = NaranjaAccento,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Entrenador: ${tas.nombreCliente}",
                fontWeight = FontWeight.Bold,
                color = TextoPrimario,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = tas.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Subida: ${tas.fecha.take(16).replace("T", " ")}",
                fontSize = 11.sp,
                color = TextoSecundario
            )

            Spacer(Modifier.height(16.dp))

            Divider(color = Color(0xFFF1F5F9))

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onVerFoto) {
                    Icon(Icons.Default.Visibility, null, tint = AzulPrimario, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver Foto", color = AzulPrimario, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onProcesar,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Valorar / Procesar", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(bitmap: ImageBitmap) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1.1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            offset = Offset.Zero
                        }
                    }
                )
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = "Foto ampliada",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}
