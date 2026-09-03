package com.example.smriti.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val time: String,
    val category: String = "Medication",
    val isEnabled: Boolean = true
)
