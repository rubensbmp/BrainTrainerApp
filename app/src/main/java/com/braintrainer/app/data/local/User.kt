package com.braintrainer.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val birthDate: Long, // Timestamp
    val avatarId: Int = 0, // Resource ID or Index
    val uid: String = "" // Firebase UID
)
