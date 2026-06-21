package com.pokeshophub.ui.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import com.pokeshophub.data.network.AuthRepository
import com.pokeshophub.ui.screens.client.HomeClienteScreen
import com.pokeshophub.ui.theme.*

class RegistroScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val authRepo = remember { AuthRepository() }

        var nombre by remember { mutableStateOf("") }
        var apellidos by remember { mutableStateOf("") }
        var dni by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var cargando by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var showSuccessDialog by remember { mutableStateOf(false) }
        var successMessage by remember { mutableStateOf("") }

        fun doRegistro() {
            if (nombre.isBlank() || apellidos.isBlank() || dni.isBlank() ||
                email.isBlank() || password.isBlank()) {
                error = "Por favor, rellena todos los campos obligatorios"
                return
            }
            if (password.length < 8) {
                error = "La contraseña debe tener al menos 8 caracteres"
                return
            }
            scope.launch {
                cargando = true
                error = null
                val result = authRepo.registro(nombre, apellidos, dni, email, telefono, direccion, password)
                cargando = false
                result.fold(
                    onSuccess = { msg ->
                        successMessage = msg
                        showSuccessDialog = true
                    },
                    onFailure = { error = it.message }
                )
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    navigator.pop()
                },
                title = { Text("Registro Enviado", fontWeight = FontWeight.Bold, color = AuthTextoPrimario) },
                text = { Text(successMessage, color = AuthTextoSecundario) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navigator.pop()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuthAzulPrimario, contentColor = Color.White)
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Crear cuenta") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AuthAzulPrimario,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = AuthFondoApp
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¡Bienvenido a POKESHOP HUB!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AuthAzulPrimario,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Rellena tus datos para crear tu cuenta de cliente",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AuthSuperficie),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Campos obligatorios
                        listOf(
                            Triple("Nombre *", nombre, { v: String -> nombre = v }),
                            Triple("Apellidos *", apellidos, { v: String -> apellidos = v }),
                            Triple("DNI/NIE *", dni, { v: String -> dni = v }),
                            Triple("Correo electrónico *", email, { v: String -> email = v }),
                        ).forEach { (label, value, onChange) ->
                            OutlinedTextField(
                                value = value,
                                onValueChange = { onChange(it); error = null },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (label.contains("email", true))
                                        KeyboardType.Email else KeyboardType.Text
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AuthAzulPrimario,
                                    focusedLabelColor = AuthAzulPrimario
                                )
                            )
                        }

                        // Opcionales
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthAzulPrimario, focusedLabelColor = AuthAzulPrimario
                            )
                        )
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthAzulPrimario, focusedLabelColor = AuthAzulPrimario
                            )
                        )

                        // Contraseña
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; error = null },
                            label = { Text("Contraseña * (mín. 8 caracteres)") },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        null, tint = TextoSecundario
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthAzulPrimario, focusedLabelColor = AuthAzulPrimario
                            )
                        )

                        error?.let {
                            Text(it, color = AuthRojoError, style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = { doRegistro() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !cargando,
                            colors = ButtonDefaults.buttonColors(containerColor = AuthAzulPrimario, contentColor = Color.White)
                        ) {
                            if (cargando) CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            else Text("Crear cuenta", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
