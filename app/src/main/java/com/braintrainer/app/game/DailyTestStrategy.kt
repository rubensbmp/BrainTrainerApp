package com.braintrainer.app.game

class DailyTestStrategy : GameStrategy {
    
    private val questionsPerGame = 4
    private val strategies = listOf(
        CalculationGameStrategy(),
        MissingSymbolGameStrategy(),
        GreatestNumberGameStrategy(),
        FlagGameStrategy(),
        MapGameStrategy(),
        NumberMemoryStrategy(),
        VisualCountStrategy(),
        MultiplicationGameStrategy()
    )
    
    // 8 games * 4 questions each = 32 questions total
    
    private var currentIndex = 0

    override fun getGameType() = "DAILY_TEST"
    
    override fun getCurrentGameType(): String {
        val strategyIndex = (currentIndex / questionsPerGame) % strategies.size
        return strategies[strategyIndex].getGameType()
    }
    
    override fun getDurationSeconds() = 480 // 32 rounds * 15s

    override fun getTargetQuestionCount() = 32

    override fun generateQuestion(difficulty: String): GameQuestion {
        val strategyIndex = (currentIndex / questionsPerGame) % strategies.size
        val strategy = strategies[strategyIndex]
        
        currentIndex++
        
        val q = strategy.generateQuestion(difficulty)
        
        return q
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}
