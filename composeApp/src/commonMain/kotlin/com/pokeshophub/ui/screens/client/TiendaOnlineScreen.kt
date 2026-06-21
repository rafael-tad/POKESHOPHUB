package com.pokeshophub.ui.screens.client

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import com.pokeshophub.data.network.BASE_URL
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
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
import com.pokeshophub.util.formatTwoDecimals
import com.pokeshophub.util.decodeImageBitmap

class TiendaOnlineScreen(val sesion: SesionUsuario, @field:Transient val onBack: (() -> Unit)? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var saldoCliente by remember { mutableStateOf(0.0) }

        var carrito by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
        var showCartDialog by remember { mutableStateOf(false) }
        var showSuccessPurchaseDialog by remember { mutableStateOf(false) }
        var procesandoCompraCart by remember { mutableStateOf(false) }
        var errorCompraCart by remember { mutableStateOf<String?>(null) }

        // Función para recargar los datos
        fun cargarDatos() {
            scope.launch {
                try {
                    cargando = true
                    errorMsg = null
                    // Obtener productos
                    productos = httpClient.get("/api/tienda/productos").body()
                    // Obtener saldo del cliente
                    val cliente: ClienteDto = httpClient.get("/api/clientes/${sesion.userId}").body()
                    saldoCliente = cliente.saldo
                } catch (e: Exception) {
                    errorMsg = "Error al cargar la tienda: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }

        val itemsEnCarrito = carrito.values.sum()

        // Diálogo del Carrito
        if (showCartDialog) {
            val totalCartAmount = carrito.entries.sumOf { (prodId, qty) ->
                val prod = productos.find { it.id == prodId }
                (prod?.precio ?: 0.0) * qty
            }

            AlertDialog(
                onDismissRequest = { if (!procesandoCompraCart) showCartDialog = false },
                modifier = Modifier.border(1.dp, NeoBorde, RoundedCornerShape(24.dp)),
                title = { Text("Mi Carrito", fontWeight = FontWeight.Bold, color = TextoPrimario) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (carrito.isEmpty()) {
                            Text("Tu carrito está vacío", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                        } else {
                            carrito.forEach { (prodId, qty) ->
                                val prod = productos.find { it.id == prodId }
                                if (prod != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.nombre, fontWeight = FontWeight.SemiBold, color = TextoPrimario, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(
                                                "${(prod.precio * qty).formatTwoDecimals()} € (${prod.precio.formatTwoDecimals()} € c/u)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextoSecundario
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (qty > 1) {
                                                        carrito = carrito + (prodId to (qty - 1))
                                                    } else {
                                                        carrito = carrito - prodId
                                                    }
                                                },
                                                enabled = !procesandoCompraCart,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, "Restar", tint = AzulPrimario, modifier = Modifier.size(18.dp))
                                            }

                                            Text(
                                                text = qty.toString(),
                                                fontWeight = FontWeight.Bold,
                                                color = TextoPrimario,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (qty < prod.stock) {
                                                        carrito = carrito + (prodId to (qty + 1))
                                                    }
                                                },
                                                enabled = qty < prod.stock && !procesandoCompraCart,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, "Sumar", tint = AzulPrimario, modifier = Modifier.size(18.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    carrito = carrito - prodId
                                                },
                                                enabled = !procesandoCompraCart,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, "Eliminar", tint = RojoError, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = NeoBorde, thickness = 1.dp)
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Total Compra: ${totalCartAmount.formatTwoDecimals()} €",
                                fontWeight = FontWeight.ExtraBold,
                                color = MoradoSecundario,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Tu Saldo Disponible: ${saldoCliente.formatTwoDecimals()} €",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (saldoCliente >= totalCartAmount) VerdeConfirmacion else RojoError
                            )

                            if (saldoCliente < totalCartAmount) {
                                Text(
                                    "Saldo insuficiente en tu monedero.",
                                    color = RojoError,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            errorCompraCart?.let { msg ->
                                Text(
                                    text = msg,
                                    color = RojoError,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (carrito.isNotEmpty()) {
                        val totalCartAmount = carrito.entries.sumOf { (prodId, qty) ->
                            val prod = productos.find { it.id == prodId }
                            (prod?.precio ?: 0.0) * qty
                        }
                        Button(
                            onClick = {
                                if (saldoCliente < totalCartAmount) {
                                    errorCompraCart = "Saldo insuficiente en tu monedero."
                                } else {
                                    scope.launch {
                                        procesandoCompraCart = true
                                        errorCompraCart = null
                                        try {
                                            val itemsReq = carrito.map { (prodId, qty) -> 
                                                ItemCompraRequest(productoId = prodId, cantidad = qty) 
                                            }
                                            val response = httpClient.post("/api/tienda/solicitar-compra/${sesion.userId}") {
                                                contentType(ContentType.Application.Json)
                                                setBody(SolicitudCompraRequest(items = itemsReq))
                                            }
                                            val res = response.body<MensajeResponse>()
                                            if (res.success) {
                                                carrito = emptyMap()
                                                showCartDialog = false
                                                showSuccessPurchaseDialog = true
                                                cargarDatos() // Refrescar stock y saldo
                                            } else {
                                                errorCompraCart = res.mensaje
                                            }
                                        } catch (e: Exception) {
                                            errorCompraCart = "Error en el pedido: ${e.message}"
                                        } finally {
                                            procesandoCompraCart = false
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulPrimario,
                                contentColor = TextoSobrePrimario,
                                disabledContainerColor = SuperficieVariante
                            ),
                            enabled = !procesandoCompraCart
                        ) {
                            if (procesandoCompraCart) {
                                CircularProgressIndicator(color = TextoSobrePrimario, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Solicitar compra", color = TextoSobrePrimario)
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCartDialog = false },
                        enabled = !procesandoCompraCart
                    ) {
                        Text("Cerrar", color = TextoSecundario)
                    }
                },
                containerColor = Superficie,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Diálogo de Éxito de Compra
        if (showSuccessPurchaseDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessPurchaseDialog = false },
                modifier = Modifier.border(1.dp, NeoBorde, RoundedCornerShape(24.dp)),
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = VerdeConfirmacion,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("Pedido Enviado", fontWeight = FontWeight.Bold, color = TextoPrimario, textAlign = TextAlign.Center) },
                text = {
                    Text(
                        "Se ha enviado el pedido a los administradores. Van a revisarlo y aceptarlo a la brevedad.",
                        textAlign = TextAlign.Center,
                        color = TextoSecundario
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showSuccessPurchaseDialog = false },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario, contentColor = TextoSobrePrimario)
                    ) {
                        Text("Aceptar", color = TextoSobrePrimario)
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
                        title = {
                            Column {
                                Text("Tienda Online", color = TextoSobrePrimario, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        actions = {
                            Box(modifier = Modifier.padding(end = 16.dp).clickable {
                                errorCompraCart = null
                                showCartDialog = true
                            }) {
                                BadgedBox(
                                    badge = {
                                        if (itemsEnCarrito > 0) {
                                            Badge(
                                                containerColor = AzulVariante,
                                                contentColor = AzulPrimario
                                            ) {
                                                Text(itemsEnCarrito.toString(), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = TextoSobrePrimario,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
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
                    cargando && productos.isEmpty() -> {
                        CircularProgressIndicator(
                            color = AzulPrimario,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMsg != null && productos.isEmpty() -> {
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
                    productos.isEmpty() -> {
                        Text(
                            text = "No hay productos disponibles en la tienda.",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextoSecundario
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(160.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                             items(productos) { prod ->
                                 val qty = carrito[prod.id] ?: 0
                                 ProductoCard(
                                     prod = prod,
                                     cantidadEnCarrito = qty,
                                     onAgregarAlCarrito = {
                                         val current = carrito[prod.id] ?: 0
                                         if (current < prod.stock) {
                                             carrito = carrito + (prod.id to (current + 1))
                                         }
                                     }
                                 )
                             }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ProductoCard(
        prod: Producto,
        cantidadEnCarrito: Int,
        onAgregarAlCarrito: () -> Unit
    ) {
        NeoBrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            backgroundColor = SuperficieTarjeta,
            shape = RoundedCornerShape(16.dp),
            shadowOffset = 6.dp
        ) {
            Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                // Imagen de producto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuperficieVariante),
                    contentAlignment = Alignment.Center
                ) {
                    if (prod.imagenUrl != null) {
                        ProductoImagen(
                            productoId = prod.id,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.CardMembership,
                            null,
                            modifier = Modifier.size(50.dp),
                            tint = AzulPrimario.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Stock badge
                    Surface(
                        color = if (prod.stock > 0) AzulPrimario.copy(alpha = 0.1f) else RojoError.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                    ) {
                        Text(
                            text = if (prod.stock > 0) "Stock: ${prod.stock}" else "Sin stock",
                            color = if (prod.stock > 0) AzulPrimario else RojoError,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    prod.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextoPrimario
                )

                Text(
                    prod.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextoSecundario
                )

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "${prod.precio.formatTwoDecimals()} €",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MoradoSecundario
                    )

                    IconButton(
                        onClick = onAgregarAlCarrito,
                        enabled = prod.stock > 0 && cantidadEnCarrito < prod.stock,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (prod.stock > 0 && cantidadEnCarrito < prod.stock) AzulPrimario else SuperficieVariante,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir al carrito",
                            tint = if (prod.stock > 0 && cantidadEnCarrito < prod.stock) TextoSobrePrimario else TextoSecundario,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoImagen(productoId: Long, modifier: Modifier = Modifier) {
    var imageBytes by remember(productoId) { mutableStateOf<ByteArray?>(null) }
    var cargando by remember(productoId) { mutableStateOf(true) }

    LaunchedEffect(productoId) {
        try {
            cargando = true
            imageBytes = httpClient.get("/api/tienda/productos/foto/$productoId").body()
        } catch (e: Exception) {
            imageBytes = null
        } finally {
            cargando = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (cargando) {
            CircularProgressIndicator(color = AzulPrimario, modifier = Modifier.size(24.dp))
        } else {
            val bitmap = remember(imageBytes) {
                imageBytes?.let { decodeImageBitmap(it) }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto de producto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                Icon(
                    Icons.Default.CardMembership,
                    null,
                    modifier = Modifier.size(50.dp),
                    tint = AzulPrimario.copy(alpha = 0.3f)
                )
            }
        }
    }
}
