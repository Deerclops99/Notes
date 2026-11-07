package com.sweetyani.misnotas.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val done: Boolean = false,
    val permanent: Boolean = false
)