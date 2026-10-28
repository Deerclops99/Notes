package com.deerclops99.notesbyvicsign99.settings

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode(val nightMode: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES)
}

object AppSettings {
    private const val PREFS = "settings"
    private const val KEY_THEME = "theme_mode"

    private fun prefs(context: Context) =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Tema que eligió el usuario, o null si todavía no eligió ninguno
    private fun savedTheme(context: Context): ThemeMode? {
        val saved = prefs(context).getString(KEY_THEME, null)
        return ThemeMode.entries.firstOrNull { it.name == saved }
    }

    // Tema que se está viendo: el elegido o, si no eligió, el del celular
    fun getTheme(context: Context): ThemeMode {
        savedTheme(context)?.let { return it }
        val night = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return if (night == Configuration.UI_MODE_NIGHT_YES) {
            ThemeMode.DARK
        } else {
            ThemeMode.LIGHT
        }
    }

    // Guarda la elección y la aplica en el momento
    fun setTheme(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME, mode.name).apply()
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
    }

    // Se llama al abrir la app para recordar la elección anterior
    fun applySavedTheme(context: Context) {
        val mode = savedTheme(context)
        AppCompatDelegate.setDefaultNightMode(
            mode?.nightMode ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }
}