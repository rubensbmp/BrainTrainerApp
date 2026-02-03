package com.braintrainer.app.game

import kotlin.random.Random

// Represents a single question/problem in a game
data class GameQuestion(
    val id: Int,
    val displayContent: String, // Question text ex: "5 + 3 = ?"
    val options: List<String> = emptyList(), // For Multiple Choice
    val correctHeader: String? = null, // Extra context
    val answer: String,
    val flashItems: List<String> = emptyList(), // Items to flash before showing options
    val imageRes: Int? = null // For Map game
)

interface GameStrategy {
    fun getGameType(): String
    fun getCurrentGameType(): String = getGameType()
    fun generateQuestion(difficulty: String): GameQuestion
    fun checkAnswer(question: GameQuestion, input: String): Boolean
    fun getDurationSeconds(): Int 
    fun getTargetQuestionCount(): Int
    fun getMaxQuestions(difficulty: String): Int = Int.MAX_VALUE
}

class CalculationGameStrategy : GameStrategy {
    override fun getGameType() = "CALCULATION"
    override fun getDurationSeconds() = 60
    override fun getTargetQuestionCount() = 10

    // Helper to calculate result of simple expression: a op1 b (op2 c)
    private fun calculate(a: Int, op1: String, b: Int, op2: String? = null, c: Int? = null): Int {
        // Simple 2 terms
        if (op2 == null || c == null) {
            return when(op1) {
                "+" -> a + b
                "-" -> a - b
                "×" -> a * b
                "÷" -> if (b != 0) a / b else 0
                else -> 0
            }
        }
        
        // 3 terms: handle precedence (* and / first)
        val isOp1High = op1 == "×" || op1 == "÷"
        val isOp2High = op2 == "×" || op2 == "÷"

        return if (isOp1High && !isOp2High) {
            // (a op1 b) op2 c
            val res1 = when(op1) {
                "×" -> a * b
                "÷" -> if (b != 0) a / b else 0
                else -> 0
            }
            when(op2) {
                "+" -> res1 + c
                "-" -> res1 - c
                else -> res1
            }
        } else if (!isOp1High && isOp2High) {
            // a op1 (b op2 c)
            val res2 = when(op2) {
                "×" -> b * c
                "÷" -> if (c != 0) b / c else 0
                else -> 0
            }
            when(op1) {
                "+" -> a + res2
                "-" -> a - res2
                else -> a
            }
        } else {
            // Both high or both low -> left to right
            val res1 = when(op1) {
                "+" -> a + b
                "-" -> a - b
                "×" -> a * b
                "÷" -> if (b != 0) a / b else 0
                else -> 0
            }
            when(op2) {
                "+" -> res1 + c
                "-" -> res1 - c
                "×" -> res1 * c
                "÷" -> if (c != 0) res1 / c else 0
                else -> res1
            }
        }
    }

    override fun generateQuestion(difficulty: String): GameQuestion {
        // Difficulty controls range of numbers
        val range = when(difficulty) {
            // Keep ranges reasonable to help find a solution under 30 quickly
            "HARD" -> 1..30 
            "MEDIUM" -> 1..20
            else -> 1..15
        }
        
        // Increased weight for Multiplication and Division (3x more likely)
        val ops = if (difficulty != "EASY") {
            listOf("+", "-", "×", "×", "×", "÷", "÷", "÷")
        } else {
             listOf("+", "-", "×", "÷")
        }
        val isHard = difficulty == "HARD"
        
        var display = ""
        var ans = -999
        
        // Loop until valid result [0, 50] is found (increased range slightly for more variety)
        while (ans < 0 || ans > 50) {
            val a = Random.nextInt(range.first, range.last)
            val b = Random.nextInt(range.first, range.last)
            val op1 = ops.random()
            
            // For division, ensure it's an integer and b != 0
            if (op1 == "÷") {
                if (b == 0 || a % b != 0) continue
            }

            if (isHard) {
                // Heuristic: If multiplying, keep numbers small
                if (op1 == "×" && (a > 10 || b > 10)) continue
                
                val c = Random.nextInt(range.first, range.last)
                val op2 = ops.random()
                
                if (op2 == "÷") {
                    // Complexity here: handle precedence. 
                    // This is simplified but valid for 3 terms.
                    if (c == 0) continue
                    if (op1 != "×" && op1 != "÷") {
                        // a + (b / c)
                        if (b % c != 0) continue
                    } else {
                        // (a * b) / c or (a / b) / c
                        val firstRes = if (op1 == "×") a * b else if (b != 0 && a % b == 0) a / b else -1
                        if (firstRes == -1 || firstRes % c != 0) continue
                    }
                }

                if (op2 == "×" && c > 10) continue

                ans = calculate(a, op1, b, op2, c)
                display = "$a $op1 $b $op2 $c = ?"
            } else {
                ans = calculate(a, op1, b)
                display = "$a $op1 $b = ?"
            }
        }

        val options = mutableSetOf<String>()
        options.add(ans.toString())
        while (options.size < 4) {
            val offset = Random.nextInt(-10, 11)
            val fake = ans + offset
            // Ensure fake options are also positive/reasonable if possible, but not strictly bound to 30
            // but for UI consistency let's keep them >= 0
            if (fake >= 0 && fake != ans) {
                options.add(fake.toString())
            }
        }
        
        return GameQuestion(
            id = Random.nextInt(),
            displayContent = display,
            options = options.toList().shuffled(),
            answer = ans.toString()
        )
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input.trim() == question.answer
    }
}

class GreatestNumberGameStrategy : GameStrategy {
    // ... (No changes to GreatestNumber)
    override fun getGameType() = "REFLEX_GREATEST"
    override fun getDurationSeconds() = 45
    override fun getTargetQuestionCount() = 20

    override fun generateQuestion(difficulty: String): GameQuestion {
        // Flash always 4 numbers as per user request
        val range = if (difficulty == "HARD") 1..200 else 1..100
        
        val flashNumbers = mutableSetOf<Int>()
        while (flashNumbers.size < 4) {
            flashNumbers.add(Random.nextInt(range.first, range.last))
        }
        val flashList = flashNumbers.toList().shuffled()
        val max = flashList.maxOrNull() ?: 0

        // Answer Options: The correct Max + 3 Random distractors
        val optionsSet = mutableSetOf<Int>()
        optionsSet.add(max)
        
        while (optionsSet.size < 4) {
            val fake = Random.nextInt(range.first, range.last)
            if (fake != max) {
                optionsSet.add(fake)
            }
        }
        val optionsList = optionsSet.toList().shuffled()

        return GameQuestion(
            id = Random.nextInt(),
            displayContent = "Qual o maior?", // Text shown during input phase if needed
            options = optionsList.map { it.toString() },
            answer = max.toString(),
            flashItems = flashList.map { it.toString() }
        )
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}

class MissingSymbolGameStrategy : GameStrategy {
    override fun getGameType() = "LOGIC_SYMBOL"
    override fun getDurationSeconds() = 60
    override fun getTargetQuestionCount() = 15
    
    // Reuse calculation helper from CalculationStrategy if moved to companion or util, 
    // but duplicating for safety and speed now.
    private fun calculate(a: Int, op1: String, b: Int, op2: String? = null, c: Int? = null): Int {
         if (op2 == null || c == null) {
            return when(op1) {
                "+" -> a + b
                "-" -> a - b
                "×" -> a * b
                "÷" -> if (b != 0) a / b else 0
                else -> 0
            }
        }
        val isOp1High = op1 == "×" || op1 == "÷"
        val isOp2High = op2 == "×" || op2 == "÷"

        return if (isOp1High && !isOp2High) {
            val res1 = when(op1) {
                "×" -> a * b
                "÷" -> if (b != 0) a / b else 0
                else -> 0
            }
            when(op2) {
                "+" -> res1 + c
                "-" -> res1 - c
                else -> res1
            }
        } else if (!isOp1High && isOp2High) {
             val res2 = when(op2) {
                "×" -> b * c
                "÷" -> if (c != 0) b / c else 0
                else -> 0
            }
            when(op1) {
                "+" -> a + res2
                "-" -> a - res2
                else -> a
            }
        } else {
            val res1 = when(op1) {
                "+" -> a + b
                "-" -> a - b
                "×" -> a * b
                "÷" -> if (b != 0) a / b else 0
                else -> 0
            }
            return when(op2) {
                "+" -> res1 + c
                "-" -> res1 - c
                "×" -> res1 * c
                "÷" -> if (c != 0) res1 / c else 0
                else -> res1
            }
        }
    }

    override fun generateQuestion(difficulty: String): GameQuestion {
        // More weight to Multiplication and Division
        val opsRaw = listOf("+", "-", "×", "×", "÷", "÷")
        val isHard = difficulty == "HARD"
        // Reduce range to facilitate finding results <= 50
        val range = 1..20 
        
        var display = ""
        var answerKey = ""
        var options = mutableListOf<String>()
        var result = -999
        
        while (result < 0 || result > 50) {
            val a = Random.nextInt(range.first, range.last)
            val b = Random.nextInt(range.first, range.last)
            val op1 = opsRaw.random()
            
            if (op1 == "÷" && (b == 0 || a % b != 0)) continue

            if (isHard) {
                val c = Random.nextInt(range.first, range.last)
                val op2 = opsRaw.random()
                
                // Complexity: ensure integer division
                if (op2 == "÷") {
                    if (c == 0) continue
                    if (op1 != "×" && op1 != "÷") { if (b % c != 0) continue }
                    else {
                        val first = if (op1 == "×") a * b else a / b
                        if (first % c != 0) continue
                    }
                }
                
                result = calculate(a, op1, b, op2, c)
                display = "$a ? $b ? $c = $result" 
                answerKey = "$op1 $op2"
                
                if (result in 0..50) {
                    val validOps = listOf("+", "-", "×", "÷")
                    val distinctOptions = mutableSetOf<String>()
                    distinctOptions.add(answerKey)
                    
                    while (distinctOptions.size < 4) {
                        val f1 = validOps.random()
                        val f2 = validOps.random()
                        val fake = "$f1 $f2"
                        distinctOptions.add(fake)
                    }
                    options = distinctOptions.toMutableList()
                }
                
            } else {
                result = calculate(a, op1, b)
                
                // Ambiguity Check: Ensure no other operator produces the same result
                var ambiguous = false
                val checkOps = listOf("+", "-", "×", "÷")
                for (otherOp in checkOps) {
                    if (otherOp == op1) continue
                    if (otherOp == "÷" && (b == 0 || a % b != 0)) continue
                    
                    val otherRes = calculate(a, otherOp, b)
                    if (otherRes == result) {
                        ambiguous = true
                        break
                    }
                }
                
                if (ambiguous) {
                    result = -999 // Force retry
                    continue
                }

                display = "$a ? $b = $result"
                answerKey = op1
                
                if (result in 0..50) {
                    options = mutableListOf("+", "-", "×", "÷")
                    // Ensure answerKey is in options (it should be)
                    if (!options.contains(answerKey)) options.add(answerKey)
                }
            }
        }
        
        return GameQuestion(
            id = Random.nextInt(),
            displayContent = display,
            options = options.shuffled(),
            answer = answerKey
        )
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}
