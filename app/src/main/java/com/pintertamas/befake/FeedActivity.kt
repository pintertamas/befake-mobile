package com.pintertamas.befake

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.pintertamas.befake.databinding.ActivityFeedBinding
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import okhttp3.ResponseBody

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var user: UserResponse

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        val userId = sharedPreferences.getLong("userId", 0)
        network = RetrofitService(token!!)
        getUser(userId)



        binding.jwt.text = token.toString()
    }

    private fun getUser(userId: Long) {
        network.getUser(
            userId = userId,
            onSuccess = this::getUserSuccess,
            onError = this::getUserError,
        )
    }

    private fun getUserSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "GET_USER",
            "Successfully queried user: $responseBody"
        )
        user = responseBody
    }

    private fun getUserError(statusCode: Int, e: Throwable) {
        Log.e("GET_USER", "Error $statusCode during user query!")
        e.printStackTrace()
    }

    private fun downloadImage(filename: String) {
        network.downloadImage(
            filename = filename,
            onSuccess = this::downloadImageSuccess,
            onError = this::downloadImageError
        )
    }

    private fun downloadImageSuccess(statusCode: Int, responseBody: ResponseBody) {
        Log.d(
            "DOWNLOAD_IMAGE",
            "Successfully downloaded image: $responseBody"
        )
    }

    private fun downloadImageError(statusCode: Int, e: Throwable) {
        Log.e("DOWNLOAD_IMAGE", "Error $statusCode during image download!")
        e.printStackTrace()
    }
}