package com.example.smriti.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val accuracy: Double,
    val reactionTimeSeconds: Double,
    val mistakes: Int,
    val difficulty: Int,
    val sequenceLength: Int,
    val aiStatus: String,
    val aiRecommendation: String
)
