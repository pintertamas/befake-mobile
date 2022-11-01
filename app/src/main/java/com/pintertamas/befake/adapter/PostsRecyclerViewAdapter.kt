package com.pintertamas.befake.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.databinding.PostItemBinding
import com.pintertamas.befake.network.response.PostResponse


class PostsRecyclerViewAdapter : RecyclerView.Adapter<PostsRecyclerViewAdapter.ViewHolder>() {

    private val postList = mutableListOf<PostResponse>()

    var itemClickListener: PostListItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            PostItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]
        holder.post = post
        // customize layout data
    }

    override fun getItemCount() = postList.size

    inner class ViewHolder(binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var post: PostResponse? = null

        init {
            itemView.setOnClickListener {
                post?.let { post -> itemClickListener?.onItemClick(post) }
            }
        }
    }

    fun addItem(post: PostResponse) {
        val size = postList.size
        postList.add(post)
        notifyItemInserted(size)
    }

    fun addAll(posts: List<PostResponse>) {
        val size = posts.size
        postList += posts
        notifyItemRangeInserted(size, posts.size)
    }

    interface PostListItemClickListener {
        fun onItemClick(post: PostResponse)
    }
}