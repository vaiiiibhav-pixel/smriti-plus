package com.example.smriti.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smriti.SmritiApp
import com.example.smriti.ai.CognitiveAI
import com.example.smriti.data.model.GameSession
import com.example.smriti.data.model.Reminder
import com.example.smriti.data.model.StoredMemory
import com.example.smriti.data.model.UserProfile
import com.example.smriti.util.TextToSpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class SmritiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as SmritiApp).repository
    val ttsHelper = TextToSpeechHelper(application)

    val sessions: StateFlow<List<GameSession>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminders: StateFlow<List<Reminder>> = repository.allReminders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories: StateFlow<List<StoredMemory>> = repository.allMemories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val caregiverMetrics: StateFlow<CognitiveAI.CaregiverMetrics> = sessions.combine(MutableStateFlow(Unit)) { sList, _ ->
        CognitiveAI.getCaregiverSummary(sList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CognitiveAI.getCaregiverSummary(emptyList())
    )

    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    // ==========================================
    // MEMORY GAME STATE
    // ==========================================
    enum class GameState {
        IDLE,
        PREPARING,
        SHOWING_SEQUENCE,
        PLAYER_TURN,
        ROUND_SUCCESS,
        ROUND_FAILED,
        GAME_OVER
    }

    private val _gameState = MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentDifficulty = MutableStateFlow(1)
    val currentDifficulty: StateFlow<Int> = _currentDifficulty.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _totalRounds = MutableStateFlow(4)
    val totalRounds: StateFlow<Int> = _totalRounds.asStateFlow()

    private val _targetSequence = MutableStateFlow<List<Int>>(emptyList())
    val targetSequence: StateFlow<List<Int>> = _targetSequence.asStateFlow()

    private val _playerInput = MutableStateFlow<List<Int>>(emptyList())
    val playerInput: StateFlow<List<Int>> = _playerInput.asStateFlow()

    private val _activeHighlight = MutableStateFlow<Int?>(null)
    val activeHighlight: StateFlow<Int?> = _activeHighlight.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _gameMistakes = MutableStateFlow(0)
    val gameMistakes: StateFlow<Int> = _gameMistakes.asStateFlow()

    private val _reactionTimes = MutableStateFlow<List<Long>>(emptyList())

    private val _lastAnalysis = MutableStateFlow<CognitiveAI.AnalysisResult?>(null)
    val lastAnalysis: StateFlow<CognitiveAI.AnalysisResult?> = _lastAnalysis.asStateFlow()

    private var roundStartTime: Long = 0L
    private var displayJob: Job? = null

    init {
        // Observe past sessions to adapt difficulty
        viewModelScope.launch {
            sessions.collect { list ->
                if (list.isNotEmpty() && _gameState.value == GameState.IDLE) {
                    _currentDifficulty.value = list.first().difficulty
                }
            }
        }
    }

    fun startGame() {
        displayJob?.cancel()
        _gameScore.value = 0
        _gameMistakes.value = 0
        _currentRound.value = 1
        _reactionTimes.value = emptyList()
        _lastAnalysis.value = null
        startRound()
    }

    fun startRound() {
        displayJob?.cancel()
        _gameState.value = GameState.PREPARING
        _playerInput.value = emptyList()

        // Number of steps in sequence depends on difficulty and round
        val seqLen = CognitiveAI.getSequenceLength(_currentDifficulty.value)
        val newSeq = List(seqLen) { Random.nextInt(0, 4) } // 4 tiles grid
        _targetSequence.value = newSeq

        displayJob = viewModelScope.launch {
            delay(900)
            _gameState.value = GameState.SHOWING_SEQUENCE
            val speed = CognitiveAI.getDisplaySpeedMs(_currentDifficulty.value)

            for (tileIndex in newSeq) {
                _activeHighlight.value = tileIndex
                delay(speed)
                _activeHighlight.value = null
                delay(220)
            }

            _gameState.value = GameState.PLAYER_TURN
            roundStartTime = System.currentTimeMillis()
        }
    }

    fun onTileClicked(tileIndex: Int) {
        if (_gameState.value != GameState.PLAYER_TURN) return

        val reactionTime = System.currentTimeMillis() - roundStartTime
        _reactionTimes.value = _reactionTimes.value + reactionTime

        val currentInput = _playerInput.value + tileIndex
        _playerInput.value = currentInput

        // Briefly illuminate clicked tile
        viewModelScope.launch {
            _activeHighlight.value = tileIndex
            delay(180)
            _activeHighlight.value = null
        }

        val stepIndex = currentInput.size - 1
        if (tileIndex != _targetSequence.value[stepIndex]) {
            // Mistake
            _gameMistakes.value = _gameMistakes.value + 1
            viewModelScope.launch {
                _gameState.value = GameState.ROUND_FAILED
                delay(1200)
                if (_currentRound.value >= _totalRounds.value) {
                    finishGame()
                } else {
                    _currentRound.value = _currentRound.value + 1
                    startRound()
                }
            }
            return
        }

        // Check if sequence completed
        if (currentInput.size == _targetSequence.value.size) {
            val roundPoints = 25
            _gameScore.value = _gameScore.value + roundPoints
            viewModelScope.launch {
                _gameState.value = GameState.ROUND_SUCCESS
                delay(1000)
                if (_currentRound.value >= _totalRounds.value) {
                    finishGame()
                } else {
                    _currentRound.value = _currentRound.value + 1
                    startRound()
                }
            }
        }
    }

    private fun finishGame() {
        _gameState.value = GameState.GAME_OVER

        val totalRoundsCount = _totalRounds.value
        val mistakes = _gameMistakes.value
        val totalSteps = totalRoundsCount * CognitiveAI.getSequenceLength(_currentDifficulty.value)
        val correctSteps = (totalSteps - mistakes).coerceAtLeast(0)
        val accuracy = if (totalSteps > 0) ((correctSteps.toDouble() / totalSteps) * 100.0) else 0.0
        val avgReactionTimeSeconds = if (_reactionTimes.value.isNotEmpty()) {
            (_reactionTimes.value.average() / 1000.0 * 10).roundToInt() / 10.0
        } else 2.0

        val currentDiff = _currentDifficulty.value
        val recentList = sessions.value
        val analysis = CognitiveAI.analyzeSession(
            score = _gameScore.value,
            accuracy = accuracy,
            reactionTime = avgReactionTimeSeconds,
            mistakes = mistakes,
            currentDifficulty = currentDiff,
            recentSessions = recentList
        )
        _lastAnalysis.value = analysis

        // Update difficulty for next time
        _currentDifficulty.value = analysis.nextDifficulty

        // Save session to Room
        val session = GameSession(
            score = _gameScore.value,
            accuracy = ((accuracy * 10).roundToInt() / 10.0),
            reactionTimeSeconds = avgReactionTimeSeconds,
            mistakes = mistakes,
            difficulty = currentDiff,
            sequenceLength = CognitiveAI.getSequenceLength(currentDiff),
            aiStatus = analysis.status,
            aiRecommendation = analysis.recommendation
        )

        viewModelScope.launch {
            repository.saveGameSession(session)
            // Speak gentle uplifting encouragement in Hinglish
            ttsHelper.speak("Bohat shaandaar koshish! Aapka cognitive score hai ${_gameScore.value}. ${analysis.recommendation}")
        }
    }

    fun resetGame() {
        displayJob?.cancel()
        _gameState.value = GameState.IDLE
        _playerInput.value = emptyList()
        _targetSequence.value = emptyList()
        _activeHighlight.value = null
    }

    // ==========================================
    // REMINDERS MANAGEMENT
    // ==========================================
    fun addReminder(title: String, time: String, category: String) {
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                time = time,
                category = category,
                isEnabled = true
            )
            repository.addReminder(reminder)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.toggleReminder(reminder.id, !reminder.isEnabled)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // ==========================================
    // STORED MEMORIES & REMINISCENCE TIMELINE
    // ==========================================
    fun addMemory(
        title: String,
        description: String,
        dateOrYear: String,
        category: String,
        emotion: String,
        location: String = ""
    ) {
        viewModelScope.launch {
            val memory = StoredMemory(
                title = title.trim(),
                description = description.trim(),
                dateOrYear = dateOrYear.trim(),
                category = category.trim(),
                emotion = emotion.trim(),
                location = location.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.addMemory(memory)
            ttsHelper.speak("Aapki pyari yaad timeline mein save ho gayi hai: ${memory.title}")
        }
    }

    fun deleteMemory(memory: StoredMemory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun speakMemory(memory: StoredMemory) {
        val narrative = "Aapki yaad: ${memory.dateOrYear} ki. ${memory.title}. ${memory.description}"
        ttsHelper.speak(narrative)
    }

    // ==========================================
    // VOICE ASSISTANT STATE
    // ==========================================
    data class ChatMessage(
        val isUser: Boolean,
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Namaste ji! 🙏 Main aapki Smriti AI assistant hoon. Aap bol kar ya neeche diye gaye options tap karke baat kar sakte hain. Medicines, memory games ya health score ke baare mein kabhi bhi poochiye!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun handleUserVoiceQuery(query: String, onNavigate: (CognitiveAI.VoiceAction) -> Unit = {}) {
        if (query.isBlank()) return

        val userMsg = ChatMessage(isUser = true, text = query)
        _chatMessages.value = _chatMessages.value + userMsg

        val response = CognitiveAI.processVoiceQuery(
            query = query,
            reminders = reminders.value,
            sessions = sessions.value
        )

        viewModelScope.launch {
            delay(400)
            val assistantMsg = ChatMessage(isUser = false, text = response.text)
            _chatMessages.value = _chatMessages.value + assistantMsg
            ttsHelper.speak(response.text)

            if (response.action != CognitiveAI.VoiceAction.NONE) {
                delay(1200)
                onNavigate(response.action)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        displayJob?.cancel()
        ttsHelper.shutdown()
    }
}
