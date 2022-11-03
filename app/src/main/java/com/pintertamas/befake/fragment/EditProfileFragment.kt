package com.pintertamas.befake.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pintertamas.befake.R
import com.pintertamas.befake.databinding.FragmentEditProfileBinding
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso

class EditProfileFragment(
    private var user: UserResponse,
    private val editUserListeners: List<EditedUserListener>
) :
    Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var network: RetrofitService
    private var picasso: Picasso = Picasso.get()

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        picasso = Picasso.Builder(requireContext()).build()
        picasso.setIndicatorsEnabled(true)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)
        println(user.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.ivBackButton.setOnClickListener {
            Log.d("BACK_ARROW", "Click")
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ivEditButton.setOnClickListener {
            Log.d("EDIT_PROFILE", "Edited")
            val userRequest = UserRequest(
                username = binding.etUsername.text.toString(),
                password = user.password,
                fullName = binding.etFullName.text.toString(),
                email = user.email,
                biography = binding.etBiography.text.toString(),
                location = binding.etLocation.text.toString()
            )
            editUser(userRequest)
        }

        binding.civProfilePicture.setOnClickListener {

        }

        binding.etFullName.setText(user.fullName ?: "")
        binding.etUsername.setText(user.username)
        binding.etBiography.setText(user.biography ?: "")
        binding.etLocation.setText(user.location ?: "")
        val cache = CacheService.getInstance()
        cache?.cacheProfilePicture(user, binding.civProfilePicture)

        return binding.root
    }

    private fun editUser(userRequest: UserRequest) {
        network.editUser(
            userRequest = userRequest,
            onSuccess = this::editUserSuccess,
            onError = this::genericError
        )
    }

    private fun editUserSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "EDIT_USER",
            "Successfully edited user: $responseBody Status code: $statusCode"
        )
        user = responseBody
        editUserListeners.forEach {
            it.updateUserDetails(user)
        }
        //login() //TODO: create a refresh token like endpoint in backend -> don't have to remember password and can authenticate still with previous JWT until new one is provisioned
        popFragment()
    }

    private fun popFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun login() {
        network.login(
            username = user.username,
            password = user.password,
            onSuccess = this::loginSuccess,
            onError = this::genericError
        )
    }

    private fun loginSuccess(statusCode: Int, responseBody: JwtResponse) {
        Log.d(
            "GET_IMAGE_URL",
            "Successfully logged in: $responseBody Status code: $statusCode"
        )
        saveUserDetails(jwtResponse = responseBody)
    }

    private fun saveUserDetails(jwtResponse: JwtResponse) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong("userId", jwtResponse.userId!!)
        editor.putString("username", jwtResponse.username)
        editor.putString("email", jwtResponse.email)
        editor.putString("jwt", jwtResponse.jwt)
        editor.apply()
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    interface EditedUserListener {
        fun updateUserDetails(user: UserResponse)
    }

    companion object {
        @JvmStatic
        fun newInstance(user: UserResponse, listeners: List<EditedUserListener>) =
            EditProfileFragment(user, listeners)
    }
}