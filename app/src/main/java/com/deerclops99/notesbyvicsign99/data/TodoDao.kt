package com.deerclops99.notesbyvicsign99.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface TodoDao {
    // Primero las permanentes; en cada grupo, pendientes antes que hechas,
// y las tareas nuevas se agregan al final
    @Query("SELECT * FROM todos ORDER BY permanent DESC, done ASC, id ASC")
    fun observeAll(): Flow<List<TodoItem>>

    // Estas dos las usa el widget: lectura puntual, sin Flow
    @Query("SELECT * FROM todos ORDER BY permanent DESC, done ASC, id ASC")
    suspend fun getAll(): List<TodoItem>
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Long): TodoItem?
    @Insert
    suspend fun insert(item: TodoItem)
    @Query("UPDATE todos SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)
    @Delete
    suspend fun delete(item: TodoItem)
    // Borra solo las tareas "del día" que ya están hechas
    @Query("DELETE FROM todos WHERE done = 1 AND permanent = 0")
    suspend fun deleteDoneDaily()

    @Update
    suspend fun update(item: TodoItem)
}
