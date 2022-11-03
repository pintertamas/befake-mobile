package com.pintertamas.befake

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.ActivityFeedBinding
import com.pintertamas.befake.fragment.ListPostsFragment
import com.pintertamas.befake.fragment.NewPostFragment
import com.pintertamas.befake.fragment.ProfileFragment
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody


class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var picasso: Picasso
    private lateinit var user: UserResponse
    private var profilePicture: String? = null
    private var canUserPost: Boolean = false
    private var beFakeTime: String? = null

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        picasso = Picasso.Builder(this).build()
        picasso.setIndicatorsEnabled(true)

        val token = sharedPreferences.getString("jwt", "")
        val userId = sharedPreferences.getLong("userId", 0)
        network = RetrofitService(token!!)
        getUser(userId)
        getProfilePictureUrl(userId)
        canUserPost()

        binding.toolbar.visibility = View.VISIBLE

        Constants.showSuccessSnackbar(this, layoutInflater, "Successful login!")
    }

    private fun addFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container_view, fragment)
        fragmentTransaction.addToBackStack(fragment.id.toString())
        fragmentTransaction.commit()
    }

    private fun addFullscreenFragment(fragment: Fragment) {
        binding.toolbar.visibility = View.GONE
        addFragment(fragment)
    }

    private fun getUser(userId: Long) {
        network.getUser(
            userId = userId,
            onSuccess = this::getUserSuccess,
            onError = this::genericError,
        )
    }

    private fun getUserSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "GET_USER",
            "Successfully queried user: $responseBody Status code: $statusCode"
        )
        user = responseBody
        binding.btnProfile.isClickable = true
        binding.btnProfile.setOnClickListener {
            addFullscreenFragment(ProfileFragment.newInstance(user))
        }
    }

    private fun getProfilePictureUrl(userId: Long) {
        network.getProfilePictureUrl(
            userId = userId,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun getImageUrlSuccess(statusCode: Int, responseBody: ResponseBody) {
        Log.d(
            "GET_IMAGE_URL",
            "Successfully got image url: $responseBody Status code: $statusCode"
        )
        profilePicture = responseBody.string()
        println(profilePicture)
        if (profilePicture != null) {
            picasso.load(profilePicture).into(binding.btnProfile)
        }
    }

    private fun canUserPost() {
        network.canUserPost(
            onSuccess = this::canUserPostSuccess,
            onError = this::genericError
        )
    }

    private fun canUserPostSuccess(statusCode: Int, responseBody: Boolean) {
        Log.d(
            "CAN_USER_POST",
            "Successful canUserPost call. $responseBody Status code: $statusCode"
        )
        canUserPost = responseBody
        val fragment: Fragment =
            if (canUserPost) NewPostFragment.newInstance() else ListPostsFragment.newInstance(user)
        addFragment(fragment)
    }

    fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
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