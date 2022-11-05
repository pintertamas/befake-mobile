package com.pintertamas.befake.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.adapter.PostsRecyclerViewAdapter
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.databinding.FragmentListPostsBinding
import com.pintertamas.befake.network.response.CommentResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.ReactionResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import java.sql.Timestamp

class ListPostsFragment(private var user: UserResponse) : Fragment(R.layout.fragment_list_posts),
    EditProfileFragment.EditedUserListener {

    private var _binding: FragmentListPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var beFakeTime: Timestamp

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
        getBeFakeTime()
        getTodaysPostByUser(user.id)
        setupRecyclerView()

        return binding.root
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

    private fun getBeFakeTime() {
        network.getBeFakeTime(
            onSuccess = this::getBeFakeTimeSuccess,
            onError = this::genericError
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
        postsRecyclerViewAdapter.setBeFakeTime(beFakeTime)
    }

    private fun getTodaysPostByUser(userId: Long) {
        network.getTodaysPostByUser(
            userId = userId,
            onSuccess = this::getTodaysPostByUserSuccess,
            onError = this::genericError
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
            onError = this::genericError
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
            onError = this::genericError
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
            onError = this::genericError
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

    private fun genericError(statusCode: Int, e: Throwable) {
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
        postsRecyclerViewAdapter = PostsRecyclerViewAdapter()
        val list = binding.root.findViewById<RecyclerView>(R.id.posts_recycler_view)
        list.layoutManager = llm
        list.adapter = postsRecyclerViewAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(user: UserResponse) = ListPostsFragment(user)
    }
}
