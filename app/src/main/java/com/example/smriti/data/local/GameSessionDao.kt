package com.example.smriti.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smriti.data.model.GameSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<GameSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSession): Long

    @Query("DELETE FROM game_sessions")
    suspend fun clearAll()
}
