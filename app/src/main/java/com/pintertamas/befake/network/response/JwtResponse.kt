package com.pintertamas.befake.network.response

import com.google.gson.annotations.SerializedName

data class JwtResponse(
    @SerializedName("userId")
    var userId: Long?,

    @SerializedName("username")
    var username: String?,

    @SerializedName("email")
    var email: String?,

    @SerializedName("jwtToken")
    var jwtToken: String?
)