package com.pintertamas.befake

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import com.pintertamas.befake.databinding.ActivityLoginBinding
import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.service.NetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val baseUrl: String = "http://192.168.0.34:8765"

    private lateinit var service: NetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(NetworkService::class.java)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        try {
            CoroutineScope(IO).launch {
                Log.d("LoginActivity", "Logging in with: $username - $password")
                val response = service.login(
                    JwtRequest(username, password)
                )
                Log.d("LoginActivity", "jwtResponse: $response")
            }
        } catch (e: Exception) {
            Log.d("LoginActivity", e.message.toString())
        }
    }
}