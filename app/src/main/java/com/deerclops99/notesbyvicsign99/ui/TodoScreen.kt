package com.deerclops99.notesbyvicsign99.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.deerclops99.notesbyvicsign99.R
import com.deerclops99.notesbyvicsign99.data.TodoItem
import com.deerclops99.notesbyvicsign99.ui.theme.DialogCancelButton
import com.deerclops99.notesbyvicsign99.ui.theme.appTextFieldColors
import com.deerclops99.notesbyvicsign99.ui.theme.pillTextColor
import kotlinx.coroutines.delay

@Composable
fun TodoScreen(
    todos: List<TodoItem>,
    selectedIds: Set<Long>,
    onClick: (TodoItem) -> Unit,
    onLongClick: (TodoItem) -> Unit,
    onToggle: (TodoItem) -> Unit,
    onClearDone: () -> Unit
) {
    val permanentItems = todos.filter { it.permanent }
    val dailyItems = todos.filter { !it.permanent }
    val hasDoneDaily = dailyItems.any { it.done }
    val selectionMode = selectedIds.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasDoneDaily) {
            Button(
                onClick = onClearDone,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            ) {
                Text(stringResource(R.string.todo_clear_done))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // El espacio de abajo evita que el botón tape la última tarea
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 88.dp
            )
        ) {
            if (permanentItems.isNotEmpty()) {
                item(key = "header_permanent") {
                    SectionLabel(
                        stringResource(R.string.todo_section_permanent)
                    )
                }
                items(permanentItems, key = { it.id }) { item ->
                    TodoRow(
                        item = item,
                        selected = item.id in selectedIds,
                        selectionMode = selectionMode,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onToggle = onToggle
                    )
                }
            }
            if (dailyItems.isNotEmpty()) {
                item(key = "header_daily") {
                    SectionLabel(
                        stringResource(R.string.todo_section_daily)
                    )
                }
                items(dailyItems, key = { it.id }) { item ->
                    TodoRow(
                        item = item,
                        selected = item.id in selectedIds,
                        selectionMode = selectionMode,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onToggle = onToggle
                    )
                }
            }
        }
    }
}

// Etiqueta rellena con el color principal del tema
@Composable
private fun SectionLabel(text: String) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = pillTextColor()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TodoRow(
    item: TodoItem,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: (TodoItem) -> Unit,
    onLongClick: (TodoItem) -> Unit,
    onToggle: (TodoItem) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Zona mínima de toque de 32 dp para que las filas queden compactas
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides 32.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (selected) {
                        Modifier.background(
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.done,
                // Mientras se selecciona, la casilla no cambia el estado
                enabled = !selectionMode,
                onCheckedChange = { onToggle(item) }
            )
            if (item.permanent) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = item.text,
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = { onClick(item) },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                            onLongClick(item)
                        }
                    )
                    .padding(vertical = 8.dp),
                textDecoration =
                    if (item.done) TextDecoration.LineThrough else null
            )
        }
    }
}

// Ventana para escribir una tarea nueva o editar una existente
@Composable
fun TodoDialog(
    initial: TodoItem?,
    onSave: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initial?.text ?: "") }
    var permanent by rememberSaveable {
        mutableStateOf(initial?.permanent ?: false)
    }
    val fieldFocus = remember { FocusRequester() }

    // Abre el teclado en el campo
    LaunchedEffect(Unit) {
        delay(100)
        runCatching { fieldFocus.requestFocus() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (initial == null) {
                        R.string.todo_new_label
                    } else {
                        R.string.todo_edit
                    }
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.todo_new_label)) },
                    singleLine = true,
                    colors = appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(fieldFocus)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = permanent,
                        onCheckedChange = { permanent = it }
                    )
                    Text(stringResource(R.string.todo_permanent))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.trim(), permanent) },
                enabled = text.isNotBlank()
            ) {
                Text(
                    stringResource(
                        if (initial == null) R.string.todo_add else R.string.todo_save
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
