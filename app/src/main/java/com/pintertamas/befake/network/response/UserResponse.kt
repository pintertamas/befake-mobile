package com.pintertamas.befake.network.response

data class UserResponse(
    var id: String,
    var username: String,
    var password: String,
    var fullName: String?,
    var email: String,
    var biography: String?,
    var location: String?,
    var profilePicture: String?,
    var registrationDate: String
)
