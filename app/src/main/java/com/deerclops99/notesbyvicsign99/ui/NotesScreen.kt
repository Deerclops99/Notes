package com.deerclops99.notesbyvicsign99.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.deerclops99.notesbyvicsign99.R
import com.deerclops99.notesbyvicsign99.data.Note
import kotlinx.coroutines.delay

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
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.cardColors()
        },
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
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
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(initial?.content ?: "") }
    val titleFocus = remember { FocusRequester() }

    // Abre el teclado en el título
    LaunchedEffect(Unit) {
        delay(100)
        runCatching { titleFocus.requestFocus() }
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), content.trim()) },
                enabled = title.isNotBlank()
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.edit_cancel))
            }
        }
    )
}