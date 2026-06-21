package com.pokeshophub.ui.screens.admin

import androidx.compose.animation.*
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
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.ResenaDto
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.theme.*

class ResenasAdminScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var reseñas by remember { mutableStateOf<List<ResenaDto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var filtroEstrellas by remember { mutableStateOf<Int?>(null) }

        fun cargarReseñas() {
            scope.launch {
                cargando = true
                try {
                    val url = if (filtroEstrellas != null) "/api/resenas?estrellas=$filtroEstrellas" else "/api/resenas"
                    reseñas = httpClient.get(url).body()
                } catch (e: Exception) {
                    println("Error cargando reseñas: ${e.message}")
                }
                cargando = false
            }
        }

        LaunchedEffect(filtroEstrellas) {
            cargarReseñas()
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Reseñas de Clientes", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AzulPrimario,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = FondoApp
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                
                // Filtros
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Superficie,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tune, null, tint = AzulPrimario, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Filtrar por valoración", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextoPrimario)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filtroEstrellas == null,
                                onClick = { filtroEstrellas = null },
                                label = { Text("Todas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AzulPrimario,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            (5 downTo 1).forEach { stars ->
                                FilterChip(
                                    selected = filtroEstrellas == stars,
                                    onClick = { filtroEstrellas = stars },
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$stars")
                                            Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = if (filtroEstrellas == stars) Color.White else NaranjaAccento)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AzulPrimario,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                if (cargando) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulPrimario, strokeWidth = 3.dp)
                    }
                } else if (reseñas.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = TextoSecundario.copy(alpha = 0.2f))
                        Text("No hay reseñas con este filtro", color = TextoSecundario, modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(reseñas) { reseña ->
                            ReseñaCard(reseña)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ReseñaCard(reseña: ResenaDto) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Superficie,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = AzulPrimario.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    reseña.nombreCliente.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = AzulPrimario
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                reseña.nombreCliente,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextoPrimario
                            )
                            Text(
                                reseña.fecha.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario
                            )
                        }
                    }
                }

                Row(modifier = Modifier.padding(vertical = 12.dp)) {
                    for (i in 1..5) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (i <= reseña.estrellas) NaranjaAccento else Color(0xFFE2E8F0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (reseña.comentario.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = FondoApp.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            reseña.comentario,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoPrimario,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
