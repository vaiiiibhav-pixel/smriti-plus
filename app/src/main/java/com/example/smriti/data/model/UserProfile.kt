package com.example.smriti.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "",
    val preferredName: String = "",
    val age: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val preferredLanguage: String = "English",
    val onboardingCompleted: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false
)
