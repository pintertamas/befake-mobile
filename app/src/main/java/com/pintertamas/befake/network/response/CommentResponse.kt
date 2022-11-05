package com.pintertamas.befake.network.response

import androidx.room.Entity

@Entity
data class CommentResponse(
    var id: Long,
    var userId: Long,
    var username: String,
    var postId: Long,
    val text: String,
    val commentTime: String
)