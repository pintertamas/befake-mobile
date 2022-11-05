package com.pintertamas.befake.network.response

import androidx.room.Entity

@Entity
data class ReactionResponse(
    var id: Long,
    var userId: Long,
    var username: String,
    var postId: Long,
    val imageName: String,
    val reactionTime: String
)