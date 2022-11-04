package com.pintertamas.befake

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.database.ImageCacheDatabase
import com.pintertamas.befake.databinding.ActivityFeedBinding
import com.pintertamas.befake.fragment.ListPostsFragment
import com.pintertamas.befake.fragment.NewPostFragment
import com.pintertamas.befake.fragment.ProfileFragment
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.fragment.EditProfileFragment
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import java.sql.Timestamp


class FeedActivity : AppCompatActivity(), EditProfileFragment.EditedUserListener {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var cache: CacheService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var picasso: Picasso
    private lateinit var user: UserResponse
    private var canUserPost: Boolean = false
    private var beFakeTime: Timestamp? = null

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
        cache = CacheService.getInstance()!!
        cache.setNetworkService(network)
        cache.initDatabase(
            Room.databaseBuilder(
                applicationContext,
                ImageCacheDatabase::class.java,
                "image-cache"
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        )
        getUser(userId)

        binding.toolbar.visibility = View.VISIBLE

        Constants.showSuccessSnackbar(this, layoutInflater, "Successful login!")
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container_view, fragment, tag)
        fragmentTransaction.addToBackStack(fragment.id.toString())
        fragmentTransaction.commit()
    }

    private fun addFullscreenFragment(fragment: Fragment, tag: String) {
        binding.toolbar.visibility = View.GONE
        addFragment(fragment, tag)
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
        loadProfileData(user)
        canUserPost()
    }

    private fun loadProfileData(user: UserResponse) {
        cache.cacheProfilePicture(user, binding.btnProfile)
        binding.btnProfile.isClickable = true
        binding.btnProfile.setOnClickListener {
            addFullscreenFragment(ProfileFragment.newInstance(user), "PROFILE_FRAGMENT")
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
        val tag: String =
            if (fragment is NewPostFragment) "NEW_POST_FRAGMENT" else "LIST_POST_FRAGMENT"
        addFragment(fragment, tag)
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }

    override fun updateUserDetails(user: UserResponse) {
        loadProfileData(user)
    }
}