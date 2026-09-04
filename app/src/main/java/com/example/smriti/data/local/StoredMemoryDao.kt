package com.example.smriti.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smriti.data.model.StoredMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredMemoryDao {
    @Query("SELECT * FROM stored_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<StoredMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: StoredMemory): Long

    @Update
    suspend fun updateMemory(memory: StoredMemory)

    @Delete
    suspend fun deleteMemory(memory: StoredMemory)

    @Query("DELETE FROM stored_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)
}
