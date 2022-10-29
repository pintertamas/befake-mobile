package com.pintertamas.befake

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pintertamas.befake.databinding.ActivityFeedBinding
import com.pintertamas.befake.network.service.RetrofitService

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        network = RetrofitService()
        sharedPreferences = this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")

        binding.jwt.text = token.toString()
    }
}