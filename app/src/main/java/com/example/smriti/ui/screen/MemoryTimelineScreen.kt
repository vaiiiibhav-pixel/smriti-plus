package com.example.smriti.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smriti.data.model.StoredMemory
import com.example.smriti.ui.theme.*
import com.example.smriti.ui.viewmodel.SmritiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTimelineScreen(
    viewModel: SmritiViewModel,
    onBack: () -> Unit
) {
    val memories by viewModel.memories.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Family",
        "Travel",
        "Milestone",
        "Celebration",
        "Youth & Childhood",
        "Daily Joy"
    )

    val filteredMemories = remember(memories, selectedCategory, searchQuery) {
        memories.filter { mem ->
            val matchesCategory = (selectedCategory == "All") || mem.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    mem.title.contains(searchQuery, ignoreCase = true) ||
                    mem.description.contains(searchQuery, ignoreCase = true) ||
                    mem.location.contains(searchQuery, ignoreCase = true) ||
                    mem.dateOrYear.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Stored Memories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Life Story & Reminiscence Timeline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("timeline_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Memory") },
                text = { Text("Preserve Memory", fontWeight = FontWeight.Bold) },
                containerColor = SagePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_memory_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("memories_timeline_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header summary banner
            item {
                TimelineHeaderCard(
                    totalMemories = memories.size,
                    filteredCount = filteredMemories.size,
                    onAddNew = { showAddDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeline_search_input"),
                    placeholder = { Text("Search memories, places, or dates...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SagePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Category Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SagePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) SagePrimary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.testTag("filter_chip_$category")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Empty State
            if (filteredMemories.isEmpty()) {
                item {
                    TimelineEmptyState(
                        isFiltered = searchQuery.isNotEmpty() || selectedCategory != "All",
                        onResetFilters = {
                            searchQuery = ""
                            selectedCategory = "All"
                        },
                        onAddMemory = { showAddDialog = true }
                    )
                }
            } else {
                // Visual Timeline Items
                items(
                    items = filteredMemories,
                    key = { it.id }
                ) { memory ->
                    val isLast = memory == filteredMemories.last()
                    TimelineItemRow(
                        memory = memory,
                        isLast = isLast,
                        onListen = { viewModel.speakMemory(memory) },
                        onDelete = { viewModel.deleteMemory(memory) }
                    )
                }
            }

            // Bottom space for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, dateOrYear, cat, emotion, loc ->
                viewModel.addMemory(
                    title = title,
                    description = desc,
                    dateOrYear = dateOrYear,
                    category = cat,
                    emotion = emotion,
                    location = loc
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TimelineHeaderCard(
    totalMemories: Int,
    filteredCount: Int,
    onAddNew: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SagePrimary.copy(alpha = 0.4f), TerracottaAccent.copy(alpha = 0.4f)))),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_header_card")
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SagePrimary
                    ) {
                        Text(
                            text = "REMINISCENCE ARCHIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Life Journey in Moments",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalMemories preserved memories • Reflecting past milestones strengthens neural memory pathways.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(54.dp)
                        .clickable { onAddNew() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Journal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    memory: StoredMemory,
    isLast: Boolean,
    onListen: () -> Unit,
    onDelete: () -> Unit
) {
    val (catIcon, catColor, catBg) = getCategoryStyling(memory.category)
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_item_${memory.id}")
    ) {
        // Left Column: Visual Timeline Node & Connecting Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(48.dp)
                .padding(top = 4.dp)
        ) {
            // Node Icon Circle
            Surface(
                shape = CircleShape,
                color = catBg,
                shadowElevation = 2.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = memory.category,
                        tint = catColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Connecting Vertical Spine
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .defaultMinSize(minHeight = 120.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    catColor.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Column: Memory Card Content
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Row: Date Pill & Emotion Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Date / Era Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = memory.dateOrYear,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Emotion Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = memory.emotion,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                // Location if specified
                if (memory.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = memory.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Memory Story / Narrative
                Text(
                    text = memory.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                    lineHeight = 21.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3
                )

                if (memory.description.length > 120) {
                    Text(
                        text = if (isExpanded) "Show Less" else "Read Full Memory",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Actions: Listen Aloud & Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Audio Playback / Read aloud
                    FilledTonalButton(
                        onClick = onListen,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("listen_memory_${memory.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen to memory",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Listen",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Category Pill Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = memory.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Delete Action
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_memory_${memory.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Memory",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEmptyState(
    isFiltered: Boolean,
    onResetFilters: () -> Unit,
    onAddMemory: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("timeline_empty_state")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isFiltered) "No memories match your filter" else "Your Timeline is Waiting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFiltered)
                    "Try selecting 'All' or clearing your search term to see other life moments."
                else
                    "Add your first cherished life memory, wedding day, or family journey to begin your timeline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (isFiltered) {
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Filters")
                }
            } else {
                Button(
                    onClick = onAddMemory,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add First Memory")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, dateOrYear: String, category: String, emotion: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateOrYear by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Family") }
    var selectedEmotion by remember { mutableStateOf("Joyful") }
    var errorMessage by remember { mutableStateOf("") }

    val categories = listOf("Family", "Travel", "Milestone", "Celebration", "Youth & Childhood", "Daily Joy")
    val emotions = listOf("Joyful", "Heartwarming", "Nostalgic", "Proud", "Peaceful")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SageContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = null,
                            tint = OnSageContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Preserve a Memory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                item {
                    Text(
                        text = "Store a personal story, milestone, or cherished moment on your visual timeline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = ""
                        },
                        label = { Text("Memory Title *") },
                        placeholder = { Text("e.g., Grandson's First Birthday") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Date or Era
                    OutlinedTextField(
                        value = dateOrYear,
                        onValueChange = {
                            dateOrYear = it
                            errorMessage = ""
                        },
                        label = { Text("Date or Era *") },
                        placeholder = { Text("e.g., October 1995, Summer of '78") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_date_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        placeholder = { Text("e.g., Shimla Hills, Pune Home") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_location_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SagePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Emotion Tag Selector
                    Text(
                        text = "Mood / Emotion",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emotions) { em ->
                            val isSel = selectedEmotion == em
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedEmotion = em },
                                label = { Text(em, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Description / Story
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            errorMessage = ""
                        },
                        label = { Text("Memory Story / Details *") },
                        placeholder = { Text("Describe the sights, sounds, emotions, and people who shared this moment...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_story_input"),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please provide a memory title."
                    } else if (dateOrYear.isBlank()) {
                        errorMessage = "Please specify a date, year, or era."
                    } else if (description.isBlank()) {
                        errorMessage = "Please enter some story details."
                    } else {
                        onSave(title, description, dateOrYear, selectedCategory, selectedEmotion, location)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_memory_button")
            ) {
                Text("Preserve Memory", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_memory_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

private fun getCategoryStyling(category: String): Triple<ImageVector, Color, Color> {
    return when (category) {
        "Family" -> Triple(Icons.Default.Favorite, AestheticRose, AestheticRoseContainer)
        "Travel" -> Triple(Icons.Default.Train, Color(0xFF3F779B), Color(0xFFE4F0F8))
        "Milestone" -> Triple(Icons.Default.School, Color(0xFF7E5B8E), Color(0xFFF4EBF7))
        "Celebration" -> Triple(Icons.Default.Celebration, TerracottaAccent, TerracottaLight)
        "Youth & Childhood" -> Triple(Icons.Default.Attractions, WarmHoney, WarmHoneyLight)
        "Daily Joy" -> Triple(Icons.Default.LocalCafe, AestheticGreen, AestheticGreenContainer)
        else -> Triple(Icons.Default.AutoStories, SagePrimary, SageContainer)
    }
}
