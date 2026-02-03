package com.braintrainer.app.game

import kotlin.random.Random

class MultiplicationGameStrategy : GameStrategy {
    private val combinationPool = mutableListOf<Pair<Int, Int>>()
    private var lastDifficulty: String? = null

    private fun refreshPool(difficulty: String) {
        combinationPool.clear()
        if (difficulty.startsWith("TABLE_")) {
            val table = difficulty.substringAfter("TABLE_").toIntOrNull() ?: 7
            for (i in 1..10) {
                combinationPool.add(table to i)
            }
        } else {
            val range = when(difficulty) {
                "EASY" -> 1..5
                "MEDIUM" -> 6..10
                "HARD" -> 11..15
                else -> 2..9
            }
            // Add all combinations within the range x 1-10
            for (t in range) {
                for (i in 1..10) {
                    combinationPool.add(t to i)
                }
            }
        }
        combinationPool.shuffle()
    }

    override fun generateQuestion(difficulty: String): GameQuestion {
        if (difficulty != lastDifficulty || combinationPool.isEmpty()) {
            lastDifficulty = difficulty
            refreshPool(difficulty)
        }

        val pair = combinationPool.removeAt(0)
        val table = pair.first
        val operand = pair.second

        val correctAnswer = table * operand
        val questionText = "$table x $operand = ?"
        
        // Generate options
        val options = mutableSetOf<String>()
        options.add(correctAnswer.toString())
        
        while (options.size < 4) {
            // Generate plausible distractors
            // +/- table value, or random close
            val type = Random.nextInt(3)
            val fake = when(type) {
                0 -> correctAnswer + table
                1 -> correctAnswer - table
                else -> correctAnswer + Random.nextInt(-5, 6)
            }
            if (fake > 0 && fake != correctAnswer) {
                options.add(fake.toString())
            }
        }

        return GameQuestion(
            id = Random.nextInt(),
            displayContent = questionText,
            answer = correctAnswer.toString(),
            options = options.shuffled().toList()
        )
    }

    override fun checkAnswer(question: GameQuestion, answer: String): Boolean {
        return question.answer == answer
    }

    override fun getDurationSeconds(): Int {
        // Shorter time per question for multiplication
        return 60 // Base for calculation
    }

    override fun getGameType(): String {
        return "MULTIPLICATION"
    }

    override fun getTargetQuestionCount(): Int {
        return 10 // Default, though ViewModel often overrides this
    }
}
