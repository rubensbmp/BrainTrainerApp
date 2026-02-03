package com.braintrainer.app.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Group(
    val groupId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberCount: Int = 0
)
