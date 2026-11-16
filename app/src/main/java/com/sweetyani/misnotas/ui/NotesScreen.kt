package com.sweetyani.misnotas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sweetyani.misnotas.R
import com.sweetyani.misnotas.data.Note
import com.sweetyani.misnotas.ui.theme.DialogCancelButton
import com.sweetyani.misnotas.ui.theme.appTextFieldColors
import kotlinx.coroutines.delay
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun NotesScreen(
    notes: List<Note>,
    selectedIds: Set<Long>,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit
) {
    val listState = rememberLazyListState()

    // Si se agregó una nota, subimos la lista para que se vea la nueva
    var lastSize by remember { mutableIntStateOf(notes.size) }
    LaunchedEffect(notes.size) {
        if (notes.size > lastSize) listState.animateScrollToItem(0)
        lastSize = notes.size
    }

    if (notes.isEmpty()) {
        Text(
            text = stringResource(R.string.notes_empty),
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // El espacio de abajo evita que el botón tape la última nota
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    selected = note.id in selectedIds,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: Note,
    selected: Boolean,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme

    // Fondo de las notas = color 2 del tema; seleccionada = un poco más
    // marcada y con borde
    val container = if (selected) {
        lerp(scheme.secondaryContainer, scheme.primary, 0.25f)
    } else {
        scheme.secondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(note) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(note)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = scheme.onSecondaryContainer
        ),
        border = if (selected) BorderStroke(2.dp, scheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(note.title, style = MaterialTheme.typography.titleMedium)
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Ventana para escribir una nota nueva o editar una existente
@Composable
fun NoteDialog(
    initial: Note?,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTitle = initial?.title ?: ""
    val initialContent = initial?.content ?: ""

    // Con el cursor al final del texto
    var title by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(initialTitle, TextRange(initialTitle.length))
        )
    }
    var content by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(initialContent, TextRange(initialContent.length))
        )
    }

    val titleFocus = remember { FocusRequester() }
    val contentFocus = remember { FocusRequester() }

    // Nota nueva: foco en el título. Editando: foco en el contenido.
    LaunchedEffect(Unit) {
        delay(100)
        runCatching {
            if (initial == null) titleFocus.requestFocus()
            else contentFocus.requestFocus()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (initial == null) R.string.notes_new else R.string.notes_edit
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.notes_title_label)) },
                    singleLine = true,
                    colors = appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(stringResource(R.string.notes_content_label))
                    },
                    minLines = 3,
                    colors = appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(contentFocus)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.text.trim(), content.text.trim()) },
                enabled = title.text.isNotBlank()
            ) {
                Text(
                    stringResource(
                        if (initial == null) {
                            R.string.notes_save
                        } else {
                            R.string.notes_save_changes
                        }
                    )
                )
            }
        },
        dismissButton = {
            DialogCancelButton(
                stringResource(R.string.edit_cancel),
                onDismiss
            )
        }
    )
}
