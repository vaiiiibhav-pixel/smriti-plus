package com.example.smriti.data.repository

import com.example.smriti.data.local.GameSessionDao
import com.example.smriti.data.local.ReminderDao
import com.example.smriti.data.local.StoredMemoryDao
import com.example.smriti.data.local.UserProfileDao
import com.example.smriti.data.model.GameSession
import com.example.smriti.data.model.Reminder
import com.example.smriti.data.model.StoredMemory
import com.example.smriti.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

class SmritiRepository(
    private val gameSessionDao: GameSessionDao,
    private val reminderDao: ReminderDao,
    private val storedMemoryDao: StoredMemoryDao,
    private val userProfileDao: UserProfileDao
) {
    val allSessions: Flow<List<GameSession>> = gameSessionDao.getAllSessions()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allMemories: Flow<List<StoredMemory>> = storedMemoryDao.getAllMemories()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? {
        return userProfileDao.getUserProfileOnce()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.saveProfile(profile)
    }

    suspend fun saveGameSession(session: GameSession): Long {
        return gameSessionDao.insertSession(session)
    }

    suspend fun addReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun toggleReminder(id: Long, isEnabled: Boolean) {
        reminderDao.toggleReminder(id, isEnabled)
    }

    suspend fun addMemory(memory: StoredMemory): Long {
        return storedMemoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: StoredMemory) {
        storedMemoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: StoredMemory) {
        storedMemoryDao.deleteMemory(memory)
    }

    suspend fun seedInitialDataIfEmpty(currentRemindersCount: Int, currentMemoriesCount: Int = 0) {
        if (currentRemindersCount == 0) {
            val defaults = listOf(
                Reminder(title = "Morning Blood Pressure Medicine", time = "08:00 AM", category = "Medication"),
                Reminder(title = "Hydration Break - Warm Water", time = "11:00 AM", category = "Water"),
                Reminder(title = "Post-Lunch Multivitamin", time = "01:30 PM", category = "Medication"),
                Reminder(title = "Evening Walk & Garden Breathe", time = "05:30 PM", category = "Exercise"),
                Reminder(title = "Calcium & Sleep Herbal Tea", time = "09:00 PM", category = "Medication")
            )
            defaults.forEach { reminderDao.insertReminder(it) }
        }

        if (currentMemoriesCount == 0) {
            val defaultMemories = listOf(
                StoredMemory(
                    title = "Morning Jasmine & Cardamom Chai",
                    description = "Fresh white jasmine blossoms blooming on the morning balcony. Sitting in the cool dawn air, sipping cardamom chai while listening to sparrows sing on the neem branches.",
                    dateOrYear = "April 2024",
                    category = "Daily Joy",
                    emotion = "Peaceful",
                    location = "Balcony Herb Garden",
                    timestamp = 1713000000000L
                ),
                StoredMemory(
                    title = "Grandson Kabir's First Steps",
                    description = "Little Kabir wobbled three tiny steps towards Daadu's outstretched arms, giggling with wide sparkling eyes before falling safely into a warm family hug.",
                    dateOrYear = "February 2019",
                    category = "Family",
                    emotion = "Joyful",
                    location = "Living Room Rug",
                    timestamp = 1550000000000L
                ),
                StoredMemory(
                    title = "30th Golden Anniversary Gathering",
                    description = "Surrounded by all our children, grandchildren, and childhood friends. We exchanged fresh marigold garlands under the evening fairy lights with vintage tunes on the gramophone.",
                    dateOrYear = "November 2011",
                    category = "Celebration",
                    emotion = "Heartwarming",
                    location = "Rose Garden Pavilion, Pune",
                    timestamp = 1321000000000L
                ),
                StoredMemory(
                    title = "Daughter Sunita's Convocation Day",
                    description = "Sunita took the stage in her convocation gown and was awarded the university gold medal. She looked down and dedicated her engineering thesis to her grandparents.",
                    dateOrYear = "October 1998",
                    category = "Milestone",
                    emotion = "Proud",
                    location = "University Auditorium, New Delhi",
                    timestamp = 908000000000L
                ),
                StoredMemory(
                    title = "Toy Train Journey into Shimla Hills",
                    description = "Riding through 103 pine-scented tunnels on the narrow-gauge heritage toy train. The crisp mountain breeze, warm roasted peanuts in paper cones, and family laughter.",
                    dateOrYear = "May 1985",
                    category = "Travel",
                    emotion = "Joyful",
                    location = "Kalka-Shimla Railway",
                    timestamp = 485000000000L
                ),
                StoredMemory(
                    title = "First Monsoon in the Ancestral Courtyard",
                    description = "The unmistakable scent of petrichor on warm clay tiles. Maa made hot, crispy onion pakoras while we floated colorful paper boats in the courtyard rain pools.",
                    dateOrYear = "July 1972",
                    category = "Youth & Childhood",
                    emotion = "Nostalgic",
                    location = "Old Courtyard, Pune",
                    timestamp = 80000000000L
                )
            )
            defaultMemories.forEach { storedMemoryDao.insertMemory(it) }
        }

        if (userProfileDao.getUserProfileOnce() == null) {
            userProfileDao.saveProfile(
                UserProfile(
                    id = 1,
                    fullName = "Ramesh Chandra Sharma",
                    preferredName = "Daadaji",
                    age = "72",
                    emergencyContactName = "Dr. Sunita Sharma (Daughter)",
                    emergencyContactPhone = "+91 98765 43210",
                    preferredLanguage = "English",
                    onboardingCompleted = true,
                    micPermissionGranted = true,
                    notificationPermissionGranted = true
                )
            )
        }
    }
}
