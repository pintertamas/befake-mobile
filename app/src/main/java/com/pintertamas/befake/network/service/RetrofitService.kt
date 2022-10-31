package com.pintertamas.befake.network.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.moshi.MoshiConverterFactory
import java.sql.Date
import java.sql.Timestamp
import kotlin.concurrent.thread


class RetrofitService() {

    private val networkService: NetworkService
    private var token: String = ""

    constructor(token: String) : this() {
        this.token = token
    }


    init {
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }).build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(NetworkService.ENDPOINT_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        this.networkService = retrofit.create(NetworkService::class.java)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (Int, T) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        Log.d("RETROFIT_SERVICE", call.request().toString())
        val handler = Handler(Looper.getMainLooper()!!)
        thread {
            var response: Response<T>? = null
            try {
                response = call.execute()
                Log.d("RESPONSE_CODE", response.code().toString())
                if (response == null || response.code() != 200) throw Exception()
                val responseBody = response.body()!!
                handler.post { onSuccess(response.code(), responseBody) }
            } catch (e: Exception) {
                val code: Int = response?.code() ?: 500
                e.printStackTrace()
                handler.post { onError(code, e) }
            }
        }
    }

    private fun <T> runCallSynchronously(
        call: Call<T>,
        onSuccess: (Int, T) -> Unit,
        onError: (Int, Throwable) -> Unit
    ): T? {
        Log.d("RETROFIT_SERVICE", call.request().toString())
        var response: Response<T>? = null
        return try {
            response = call.execute()
            Log.d("RESPONSE_CODE", response.code().toString())
            if (response == null || response.code() != 200) throw Exception()
            val responseBody = response.body()!!
            onSuccess(response.code(), responseBody)
            responseBody
        } catch (e: Exception) {
            val code: Int = response?.code() ?: 500
            e.printStackTrace()
            onError(code, e)
            null
        }
    }

    fun login(
        username: String,
        password: String,
        onSuccess: (Int, JwtResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
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
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
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

    fun getUser(
        userId: Long,
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getUserRequest = networkService.getUser(userId)
        runCallOnBackgroundThread(getUserRequest, onSuccess, onError)
    }

    fun getProfilePictureUrl(
        userId: Long,
        onSuccess: (Int, ResponseBody) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getProfilePictureUrlRequest = networkService.getProfilePictureUrl(userId)
        runCallOnBackgroundThread(getProfilePictureUrlRequest, onSuccess, onError)
    }

    fun canUserPost(
        onSuccess: (Int, Boolean) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getLastPostByUserRequest = networkService.canUserPost()
        runCallOnBackgroundThread(getLastPostByUserRequest, onSuccess, onError)
    }

    fun getBeFakeTime(
        onSuccess: (Int, String) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getBeFakeTimeRequest = networkService.getBeFakeTime()
        runCallOnBackgroundThread(getBeFakeTimeRequest, onSuccess, onError)
    }
}