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
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.FragmentCommentsBinding
import com.pintertamas.befake.network.response.CommentResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.ReactionResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.pintertamas.befake.service.Functions
import okhttp3.RequestBody.Companion.toRequestBody
import java.sql.Timestamp

class CommentsFragment(
    private val post: PostResponse,
    private val comments: List<CommentResponse>,
    private val reactions: List<ReactionResponse>,
) :
    Fragment(R.layout.fragment_comments) {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cache: CacheService

    private val sharedPrefName = "user_shared_preference"

    private lateinit var commentsRecyclerViewAdapter: CommentsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentsBinding.inflate(layoutInflater)

        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
        }

        binding.reactionsButton.setOnClickListener {
            val fragment: Fragment = ReactionsFragment.newInstance(
                post,
                reactions
            )
            goToReactions(fragment, "REACTIONS")
        }

        cache = CacheService.getInstance()!!
        cache.cacheProfilePicture(post.userId, binding.userPost.commenterProfilePicture)
        binding.userPost.commenterName.text = post.username
        binding.userPost.commentText.text = post.description
        binding.userPost.lateTime.text =
            Functions.calculateLateness(post.beFakeTime, post.postingTime)

        binding.sendCommentButton.setOnClickListener {
            val comment: String = binding.etAddComment.text.toString()
            comment(post.id, comment)
            binding.etAddComment.setText("")
            binding.etAddComment.clearFocus()
        }

        setupRecyclerView()
        getCommentsOnPost(post.id)

        return binding.root
    }

    private fun goToReactions(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment, tag)
            //.addToBackStack(fragment.id.toString())
            .commit()
    }

    private fun setupRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        commentsRecyclerViewAdapter =
            CommentsRecyclerViewAdapter()
        commentsRecyclerViewAdapter.reloadComments(comments)
        val list = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
        list.layoutManager = llm
        list.adapter = commentsRecyclerViewAdapter
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
        commentsRecyclerViewAdapter.reloadComments(responseBody)
    }

    private fun comment(postId: Long, comment: String) {
        network.comment(
            postId = postId,
            comment = comment,
            onSuccess = this::onCommentSuccess,
            onError = this::generalError,
        )
    }

    private fun onCommentSuccess(statusCode: Int, responseBody: CommentResponse) {
        Log.d("COMMENT_SUCCESSFUL", "Status code: $statusCode")
        commentsRecyclerViewAdapter.addComment(responseBody)
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    companion object {
        @JvmStatic
        fun newInstance(post: PostResponse, comments: List<CommentResponse>, reactions: List<ReactionResponse>) =
            CommentsFragment(post, comments, reactions)
    }
}