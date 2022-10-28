package com.pintertamas.befake.network.service

import android.os.Handler
import android.os.Looper
import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread

class RetrofitService {

    private val networkService: NetworkService

    init {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(NetworkService.ENDPOINT_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        this.networkService = retrofit.create(NetworkService::class.java)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper()!!)
        thread {
            try {
                val response = call.execute().body()!!
                handler.post { onSuccess(response) }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { onError(e) }
            }
        }
    }

    fun login(
        username: String,
        password: String,
        onSuccess: (JwtResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val loginRequest = networkService.login(JwtRequest(username, password))
        runCallOnBackgroundThread(loginRequest, onSuccess, onError)
    }

    fun register(
        username: String,
        password: String,
        fullName: String?,
        email: String,
        bibliography: String?,
        location: String?,
        onSuccess: (UserResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val registerRequest = networkService.register(
            UserRequest(
                username,
                password,
                fullName,
                email,
                bibliography,
                location
            )
        )
        runCallOnBackgroundThread(registerRequest, onSuccess, onError)
    }

}