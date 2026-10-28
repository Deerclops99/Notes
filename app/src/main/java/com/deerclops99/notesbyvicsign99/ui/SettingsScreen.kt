package com.deerclops99.notesbyvicsign99.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.deerclops99.notesbyvicsign99.R
import com.deerclops99.notesbyvicsign99.settings.AppLang
import com.deerclops99.notesbyvicsign99.settings.AppLanguage
import com.deerclops99.notesbyvicsign99.settings.AppSettings
import com.deerclops99.notesbyvicsign99.settings.ThemeMode

@Composable
fun SettingsScreen(onLanguageChanged: () -> Unit) {
    val context = LocalContext.current
    var theme by remember { mutableStateOf(AppSettings.getTheme(context)) }
    val language = AppLanguage.current()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ---------- Idioma (primero) ----------
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium
        )
        AppLang.entries.forEach { lang ->
            OptionRow(
                label = stringResource(lang.labelRes()),
                selected = language == lang,
                onClick = {
                    AppLanguage.set(context, lang)
                    onLanguageChanged()
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Tema (después) ----------
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium
        )
        ThemeMode.entries.forEach { mode ->
            OptionRow(
                label = stringResource(mode.labelRes()),
                selected = theme == mode,
                onClick = {
                    theme = mode
                    AppSettings.setTheme(context, mode)
                }
            )
        }
    }
}
@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}


@StringRes
private fun AppLang.labelRes(): Int = when (this) {
    AppLang.EN -> R.string.lang_en
    AppLang.ES -> R.string.lang_es
    AppLang.PT -> R.string.lang_pt
}

@StringRes
private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

