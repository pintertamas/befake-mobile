package com.pintertamas.befake.network.model

import com.google.gson.annotations.SerializedName

class JwtDto(
    @SerializedName("userId")
    var userId: Long,

    @SerializedName("username")
    var username: String,

    @SerializedName("email")
    var email: String,

    @SerializedName("jwtToken")
    var jwtToken: String
)