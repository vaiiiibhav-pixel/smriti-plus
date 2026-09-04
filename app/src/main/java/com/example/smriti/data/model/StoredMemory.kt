package com.example.smriti.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stored_memories")
data class StoredMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val dateOrYear: String,
    val category: String,
    val emotion: String = "Joyful",
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
