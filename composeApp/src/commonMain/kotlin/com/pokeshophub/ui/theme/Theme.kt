package com.pokeshophub.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// —— Estado del Tema Global ——
val isAppDarkTheme = mutableStateOf(false)

// —— Colores Auth (Se mantienen idénticos para no alterar la pantalla de inicio de sesión y registro) ——
val AuthAzulPrimario = Color(0xFF3B4CCA) // Pokémon Royal Blue
val AuthAzulVariante = Color(0xFFFFDE00) // Pokémon Gold/Yellow
val AuthAzulOscuro = Color(0xFF1C2D5A)
val AuthSuperficie = Color(0xFFFFFFFF)
val AuthTextoPrimario = Color(0xFF0F172A)
val AuthTextoSecundario = Color(0xFF475569)
val AuthFondoApp = Color(0xFFF8FAFC)
val AuthRojoError = Color(0xFFEF4444)

// —— Colores Minimalistas Premium (Estilo Pokémon / Inicio de Sesión) ——
val AzulPrimario: Color get() = if (isAppDarkTheme.value) Color(0xFF5C6BC0) else Color(0xFF3B4CCA) // Pokémon Royal Blue
val AzulVariante: Color get() = if (isAppDarkTheme.value) Color(0xFFFFDE00) else Color(0xFFFFDE00) // Pokémon Gold/Yellow
val AzulOscuro: Color get() = if (isAppDarkTheme.value) Color(0xFF0F172A) else Color(0xFF1C2D5A) // Pokémon Dark Navy

val MoradoSecundario: Color get() = if (isAppDarkTheme.value) Color(0xFF94A3B8) else Color(0xFF475569) // Gris Slate elegante

val NaranjaAccento: Color get() = if (isAppDarkTheme.value) Color(0xFFF59E0B) else Color(0xFFB45309)
val VerdeConfirmacion: Color get() = if (isAppDarkTheme.value) Color(0xFF4CAF50) else Color(0xFF2E7D32)
val RojoError: Color get() = if (isAppDarkTheme.value) Color(0xFFEF5350) else Color(0xFFD32F2F)
val AmarilloAlerta: Color get() = if (isAppDarkTheme.value) Color(0xFFFFCA28) else Color(0xFFF57F17)

// Fondos y superficies basados en el tema de inicio de sesión
val FondoApp: Color get() = if (isAppDarkTheme.value) Color(0xFF0B0F19) else Color(0xFFF8FAFC) // Fondo azul noche en dark, fondo gris claro en light
val Superficie: Color get() = if (isAppDarkTheme.value) Color(0xFF161E2E) else Color(0xFFFFFFFF)
val SuperficieVariante: Color get() = if (isAppDarkTheme.value) Color(0xFF1F2937) else Color(0xFFEEF2F6)
val SuperficieTarjeta: Color get() = if (isAppDarkTheme.value) Color(0xFF161E2E) else Color(0xFFFFFFFF)

// Bordes y sombras minimalistas premium (Gris piedra fino en light, azul noche intermedio en dark)
val NeoBorde: Color get() = if (isAppDarkTheme.value) Color(0xFF1F2937) else Color(0xFFE2E8F0)
val NeoSombra: Color get() = if (isAppDarkTheme.value) Color.Transparent else Color(0x08000000)

// Texto elegante y refinado
val TextoPrimario: Color get() = if (isAppDarkTheme.value) Color(0xFFF8FAFC) else Color(0xFF0F172A)
val TextoSecundario: Color get() = if (isAppDarkTheme.value) Color(0xFF94A3B8) else Color(0xFF475569)
val TextoPlaceholder: Color get() = if (isAppDarkTheme.value) Color(0xFF6B7280) else Color(0xFF94A3B8)
val TextoSobrePrimario: Color get() = Color.White // Siempre blanco sobre fondo azul royal



// Degradado TCG
val GradienteColores: List<Color> get() = if (isAppDarkTheme.value) {
    listOf(Color(0xFFFAFAF9), Color(0xFFA1A1AA), Color(0xFFD4B280))
} else {
    listOf(Color(0xFF18181B), Color(0xFF71717A), Color(0xFFC5A880))
}

private val EsquemaColoresLight = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1C2D5A),
    secondary = Color(0xFFEC4899),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEE2E2),
    onSecondaryContainer = Color(0xFFEC4899),
    tertiary = Color(0xFF8B5CF6),
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFEF4444),
    onError = Color.White,
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF1F5F9),
)

private val EsquemaColoresDark = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFF472B6),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF881337),
    onSecondaryContainer = Color(0xFFFCE7F3),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFFF8FAFC),
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFF5F3FF),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFF87171),
    onError = Color(0xFF0F172A),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
)

@Composable
fun PokeshopHubTheme(content: @Composable () -> Unit) {
    val darkTheme = isAppDarkTheme.value
    val colors = if (darkTheme) EsquemaColoresDark else EsquemaColoresLight
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun NeoBrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Superficie,
    onClick: (() -> Unit)? = null,
    shadowOffset: Dp = 6.dp, // ignored in premium minimal
    shape: CornerBasedShape = RoundedCornerShape(16.dp), // upgraded corner radius
    borderWidth: Dp = 1.dp, // thinner border
    content: @Composable BoxScope.() -> Unit
) {
    val shadowModifier = if (isAppDarkTheme.value) {
        Modifier
    } else {
        Modifier.shadow(elevation = 2.dp, shape = shape, clip = false)
    }

    val baseModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .then(baseModifier)
            .fillMaxWidth()
            .background(color = backgroundColor, shape = shape)
            .border(borderWidth, NeoBorde, shape)
    ) {
        content()
    }
}

