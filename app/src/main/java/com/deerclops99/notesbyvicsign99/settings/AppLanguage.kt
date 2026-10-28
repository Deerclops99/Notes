package com.deerclops99.notesbyvicsign99.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

enum class AppLang(val tag: String) {
    EN("en"),
    ES("es"),
    PT("pt")
}

object AppLanguage {
    private const val PREFS = "settings"
    private const val KEY_LANG = "app_language"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun fromTag(tag: String?): AppLang? =
        AppLang.entries.firstOrNull { tag != null && tag.startsWith(it.tag) }

    // Idioma que se está viendo ahora (para marcar la opción en Ajustes)
    fun current(): AppLang {
        val chosen = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
        fromTag(chosen)?.let { return it }

        val phone = ConfigurationCompat.getLocales(
            Resources.getSystem().configuration
        )
        for (i in 0 until phone.size()) {
            fromTag(phone.get(i)?.language)?.let { return it }
        }
        return AppLang.EN
    }

    // Guarda la elección al instante (la lee el widget) y la aplica
    fun set(context: Context, lang: AppLang) {
        prefs(context).edit().putString(KEY_LANG, lang.tag).apply()
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(lang.tag)
        )
    }

    // Copia el idioma que tiene Android a nuestras preferencias, por si
    // se cambió desde los Ajustes del celular
    fun syncFromSystem(context: Context) {
        val tags = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
        prefs(context).edit().putString(KEY_LANG, tags).apply()
    }

    // Idioma guardado ("" si todavía no eligió ninguno)
    fun savedTag(context: Context): String =
        prefs(context).getString(KEY_LANG, "").orEmpty()

    // Emite un valor nuevo cada vez que cambia el idioma guardado
    fun savedTagFlow(context: Context): Flow<String> = callbackFlow {
        val p = prefs(context)
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_LANG) trySend(savedTag(context))
            }
        p.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { p.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // Para el widget: un contexto con ese idioma
    fun localized(context: Context, tag: String): Context {
        val first = tag.substringBefore(",")
        if (first.isEmpty()) return context

        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(first))
        return context.createConfigurationContext(config)
    }
}
