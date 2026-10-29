package com.deerclops99.notesbyvicsign99.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.deerclops99.notesbyvicsign99.settings.AppTheme

@Composable
fun NotesTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = appTheme.colorScheme,
        typography = Typography,
        content = content
    )
}

// Texto de las etiquetas "Permanentes" / "Del día": negro en los temas
// claros, y el color de las letras de botón en el oscuro
@Composable
fun pillTextColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (scheme.background.luminance() < 0.5f) {
        scheme.onPrimary
    } else {
        Color.Black
    }
}

// Colores de los campos de texto: el foco usa colores que se leen en
// todos los temas (el color principal puede ser muy claro)
@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.onSurface
)

// Botón "Cancelar" de las ventanas, en un color que se lee siempre
@Composable
fun DialogCancelButton(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text)
    }
}
