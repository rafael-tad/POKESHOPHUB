package com.pokeshophub.ui.screens.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.CrearResenaRequest
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class ResenaClienteScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var estrellas by remember { mutableStateOf(0) }
        var comentario by remember { mutableStateOf("") }
        var enviando by remember { mutableStateOf(false) }
        var enviadoConExito by remember { mutableStateOf(false) }

        val ratingText = when (estrellas) {
            1 -> "Malo"
            2 -> "Regular"
            3 -> "Bueno"
            4 -> "Muy Bueno"
            5 -> "¡Excelente!"
            else -> "Selecciona una puntuación"
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Valorar Servicio", fontWeight = FontWeight.ExtraBold, color = TextoSobrePrimario) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBackIosNew, "Volver", modifier = Modifier.size(20.dp))
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo decorativo superior plano Neo-brutalist
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(AzulPrimario)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!enviadoConExito) {
                        // ── Tarjeta Principal ─────────────────────────────
                        NeoBrutalistCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = Superficie,
                            shadowOffset = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Reviews,
                                    null,
                                    tint = AzulPrimario.copy(alpha = 0.1f),
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Text(
                                    "¿Qué te parece la app?",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextoPrimario,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                
                                Text(
                                    "Tu feedback es vital para nosotros",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextoSecundario,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                                )

                                // Selector de estrellas
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 1..5) {
                                        val isSelected = i <= estrellas
                                        val scale by animateFloatAsState(if (isSelected) 1.2f else 1f)
                                        
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                                            contentDescription = null,
                                            tint = if (isSelected) NaranjaAccento else Color(0xFFCBD5E1),
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clickable { estrellas = i }
                                                .animateContentSize()
                                        )
                                    }
                                }

                                Text(
                                    ratingText,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (estrellas > 0) AzulPrimario else TextoSecundario.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
                                )

                                // Campo de comentario
                                OutlinedTextField(
                                    value = comentario,
                                    onValueChange = { comentario = it },
                                    placeholder = { Text("Escribe aquí tu experiencia...", color = TextoSecundario.copy(alpha = 0.5f)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeoBorde,
                                        unfocusedBorderColor = NeoBorde.copy(alpha = 0.6f),
                                        focusedLabelColor = NeoBorde,
                                        unfocusedLabelColor = TextoSecundario,
                                        focusedContainerColor = SuperficieVariante,
                                        unfocusedContainerColor = SuperficieVariante
                                    )
                                )

                                Spacer(Modifier.height(32.dp))

                                // Botón
                                Button(
                                    onClick = {
                                        scope.launch {
                                            enviando = true
                                            try {
                                                val response = httpClient.post("/api/resenas") {
                                                    contentType(ContentType.Application.Json)
                                                    setBody(CrearResenaRequest(sesion.userId, estrellas, comentario))
                                                }
                                                if (response.status == HttpStatusCode.OK) {
                                                    enviadoConExito = true
                                                } else {
                                                    snackbarHostState.showSnackbar("Error del servidor: ${response.status}")
                                                }
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Error de conexión: Verifique el servidor")
                                                println("Error enviando reseña: ${e.message}")
                                            }
                                            enviando = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AzulPrimario,
                                        contentColor = TextoSobrePrimario,
                                        disabledContainerColor = SuperficieVariante
                                    ),
                                    enabled = estrellas > 0 && !enviando
                                ) {
                                    if (enviando) {
                                        CircularProgressIndicator(color = TextoSobrePrimario, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                    } else {
                                        Text("ENVIAR VALORACIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = TextoSobrePrimario)
                                    }
                                }
                            }
                        }
                    } else {
                        // Pantalla de éxito
                        Column(
                            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    modifier = Modifier.size(120.dp),
                                    shape = CircleShape,
                                    color = VerdeConfirmacion.copy(alpha = 0.15f)
                                ) {}
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = VerdeConfirmacion,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            Text(
                                "¡Gracias por tu apoyo!",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextoPrimario,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                "Tu valoración ha sido registrada con éxito y nos ayuda a seguir creciendo.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoSecundario,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                            )
                            
                            Spacer(Modifier.height(48.dp))
                            
                            Button(
                                onClick = { navigator.pop() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario, contentColor = TextoSobrePrimario)
                            ) {
                                Text("VOLVER AL PANEL PRINCIPAL", fontWeight = FontWeight.Bold, color = TextoSobrePrimario)
                            }
                        }
                    }
                }
            }
        }
    }
}
