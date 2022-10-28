package com.pintertamas.befake.network.service

import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface NetworkService {

    companion object {
        const val ENDPOINT_URL = "http://192.168.0.34:8765"

        const val MULTIPART_FORM_DATA = "multipart/form-data"
        const val PHOTO_MULTIPART_KEY_IMG = "image"
    }

    @POST("/auth/login")
    fun login(
        //@Header("Authorization") token: String,
        @Body jwtRequest: JwtRequest
    ) : Call<JwtResponse>

    @POST("/user/register")
    fun register(
        @Body userRequest: UserRequest
    ) : Call<UserResponse>
}