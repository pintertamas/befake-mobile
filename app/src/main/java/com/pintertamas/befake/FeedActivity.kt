package com.pintertamas.befake

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.ActivityFeedBinding
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private var user: UserResponse? = null
    private var profilePicture: String = ""

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

        Constants.showSuccessSnackbar(this, layoutInflater, "Successful login!")

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
            "Successfully queried user: $responseBody Status code: $statusCode"
        )
        user = responseBody
        if (user!!.profilePicture != null) {
            getProfilePictureUrl(user!!.id.toLong())
        }
    }

    private fun getUserError(statusCode: Int, e: Throwable) {
        Log.e("GET_USER", "Error $statusCode during user query!")
        e.printStackTrace()
    }

    private fun getProfilePictureUrl(userId: Long) {
        network.getProfilePictureUrl(
            userId = userId,
            onSuccess = this::getImageUrlSuccess,
            onError = this::getImageUrlError
        )
    }

    /*private fun getImageUrl(filename: String) {
        network.getImageUrl(
            filename = filename,
            onSuccess = this::getImageUrlSuccess,
            onError = this::getImageUrlError
        )
    }*/

    private fun getImageUrlSuccess(statusCode: Int, responseBody: ResponseBody) {
        Log.d(
            "GET_IMAGE_URL",
            "Successfully got image url: $responseBody Status code: $statusCode"
        )
        profilePicture = responseBody.string()
        Picasso.get().load(profilePicture).into(binding.btnProfile)
    }

    private fun getImageUrlError(statusCode: Int, e: Throwable) {
        Log.e("GET_IMAGE_URL", "Error $statusCode during image download!")
        e.printStackTrace()
    }
}