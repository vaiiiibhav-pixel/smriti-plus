package com.example.smriti.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smriti.data.model.GameSession
import com.example.smriti.data.model.Reminder
import com.example.smriti.data.model.StoredMemory
import com.example.smriti.data.model.UserProfile

@Database(entities = [GameSession::class, Reminder::class, StoredMemory::class, UserProfile::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun storedMemoryDao(): StoredMemoryDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smriti_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
