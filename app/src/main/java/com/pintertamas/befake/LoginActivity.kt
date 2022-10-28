package com.pintertamas.befake

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pintertamas.befake.databinding.ActivityLoginBinding
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.service.RetrofitService


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var network: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        network = RetrofitService()

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            login(username, password)
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(username: String, password: String) {
        network.login(
            username = username,
            password = password,
            onSuccess = this::loginSuccess,
            onError = this::loginError,
        )
    }

    private fun loginSuccess(responseBody: JwtResponse) {
        Toast.makeText(
            this,
            "Successful login! Token: ${responseBody.jwtToken}",
            Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(this, FeedActivity::class.java))
    }

    private fun loginError(e: Throwable) {
        Toast.makeText(this, "Error during login!", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}