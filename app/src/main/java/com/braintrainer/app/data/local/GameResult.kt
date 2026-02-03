package com.braintrainer.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@com.google.firebase.firestore.IgnoreExtraProperties
@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = 0, // Timestamp
    val gameType: String = "", // "CALCULATION", "REFLEX", "DAILY_TEST", etc.
    val score: Int = 0, // Points or correctness percentage
    val durationSeconds: Int = 0,
    val grade: String = "", // "A", "B", "C"...
    val brainAge: Int? = null // Only for Daily Test
)
