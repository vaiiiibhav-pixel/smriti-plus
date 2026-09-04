package com.example.smriti.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smriti.data.model.UserProfile
import com.example.smriti.ui.theme.*
import com.example.smriti.ui.viewmodel.SmritiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeOnboardingScreen(
    viewModel: SmritiViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    var currentStep by remember { mutableStateOf(1) } // Step 1: Permissions, Step 2: Personal Profile

    // Permission state trackers
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Profile form state prefilled from existing profile if available
    var fullName by remember(userProfile) { mutableStateOf(userProfile?.fullName ?: "") }
    var preferredName by remember(userProfile) { mutableStateOf(userProfile?.preferredName ?: "") }
    var age by remember(userProfile) { mutableStateOf(userProfile?.age ?: "") }
    var emergencyContactName by remember(userProfile) { mutableStateOf(userProfile?.emergencyContactName ?: "") }
    var emergencyContactPhone by remember(userProfile) { mutableStateOf(userProfile?.emergencyContactPhone ?: "") }
    var preferredLanguage by remember(userProfile) { mutableStateOf(userProfile?.preferredLanguage ?: "English") }
    var formError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentStep == 1) "Permissions Setup" else "Profile & Emergency Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onComplete) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Return to Home"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onComplete) {
                        Text("Skip to App", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Brand Header Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Smriti Logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Welcome to SMRITI+",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Elderly Memory, Cognitive Care & Daily Wellness",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress indicator (Step 1 or Step 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepPill(stepNumber = 1, label = "Permissions", isActive = currentStep == 1, isCompleted = currentStep > 1)
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(2.dp)
                        .background(if (currentStep > 1) MaterialTheme.colorScheme.primary else SmritiBorder)
                )
                StepPill(stepNumber = 2, label = "Personal Info", isActive = currentStep == 2, isCompleted = false)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentStep == 1) {
                // ==========================================
                // STEP 1: PERMISSION REQUEST CARDS
                // ==========================================
                Text(
                    text = "App Permissions Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "To provide voice companion support and medicine alerts, please grant the essential permissions below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Microphone permission card
                PermissionCard(
                    title = "Microphone & Voice Access",
                    description = "Enables hands-free voice assistant navigation and memory narration for comfortable, natural talking.",
                    icon = Icons.Default.Mic,
                    isGranted = hasMicPermission,
                    onRequest = {
                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    testTag = "grant_mic_permission_button"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notification permission card
                PermissionCard(
                    title = "Medicine & Water Notifications",
                    description = "Sends timely, gentle reminders for daily medications, hydration breaks, and brain wellness exercises.",
                    icon = Icons.Default.NotificationsActive,
                    isGranted = hasNotificationPermission,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            hasNotificationPermission = true
                        }
                    },
                    testTag = "grant_notif_permission_button"
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        currentStep = 2
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("continue_to_profile_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Continue to Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue",
                        tint = Color.White
                    )
                }
            } else {
                // ==========================================
                // STEP 2: PERSONAL INFORMATION FORM
                // ==========================================
                Text(
                    text = "Your Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Helps personalize your greeting, voice assistant responses, and caregiver emergency support.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name (e.g. Ramesh Chandra Sharma)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_full_name")
                        )

                        OutlinedTextField(
                            value = preferredName,
                            onValueChange = { preferredName = it },
                            label = { Text("What should we call you? (e.g. Daadaji / Ramesh Ji)") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_preferred_name")
                        )

                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age (e.g. 72)") },
                            leadingIcon = {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = SmritiAccentAmber)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_age")
                        )

                        Divider(color = SmritiBorder, thickness = 1.dp)

                        Text(
                            text = "Emergency / Caregiver Contact",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = { Text("Caregiver / Doctor Name (e.g. Dr. Sunita - Daughter)") },
                            leadingIcon = {
                                Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_emergency_name")
                        )

                        OutlinedTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = { Text("Emergency Phone Number (e.g. +91 98765 43210)") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = SmritiSuccess)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_emergency_phone")
                        )

                        // Language Preference Selector
                        Text(
                            text = "Preferred Companion Language",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("English", "Hindi", "Bilingual").forEach { lang ->
                                val isSelected = preferredLanguage == lang
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { preferredLanguage = lang }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = lang,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (formError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SmritiError,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { currentStep = 1 },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = {
                            val finalName = if (preferredName.isNotBlank()) preferredName.trim() else if (fullName.isNotBlank()) fullName.trim() else "Respected Friend"
                            val profile = UserProfile(
                                id = 1,
                                fullName = fullName.trim(),
                                preferredName = finalName,
                                age = age.trim(),
                                emergencyContactName = emergencyContactName.trim(),
                                emergencyContactPhone = emergencyContactPhone.trim(),
                                preferredLanguage = preferredLanguage,
                                onboardingCompleted = true,
                                micPermissionGranted = hasMicPermission,
                                notificationPermissionGranted = hasNotificationPermission
                            )
                            viewModel.saveUserProfile(profile)
                            viewModel.ttsHelper.speak("Welcome $finalName ji! Aapka Smriti memory aur wellness saathi taiyaar hai.")
                            onComplete()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SmritiSuccess),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp)
                            .testTag("save_profile_and_start_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save & Start App",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepPill(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = when {
                isCompleted -> SmritiSuccess
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (isGranted) SmritiSuccess.copy(alpha = 0.5f) else SmritiBorder,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isGranted) SmritiSuccessContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) SmritiSuccess else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SmritiSuccessContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Granted",
                            tint = SmritiSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Allowed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SmritiSuccess
                        )
                    }
                }
            } else {
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag(testTag)
                ) {
                    Text(
                        text = "Allow",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
