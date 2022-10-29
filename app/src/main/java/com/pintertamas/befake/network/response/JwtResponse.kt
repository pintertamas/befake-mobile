package com.pintertamas.befake.network.response

import com.squareup.moshi.Json

data class JwtResponse(
    @Json(name = "userId")
    var userId: Long?,

    @Json(name = "username")
    var username: String?,

    @Json(name = "email")
    var email: String?,

    @Json(name = "jwtToken")
    var jwt: String?
)