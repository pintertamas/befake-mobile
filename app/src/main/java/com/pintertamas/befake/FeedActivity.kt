package com.pintertamas.befake

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentTransaction
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.ActivityFeedBinding
import com.pintertamas.befake.fragment.ListPostsFragment
import com.pintertamas.befake.fragment.NewPostFragment
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import kotlinx.coroutines.awaitAll
import okhttp3.ResponseBody

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private var profilePicture: String = ""
    private var canUserPost: Boolean = false
    private var beFakeTime: String? = null

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
        getProfilePictureUrl(userId)
        canUserPost()

        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

            //transaction.add(R.id.new_post_fragment, NewPostFragment.newInstance(), "NEW_POST")
            transaction.add(R.id.list_posts_fragment, ListPostsFragment.newInstance(), "LIST_POSTS")

            transaction.commit()
        }

        Constants.showSuccessSnackbar(this, layoutInflater, "Successful login!")
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

    private fun canUserPost() {
        network.canUserPost(
            onSuccess = this::canUserPostSuccess,
            onError = this::canUserPostError
        )
    }

    private fun canUserPostSuccess(statusCode: Int, responseBody: Boolean) {
        Log.d(
            "CAN_USER_POST",
            "Successful canUserPost call. $responseBody Status code: $statusCode"
        )
        canUserPost = responseBody
    }

    private fun canUserPostError(statusCode: Int, e: Throwable) {
        Log.e("CAN_USER_POST", "Error $statusCode during getting last post!")
        e.printStackTrace()
    }

    /*private fun getBeFakeTime() {
        network.getBeFakeTime(
            onSuccess = this::getBeFakeTimeSuccess,
            onError = this::getBeFakeTimeError
        )
    }

    private fun getBeFakeTimeSuccess(statusCode: Int, responseBody: String) {
        Log.d(
            "GET_BEFAKE_TIME",
            "Successfully got befake time: $responseBody Status code: $statusCode"
        )
        beFakeTime = responseBody
        binding.befakeTime.text = beFakeTime.toString()
    }

    private fun getBeFakeTimeError(statusCode: Int, e: Throwable) {
        Log.e("GET_BEFAKE_TIME", "Error $statusCode during getting befake time!")
        e.printStackTrace()
    }*/
}