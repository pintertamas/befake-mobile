package com.pintertamas.befake.network.response

import androidx.room.Entity

@Entity
data class FriendshipResponse(
    var id: Long,
    var user1Id: Long,
    var user2Id: Long,
    var status: String,
    var since: String
)