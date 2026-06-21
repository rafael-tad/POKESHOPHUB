package com.pokeshophub.ui.screens.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.Notificacion
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.model.ClienteDto
import com.pokeshophub.data.network.AuthRepository
import com.pokeshophub.data.network.httpClient
import com.pokeshophub.ui.screens.auth.LoginScreen
import com.pokeshophub.ui.theme.*
import com.pokeshophub.util.formatTwoDecimals
import org.jetbrains.compose.resources.painterResource
import pokeshophub.composeapp.generated.resources.*

class HomeClienteScreen(val sesion: SesionUsuario) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepo = remember { AuthRepository() }
        val scope = rememberCoroutineScope()

        var selectedTab by remember { mutableStateOf(0) }
        var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
        var saldoCliente by remember { mutableStateOf(0.0) }
        
        LaunchedEffect(selectedTab) {
            if (selectedTab == 0) {
                scope.launch {
                    try {
                        val lista: List<Notificacion> = httpClient.get("/api/notificaciones/${sesion.userId}").body()
                        notificaciones = lista.sortedByDescending { it.id }
                    } catch (_: Exception) {}
                }
                scope.launch {
                    try {
                        val cliente: ClienteDto = httpClient.get("/api/clientes/${sesion.userId}").body()
                        saldoCliente = cliente.saldo
                    } catch (_: Exception) {}
                }
            }
        }

        val onMarcarLeida: (Long) -> Unit = { id ->
            scope.launch {
                try {
                    httpClient.patch("/api/notificaciones/$id/leida")
                    notificaciones = notificaciones.map { if (it.id == id) it.copy(leida = true) else it }
                } catch (e: Exception) {}
            }
        }

        Scaffold(
            containerColor = FondoApp,
            bottomBar = {
                NavigationBar(
                    containerColor = AzulPrimario,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .border(1.dp, NeoBorde, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                    val items = listOf(
                        Triple("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
                        Triple("Tasaciones", Icons.Outlined.PhotoCamera, Icons.Filled.PhotoCamera),
                        Triple("Chat", Icons.Outlined.Forum, Icons.Filled.Forum),
                        Triple("Perfil", Icons.Outlined.Person, Icons.Filled.Person)
                    )
                    items.forEachIndexed { index, (label, iconOut, iconFil) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(if(selectedTab == index) iconFil else iconOut, null) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextoSobrePrimario,
                                selectedTextColor = TextoSobrePrimario,
                                unselectedIconColor = TextoSobrePrimario.copy(alpha = 0.4f),
                                unselectedTextColor = TextoSobrePrimario.copy(alpha = 0.4f),
                                indicatorColor = AzulVariante.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Sin Canvas difusos decorativos para mantener la estética Neo-brutalista limpia

                val onBackToHome = { selectedTab = 0 }
                when (selectedTab) {
                    0 -> HomeTabContent(sesion, navigator, notificaciones, authRepo, onMarcarLeida, saldoCliente)
                    1 -> TasacionesTabContent(sesion, onBackToHome)
                    2 -> MensajesTabContent(sesion, onBackToHome)
                    3 -> PerfilTabContent(sesion, onBackToHome)
                }
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    sesion: SesionUsuario,
    navigator: cafe.adriel.voyager.navigator.Navigator,
    notificaciones: List<Notificacion>,
    authRepo: AuthRepository,
    onMarcarLeida: (Long) -> Unit,
    saldoCliente: Double
) {
    var expandedNotifs by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            // Cabecera con Logotipo Oficial
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_pokeshop),
                    contentDescription = "Logo PokeShop Hub",
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "PokeShop Hub",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = TextoPrimario
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Hola, ${sesion.nombre.split(" ").firstOrNull()}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = TextoPrimario)
                    Text("Estatus: Entrenador Activo", style = MaterialTheme.typography.bodyMedium, color = AzulPrimario.copy(alpha = 0.6f))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = { expandedNotifs = true },
                            modifier = Modifier.background(AzulPrimario.copy(alpha = 0.05f), CircleShape)
                        ) {
                            BadgedBox(badge = { if(notificaciones.any{!it.leida}) Badge(containerColor = AzulPrimario) }) {
                                Icon(Icons.Default.NotificationsNone, null, tint = AzulPrimario)
                            }
                        }
                        
                        MaterialTheme(
                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))
                        ) {
                            DropdownMenu(
                                expanded = expandedNotifs,
                                onDismissRequest = { expandedNotifs = false },
                                modifier = Modifier
                                    .width(280.dp)
                                    .background(Superficie)
                            ) {
                                Text(
                                    "Notificaciones Recientes",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextoPrimario
                                )
                                Divider(color = Color(0xFFF1F5F9))
                                
                                val ultimas = notificaciones.filter { !it.leida }.take(3)
                                if (ultimas.isEmpty()) {
                                    Text("No hay notificaciones nuevas", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                } else {
                                    ultimas.forEach { notif ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(notif.titulo, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(notif.mensaje, style = MaterialTheme.typography.bodySmall, color = TextoSecundario, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                }
                                            },
                                            onClick = { 
                                                onMarcarLeida(notif.id)
                                            }
                                        )
                                    }
                                }
                                
                                Divider(color = Color(0xFFF1F5F9))
                                TextButton(
                                    onClick = { 
                                        expandedNotifs = false
                                        navigator.push(NotificacionesScreen(sesion)) 
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Ver todas las notificaciones", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            authRepo.cerrarSesion()
                            navigator.replaceAll(LoginScreen())
                        },
                        modifier = Modifier.background(RojoError.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Logout, null, tint = RojoError)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            NeoBrutalistCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                backgroundColor = AzulPrimario,
                shape = RoundedCornerShape(16.dp),
                shadowOffset = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TextoSobrePrimario.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, TextoSobrePrimario, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, null, tint = TextoSobrePrimario, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(scrollState.maxValue) {
                            if (scrollState.maxValue > 0) {
                                while (true) {
                                    scrollState.animateScrollTo(
                                        value = scrollState.maxValue,
                                        animationSpec = tween(
                                            durationMillis = (scrollState.maxValue * 15).coerceAtLeast(3000),
                                            easing = LinearEasing
                                        )
                                    )
                                    scrollState.scrollTo(0)
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState, enabled = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¡Te damos la bienvenida a PokeShop Hub, ${sesion.nombre}! Explora nuestro catálogo de productos más recientes de la expansión Mega Evolution, reserva plazas en torneos presenciales, solicita tasaciones de tus cartas con la cámara, y mantente al día con nuestras notificaciones. ¡Disfruta de la mejor comunidad TCG!            ",
                                color = TextoSobrePrimario,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("Áreas de PokeShop", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = TextoPrimario)
            Spacer(Modifier.height(16.dp))
        }
        items(menuItems) { item ->
            ServiceRowLuminous(item = item, onClick = { item.onClick(navigator, sesion) })
            Spacer(Modifier.height(12.dp))
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun TasacionesTabContent(sesion: SesionUsuario, onBack: () -> Unit) {
    SubidaTasacionScreen(sesion, onBack).Content()
}

@Composable
private fun MensajesTabContent(sesion: SesionUsuario, onBack: () -> Unit) {
    MensajesClienteScreen(sesion = sesion, onBack = onBack).Content()
}

@Composable
private fun PerfilTabContent(sesion: SesionUsuario, onBack: () -> Unit) {
    PerfilScreen(sesion, onBack).Content()
}

@Composable
private fun ServiceRowLuminous(item: MenuItem, onClick: () -> Unit) {
    NeoBrutalistCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Superficie,
        shape = RoundedCornerShape(16.dp),
        shadowOffset = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(item.color.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icono, null, tint = item.color, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(item.titulo, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextoPrimario)
                    Text(item.subtitulo, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
        }
    }
}

private data class MenuItem(val titulo: String, val subtitulo: String, val icono: ImageVector, val color: Color, val onClick: (cafe.adriel.voyager.navigator.Navigator, SesionUsuario) -> Unit)
private val menuItems: List<MenuItem> get() = listOf(
    MenuItem("Tienda Online", "Catálogo y compra de cartas TCG", Icons.Outlined.ShoppingCart, AzulPrimario, { nav, sesion -> nav.push(TiendaOnlineScreen(sesion)) }),
    MenuItem("Monedero Virtual", "Consulta tu saldo y recarga", Icons.Outlined.AccountBalanceWallet, VerdeConfirmacion, { nav, sesion -> nav.push(BilleteraVirtualScreen(sesion)) }),
    MenuItem("Reserva de Torneos", "Apúntate a eventos Pokémon", Icons.Outlined.EventNote, NaranjaAccento, { nav, sesion -> nav.push(ReservaTorneosScreen(sesion)) }),
    MenuItem("Tasación con Cámara", "Valora tus cartas físicas", Icons.Outlined.PhotoCamera, MoradoSecundario, { nav, sesion -> nav.push(SubidaTasacionScreen(sesion)) }),
    MenuItem("Valorar PokeShop", "Tu opinión cuenta", Icons.Outlined.Stars, AzulVariante, { nav, sesion -> nav.push(ResenaClienteScreen(sesion)) }),
)
