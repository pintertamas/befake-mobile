package com.pintertamas.befake.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.adapter.CommentsRecyclerViewAdapter
import com.pintertamas.befake.adapter.ReactionsRecyclerViewAdapter
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.FragmentCommentsBinding
import com.pintertamas.befake.databinding.FragmentReactionsBinding
import com.pintertamas.befake.network.response.CommentResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.ReactionResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.pintertamas.befake.service.Functions
import kotlinx.coroutines.NonDisposableHandle.parent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.sql.Timestamp

class ReactionsFragment(
    private val post: PostResponse,
    private val reactions: List<ReactionResponse>
) :
    Fragment(R.layout.fragment_reactions) {

    private var _binding: FragmentReactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cache: CacheService

    private val sharedPrefName = "user_shared_preference"

    private lateinit var reactionsRecyclerViewAdapter: ReactionsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReactionsBinding.inflate(layoutInflater)

        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
        }

        binding.commentsButton.setOnClickListener {
            val fragment: Fragment = CommentsFragment.newInstance(
                post,
                emptyList(),
                reactions
            )

            setupRecyclerView()
            goToComments(fragment, "COMMENTS")
        }

        cache = CacheService.getInstance()!!
        cache.cacheProfilePicture(post.userId, binding.userPost.commenterProfilePicture)
        binding.userPost.commenterName.text = post.username
        binding.userPost.commentText.text = post.description
        binding.userPost.lateTime.text =
            Functions.calculateLateness(post.beFakeTime, post.postingTime)

        setupRecyclerView()
        getReactionsOnPost(post.id)

        return binding.root
    }

    private fun setupRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        reactionsRecyclerViewAdapter =
            ReactionsRecyclerViewAdapter()
        reactionsRecyclerViewAdapter.reloadReactions(reactions)
        val list = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
        list.layoutManager = llm
        list.adapter = reactionsRecyclerViewAdapter
    }

    private fun goToComments(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment, tag)
            //.addToBackStack(fragment.id.toString())
            .commit()
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
        val comments: List<ReactionResponse> = responseBody
        if (comments.isEmpty()) return
        reactionsRecyclerViewAdapter.reloadReactions(responseBody)
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    companion object {
        @JvmStatic
        fun newInstance(post: PostResponse, reactions: List<ReactionResponse>) =
            ReactionsFragment(post, reactions)
    }
}