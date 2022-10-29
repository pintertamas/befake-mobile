package com.pintertamas.befake

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.pintertamas.befake.databinding.ActivityLoginBinding
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.service.RetrofitService

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences

    private var isUsernameCorrect: Boolean = false
    private var isPasswordCorrect: Boolean = false
    private val passwordRegex: Regex = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")
    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        network = RetrofitService()
        sharedPreferences = this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val intentUsername: String? = intent.getStringExtra("username")
        val intentPassword: String? = intent.getStringExtra("password")

        if (intentUsername != null && intentPassword != null)
            login(intentUsername, intentPassword)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            login(username, password)
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.etUsername.addTextChangedListener { text ->
            if (text.toString().length < 4) {
                isUsernameCorrect = false
                toggleLoginButtonState()
            } else {
                isUsernameCorrect = true
                toggleLoginButtonState()
            }
        }

        binding.etPassword.addTextChangedListener { text ->
            if (text.toString().length < 8 || !text.toString().matches(passwordRegex)) {
                isPasswordCorrect = false
                toggleLoginButtonState()
            } else {
                isPasswordCorrect = true
                toggleLoginButtonState()
            }
        }

        // this is just a hardcoded backdoor cheat to fasten testing
        binding.logo.setOnClickListener {
            login("Tomi", "TestPassword1")
        }
    }

    private fun toggleLoginButtonState() {
        binding.btnLogin.isEnabled = isUsernameCorrect && isPasswordCorrect
        binding.btnLogin.isClickable = binding.btnLogin.isEnabled
        if (binding.btnLogin.isEnabled)
            binding.btnLogin.background.setTint(
                ContextCompat.getColor(
                    baseContext,
                    R.color.primary
                )
            )
        else
            binding.btnLogin.background.setTint(
                ContextCompat.getColor(
                    baseContext,
                    R.color.light_grey
                )
            )
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
            "Successful login! Token: ${responseBody.jwt}",
            Toast.LENGTH_SHORT
        ).show()
        saveUserDetails(responseBody)
        startActivity(Intent(this, FeedActivity::class.java))
        finish()
    }

    private fun saveUserDetails(jwtResponse: JwtResponse) {
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putLong("userId", jwtResponse.userId!!)
        editor.putString("username", jwtResponse.username)
        editor.putString("email", jwtResponse.email)
        editor.putString("jwt", jwtResponse.jwt)
        editor.apply()
    }

    private fun loginError(e: Throwable) {
        Toast.makeText(this, "Error during login!", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}