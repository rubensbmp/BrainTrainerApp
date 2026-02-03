package com.braintrainer.app.game

import kotlin.random.Random

class NumberMemoryStrategy : GameStrategy {
    override fun getGameType() = "NUMBER_MEMORY"
    override fun getDurationSeconds() = 60
    override fun getTargetQuestionCount() = 10

    override fun generateQuestion(difficulty: String): GameQuestion {
        val digitCount = when(difficulty) {
            "EASY" -> 4
            "HARD" -> 8
            else -> 6 // Medium
        }
        
        // Generate random number
        val sb = StringBuilder()
        repeat(digitCount) {
             sb.append(Random.nextInt(0, 10))
        }
        val correct = sb.toString()
        
        // Generate options
        val options = mutableSetOf<String>()
        options.add(correct)
        
        while(options.size < 4) {
            val type = Random.nextInt(3)
            val distractor = when(type) {
                0 -> changeDigits(correct, 1) // Change one digit
                1 -> changeDigits(correct, 2) // Change two digits
                else -> swapAdjacentDigits(correct) // Swap adjacent
            }
            if (distractor != correct) options.add(distractor)
        }
        
        return GameQuestion(
            id = Random.nextInt(),
            displayContent = "Qual era o número?", // Shown AFTER flash
            flashItems = listOf(correct), // Shown DURING flash
            options = options.toList().shuffled(),
            answer = correct
        )
    }

    private fun changeDigits(input: String, count: Int): String {
        val chars = input.toCharArray()
        val indices = (0 until chars.size).shuffled().take(count)
        indices.forEach { index ->
            var newDigit = Random.nextInt(10).digitToChar()
            while (newDigit == chars[index]) {
                newDigit = Random.nextInt(10).digitToChar()
            }
            chars[index] = newDigit
        }
        return String(chars)
    }

    private fun swapAdjacentDigits(input: String): String {
        if (input.length < 2) return input
        val chars = input.toCharArray()
        val index = Random.nextInt(chars.size - 1)
        val temp = chars[index]
        chars[index] = chars[index+1]
        chars[index+1] = temp
        return String(chars)
    }


    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}
