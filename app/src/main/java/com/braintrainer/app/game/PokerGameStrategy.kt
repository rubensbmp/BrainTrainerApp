package com.braintrainer.app.game

import kotlin.random.Random

class PokerGameStrategy : GameStrategy {
    override fun getGameType() = "POKER_HAND"
    override fun getDurationSeconds() = 120
    override fun getTargetQuestionCount() = 10

    // Card representation: "RankSuit" e.g., "AS" (Ace Spades), "TD" (Ten Diamonds), "2H" (Two Hearts)
    // Ranks: 2-9, T, J, Q, K, A
    // Suits: S, H, D, C (Spades, Hearts, Diamonds, Clubs)

    private val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")
    private val suits = listOf("S", "H", "D", "C")
    
    // Ordered Hand Types
    private val handTypes = listOf(
        "Royal Flush", 
        "Straight Flush", 
        "Four of a Kind", 
        "Full House", 
        "Flush", 
        "Straight", 
        "Three of a Kind", 
        "Two Pair", 
        "Pair", 
        "High Card"
    )

    // Helper class for Hand Evaluation
    data class Card(val rank: Int, val suit: String, val raw: String, val originalIndex: Int) : Comparable<Card> {
        override fun compareTo(other: Card): Int = this.rank.compareTo(other.rank)
    }

    private fun parseCard(raw: String, index: Int): Card {
        val rChar = raw.substring(0, 1) 
        val rankScore = when(rChar) {
            "2" -> 2; "3" -> 3; "4" -> 4; "5" -> 5; "6" -> 6; "7" -> 7; "8" -> 8; "9" -> 9
            "T" -> 10; "J" -> 11; "Q" -> 12; "K" -> 13; "A" -> 14
            else -> 0
        }
        val suit = raw.substring(1, 2)
        return Card(rankScore, suit, raw, index)
    }

    private fun evaluateHand(cards: List<String>): Pair<String, List<Int>> {
        val parsed = cards.mapIndexed { index, s -> parseCard(s, index) }.sortedByDescending { it.rank }
        
        // Count frequencies
        val rankMap = parsed.groupBy { it.rank }
        val suitMap = parsed.groupBy { it.suit }
        
        val flushSuit = suitMap.entries.firstOrNull { it.value.size >= 5 }?.key
        val isFlush = flushSuit != null
        val flushCards = if (isFlush) parsed.filter { it.suit == flushSuit }.sortedByDescending { it.rank } else emptyList()
        
        // Helper to find straight
        fun getStraightIndices(cardList: List<Card>): List<Int>? {
             val uniqueRanks = cardList.map { it.rank }.distinct().sortedDescending()
             if (uniqueRanks.size < 5) return null
             
             // Check normal straight
             for (i in 0..uniqueRanks.size - 5) {
                 if (uniqueRanks[i] - uniqueRanks[i+4] == 4) {
                     val straightRanks = uniqueRanks.subList(i, i+5)
                     val resultIndices = mutableListOf<Int>()
                     for (r in straightRanks) {
                         resultIndices.add(cardList.first { it.rank == r }.originalIndex)
                     }
                     return resultIndices
                 }
             }
             // Check Wheel (A, 5, 4, 3, 2)
             if (uniqueRanks.contains(14) && uniqueRanks.contains(2) && uniqueRanks.contains(3) && uniqueRanks.contains(4) && uniqueRanks.contains(5)) {
                 val wheelRanks = listOf(14, 5, 4, 3, 2)
                 val resultIndices = mutableListOf<Int>()
                 for (r in wheelRanks) {
                     resultIndices.add(cardList.first { it.rank == r }.originalIndex)
                 }
                 return resultIndices
             }
             return null
        }

        val straightIndices = getStraightIndices(parsed)
        val isStraight = straightIndices != null
        
        // Check Straight Flush
        if (isFlush) {
             val sfIndices = getStraightIndices(flushCards)
             if (sfIndices != null) {
                 // Is it Royal?
                 val highestInSF = parsed.filter { sfIndices.contains(it.originalIndex) }.maxOf { it.rank }
                 // Need to check strict Royal (A-K-Q-J-10)
                 if (highestInSF == 14 && !sfIndices.any { parsed.find { p -> p.originalIndex == it }?.rank == 2 }) { 
                     return "Royal Flush" to sfIndices
                 }
                 return "Straight Flush" to sfIndices
             }
        }
        
        // Four of a Kind
        val fourKindEntry = rankMap.entries.firstOrNull { it.value.size == 4 }
        if (fourKindEntry != null) {
            // Strict: Only the 4 cards
            val indices = fourKindEntry.value.map { it.originalIndex }
            return "Four of a Kind" to indices
        }
        
        // Full House
        val threeKinds = rankMap.filter { it.value.size >= 3 }.keys.sortedDescending()
        val twoKinds = rankMap.filter { it.value.size >= 2 }.keys.sortedDescending()
        
        if (threeKinds.isNotEmpty()) {
             val mainThreeRank = threeKinds[0]
             val pairRank = twoKinds.firstOrNull { it != mainThreeRank }
             
             if (pairRank != null) {
                 val indices = mutableListOf<Int>()
                 indices.addAll(rankMap[mainThreeRank]!!.take(3).map { it.originalIndex })
                 indices.addAll(rankMap[pairRank]!!.take(2).map { it.originalIndex })
                 return "Full House" to indices
             }
        }
        
        if (isFlush) {
            return "Flush" to flushCards.take(5).map { it.originalIndex }
        }
        
        if (isStraight) {
            return "Straight" to straightIndices!!
        }
        
        if (threeKinds.isNotEmpty()) {
            val rank = threeKinds[0]
            val indices = rankMap[rank]!!.take(3).map { it.originalIndex }
            return "Three of a Kind" to indices
        }
        
        // Two Pair
        val pairs = rankMap.filter { it.value.size >= 2 }.keys.sortedDescending()
        if (pairs.size >= 2) {
            val highPair = pairs[0]
            val lowPair = pairs[1]
            val indices = mutableListOf<Int>()
            indices.addAll(rankMap[highPair]!!.take(2).map { it.originalIndex })
            indices.addAll(rankMap[lowPair]!!.take(2).map { it.originalIndex })
            return "Two Pair" to indices
        }
        
        if (pairs.size == 1) {
            val rank = pairs[0]
            val indices = rankMap[rank]!!.take(2).map { it.originalIndex }
            return "Pair" to indices
        }
        
        // High Card (Strict: Only the 1 highest card)
        return "High Card" to parsed.take(1).map { it.originalIndex }
    }

    override fun generateQuestion(difficulty: String): GameQuestion {
        // Generate full deck
        val deck = ArrayList<String>()
        for (r in ranks) {
            for (s in suits) {
                deck.add("$r$s")
            }
        }
        
        var attempts = 0
        while (true) {
            deck.shuffle()
            
            // Deal 7 cards (2 Hole + 5 Community)
            val holeCards = deck.take(2)
            val community = deck.subList(2, 7) 
            val all7 = holeCards + community
            
            val eval = evaluateHand(all7)
            val correctHand = eval.first
            val winningIndices = eval.second
            
            // CUSTOM RULE: Must use at least one hole card (Index 0 or 1)
            val usesHoleCard = winningIndices.any { it == 0 || it == 1 }
            
            // If valid or too many attempts (to prevent infinite loop), accept
            if (usesHoleCard || attempts > 100) {
                 val indicesStr = winningIndices.joinToString(",")
                 
                 // Generate options: Correct + 3 weaker/incorrect hands
                val optionsSet = mutableSetOf<String>()
                optionsSet.add(correctHand)
                
                while (optionsSet.size < 4) {
                     val fake = handTypes.random()
                     if (fake != correctHand) {
                         optionsSet.add(fake)
                     }
                }
                
                val displayHole = holeCards.joinToString(",")
                val displayFlop = community.subList(0, 3).joinToString(",")
                val displayTurn = community[3]
                val displayRiver = community[4]
                // Winning Indices passed as extra items
                val flashData = listOf(displayHole, displayFlop, displayTurn, displayRiver, "WIN:$indicesStr")
        
                return GameQuestion(
                    id = Random.nextInt(),
                    displayContent = "Qual a melhor mão?", 
                    options = optionsSet.toList().shuffled(),
                    answer = correctHand,
                    flashItems = flashData
                )
            }
            attempts++
        }
    }

    override fun checkAnswer(question: GameQuestion, input: String): Boolean {
        return input == question.answer
    }
}
