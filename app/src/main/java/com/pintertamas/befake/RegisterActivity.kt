package com.pintertamas.befake

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.pintertamas.befake.databinding.ActivityRegisterBinding
import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.CustomErrorSnackbarBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var network: RetrofitService
    private lateinit var credentials: JwtRequest

    private var isUsernameCorrect: Boolean = false
    private var isPasswordCorrect: Boolean = false
    private var isEmailCorrect: Boolean = false
    private val passwordRegex: Regex = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")
    private val emailRegex: Regex =
        Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")

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

            credentials = JwtRequest(username, password)

            register(username, password, fullName, email, bibliography, location)
        }

        binding.etUsername.addTextChangedListener { text ->
            if (text.toString().length < 4 || text.toString().length > 16) {
                binding.etUsername.setTextColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.red
                    )
                )
                isUsernameCorrect = false
                toggleRegisterButtonState()
            } else {
                binding.etUsername.setTextColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.dark_grey
                    )
                )
                isUsernameCorrect = true
                toggleRegisterButtonState()
            }
        }

        binding.etPassword.addTextChangedListener { text ->
            if (text.toString().length < 8 || !text.toString().matches(passwordRegex)) {
                binding.etPassword.setTextColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.red
                    )
                )
                isPasswordCorrect = false
                toggleRegisterButtonState()
            } else {
                binding.etPassword.setTextColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.dark_grey
                    )
                )
                isPasswordCorrect = true
                toggleRegisterButtonState()
            }
        }

        binding.etEmail.addTextChangedListener { text ->
            if (!text.toString().matches(emailRegex)) {
                binding.etEmail.setTextColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.red
                    )
                )
                isEmailCorrect = false
                toggleRegisterButtonState()
            } else {
                binding.etEmail.setTextColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.dark_grey
                    )
                )
                isEmailCorrect = true
                toggleRegisterButtonState()
            }
        }
    }

    private fun toggleRegisterButtonState() {
        binding.btnRegister.isEnabled = isUsernameCorrect && isPasswordCorrect && isEmailCorrect
        if (binding.btnRegister.isEnabled)
            binding.btnRegister.background.setTint(
                ContextCompat.getColor(
                    baseContext,
                    R.color.primary
                )
            )
        else
            binding.btnRegister.background.setTint(
                ContextCompat.getColor(
                    baseContext,
                    R.color.light_grey
                )
            )
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

    private fun registrationSuccess(statusCode: Int, responseBody: UserResponse?) {
        Constants.showSuccessSnackbar(
            this,
            layoutInflater,
            "Successful registration! Welcome ${responseBody?.username}"
        )
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra(
                    "username",
                    credentials.username
                ).putExtra(
                    "password",
                    credentials.password
                )
        )
        finish()
    }

    private fun registrationError(statusCode: Int, e: Throwable) {
        var errorMessage = ""
        if (statusCode == 400) errorMessage = "User already exists"
        if (statusCode == 500) errorMessage = "Something unexpected happened"
        Constants.showErrorSnackbar(this, layoutInflater, errorMessage)
        e.printStackTrace()
    }
}