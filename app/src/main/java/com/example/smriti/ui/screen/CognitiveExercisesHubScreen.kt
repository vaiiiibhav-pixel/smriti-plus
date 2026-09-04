package com.example.smriti.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smriti.ui.theme.*
import com.example.smriti.ui.viewmodel.SmritiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BrainExerciseTab(val title: String, val icon: ImageVector) {
    SEQUENCE("Pattern Sequence", Icons.Default.GridView),
    WORD_MATCH("Word & Picture Recall", Icons.Default.Category),
    DAILY_CHALLENGE("Daily Cognitive Plan", Icons.Default.Verified)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CognitiveExercisesHubScreen(
    viewModel: SmritiViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BrainExerciseTab.SEQUENCE) }
    val difficulty by viewModel.currentDifficulty.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cognitive Brain Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Neuro-Adaptive Memory Exercises",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("cognitive_hub_back_button")
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
                        color = SageContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = OnSageContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lvl $difficulty",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSageContainer
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
        ) {
            // Tab Selector Bar
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SagePrimary,
                modifier = Modifier.testTag("cognitive_tabs")
            ) {
                BrainExerciseTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (selectedTab) {
                    BrainExerciseTab.SEQUENCE -> {
                        SequenceExerciseView(viewModel = viewModel)
                    }
                    BrainExerciseTab.WORD_MATCH -> {
                        WordPictureRecallView(viewModel = viewModel)
                    }
                    BrainExerciseTab.DAILY_CHALLENGE -> {
                        DailyCognitivePlanView(
                            viewModel = viewModel,
                            onStartSequence = { selectedTab = BrainExerciseTab.SEQUENCE },
                            onStartWordRecall = { selectedTab = BrainExerciseTab.WORD_MATCH }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: SEQUENCE EXERCISE VIEW (Enhanced with custom speed & grid controls)
// =========================================================================
@Composable
fun SequenceExerciseView(viewModel: SmritiViewModel) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sequence_exercise_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Score and Difficulty Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$score pts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SagePrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = TerracottaAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Round $round of $totalRounds",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Status Banner
        item {
            GameStatusBanner(gameState = gameState)
        }

        // 2x2 Interactive Tile Matrix
        item {
            val tileColors = listOf(
                Pair(GameTileCoral, "Coral Rose"),
                Pair(GameTileBlue, "Nordic Blue"),
                Pair(GameTileEmerald, "Forest Jade"),
                Pair(GameTileAmber, "Warm Amber")
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
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
        }

        // Action Controls / Game Over Screen
        item {
            when (gameState) {
                SmritiViewModel.GameState.IDLE -> {
                    Button(
                        onClick = { viewModel.startGame() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Start Sequence Recall",
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
                        onBack = { viewModel.resetGame() }
                    )
                }
                else -> {
                    OutlinedButton(
                        onClick = { viewModel.resetGame() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Cancel & Restart",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: WORD & PICTURE RECALL (Card Matching Pairs for Association Recall)
// =========================================================================
data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun WordPictureRecallView(viewModel: SmritiViewModel) {
    val coroutineScope = rememberCoroutineScope()

    // Card pool with items meaningful to older adults & nostalgic association
    val cardItems = remember {
        listOf(
            Triple("Tea Cup", Icons.Default.LocalCafe, GameTileCoral),
            Triple("Garden Flower", Icons.Default.Yard, GameTileEmerald),
            Triple("Vintage Radio", Icons.Default.Radio, GameTileAmber),
            Triple("Story Book", Icons.Default.AutoStories, GameTileBlue),
            Triple("Family Home", Icons.Default.Home, GameTilePurple),
            Triple("Heritage Train", Icons.Default.Train, GameTileCyan)
        )
    }

    var cards by remember {
        mutableStateOf(generateCards(cardItems.take(4)))
    }
    var flippedCardIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var matchesFound by remember { mutableStateOf(0) }
    var movesCount by remember { mutableStateOf(0) }
    var isBusy by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    fun restartGame() {
        cards = generateCards(cardItems.shuffled().take(4))
        flippedCardIds = emptyList()
        matchesFound = 0
        movesCount = 0
        isBusy = false
        isCompleted = false
    }

    fun onCardClicked(card: MemoryCard) {
        if (isBusy || card.isFlipped || card.isMatched || flippedCardIds.size >= 2) return

        val newCards = cards.map {
            if (it.id == card.id) it.copy(isFlipped = true) else it
        }
        cards = newCards
        val currentFlipped = flippedCardIds + card.id
        flippedCardIds = currentFlipped

        if (currentFlipped.size == 2) {
            movesCount++
            val firstCard = newCards.first { it.id == currentFlipped[0] }
            val secondCard = newCards.first { it.id == currentFlipped[1] }

            if (firstCard.pairId == secondCard.pairId) {
                // Match!
                coroutineScope.launch {
                    delay(300)
                    cards = cards.map {
                        if (it.id == firstCard.id || it.id == secondCard.id) {
                            it.copy(isMatched = true)
                        } else it
                    }
                    flippedCardIds = emptyList()
                    val newMatches = matchesFound + 1
                    matchesFound = newMatches
                    if (newMatches >= 4) {
                        isCompleted = true
                        viewModel.speakMemory(
                            com.example.smriti.data.model.StoredMemory(
                                title = "Word Matching Completed",
                                description = "Brilliant visual association! You matched all pairs in $movesCount moves.",
                                dateOrYear = "Today",
                                category = "Daily Joy"
                            )
                        )
                    }
                }
            } else {
                // No match - flip back
                isBusy = true
                coroutineScope.launch {
                    delay(900)
                    cards = cards.map {
                        if (it.id == firstCard.id || it.id == secondCard.id) {
                            it.copy(isFlipped = false)
                        } else it
                    }
                    flippedCardIds = emptyList()
                    isBusy = false
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("word_picture_recall_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Metric Bar
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pairs Matched",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$matchesFound of 4",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SagePrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Moves Taken",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$movesCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaAccent
                        )
                    }

                    OutlinedButton(
                        onClick = { restartGame() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Shuffle", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }

        // Completion Banner
        if (isCompleted) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AestheticGreenContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = AestheticGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wonderful Recall!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AestheticGreen
                        )
                        Text(
                            text = "Completed in $movesCount moves. Your visual recognition pathways are stimulated!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = AestheticGreen
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { restartGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Play New Round")
                        }
                    }
                }
            }
        }

        // Card Grid (4x2 tiles = 8 cards)
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(cards, key = { it.id }) { card ->
                    MemoryFlipCard(
                        card = card,
                        onClick = { onCardClicked(card) }
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = WarmHoney,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Visual picture association exercises tap into episodic memory pathways, reinforcing recognition without time stress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AestheticTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryFlipCard(
    card: MemoryCard,
    onClick: () -> Unit
) {
    val isRevealed = card.isFlipped || card.isMatched

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                card.isMatched -> AestheticGreenContainer
                isRevealed -> card.color.copy(alpha = 0.18f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (card.isMatched) listOf(AestheticGreen, AestheticGreen)
                else if (isRevealed) listOf(card.color, card.color)
                else listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRevealed) 3.dp else 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .testTag("flip_card_${card.id}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isRevealed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (card.isMatched) AestheticGreen.copy(alpha = 0.2f) else card.color.copy(alpha = 0.25f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = card.icon,
                                contentDescription = card.name,
                                tint = if (card.isMatched) AestheticGreen else card.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (card.isMatched) AestheticGreen else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Tap to flip",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap to Reveal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun generateCards(pool: List<Triple<String, ImageVector, Color>>): List<MemoryCard> {
    var idCounter = 0
    val list = mutableListOf<MemoryCard>()
    pool.forEachIndexed { pairIndex, item ->
        list.add(MemoryCard(id = idCounter++, pairId = pairIndex, name = item.first, icon = item.second, color = item.third))
        list.add(MemoryCard(id = idCounter++, pairId = pairIndex, name = item.first, icon = item.second, color = item.third))
    }
    return list.shuffled()
}

// =========================================================================
// TAB 3: DAILY COGNITIVE PLAN & STREAK BADGES
// =========================================================================
@Composable
fun DailyCognitivePlanView(
    viewModel: SmritiViewModel,
    onStartSequence: () -> Unit,
    onStartWordRecall: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val todayCompleted = sessions.isNotEmpty()
    val streakDays = remember(sessions) {
        if (sessions.isEmpty()) 0 else (sessions.size).coerceAtMost(7)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_cognitive_plan_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Streak Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(WarmHoney.copy(alpha = 0.5f), TerracottaAccent.copy(alpha = 0.5f))
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(WarmHoneyLight.copy(alpha = 0.45f), TerracottaLight.copy(alpha = 0.35f))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WarmHoney
                            ) {
                                Text(
                                    text = "DAILY BRAIN WELLNESS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$streakDays Day Practice Streak",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AestheticTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (todayCompleted)
                                    "✨ Today's brain routine completed! Your cognitive consistency protects memory."
                                else
                                    "Complete one 3-minute exercise today to extend your wellness streak.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AestheticTextSecondary,
                                lineHeight = 19.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = WarmHoneyLight,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = WarmHoney,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Recommended Exercises Checklist
        item {
            Text(
                text = "Today's Recommended Exercises",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AestheticTextPrimary
            )
        }

        // Exercise 1: Pattern Sequence
        item {
            ExercisePlanCard(
                title = "Working Memory: Light Sequences",
                duration = "3 mins • Visual Recall",
                description = "Focus on glowing color sequences and repeat the chain. Strengthens immediate working recall.",
                isDone = sessions.isNotEmpty(),
                icon = Icons.Default.GridView,
                iconColor = SagePrimary,
                iconBg = SageContainer,
                buttonText = if (sessions.isNotEmpty()) "Practice Again" else "Start Exercise",
                onClick = onStartSequence
            )
        }

        // Exercise 2: Word & Picture Recall
        item {
            ExercisePlanCard(
                title = "Episodic Association: Word & Picture Pairs",
                duration = "2 mins • Category Association",
                description = "Flip cards to find matching everyday items. Activates semantic and visual association centres.",
                isDone = false,
                icon = Icons.Default.Category,
                iconColor = TerracottaAccent,
                iconBg = TerracottaLight,
                buttonText = "Start Matching",
                onClick = onStartWordRecall
            )
        }

        // Cognitive Milestones & Badges
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cognitive Milestones & Badges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MilestoneBadgeItem(
                    name = "First Step",
                    sub = "1st Game",
                    isUnlocked = sessions.isNotEmpty(),
                    icon = Icons.Default.Flag,
                    modifier = Modifier.weight(1f)
                )
                MilestoneBadgeItem(
                    name = "Sharp Recall",
                    sub = "Score 80+",
                    isUnlocked = sessions.any { it.score >= 80 },
                    icon = Icons.Default.MilitaryTech,
                    modifier = Modifier.weight(1f)
                )
                MilestoneBadgeItem(
                    name = "Zen Master",
                    sub = "3+ Sessions",
                    isUnlocked = sessions.size >= 3,
                    icon = Icons.Default.SelfImprovement,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExercisePlanCard(
    title: String,
    duration: String,
    description: String,
    isDone: Boolean,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = iconBg,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isDone) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AestheticGreenContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = AestheticGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AestheticGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant else SagePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                )
            }
        }
    }
}

@Composable
fun MilestoneBadgeItem(
    name: String,
    sub: String,
    isUnlocked: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (isUnlocked) SmritiPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
