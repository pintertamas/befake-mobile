package com.pintertamas.befake.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.FragmentEditProfileBinding
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.JwtResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.notify
import java.io.File


class EditProfileFragment(
    private var user: UserResponse,
    private val editUserListeners: List<EditedUserListener>
) :
    Fragment(com.pintertamas.befake.R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var network: RetrofitService
    private lateinit var imagePicker: ImagePicker.Builder
    private lateinit var fileUri: Uri

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)
        println(user.toString())

        imagePicker = ImagePicker.with(requireActivity())
            .crop(2F, 3F)
            .compress(1024) // Final image size will be less than 1 MB(Optional)
            .maxResultSize(720, 1080)    // Final image resolution will be less than 1080 x 1080
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d("ON_ACTIVITY_RESULT", "RESULT_OK")
            }
            ImagePicker.RESULT_ERROR -> {
                Log.d("ON_ACTIVITY_RESULT", "RESULT_ERROR")
            }
            else -> {
                Log.d("ON_ACTIVITY_RESULT", "TASK_CANCELLED")
            }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    fileUri = data?.data!!
                    val file = File(fileUri.path!!)

                    Log.d("EDIT_PROFILE_PICTURE", file.toString())

                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    Log.d(
                        "EDIT_PROFILE_PICTURE",
                        "Content type: ${requestBody.contentType().toString()}"
                    )

                    val multipart =
                        MultipartBody.Part.createFormData("picture", file.name, requestBody)

                    Log.d("EDIT_PROFILE_PICTURE", multipart.toString())

                    editProfilePicture(multipart)

                    binding.civProfilePicture.setImageURI(fileUri)
                    Log.d("SELECT_IMAGE", file.toString())
                }
                ImagePicker.RESULT_ERROR -> {
                    Constants.showErrorSnackbar(
                        requireContext(),
                        layoutInflater,
                        ImagePicker.getError(data)
                    )
                }
                else -> {
                    Constants.showErrorSnackbar(requireContext(), layoutInflater, "Task Cancelled")
                }
            }
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
            imagePicker.createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
        }

        binding.etFullName.setText(user.fullName ?: "")
        binding.etUsername.setText(user.username)
        binding.etBiography.setText(user.biography ?: "")
        binding.etLocation.setText(user.location ?: "")
        val cache = CacheService.getInstance()
        cache?.cacheProfilePicture(user, binding.civProfilePicture)

        return binding.root
    }

    private fun editProfilePicture(file: MultipartBody.Part) {
        network.editProfilePicture(
            file = file,
            onSuccess = this::editProfilePictureSuccess,
            onError = this::genericError
        )
    }

    private fun editProfilePictureSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "EDIT_PROFILE_PICTURE",
            "Successfully edited profile picture: $responseBody Status code: $statusCode"
        )
        val cache = CacheService.getInstance()
        if (user.profilePicture != null)
            cache?.resetKey(user.profilePicture!!)
        editUserListeners.forEach {
            it.updateUserDetails(user)
        }
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