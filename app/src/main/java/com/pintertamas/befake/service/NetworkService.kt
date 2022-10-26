package com.pintertamas.befake.service

import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.response.JwtResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface NetworkService {

    @POST("/auth/login")
    suspend fun login(
        //@Header("Authorization") token: String,
        @Body jwtRequest: JwtRequest
    ) : JwtResponse
}