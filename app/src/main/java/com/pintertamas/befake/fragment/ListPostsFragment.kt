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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pintertamas.befake.R
import com.pintertamas.befake.adapter.PostsRecyclerViewAdapter
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.FragmentListPostsBinding
import com.pintertamas.befake.network.response.*
import com.pintertamas.befake.network.service.RetrofitService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ListPostsFragment(private var user: UserResponse) : Fragment(R.layout.fragment_list_posts),
    EditProfileFragment.EditedUserListener, PostsRecyclerViewAdapter.ReactionListener {

    private var _binding: FragmentListPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var network: RetrofitService
    private lateinit var imagePicker: ImagePicker.Builder
    private lateinit var sharedPreferences: SharedPreferences
    //private lateinit var beFakeTime: Timestamp
    private var reactionOnPostID: Long? = null
    private var reactionPosition: Int? = null

    private lateinit var postsRecyclerViewAdapter: PostsRecyclerViewAdapter

    private val sharedPrefName = "user_shared_preference"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListPostsBinding.inflate(inflater, container, false)

        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        imagePicker = ImagePicker.with(requireActivity())
            .cropSquare()
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

        getTodaysPostByUser(user.id)
        setupRecyclerView()

        return binding.root
    }

    override fun reaction(postId: Long, position: Int) {
        Log.d("REACTION", "Reacting to post: $postId")
        reactionOnPostID = postId
        reactionPosition = position
        imagePicker.createIntent { intent ->
            startForReactionImageResult.launch(intent)
        }
    }

    private fun react(multipart: MultipartBody.Part) {
        network.react(
            reaction = multipart,
            postId = reactionOnPostID!!,
            position = reactionPosition!!,
            onSuccess = this::onReactionSuccess,
            onError = this::generalError,
        )
    }

    private fun onReactionSuccess(statusCode: Int, responseBody: ReactionResponse, position: Int) {
        Log.d("REACTION_SUCCESSFUL", "Status code: $statusCode")
        postsRecyclerViewAdapter.notifyItemChanged(position + 1)
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

    private val startForReactionImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    val multipart = getMultiPartReactionImageFromUri(fileUri)
                    if (reactionOnPostID != null && reactionPosition != null)
                        react(multipart)
                    reactionOnPostID = null
                    reactionPosition = null
                }
                ImagePicker.RESULT_ERROR -> {
                    Constants.showErrorSnackbar(
                        requireContext(),
                        layoutInflater,
                        ImagePicker.getError(data)
                    )
                }
                else -> {
                    Constants.showErrorSnackbar(
                        requireContext(),
                        layoutInflater,
                        "Upload Cancelled"
                    )
                }
            }
        }

    private fun getMultiPartReactionImageFromUri(uri: Uri): MultipartBody.Part {
        val file = File(uri.path!!)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("reaction", file.name, requestBody)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireActivity().supportFragmentManager.fragments.size == 1)
                        requireActivity().finish()
                    else requireActivity().supportFragmentManager.popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    /*private fun getBeFakeTime() {
        network.getBeFakeTime(
            onSuccess = this::getBeFakeTimeSuccess,
            onError = this::generalError
        )
    }

    private fun getBeFakeTimeSuccess(statusCode: Int, responseBody: String) {
        Log.d(
            "GET_BEFAKE_TIME",
            "Successfully got befake time: $responseBody Status code: $statusCode"
        )
        val beFakeTimeString = responseBody
        println(beFakeTimeString)
        val beFakeTimeTimestamp = Constants.convertStringToTimestamp(beFakeTimeString)
        println(beFakeTimeTimestamp)
        beFakeTime = beFakeTimeTimestamp
    }*/

    private fun getTodaysPostByUser(userId: Long) {
        network.getTodaysPostByUser(
            userId = userId,
            onSuccess = this::getTodaysPostByUserSuccess,
            onError = this::generalError
        )
    }

    private fun getTodaysPostByUserSuccess(statusCode: Int, responseBody: PostResponse) {
        Log.d(
            "GET_TODAYS_POST_BY_USER",
            "Successfully got today's post from user: $responseBody Status code: $statusCode"
        )
        val post: PostResponse = responseBody
        postsRecyclerViewAdapter.setUserCard(user)
        postsRecyclerViewAdapter.setUserPost(post)
        getPostsFromFriends()
        getCommentsOnPost(post.id)
        getReactionsOnPost(post.id)
    }

    private fun getPostsFromFriends() {
        network.getPostsFromFriends(
            onSuccess = this::getPostsFromFriendsSuccess,
            onError = this::generalError
        )
    }

    private fun getPostsFromFriendsSuccess(statusCode: Int, responseBody: List<PostResponse>) {
        Log.d(
            "GET_POSTS_FROM_FRIENDS",
            "Successfully got posts from friends: $responseBody Status code: $statusCode"
        )
        val postsByFriends: List<PostResponse> = responseBody
        postsRecyclerViewAdapter.addAll(postsByFriends)
        postsByFriends.forEach {
            getCommentsOnPost(it.id)
            getReactionsOnPost(it.id)
        }

    }

    private fun getReactionsOnPost(postId: Long) {
        network.getReactionsOnPost(
            postId = postId,
            onSuccess = this::getReactionsOnPostSuccess,
            onError = this::generalError
        )
    }

    private fun getReactionsOnPostSuccess(statusCode: Int, responseBody: List<ReactionResponse>) {
        Log.d(
            "GET_COMMENTS_ON_POST",
            "Successfully got posts from friends: $responseBody Status code: $statusCode"
        )
        val reactions: List<ReactionResponse> = responseBody
        if (reactions.isEmpty()) return
        postsRecyclerViewAdapter.addReactions(reactions[0].postId, reactions)
    }

    private fun getCommentsOnPost(postId: Long) {
        network.getCommentsOnPost(
            postId = postId,
            onSuccess = this::getCommentsOnPostSuccess,
            onError = this::generalError
        )
    }

    private fun getCommentsOnPostSuccess(statusCode: Int, responseBody: List<CommentResponse>) {
        Log.d(
            "GET_COMMENTS_ON_POST",
            "Successfully got posts from friends: $responseBody Status code: $statusCode"
        )
        val comments: List<CommentResponse> = responseBody
        if (comments.isEmpty()) return
        postsRecyclerViewAdapter.addComments(comments[0].postId, comments)
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun updateUserDetails(user: UserResponse) {
        Log.d("UPDATING_USER_DETAILS", user.toString())
        this.user = user
        postsRecyclerViewAdapter.updateUser(user)
    }

    private fun setupRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        postsRecyclerViewAdapter = PostsRecyclerViewAdapter(this, requireActivity())
        val list = binding.root.findViewById<RecyclerView>(R.id.posts_recycler_view)
        list.layoutManager = llm
        list.adapter = postsRecyclerViewAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(user: UserResponse) = ListPostsFragment(user)
    }
}