package com.deerclops99.notesbyvicsign99.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.deerclops99.notesbyvicsign99.R
import com.deerclops99.notesbyvicsign99.settings.AppLang
import com.deerclops99.notesbyvicsign99.settings.AppLanguage
import com.deerclops99.notesbyvicsign99.settings.AppTheme
import com.deerclops99.notesbyvicsign99.settings.AppThemes
import com.deerclops99.notesbyvicsign99.settings.ThemeDev
import com.deerclops99.notesbyvicsign99.ui.theme.DialogCancelButton

@Composable
fun SettingsScreen(onLanguageChanged: () -> Unit) {
    val context = LocalContext.current
    var selectedId by remember {
        mutableStateOf(AppThemes.selected(context).id)
    }
    var unlocked by remember {
        mutableStateOf(AppThemes.unlockedIds(context))
    }
    var lockedTheme by remember { mutableStateOf<AppTheme?>(null) }
    val language = AppLanguage.current()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        // ---------- Temas (después) ----------
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium
        )
        AppThemes.all.forEach { theme ->
            ThemeRow(
                theme = theme,
                selected = theme.id == selectedId,
                locked = theme.id !in unlocked,
                onClick = {
                    if (theme.id in unlocked) {
                        AppThemes.select(context, theme)
                        selectedId = theme.id
                    } else {
                        lockedTheme = theme
                    }
                }
            )
        }

        // Espacio al final para que nada quede tapado
        Spacer(Modifier.height(24.dp))
    }

    // ---------- Aviso de tema bloqueado ----------
    lockedTheme?.let { theme ->
        val themeName = stringResource(theme.nameRes)
        AlertDialog(
            onDismissRequest = { lockedTheme = null },
            title = { Text(stringResource(R.string.theme_locked_title)) },
            text = {
                Text(
                    if (ThemeDev.SIMULATE_AD) {
                        stringResource(R.string.theme_locked_message, themeName)
                    } else {
                        stringResource(R.string.theme_ad_soon)
                    }
                )
            },
            confirmButton = {
                if (ThemeDev.SIMULATE_AD) {
                    Button(
                        onClick = {
                            // AQUÍ irá el anuncio real: mostrarlo y, cuando
                            // el usuario lo complete, ejecutar lo de abajo
                            AppThemes.unlock(context, theme)
                            unlocked = AppThemes.unlockedIds(context)
                            AppThemes.select(context, theme)
                            selectedId = theme.id
                            lockedTheme = null
                        }
                    ) {
                        Text(stringResource(R.string.theme_watch_ad))
                    }
                }
            },
            dismissButton = {
                DialogCancelButton(
                    stringResource(R.string.edit_cancel)
                ) { lockedTheme = null }
            }
        )
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
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun ThemeRow(
    theme: AppTheme,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.6f else 1f)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(Modifier.width(8.dp))
        // Fondo, color 1 y color 2 del tema
        theme.swatches.forEach { color ->
            Dot(color)
            Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(theme.nameRes),
            modifier = Modifier.weight(1f)
        )
        if (locked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.theme_locked_title),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(color, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}

@StringRes
private fun AppLang.labelRes(): Int = when (this) {
    AppLang.EN -> R.string.lang_en
    AppLang.ES -> R.string.lang_es
    AppLang.PT -> R.string.lang_pt
}
