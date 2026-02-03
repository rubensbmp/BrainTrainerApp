package com.braintrainer.app.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val brainAge: Int = 0,
    val totalMatches: Int = 0,
    val avgGrade: String = "F",
    val groupIds: List<String> = emptyList(),
    val avatarId: Int = 1,
    val photoUrl: String = ""
)
