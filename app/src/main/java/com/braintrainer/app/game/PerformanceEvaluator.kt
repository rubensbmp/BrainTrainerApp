package com.braintrainer.app.game

object PerformanceEvaluator {
    fun evaluate(correct: Int, total: Int, timeSeconds: Int): EvaluationResult {
        if (total == 0) return EvaluationResult("F", "Sem dados", 0)

        val percentage = (correct.toFloat() / total.toFloat()) * 100
        
        val grade = when {
            percentage >= 95 -> "A+"
            percentage >= 90 -> "A"
            percentage >= 85 -> "B+"
            percentage >= 80 -> "B"
            percentage >= 70 -> "C+"
            percentage >= 60 -> "C"
            percentage >= 40 -> "D"
            else -> "E"
        }

        val feedback = when (grade) {
            "A+", "A" -> "FEEDBACK_EXCELLENT"
            "B+", "B" -> "FEEDBACK_GOOD"
            "C+", "C" -> "FEEDBACK_AVERAGE"
            "D" -> "FEEDBACK_POOR"
            else -> "FEEDBACK_BAD"
        }
        
        // Stricter Brain Age Logic
        // Base age = 20 (Ideal)
        var age = 20
        
        // Penalty for Accuracy < 100%
        // Each mistake adds 3 years
        val mistakes = total - correct
        age += (mistakes * 3)

        // Penalty for speed
        // Ideal speed: < 1.0 second per question (Strict)
        val avgTime = if (correct > 0) timeSeconds.toFloat() / correct else 10f
        val threshold = 1.0f
        
        if (avgTime > threshold) {
            // For every second slower than threshold, add 5 years
            val slowness = avgTime - threshold
            age += (slowness * 5).toInt()
        }
        
        if (age > 80) age = 80
        if (age < 20) age = 20

        return EvaluationResult(grade, feedback, age)
    }

    data class EvaluationResult(
        val grade: String,
        val feedback: String,
        val brainAge: Int
    )
}
