package com.pintertamas.befake.network.request

data class UserRequest(
    var username: String,
    var password: String,
    var fullName: String?,
    var email: String,
    var biography: String?,
    var location: String?
)