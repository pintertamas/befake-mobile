package com.pintertamas.befake.network.service

import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.sql.Timestamp

interface NetworkService {

    companion object {
        const val ENDPOINT_URL = "http://192.168.0.34:8765"

        const val MULTIPART_FORM_DATA = "multipart/form-data"
        const val PHOTO_MULTIPART_KEY_IMG = "image"
    }

    @POST("/auth/login")
    fun login(
        @Body jwtRequest: JwtRequest
    ): Call<JwtResponse>

    @POST("/user/register")
    fun register(
        @Body userRequest: UserRequest
    ): Call<UserResponse>

    @GET("/user")
    fun getUser(
        @Query("userId") userId: Long
    ): Call<UserResponse>

    @GET("/user/profile-picture/{userId}")
    fun getProfilePictureUrl(
        @Path("userId") userId: Long
    ): Call<ResponseBody>

    @GET("/post/{filename}")
    fun getImageUrl(
        @Path("filename") filename: String
    ): Call<ResponseBody>

    @GET("/post/user-can-post")
    fun canUserPost(
    ): Call<Boolean>

    @GET("/post/friends")
    fun getPostsFromFriends(
    ): Call<List<PostResponse>>

    @GET("/post/today/{userId}")
    fun getTodaysPostByUser(
        @Path("userId") userId: Long
    ): Call<PostResponse>

    @GET("/time/last-befake-time")
    fun getBeFakeTime(
    ): Call<String>
}