package com.pintertamas.befake.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pintertamas.befake.R
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.FragmentNewPostBinding
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class NewPostFragment(private val user: UserResponse) : Fragment(R.layout.fragment_new_post) {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var network: RetrofitService
    private lateinit var imagePicker: ImagePicker.Builder
    private var mainPhoto: MultipartBody.Part? = null
    private var selfiePhoto: MultipartBody.Part? = null

    private val sharedPrefName = "user_shared_preference"

    private var locationPermission: Boolean = false
    private var cameraPermission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        imagePicker = ImagePicker.with(requireActivity())
            .crop(2F, 3F)
            .compress(1024) // Final image size will be less than 1 MB(Optional)
            .maxResultSize(720, 1080)    // Final image resolution will be less than 1080 x 1080
            .cameraOnly()
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )

        checkPermissions()
    }

    private fun checkPermissions() {
        locationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        cameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!locationPermission || !cameraPermission
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA), 1
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED)
                    ) {
                        Log.d("PERMISSIONS", "Permission Granted")
                        locationPermission = true
                    }
                } else {
                    Log.d("PERMISSIONS", "Permission Denied")
                    locationPermission = false
                }
                if (grantResults.isNotEmpty() && grantResults[1] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED)
                    ) {
                        Log.d("PERMISSIONS", "Permission Granted")
                        cameraPermission = true
                    }
                } else {
                    Log.d("PERMISSIONS", "Permission Denied")
                    cameraPermission = false
                }
                return
            }
        }
    }

    @Deprecated("Deprecated in Java")
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
                    val fileUri = data?.data!!
                    val multipart = getMultiPartImageFromUri(fileUri)

                    if (mainPhoto == null) mainPhoto = multipart
                    else selfiePhoto = multipart

                    if (selfiePhoto != null) {
                        createPost(mainPhoto!!, selfiePhoto!!)
                        mainPhoto = null
                        selfiePhoto = null
                        Log.d("CREATING_POST", "Creating post")
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    mainPhoto = null
                    selfiePhoto = null
                    Constants.showErrorSnackbar(
                        requireContext(),
                        layoutInflater,
                        ImagePicker.getError(data)
                    )
                }
                else -> {
                    mainPhoto = null
                    selfiePhoto = null
                    Constants.showErrorSnackbar(
                        requireContext(),
                        layoutInflater,
                        "Upload Cancelled"
                    )
                }
            }
        }

    private fun getMultiPartTextFromString(text: String): RequestBody {
        return text.toRequestBody(
            ("text/plain").toMediaTypeOrNull(),
        )
    }

    private fun getMultipartLocation(): RequestBody {
        // TODO get the location of the user and convert it to text
        return getMultiPartTextFromString("Budapest")
    }

    private fun getMultiPartImageFromUri(uri: Uri): MultipartBody.Part {
        val file = File(uri.path!!)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartName = if (mainPhoto == null) "main" else "selfie"
        return MultipartBody.Part.createFormData(multipartName, file.name, requestBody)
    }

    private fun createPost(
        mainPhoto: MultipartBody.Part,
        selfiePhoto: MultipartBody.Part,
    ) {
        val location = getMultipartLocation()
        network.createPost(
            mainPhoto = mainPhoto,
            selfiePhoto = selfiePhoto,
            location = location,
            onSuccess = this::createPostSuccess,
            onError = this::generalError
        )
    }

    private fun createPostSuccess(statusCode: Int, responseBody: PostResponse) {
        Log.d(
            "GET_USER",
            "Successfully queried user: $responseBody Status code: $statusCode"
        )
        val fragment: Fragment = ListPostsFragment.newInstance(user)
        popBackStack()
        replaceFragment(fragment, "LIST_POST_FRAGMENT")
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }

    private fun popBackStack() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        println("Adding fragment ${fragment.id} with tag $tag")
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_view, fragment, tag)
        fragmentTransaction.addToBackStack(fragment.id.toString())
        fragmentTransaction.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        binding.postBefakeButton.setOnClickListener {
            checkPermissions()
            if (cameraPermission && locationPermission) {
                imagePicker.createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
                imagePicker.createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
            } else Constants.showErrorSnackbar(
                requireContext(),
                layoutInflater,
                "Permissions are required to capture images"
            )
        }

        return binding.root
    }

    private val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val toolbar: View? = requireActivity().findViewById(R.id.toolbar)
                if (toolbar?.visibility == View.VISIBLE) {
                    requireActivity().finish()
                } else {
                    toolbar?.visibility = View.VISIBLE
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance(user: UserResponse) = NewPostFragment(user)
    }
}
