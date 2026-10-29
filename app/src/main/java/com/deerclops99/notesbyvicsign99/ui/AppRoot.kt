package com.deerclops99.notesbyvicsign99.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deerclops99.notesbyvicsign99.R
import com.deerclops99.notesbyvicsign99.ui.theme.DialogCancelButton
import kotlinx.coroutines.launch

// Marca o desmarca un id dentro de la selección
private fun Set<Long>.toggle(id: Long): Set<Long> =
    if (id in this) this - id else this + id

@Composable
fun AppRoot(
    vm: MainViewModel = viewModel(),
    requestedTab: Int? = null,
    onTabRequestHandled: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val page = pagerState.currentPage

    // Ventanas de escribir/editar (se conservan al girar)
    var noteDialogOpen by rememberSaveable { mutableStateOf(false) }
    var noteEditingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var todoDialogOpen by rememberSaveable { mutableStateOf(false) }
    var todoEditingId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Selección múltiple (ids de lo marcado) y confirmación de notas
    var selectedNotes by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selectedTodos by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var confirmDeleteNotes by remember { mutableStateOf(false) }

    // Si el widget pidió una pestaña, la mostramos
    LaunchedEffect(requestedTab) {
        if (requestedTab != null) {
            pagerState.scrollToPage(requestedTab)
            onTabRequestHandled()
        }
    }

    // Al cambiar de pestaña se cancela la selección
    LaunchedEffect(page) {
        selectedNotes = emptySet()
        selectedTodos = emptySet()
    }

    val notes by vm.notes.collectAsState()
    val todos by vm.todos.collectAsState()
    val tabTitles = listOf(
        stringResource(R.string.tab_notes),
        stringResource(R.string.tab_todos),
        stringResource(R.string.tab_settings)
    )

    val deletedOne = stringResource(R.string.todo_deleted)
    val undoLabel = stringResource(R.string.todo_undo)

    // ¿Hay algo seleccionado en la pestaña actual?
    val selecting = (page == 0 && selectedNotes.isNotEmpty()) ||
            (page == 1 && selectedTodos.isNotEmpty())

    // El gesto de atrás cancela la selección
    BackHandler(enabled = selecting) {
        selectedNotes = emptySet()
        selectedTodos = emptySet()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                // Acá puede ir el banner de anuncios más adelante
                PrimaryTabRow(
                    selectedTabIndex = page,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = page == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        // (+) normalmente; cesto de basura cuando hay algo seleccionado
        floatingActionButton = {
            AnimatedVisibility(
                visible = page != 2,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        when {
                            page == 0 && selectedNotes.isNotEmpty() -> {
                                confirmDeleteNotes = true
                            }
                            page == 1 && selectedTodos.isNotEmpty() -> {
                                val toDelete = todos.filter {
                                    it.id in selectedTodos
                                }
                                selectedTodos = emptySet()
                                if (toDelete.isNotEmpty()) {
                                    vm.deleteTodos(toDelete)
                                    val message = if (toDelete.size == 1) {
                                        deletedOne
                                    } else {
                                        context.getString(
                                            R.string.todos_deleted,
                                            toDelete.size
                                        )
                                    }
                                    scope.launch {
                                        snackbarHostState
                                            .currentSnackbarData?.dismiss()
                                        val result =
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                actionLabel = undoLabel,
                                                duration = SnackbarDuration.Short
                                            )
                                        if (result ==
                                            SnackbarResult.ActionPerformed
                                        ) {
                                            vm.restoreTodos(toDelete)
                                        }
                                    }
                                }
                            }
                            page == 0 -> {
                                noteEditingId = null
                                noteDialogOpen = true
                            }
                            else -> {
                                todoEditingId = null
                                todoDialogOpen = true
                            }
                        }
                    },
                    containerColor = if (selecting) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (selecting) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                ) {
                    Crossfade(targetState = selecting, label = "fab_icon") {
                            isDelete ->
                        Icon(
                            imageVector = if (isDelete) {
                                Icons.Default.Delete
                            } else {
                                Icons.Default.Add
                            },
                            contentDescription = stringResource(
                                when {
                                    isDelete && page == 0 -> R.string.notes_delete
                                    isDelete -> R.string.todo_delete
                                    page == 0 -> R.string.notes_new
                                    else -> R.string.todo_new_label
                                }
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { p ->
            when (p) {
                0 -> NotesScreen(
                    notes = notes,
                    selectedIds = selectedNotes,
                    onClick = { note ->
                        if (selectedNotes.isNotEmpty()) {
                            selectedNotes = selectedNotes.toggle(note.id)
                        } else {
                            noteEditingId = note.id
                            noteDialogOpen = true
                        }
                    },
                    onLongClick = { note ->
                        selectedNotes = selectedNotes + note.id
                    }
                )
                1 -> TodoScreen(
                    todos = todos,
                    selectedIds = selectedTodos,
                    onClick = { item ->
                        if (selectedTodos.isNotEmpty()) {
                            selectedTodos = selectedTodos.toggle(item.id)
                        } else {
                            todoEditingId = item.id
                            todoDialogOpen = true
                        }
                    },
                    onLongClick = { item ->
                        selectedTodos = selectedTodos + item.id
                    },
                    onToggle = vm::toggleTodo,
                    onClearDone = vm::clearDoneDaily
                )
                else -> SettingsScreen(
                    onLanguageChanged = vm::refreshWidget
                )
            }
        }
    }

    // ---------- Confirmar el borrado de notas seleccionadas ----------
    val notesToDelete = notes.filter { it.id in selectedNotes }
    if (confirmDeleteNotes && notesToDelete.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { confirmDeleteNotes = false },
            title = {
                Text(
                    if (notesToDelete.size == 1) {
                        stringResource(R.string.notes_delete_title)
                    } else {
                        stringResource(
                            R.string.notes_delete_many_title,
                            notesToDelete.size
                        )
                    }
                )
            },
            text = {
                Text(
                    if (notesToDelete.size == 1) {
                        stringResource(
                            R.string.notes_delete_message,
                            notesToDelete[0].title
                        )
                    } else {
                        stringResource(R.string.notes_delete_many_message)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmDeleteNotes = false
                        selectedNotes = emptySet()
                        vm.deleteNotes(notesToDelete)
                    }
                ) {
                    Text(stringResource(R.string.notes_delete_confirm))
                }
            },
            dismissButton = {
                DialogCancelButton(
                    stringResource(R.string.notes_delete_cancel)
                ) { confirmDeleteNotes = false }
            }
        )
    }

    // ---------- Ventana de notas (nueva o editar) ----------
    val editingNote = notes.firstOrNull { it.id == noteEditingId }
    if (noteDialogOpen && (noteEditingId == null || editingNote != null)) {
        NoteDialog(
            initial = editingNote,
            onSave = { title, content ->
                if (editingNote != null) {
                    vm.updateNote(
                        editingNote.copy(title = title, content = content)
                    )
                } else {
                    vm.addNote(title, content)
                }
                noteDialogOpen = false
                noteEditingId = null
            },
            onDismiss = {
                noteDialogOpen = false
                noteEditingId = null
            }
        )
    }

    // ---------- Ventana de tareas (nueva o editar) ----------
    val editingTodo = todos.firstOrNull { it.id == todoEditingId }
    if (todoDialogOpen && (todoEditingId == null || editingTodo != null)) {
        TodoDialog(
            initial = editingTodo,
            onSave = { text, permanent ->
                if (editingTodo != null) {
                    vm.updateTodo(
                        editingTodo.copy(text = text, permanent = permanent)
                    )
                } else {
                    vm.addTodo(text, permanent)
                }
                todoDialogOpen = false
                todoEditingId = null
            },
            onDismiss = {
                todoDialogOpen = false
                todoEditingId = null
            }
        )
    }
}
