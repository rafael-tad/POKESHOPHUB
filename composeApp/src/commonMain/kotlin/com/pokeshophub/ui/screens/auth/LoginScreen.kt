package com.pokeshophub.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import com.pokeshophub.data.model.SesionUsuario
import com.pokeshophub.data.network.AuthRepository
import com.pokeshophub.ui.screens.admin.DashboardAdminScreen
import com.pokeshophub.ui.screens.client.HomeClienteScreen
import com.pokeshophub.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import pokeshophub.composeapp.generated.resources.*

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val authRepo = remember { AuthRepository() }

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var cargando by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val focusManager = LocalFocusManager.current

        fun doLogin() {
            if (email.isBlank() || password.isBlank()) {
                error = "Por favor, rellena todos los campos"
                return
            }
            scope.launch {
                cargando = true
                error = null
                val result = authRepo.login(email.trim(), password)
                cargando = false
                result.fold(
                    onSuccess = { sesion ->
                        if (sesion.esAdmin) navigator.replace(DashboardAdminScreen(sesion))
                        else navigator.replace(HomeClienteScreen(sesion))
                    },
                    onFailure = { error = it.message }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AuthAzulOscuro, AuthAzulPrimario, AuthAzulOscuro)
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.3f)) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    quadraticBezierTo(
                        size.width * 0.5f, size.height * 0.8f,
                        size.width, size.height * 0.6f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.05f)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = size.width * 0.4f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))

                // ── Logo PokeShop Hub ──────────────────────────────────
                Image(
                    painter = painterResource(Res.drawable.logo_pokeshop),
                    contentDescription = "Logo PokeShop Hub",
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(20.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "POKESHOP HUB",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tu tienda y centro Pokemon de confianza",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(48.dp))

                // ── Tarjeta de Login ───────────────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = AuthSuperficie,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Bienvenido de nuevo",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = AuthTextoPrimario
                        )
                        Text(
                            "Introduce tus credenciales para acceder",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuthTextoSecundario,
                            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; error = null },
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = AuthAzulPrimario) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthAzulPrimario,
                                focusedLabelColor = AuthAzulPrimario,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(Modifier.height(20.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; error = null },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Https, null, tint = AuthAzulPrimario) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        null,
                                        tint = AuthTextoSecundario
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { doLogin() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthAzulPrimario,
                                focusedLabelColor = AuthAzulPrimario,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        // Error message
                        AnimatedVisibility(
                            visible = error != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AuthRojoError.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AuthRojoError.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, null, tint = AuthRojoError, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(error ?: "", color = AuthTextoPrimario, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Botón Login
                        Button(
                            onClick = { if (!cargando) doLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(if (cargando) 0.dp else 8.dp, RoundedCornerShape(18.dp), clip = false),
                            shape = RoundedCornerShape(18.dp),
                            enabled = true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuthAzulPrimario,
                                contentColor = Color.White
                            )
                        ) {
                            if (cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Iniciando...",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            } else {
                                Text(
                                    "Iniciar Sesión",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Enlace registro
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodySmall, color = AuthTextoSecundario)
                            TextButton(onClick = { navigator.push(RegistroScreen()) }) {
                                Text(
                                    "Regístrate",
                                    color = AuthAzulPrimario,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
