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
import androidx.compose.ui.draw.shadow
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.*
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*
import com.pokeshophub.util.formatTwoDecimals

class ReservaTorneosScreen(val sesion: SesionUsuario, @field:Transient val onBack: (() -> Unit)? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var torneos by remember { mutableStateOf<List<Torneo>>(emptyList()) }
        var inscritosIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
        var saldoCliente by remember { mutableStateOf(0.0) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var procesandoId by remember { mutableStateOf<Long?>(null) }

        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    // Obtener todos los torneos
                    val listaTorneos: List<Torneo> = httpClient.get("/api/torneos").body()
                    torneos = listaTorneos
                    
                    // Obtener torneos en los que está inscrito el cliente
                    val listaInscritos: List<Torneo> = httpClient.get("/api/torneos/inscritos/${sesion.userId}").body()
                    inscritosIds = listaInscritos.map { it.id }.toSet()

                    // Obtener saldo cliente
                    val cliente: ClienteDto = httpClient.get("/api/clientes/${sesion.userId}").body()
                    saldoCliente = cliente.saldo
                } catch (e: Exception) {
                    errorMsg = "Error al cargar los torneos: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }

        fun inscribirse(torneoId: Long) {
            scope.launch {
                procesandoId = torneoId
                try {
                    val response = httpClient.post("/api/torneos/$torneoId/inscribir/${sesion.userId}")
                    if (response.status.value in 200..299) {
                        cargarDatos()
                    } else {
                        val res = response.body<MensajeResponse>()
                        errorMsg = res.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al inscribirse: ${e.message}"
                } finally {
                    procesandoId = null
                }
            }
        }

        fun desapuntarse(torneoId: Long) {
            scope.launch {
                procesandoId = torneoId
                try {
                    val response = httpClient.post("/api/torneos/$torneoId/desapuntar/${sesion.userId}")
                    if (response.status.value in 200..299) {
                        cargarDatos()
                    } else {
                        val res = response.body<MensajeResponse>()
                        errorMsg = res.mensaje
                    }
                } catch (e: Exception) {
                    errorMsg = "Error al desapuntarse: ${e.message}"
                } finally {
                    procesandoId = null
                }
            }
        }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Reserva de Torneos", color = TextoSobrePrimario, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Monedero: ${saldoCliente.formatTwoDecimals()} €", color = TextoSobrePrimario.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        },
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
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Sin Canvas difusos decorativos para mantener la estética Neo-brutalista limpia

                when {
                    cargando && torneos.isEmpty() -> {
                        CircularProgressIndicator(
                            color = AzulPrimario,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMsg != null && torneos.isEmpty() -> {
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
                    torneos.isEmpty() -> {
                        Text(
                            text = "No hay torneos programados.",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextoSecundario
                        )
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            errorMsg?.let { msg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = RojoError.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, NeoBorde),
                                    shape = RoundedCornerShape(12.dp)
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
                                items(torneos) { torneo ->
                                    val inscrito = inscritosIds.contains(torneo.id)
                                    TorneoCard(
                                        torneo = torneo,
                                        inscrito = inscrito,
                                        saldo = saldoCliente,
                                        procesando = procesandoId == torneo.id,
                                        onAccionClick = {
                                            if (inscrito) desapuntarse(torneo.id)
                                            else inscribirse(torneo.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TorneoCard(
        torneo: Torneo,
        inscrito: Boolean,
        saldo: Double,
        procesando: Boolean,
        onAccionClick: () -> Unit
    ) {
        val totalParticipantes = torneo.participantesCount
        val cuposDisponibles = torneo.maxParticipantes - totalParticipantes
        val sinCupos = cuposDisponibles <= 0
        val precio = torneo.precioInscripcion

        NeoBrutalistCard(
            modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
            backgroundColor = Superficie,
            shape = RoundedCornerShape(20.dp),
            shadowOffset = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Categoria/Estado Badge
                    Surface(
                        color = when {
                            inscrito -> VerdeConfirmacion.copy(alpha = 0.12f)
                            sinCupos -> RojoError.copy(alpha = 0.12f)
                            else -> AzulPrimario.copy(alpha = 0.08f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when {
                                inscrito -> "INSCRITO"
                                sinCupos -> "COMPLETO"
                                else -> "INSCRIPCIÓN ABIERTA"
                            },
                            color = when {
                                inscrito -> VerdeConfirmacion
                                sinCupos -> RojoError
                                else -> if (isAppDarkTheme.value) AzulPrimario else TextoSecundario
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Precio
                    Text(
                        text = if (precio > 0.0) "${precio.formatTwoDecimals()} €" else "Gratis",
                        fontWeight = FontWeight.ExtraBold,
                        color = MoradoSecundario,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = torneo.nombre,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoPrimario
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = torneo.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(color = NeoBorde, thickness = 1.dp)

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = TextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${torneo.fecha} • ${torneo.hora}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, tint = TextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Participantes: $totalParticipantes / ${torneo.maxParticipantes}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                        }
                    }

                    Button(
                        onClick = onAccionClick,
                        enabled = !procesando && (inscrito || (!sinCupos && (precio == 0.0 || saldo >= precio))),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inscrito) RojoError else AzulPrimario,
                            contentColor = if (inscrito) Color.White else TextoSobrePrimario,
                            disabledContainerColor = SuperficieVariante
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(color = if (inscrito) Color.White else TextoSobrePrimario, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (inscrito) "Desapuntarse" else "Inscribirse",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
