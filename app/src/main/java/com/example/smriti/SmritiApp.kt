package com.example.smriti

import android.app.Application
import com.example.smriti.data.local.AppDatabase
import com.example.smriti.data.repository.SmritiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmritiApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: SmritiRepository by lazy {
        SmritiRepository(
            database.gameSessionDao(),
            database.reminderDao(),
            database.storedMemoryDao(),
            database.userProfileDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = repository.allReminders.first()
            val memories = repository.allMemories.first()
            repository.seedInitialDataIfEmpty(reminders.size, memories.size)
        }
    }
}
