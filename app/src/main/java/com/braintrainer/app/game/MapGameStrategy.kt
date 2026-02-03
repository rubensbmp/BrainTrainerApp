package com.braintrainer.app.game

import kotlin.random.Random

class MapGameStrategy : GameStrategy {
    override fun getGameType() = "MAP_QUIZ"
    override fun getDurationSeconds() = 60
    override fun getTargetQuestionCount() = 15
    
    // Sharing usage tracking concept
    private val usedCodes = mutableSetOf<String>()

    override fun generateQuestion(difficulty: String): GameQuestion {
        // Input format expected: "REGION|DIFFICULTY" e.g. "AMERICAS|HARD"
        val parts = difficulty.split("|")
        val region = parts[0]
        val difficultyLevel = if (parts.size > 1) parts[1] else "MEDIUM"
        
        // 1. Filter by Region
        val regionPool = if (region == "WORLD") {
            CountryData.allCountries
        } else {
            CountryData.allCountries.filter { it.continent == region }
        }
        
        // 2. Filter by Difficulty (Reuse logic)
        val maxTier = when(difficultyLevel) {
            "EASY" -> 1
            "HARD" -> 3
            else -> 2 
        }
        
        // Strategy: We need countries that HAVE a map resource.
        // We will try finding a valid one from candidates.
        
        // Prioritize by tier, but fallback if no maps found.
        val difficultyPool = regionPool.filter { it.difficulty <= maxTier }
        
        // Combine unused-check with map-existence-check
        // We can't check ALL maps efficiently if list is huge without context, but reflection is fast enough for ~200 items or just check on demand.
        
        // Get generic candidates first (logic from FlagGame)
        var candidates = difficultyPool.filter { !usedCodes.contains(it.code) }
        
        if (candidates.isEmpty()) {
            candidates = regionPool.filter { !usedCodes.contains(it.code) }
        }
        if (candidates.isEmpty()) {
             candidates = difficultyPool // repeats allowed
        }
        if (candidates.isEmpty()) {
             candidates = regionPool
        }
        
        // Shuffle candidates to pick random
        val shuffled = candidates.shuffled()
        
        var selectedCountry: Country? = null
        var selectedResId: Int = 0
        
        // Find one with a map
        for (c in shuffled) {
            val resName = "map_" + c.code.lowercase()
            try {
                // Dynamic Resource Lookup via Reflection on R class
                val field = com.braintrainer.app.R.drawable::class.java.getField(resName)
                val resId = field.getInt(null)
                if (resId != 0) {
                    selectedCountry = c
                    selectedResId = resId
                    break
                }
            } catch (e: Exception) {
                // Map not found for this country, skip
                continue
            }
        }
        
        // Fallback if NO country in the pool has a map (Unlikely unless pure text fallback?)
        if (selectedCountry == null) {
            // Picking a known safe one as emergency fallback to avoid crash/empty
            // Assuming Australia exists
            val fallback = CountryData.allCountries.first { it.code == "AU" }
             try {
                val field = com.braintrainer.app.R.drawable::class.java.getField("map_au")
                selectedResId = field.getInt(null)
                selectedCountry = fallback
            } catch (e: Exception) {
                 // Should Not Happen
                 selectedCountry = fallback
                 selectedResId = com.braintrainer.app.R.drawable.ic_game_map
            }
        }
        
        usedCodes.add(selectedCountry!!.code)
        
        // Distractors
        val options = mutableSetOf<String>()
        options.add(selectedCountry.getDisplayName())
        while (options.size < 4) {
             val fake = CountryData.allCountries.random()
             if (fake.name != selectedCountry.name) options.add(fake.getDisplayName())
        }
        
        return GameQuestion(
            id = Random.nextInt(),
            displayContent = "", // Image only
            options = options.toList().shuffled(),
            answer = selectedCountry.getDisplayName(),
            imageRes = selectedResId
        )
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }

    override fun getMaxQuestions(difficulty: String): Int {
        val parts = difficulty.split("|")
        val region = parts[0]
        
        val pool = if (region == "WORLD") {
            CountryData.allCountries
        } else {
            CountryData.allCountries.filter { it.continent == region }
        }
        
        // Count valid maps
        var validCount = 0
        for (c in pool) {
             val resName = "map_" + c.code.lowercase()
             try {
                 val field = com.braintrainer.app.R.drawable::class.java.getField(resName)
                 if (field.getInt(null) != 0) {
                     validCount++
                 }
             } catch (e: Exception) {
                 // ignore
             }
        }
        return if (validCount > 0) validCount else 1 // Return 1 to avoid 0 rounds
    }
}
