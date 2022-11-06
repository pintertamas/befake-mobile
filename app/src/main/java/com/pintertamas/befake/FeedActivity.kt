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
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.fragment.*
import com.pintertamas.befake.network.service.RetrofitService
import java.sql.Timestamp


class FeedActivity : AppCompatActivity(), EditProfileFragment.EditedUserListener {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var cache: CacheService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var user: UserResponse
    private var canUserPost: Boolean = false

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        val userId = sharedPreferences.getLong("userId", -1)
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
        loadUserList()

        binding.toolbar.visibility = View.VISIBLE

        Constants.showSuccessSnackbar(this, layoutInflater, "Successful login!")
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.visibility = View.VISIBLE
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        println("Adding fragment ${fragment.id} with tag $tag")
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_view, fragment, tag)
        fragmentTransaction.addToBackStack(fragment.id.toString())
        fragmentTransaction.commit()
    }

    private fun addFullscreenFragment(fragment: Fragment, tag: String) {
        binding.toolbar.visibility = View.GONE
        replaceFragment(fragment, tag)
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

    private fun loadUserList() {
        network.loadUserList(
            onSuccess = this::onLoadUsersSuccess,
            onError = this::genericError
        )
    }

    private fun onLoadUsersSuccess(statusCode: Int, responseBody: List<UserResponse>) {
        Log.d(
            "USER_LIST",
            "Successful loadUserList call. $responseBody Status code: $statusCode"
        )
        binding.btnAddFriend.isClickable = true
        Log.d("USER_LIST", responseBody.toString())
        val fragment: Fragment = FriendFragment.newInstance(responseBody)
        binding.btnAddFriend.setOnClickListener {
            findViewById<View>(R.id.toolbar).visibility = View.GONE
            addFullscreenFragment(fragment, "ADD_FRIENDS")
        }
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

        replaceFragment(ListPostsFragment.newInstance(user), "LIST_POST_FRAGMENT")
        if (canUserPost) replaceFragment(NewPostFragment.newInstance(), "NEW_POST_FRAGMENT")
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }

    override fun updateUserDetails(user: UserResponse) {
        loadProfileData(user)
    }
}