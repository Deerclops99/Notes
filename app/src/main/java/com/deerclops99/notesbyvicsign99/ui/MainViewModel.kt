package com.deerclops99.notesbyvicsign99.ui

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deerclops99.notesbyvicsign99.data.AppDatabase
import com.deerclops99.notesbyvicsign99.data.Note
import com.deerclops99.notesbyvicsign99.data.TodoItem
import com.deerclops99.notesbyvicsign99.widget.TodoWidget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.get(application)
    private val noteDao = db.noteDao()
    private val todoDao = db.todoDao()
    private val appContext = application.applicationContext

    val notes: StateFlow<List<Note>> = noteDao.observeAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val todos: StateFlow<List<TodoItem>> = todoDao.observeAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private suspend fun updateWidget() {
        TodoWidget().updateAll(appContext)
    }

    // Lo llama la pantalla de Ajustes cuando cambia el idioma
    fun refreshWidget() {
        viewModelScope.launch { updateWidget() }
    }

    // ---------- Notas ----------
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            noteDao.insert(Note(title = title, content = content))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteDao.delete(note) }
    }

    // ---------- Tareas ----------
    fun addTodo(text: String, permanent: Boolean) {
        viewModelScope.launch {
            todoDao.insert(TodoItem(text = text, permanent = permanent))
            updateWidget()
        }
    }

    fun toggleTodo(item: TodoItem) {
        viewModelScope.launch {
            todoDao.setDone(item.id, !item.done)
            updateWidget()
        }
    }

    fun deleteTodo(item: TodoItem) {
        viewModelScope.launch {
            todoDao.delete(item)
            updateWidget()
        }
    }

    // Deshacer: vuelve a guardar la tarea con el mismo id, así recupera
    // su lugar en la lista
    fun restoreTodo(item: TodoItem) {
        viewModelScope.launch {
            todoDao.insert(item)
            updateWidget()
        }
    }

    fun clearDoneDaily() {
        viewModelScope.launch {
            todoDao.deleteDoneDaily()
            updateWidget()
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { noteDao.update(note) }
    }

    fun updateTodo(item: TodoItem) {
        viewModelScope.launch {
            todoDao.update(item)
            updateWidget()
        }
    }

    fun deleteNotes(items: List<Note>) {
        viewModelScope.launch { items.forEach { noteDao.delete(it) } }
    }

    fun deleteTodos(items: List<TodoItem>) {
        viewModelScope.launch {
            items.forEach { todoDao.delete(it) }
            updateWidget()
        }
    }

    fun restoreTodos(items: List<TodoItem>) {
        viewModelScope.launch {
            items.forEach { todoDao.insert(it) }
            updateWidget()
        }
    }
}

