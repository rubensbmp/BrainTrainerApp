package com.braintrainer.app.ui.game

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.braintrainer.app.data.local.AppDatabase
import com.braintrainer.app.data.local.GameResult
import com.braintrainer.app.game.CalculationGameStrategy
import com.braintrainer.app.game.GameQuestion
import com.braintrainer.app.game.GameStrategy
import com.braintrainer.app.game.GreatestNumberGameStrategy
import com.braintrainer.app.game.MissingSymbolGameStrategy
import com.braintrainer.app.game.FlagGameStrategy
import com.braintrainer.app.game.MapGameStrategy
import com.braintrainer.app.game.PerformanceEvaluator
import com.braintrainer.app.game.NumberMemoryStrategy
import com.braintrainer.app.game.VisualCountStrategy
import com.braintrainer.app.game.DailyTestStrategy
import com.braintrainer.app.game.MultiplicationGameStrategy
import kotlinx.coroutines.launch

class GameViewModel(
    application: Application, 
    private val gameType: String,
    private val difficulty: String,
    private val targetRounds: Int,
    private val flashTime: Long,
    private val isPractice: Boolean
) : AndroidViewModel(application) {
    
    private val gameResultDao = AppDatabase.getDatabase(application).gameResultDao()
    private lateinit var strategy: GameStrategy

    private val _currentQuestion = MutableLiveData<GameQuestion>()
    val currentQuestion: LiveData<GameQuestion> = _currentQuestion

    private val _timeLeft = MutableLiveData<Int>()
    val timeLeft: LiveData<Int> = _timeLeft
    
    // Valid values: "FLASHING", "INPUT"
    private val _viewState = MutableLiveData<String>("INPUT")
    val viewState: LiveData<String> = _viewState
    
    // Expose rounds
    private val _roundsInfo = MutableLiveData<String>()
    val roundsInfo: LiveData<String> = _roundsInfo
    
    // Event for Answer Result (Correct/Wrong) for Sound effects
    private val _answerResult = MutableLiveData<Boolean>()
    val answerResult: LiveData<Boolean> = _answerResult
    
    private val _gameFinished = MutableLiveData<PerformanceEvaluator.EvaluationResult?>()
    val gameFinished: LiveData<PerformanceEvaluator.EvaluationResult?> = _gameFinished

    private val _currentGameType = MutableLiveData<String>()
    val currentGameType: LiveData<String> = _currentGameType

    fun getIsGameStarted() = isGameStarted

    private var lastStrategyName: String? = null
    private var isTimerPaused = false
    private var isGameStarted = false

    private var score = 0
    private var questionCount = 0
    private var startTimeMillis = 0L
    private var totalTimeMillis = 0L // Actual time spent
    
    private var timer: CountDownTimer? = null
    private var actualTargetRounds = targetRounds
    private var timeRemainingMillis = 0L

    init {
        // Factory logic for strategies (could be injected)
        strategy = when (gameType) {
            "CALCULATION" -> CalculationGameStrategy()
            "REFLEX_GREATEST" -> GreatestNumberGameStrategy()
            "LOGIC_SYMBOL" -> MissingSymbolGameStrategy()
            "FLAG_QUIZ" -> FlagGameStrategy()
            "MAP_QUIZ" -> MapGameStrategy()
            "NUMBER_MEMORY" -> NumberMemoryStrategy()
            "VISUAL_COUNT" -> VisualCountStrategy()
            "DAILY_TEST" -> DailyTestStrategy()
            "MULTIPLICATION" -> MultiplicationGameStrategy()
            "POKER_HAND" -> com.braintrainer.app.game.PokerGameStrategy()
            else -> CalculationGameStrategy() // Fallback
        }
        // Initial intro logic
        _currentGameType.value = gameType
    }

    fun startGame() {
        if (isGameStarted) return 
        isGameStarted = true
        
        // Clamp rounds based on strategy limits (e.g. Map Game)
        val maxAvailable = strategy.getMaxQuestions(difficulty)
        actualTargetRounds = targetRounds.coerceAtMost(maxAvailable)
        
        score = 0
        questionCount = 0
        startTimeMillis = System.currentTimeMillis()
        totalTimeMillis = 0L
        isTimerPaused = false
        timeRemainingMillis = strategy.getDurationSeconds() * 1000L
        
        nextQuestion()
    }

    private fun startTimer(duration: Long = -1L) {
        val ms = if (duration > 0) duration else timeRemainingMillis
        if (ms <= 0) return

        timer?.cancel()
        timer = object : CountDownTimer(ms, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMillis = millisUntilFinished
                _timeLeft.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                finishGame(timeOut = true)
            }
        }.start()
    }

    fun setTimerPaused(paused: Boolean) {
        if (paused) {
            isTimerPaused = true
            timer?.cancel()
        } else {
             // Resume
             if (isTimerPaused) {
                 isTimerPaused = false
                 startTimer(timeRemainingMillis)
             }
        }
    }

    fun acknowledgeGameType() {
        setTimerPaused(false)
        proceedToQuestion()
    }

    fun submitAnswer(answer: String) {
        if (_viewState.value == "FLASHING" || isTimerPaused) return 

        val q = _currentQuestion.value ?: return
        
        val isCorrect = strategy.checkAnswer(q, answer)
        _answerResult.value = isCorrect
        
        if (isCorrect) {
            score++
        }
        
        // Manual Pause for Poker
        if (gameType == "POKER_HAND") {
            setTimerPaused(true)
            return
        }
        
        // Delay before moving to next question to allow visual feedback
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) 
            advanceGame()
        }
    }

    fun forceNextQuestion() {
        setTimerPaused(false)
        viewModelScope.launch {
             advanceGame()
        }
    }
    
    private fun advanceGame() {
        questionCount++
    
        if (questionCount >= actualTargetRounds) {
            finishGame(timeOut = false)
        } else {
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        _roundsInfo.value = "${questionCount + 1}/$actualTargetRounds"
        
        val currentStrategyType = strategy.getCurrentGameType()
        if (currentStrategyType != lastStrategyName) {
            lastStrategyName = currentStrategyType
            _currentGameType.value = currentStrategyType
            return // Wait for UI acknowledgement via acknowledgeGameType()
        }

        proceedToQuestion()
    }

    private fun proceedToQuestion() {
        val qGenerated = strategy.generateQuestion(difficulty)
        
        // Sort options (Numeric or Alphabetical)
        val sortedOptions = if (qGenerated.options.all { it.toIntOrNull() != null }) {
            qGenerated.options.sortedBy { it.toInt() }
        } else {
            qGenerated.options.sorted()
        }
        val q = qGenerated.copy(options = sortedOptions)
        
        if (gameType == "POKER_HAND") {
            _currentQuestion.value = q
            _viewState.value = "INPUT"
        } else if (q.flashItems.isNotEmpty()) {
            // Enter Flash Mode
            _currentQuestion.value = q
            _viewState.value = "FLASHING"
            
            // Force reasonable flash time for Daily Test to ensure visibility
            val effectiveFlashTime = if (gameType == "DAILY_TEST") 1500L else flashTime
            
            // Launch coroutine to wait flashTime then switch to INPUT
            viewModelScope.launch {
                kotlinx.coroutines.delay(effectiveFlashTime) 
                if (!isTimerPaused) { // Only switch if not paused by a sudden transition (edge case)
                    _viewState.value = "INPUT"
                }
            }
        } else {
            // Standard Mode
            _currentQuestion.value = q
            _viewState.value = "INPUT"
        }
    }

    private fun finishGame(timeOut: Boolean) {
        timer?.cancel()
        totalTimeMillis = System.currentTimeMillis() - startTimeMillis
        
        val timeSeconds = (totalTimeMillis / 1000).toInt()
        val result = PerformanceEvaluator.evaluate(score, actualTargetRounds, timeSeconds)
        
        saveResult(result, timeSeconds)
        _gameFinished.value = result
    }

    private fun saveResult(result: PerformanceEvaluator.EvaluationResult, timeSeconds: Int) {
        if (isPractice) return

        viewModelScope.launch {
            val entry = GameResult(
                date = System.currentTimeMillis(),
                gameType = gameType,
                score = score,
                durationSeconds = timeSeconds,
                grade = result.grade,
                brainAge = result.brainAge // Only truly relevant for Daily Test, but saving anyway
            )
            gameResultDao.insertResult(entry)

            // INTERNET SYNC: If logged in, upload to Firebase immediately
            val socialRepo = com.braintrainer.app.data.SocialRepository()
            if (socialRepo.getCurrentUser() != null) {
                try {
                    socialRepo.uploadGameResults(listOf(entry))
                    
                    // Also update the global profile (Age, Matches) to keep Ranking accurate
                    val stats = gameResultDao.getGlobalStats()
                    socialRepo.syncUserProfile(
                        name = "", // syncUserProfile handles empty name by using existing
                        brainAge = (stats.avgBrainAge ?: 40.0).toInt(),
                        totalMatches = stats.totalGames,
                        avgGrade = stats.avgGrade ?: "F",
                        avatarId = -1 // -1 flag to keep existing
                    )
                } catch (e: Exception) {
                    // Silently fail if no internet, SocialViewModel will catch up later
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    class Factory(
        private val application: Application, 
        private val gameType: String,
        private val difficulty: String,
        private val rounds: Int,
        private val flashTime: Long,
        private val isPractice: Boolean = false
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(application, gameType, difficulty, rounds, flashTime, isPractice) as T
        }
    }
}
