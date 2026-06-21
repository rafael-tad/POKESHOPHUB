package com.pokeshophub

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import com.pokeshophub.data.network.AuthRepository
import com.pokeshophub.ui.screens.admin.DashboardAdminScreen
import com.pokeshophub.ui.screens.auth.LoginScreen
import com.pokeshophub.ui.screens.client.HomeClienteScreen
import com.pokeshophub.ui.theme.PokeshopHubTheme

@Composable
@Preview
fun App() {
    PokeshopHubTheme {
        val authRepo = remember { AuthRepository() }
        val sesionInicial = remember { authRepo.sesionGuardada() }

        // Si hay sesiÃ³n guardada, va directo al home correspondiente; si no, al login
        val pantallaInicial = when {
            sesionInicial?.esAdmin == true -> DashboardAdminScreen(sesionInicial)
            sesionInicial?.esCliente == true -> HomeClienteScreen(sesionInicial)
            else -> LoginScreen()
        }

        Navigator(pantallaInicial)
    }
}
