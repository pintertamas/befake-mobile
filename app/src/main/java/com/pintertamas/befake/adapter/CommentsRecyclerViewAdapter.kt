package com.pintertamas.befake.adapter

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.CommentItemBinding
import com.pintertamas.befake.databinding.PostItemBinding
import com.pintertamas.befake.databinding.UserCardBinding
import com.pintertamas.befake.network.response.CommentResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.ReactionResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.pintertamas.befake.service.Functions
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class CommentsRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cache: CacheService
    private val commentList = mutableListOf<CommentResponse>()

    private val sharedPrefName = "user_shared_preference"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        sharedPreferences =
            parent.context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        return CommentItemViewHolder(
            CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as CommentItemViewHolder).bind(position)
        holder.itemView.setOnClickListener {
            Log.d("POSITION", position.toString())
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    private inner class CommentItemViewHolder(private val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var isSelected: Boolean = false

        fun bind(position: Int) {
            binding.commenterName.text = commentList[position].username
            binding.commentText.text = commentList[position].text
            binding.lateTime.text = Functions.calculateTimeAgo(commentList[position].commentTime)
        }
    }

    fun addComment(comment: CommentResponse) {
        val size = commentList.size
        this.commentList.add(comment)
        notifyItemInserted(size)
    }

    fun reloadComments(comments: List<CommentResponse>) {
        commentList.clear()
        this.commentList.addAll(comments)
        notifyItemChanged(0, commentList.size)
    }

    private fun getUserByUserId(userId: Long) {
        network.getUserByUserId(
            userId = userId,
            onSuccess = this::getUserByUserIdSuccess,
            onError = this::generalError,
        )
    }

    private fun getUserByUserIdSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d("USER_BY_USER_ID_SUCCESSFUL", "Status code: $statusCode")
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }
}