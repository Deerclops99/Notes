package com.deerclops99.notesbyvicsign99.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Note>>
    @Insert
    suspend fun insert(note: Note)
    @Delete
    suspend fun delete(note: Note)

    @Update
    suspend fun update(note: Note)
}

