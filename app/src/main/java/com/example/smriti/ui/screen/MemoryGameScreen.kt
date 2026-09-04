package com.example.smriti.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smriti.ui.theme.*
import com.example.smriti.ui.viewmodel.SmritiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
    viewModel: SmritiViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val difficulty by viewModel.currentDifficulty.collectAsState()
    val round by viewModel.currentRound.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    val score by viewModel.gameScore.collectAsState()
    val activeHighlight by viewModel.activeHighlight.collectAsState()
    val lastAnalysis by viewModel.lastAnalysis.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetGame()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Memory Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("game_back_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Level $difficulty",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // SCORE AND ROUND HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$score pts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = "Round $round of $totalRounds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STATUS BANNER
            GameStatusBanner(gameState = gameState)

            Spacer(modifier = Modifier.height(28.dp))

            // 2x2 COLOR TILE MATRIX (Elderly friendly large buttons)
            val tileColors = listOf(
                Pair(GameTileCoral, "Red Block"),
                Pair(GameTileBlue, "Blue Block"),
                Pair(GameTileEmerald, "Green Block"),
                Pair(GameTileAmber, "Yellow Block")
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameTile(
                            index = 0,
                            color = tileColors[0].first,
                            label = "1",
                            isHighlighted = activeHighlight == 0,
                            isEnabled = gameState == SmritiViewModel.GameState.PLAYER_TURN,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onTileClicked(0) }
                        )
                        GameTile(
                            index = 1,
                            color = tileColors[1].first,
                            label = "2",
                            isHighlighted = activeHighlight == 1,
                            isEnabled = gameState == SmritiViewModel.GameState.PLAYER_TURN,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onTileClicked(1) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameTile(
                            index = 2,
                            color = tileColors[2].first,
                            label = "3",
                            isHighlighted = activeHighlight == 2,
                            isEnabled = gameState == SmritiViewModel.GameState.PLAYER_TURN,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onTileClicked(2) }
                        )
                        GameTile(
                            index = 3,
                            color = tileColors[3].first,
                            label = "4",
                            isHighlighted = activeHighlight == 3,
                            isEnabled = gameState == SmritiViewModel.GameState.PLAYER_TURN,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { viewModel.onTileClicked(3) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ACTION CONTROLS
            when (gameState) {
                SmritiViewModel.GameState.IDLE -> {
                    Button(
                        onClick = { viewModel.startGame() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("start_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Start Exercise",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                SmritiViewModel.GameState.GAME_OVER -> {
                    GameOverCard(
                        score = score,
                        analysis = lastAnalysis,
                        onPlayAgain = { viewModel.startGame() },
                        onBack = onBack
                    )
                }
                else -> {
                    OutlinedButton(
                        onClick = { viewModel.resetGame() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Restart Exercise",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GameStatusBanner(gameState: SmritiViewModel.GameState) {
    val (text, bgColor, textColor, icon) = when (gameState) {
        SmritiViewModel.GameState.IDLE -> Quad(
            "Tap 'Start Exercise'. Watch the glowing blocks and repeat the pattern!",
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            Icons.Default.Info
        )
        SmritiViewModel.GameState.PREPARING -> Quad(
            "Get ready...",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.HourglassTop
        )
        SmritiViewModel.GameState.SHOWING_SEQUENCE -> Quad(
            "👀 Watch closely! Memorize the sequence...",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Visibility
        )
        SmritiViewModel.GameState.PLAYER_TURN -> Quad(
            "👉 Your Turn! Tap the blocks in order",
            AestheticGreenContainer,
            AestheticGreen,
            Icons.Default.TouchApp
        )
        SmritiViewModel.GameState.ROUND_SUCCESS -> Quad(
            "🎉 Excellent recall! Perfect!",
            AestheticGreenContainer,
            AestheticGreen,
            Icons.Default.CheckCircle
        )
        SmritiViewModel.GameState.ROUND_FAILED -> Quad(
            "Good try! Let's continue to the next round.",
            AestheticRoseContainer,
            AestheticRose,
            Icons.Default.Refresh
        )
        SmritiViewModel.GameState.GAME_OVER -> Quad(
            "Exercise Completed!",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.EmojiEvents
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun GameTile(
    index: Int,
    color: Color,
    label: String,
    isHighlighted: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tile_scale"
    )

    val glowBorder = if (isHighlighted) {
        Modifier.border(4.dp, Color.White, RoundedCornerShape(26.dp))
    } else {
        Modifier.border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
    }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) color else color.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 12.dp else 4.dp
        ),
        modifier = modifier
            .scale(scale)
            .then(glowBorder)
            .testTag("game_tile_$index")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverCard(
    score: Int,
    analysis: com.example.smriti.ai.CognitiveAI.AnalysisResult?,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_over_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = AmberLight,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = AmberAccent,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Score: $score pts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (analysis != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cognitive AI Insight: ${analysis.status}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = analysis.recommendation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Next session will adapt to Level ${analysis.nextDifficulty} (${analysis.trend})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(text = "Home")
                }

                Button(
                    onClick = onPlayAgain,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("play_again_button")
                ) {
                    Text(text = "Play Again", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
