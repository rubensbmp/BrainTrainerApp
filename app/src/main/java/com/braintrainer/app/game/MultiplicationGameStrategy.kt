package com.braintrainer.app.game

import kotlin.random.Random

class MultiplicationGameStrategy : GameStrategy {
    private var lastCombination: String? = null

    override fun generateQuestion(difficulty: String): GameQuestion {
        var table: Int
        var operand: Int
        var combination: String

        do {
            if (difficulty.startsWith("TABLE_")) {
                // "TABLE_7" -> 7
                table = difficulty.substringAfter("TABLE_").toIntOrNull() ?: 7
                operand = Random.nextInt(1, 11) // 1 to 10
            } else {
                // General difficulty (Daily Test or Random)
                val range = when(difficulty) {
                    "EASY" -> 1..5
                    "MEDIUM" -> 6..10
                    "HARD" -> 11..15
                    else -> 2..9
                }
                table = range.random()
                operand = Random.nextInt(1, 11)
            }
            combination = "$table-x-$operand"
        } while (combination == lastCombination)

        lastCombination = combination

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
