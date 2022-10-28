
package com.pintertamas.befake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.pintertamas.befake.databinding.ActivityLoginBinding
import com.pintertamas.befake.databinding.ActivityRegisterBinding
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var network: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        network = RetrofitService()

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val fullName = binding.etFullName.text?.toString()
            val email = binding.etEmail.text.toString()
            val bibliography = binding.etBiography.text?.toString()
            val location = binding.etLocation.text?.toString()

            register(username, password, fullName, email, bibliography, location)
        }
    }

    private fun register(
        username: String,
        password: String,
        fullName: String?,
        email: String,
        bibliography: String?,
        location: String?
    ) {
        network.register(
            username = username,
            password = password,
            fullName = fullName,
            email = email,
            bibliography = bibliography,
            location = location,
            onSuccess = this::registrationSuccess,
            onError = this::registrationError,
        )
    }

    private fun registrationSuccess(responseBody: UserResponse) {
        Toast.makeText(
            this,
            "Successful login! User: $responseBody",
            Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun registrationError(e: Throwable) {
        Toast.makeText(this, "Error during registration!", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}