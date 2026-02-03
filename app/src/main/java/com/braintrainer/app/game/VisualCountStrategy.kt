package com.braintrainer.app.game

import kotlin.random.Random

class VisualCountStrategy : GameStrategy {
    override fun getGameType() = "VISUAL_COUNT"
    override fun getDurationSeconds() = 60
    override fun getTargetQuestionCount() = 12

    override fun generateQuestion(difficulty: String): GameQuestion {
        // Red circles (Target): 2 to 8
        val redCount = if (difficulty == "HARD") Random.nextInt(5, 12) else Random.nextInt(2, 9)
        val blueCount = if (difficulty == "HARD") Random.nextInt(15, 25) else Random.nextInt(4, 11)
        
        val correct = redCount.toString()
        
        // Options: correct + 3 other numbers in range [2, 9] (unique)
        val options = mutableSetOf<String>()
        options.add(correct)
        
        while (options.size < 4) {
            val fake = Random.nextInt(2, 10)
            if (fake != redCount) options.add(fake.toString())
        }
        
        // Config: VISUAL:<targets>:<distractors>
        val config = "VISUAL:$redCount:$blueCount"
        
        return GameQuestion(
            id = Random.nextInt(),
            displayContent = "game_visual_question",
            flashItems = listOf(config),
            options = options.toList().shuffled(),
            answer = correct
        )
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}
