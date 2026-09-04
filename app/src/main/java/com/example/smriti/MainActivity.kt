package com.example.smriti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smriti.ui.screen.*
import com.example.smriti.ui.theme.SmritiTheme
import com.example.smriti.ui.viewmodel.SmritiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SmritiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmritiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmritiNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

object NavRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val MEMORY_GAME = "memory_game"
    const val VOICE_ASSISTANT = "voice_assistant"
    const val REMINDERS = "reminders"
    const val CAREGIVER = "caregiver_dashboard"
    const val MEMORY_TIMELINE = "memory_timeline"
}

@Composable
fun SmritiNavHost(viewModel: SmritiViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.ONBOARDING) {
            WelcomeOnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGame = { navController.navigate(NavRoutes.MEMORY_GAME) },
                onNavigateToVoice = { navController.navigate(NavRoutes.VOICE_ASSISTANT) },
                onNavigateToReminders = { navController.navigate(NavRoutes.REMINDERS) },
                onNavigateToCaregiver = { navController.navigate(NavRoutes.CAREGIVER) },
                onNavigateToTimeline = { navController.navigate(NavRoutes.MEMORY_TIMELINE) },
                onEditProfile = { navController.navigate(NavRoutes.ONBOARDING) }
            )
        }

        composable(NavRoutes.MEMORY_GAME) {
            CognitiveExercisesHubScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.VOICE_ASSISTANT) {
            VoiceAssistantScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToGame = { navController.navigate(NavRoutes.MEMORY_GAME) },
                onNavigateToReminders = { navController.navigate(NavRoutes.REMINDERS) },
                onNavigateToCaregiver = { navController.navigate(NavRoutes.CAREGIVER) },
                onNavigateToTimeline = { navController.navigate(NavRoutes.MEMORY_TIMELINE) }
            )
        }

        composable(NavRoutes.REMINDERS) {
            RemindersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CAREGIVER) {
            CaregiverDashboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MEMORY_TIMELINE) {
            MemoryTimelineScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
