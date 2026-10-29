package com.deerclops99.notesbyvicsign99.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.deerclops99.notesbyvicsign99.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Interruptores para probar mientras todavía no hay anuncios reales
object ThemeDev {
    // true = "Ver anuncio" desbloquea al instante (simula el anuncio)
    const val SIMULATE_AD = true

    // true = cada vez que se abre la app, los temas vuelven a bloquearse
    const val RELOCK_ON_START = false

    // false = colores exactos de tu lista: las letras sobre el color 1 son
    //         siempre el color 3.
    // true  = si el contraste es muy bajo (Limón, Mango, Durazno, Naranja),
    //         esas letras pasan a negro para que se lean.
    const val FIX_LOW_CONTRAST = false
}

class AppTheme(
    val id: String,
    @StringRes val nameRes: Int,
    private val color1: Color,   // principal
    private val color2: Color,   // secundario
    private val color3: Color,   // fondo
    val dark: Boolean = false,
    val free: Boolean = false,
    // Mango y Sandía: el widget usa el color 2 más claro como fondo
    private val widgetUsesColor2: Boolean = false
) {
    val colorScheme: ColorScheme by lazy { buildScheme() }

    // Fondo del cuerpo del widget
    val widgetBody: Color
        get() = when {
            dark -> colorScheme.background
            widgetUsesColor2 -> lerp(color2, Color.White, 0.35f)
            else -> color3
        }

    // Texto de las etiquetas "Permanentes" / "Del día"
    val pillText: Color
        get() = if (dark) colorScheme.onPrimary else Color.Black

    // Tres muestras de color para la lista de Ajustes
    val swatches: List<Color>
        get() = listOf(
            if (dark) colorScheme.background else color3,
            color1,
            color2
        )

    private fun buildScheme(): ColorScheme {
        val letters = lettersOn(color1, color3)

        if (!dark) {
            return lightColorScheme(
                primary = color1,
                onPrimary = letters,
                primaryContainer = color1,
                onPrimaryContainer = letters,
                inversePrimary = color2,
                secondary = color2,
                onSecondary = Color.Black,
                secondaryContainer = color2,
                onSecondaryContainer = Color.Black,
                background = color3,
                onBackground = Color.Black,
                surface = color3,
                onSurface = Color.Black,
                surfaceVariant = lerp(color3, color2, 0.40f),
                onSurfaceVariant = Color(0xFF3A3A3A),
                surfaceTint = color3,
                outline = lerp(color1, Color.Black, 0.25f),
                outlineVariant = lerp(color2, color1, 0.40f),
                surfaceContainerLowest = lerp(color3, Color.White, 0.50f),
                surfaceContainerLow = color3,
                surfaceContainer = lerp(color3, color2, 0.20f),
                surfaceContainerHigh = lerp(color3, color2, 0.30f),
                surfaceContainerHighest = lerp(color3, color2, 0.40f)
            )
        }

        // Tema oscuro (Uva): todo a la inversa
        val bg = lerp(color1, Color.Black, 0.85f)
        return darkColorScheme(
            primary = color1,
            onPrimary = letters,
            primaryContainer = color1,
            onPrimaryContainer = letters,
            inversePrimary = color1,
            secondary = color2,
            onSecondary = Color.Black,
            secondaryContainer = lerp(color1, Color.Black, 0.55f),
            onSecondaryContainer = color3,
            background = bg,
            onBackground = color3,
            surface = bg,
            onSurface = color3,
            surfaceVariant = lerp(color1, Color.Black, 0.70f),
            onSurfaceVariant = lerp(color3, Color.Black, 0.25f),
            surfaceTint = bg,
            outline = lerp(color1, color3, 0.35f),
            outlineVariant = lerp(color1, Color.Black, 0.55f),
            surfaceContainerLowest = lerp(color1, Color.Black, 0.92f),
            surfaceContainerLow = bg,
            surfaceContainer = lerp(color1, Color.Black, 0.78f),
            surfaceContainerHigh = lerp(color1, Color.Black, 0.72f),
            surfaceContainerHighest = lerp(color1, Color.Black, 0.66f)
        )
    }
}

object AppThemes {

    // ---- TUS TEMAS: el orden de acá es el orden en Ajustes ----
    // Los 3 primeros son gratis; el primero es el tema por defecto
    val all: List<AppTheme> = listOf(
        AppTheme(
            "orange", R.string.theme_orange,
            Color(0xFFF28C28), Color(0xFFFFD08A), Color(0xFFFFF4E5),
            free = true
        ),
        AppTheme(
            "grape", R.string.theme_grape,
            Color(0xFF8250A5), Color(0xFFC6A5E2), Color(0xFFF6EEFC),
            dark = true, free = true
        ),
        AppTheme(
            "coconut", R.string.theme_coconut,
            Color(0xFF96765A), Color(0xFFD8C7AD), Color(0xFFFAF7F0),
            free = true
        ),
        AppTheme(
            "strawberry", R.string.theme_strawberry,
            Color(0xFFE94358), Color(0xFFFFB6B9), Color(0xFFFFF0F0)
        ),
        AppTheme(
            "watermelon", R.string.theme_watermelon,
            Color(0xFF26966A), Color(0xFFF56F79), Color(0xFFE6F5EC),
            widgetUsesColor2 = true
        ),
        AppTheme(
            "lemon", R.string.theme_lemon,
            Color(0xFFD4B800), Color(0xFFF5E66B), Color(0xFFFFFCE5)
        ),
        AppTheme(
            "blueberry", R.string.theme_blueberry,
            Color(0xFF4D5BC7), Color(0xFFAEB8F2), Color(0xFFF0F1FF)
        ),
        AppTheme(
            "mango", R.string.theme_mango,
            Color(0xFFFFB52E), Color(0xFFFF7657), Color(0xFFFFF3D7),
            widgetUsesColor2 = true
        ),
        AppTheme(
            "peach", R.string.theme_peach,
            Color(0xFFE994B8), Color(0xFFF8C9D9), Color(0xFFFFF1F6)
        ),
        AppTheme(
            "kiwi", R.string.theme_kiwi,
            Color(0xFF71983D), Color(0xFFB9D77E), Color(0xFFF1F7E5)
        )
    )

    private const val PREFS = "settings"
    private const val KEY_THEME = "theme_id"
    private const val KEY_UNLOCKED = "unlocked_themes"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun byId(id: String?): AppTheme =
        all.firstOrNull { it.id == id } ?: all.first()

    // Ids de los temas que se pueden usar (gratis + desbloqueados)
    fun unlockedIds(context: Context): Set<String> {
        val saved = prefs(context)
            .getStringSet(KEY_UNLOCKED, emptySet()).orEmpty()
        return saved + all.filter { it.free }.map { it.id }
    }

    // Tema elegido (si no hay uno válido, el primero de la lista)
    fun selected(context: Context): AppTheme {
        val theme = byId(prefs(context).getString(KEY_THEME, null))
        return if (theme.id in unlockedIds(context)) theme else all.first()
    }

    fun select(context: Context, theme: AppTheme) {
        prefs(context).edit().putString(KEY_THEME, theme.id).apply()
        applyNightMode(theme)
    }

    // Se llama cuando el usuario completa el anuncio
    fun unlock(context: Context, theme: AppTheme) {
        val saved = prefs(context)
            .getStringSet(KEY_UNLOCKED, emptySet()).orEmpty()
        prefs(context).edit()
            .putStringSet(KEY_UNLOCKED, saved + theme.id)
            .apply()
    }

    // Al abrir la app: recuerda el tema elegido
    fun applySaved(context: Context) {
        if (ThemeDev.RELOCK_ON_START) {
            prefs(context).edit().remove(KEY_UNLOCKED).apply()
        }
        applyNightMode(selected(context))
    }

    private fun applyNightMode(theme: AppTheme) {
        AppCompatDelegate.setDefaultNightMode(
            if (theme.dark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    // Emite el tema cada vez que cambia (lo usan la app y el widget)
    fun selectedFlow(context: Context): Flow<AppTheme> = callbackFlow {
        val p = prefs(context)
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_THEME) trySend(selected(context))
            }
        p.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { p.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}

// ---------------------------------------------------------------------

private fun contrast(a: Color, b: Color): Float {
    val l1 = a.luminance()
    val l2 = b.luminance()
    return (maxOf(l1, l2) + 0.05f) / (minOf(l1, l2) + 0.05f)
}

// Letras sobre el color principal: el color 3 (o negro si se pidió
// corregir el contraste bajo)
private fun lettersOn(principal: Color, color3: Color): Color =
    if (ThemeDev.FIX_LOW_CONTRAST && contrast(principal, color3) < 3f) {
        Color.Black
    } else {
        color3
    }
