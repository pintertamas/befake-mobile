package com.pintertamas.befake.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Jwt(
    val userId: Long = 0L,
    val username: String = "",
    val email: String = "",
    val jwtToken: String = ""
) : Parcelable