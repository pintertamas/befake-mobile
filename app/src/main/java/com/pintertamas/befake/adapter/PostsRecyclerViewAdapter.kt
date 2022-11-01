package com.pintertamas.befake.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse


class PostsRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var userCardDetails: UserResponse
    private val postList = mutableListOf<PostResponse>()

    var itemClickListener: PostListItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == USER_CARD_VIEW) {
            UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.user_card, parent, false)
            )
        } else PostItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as UserViewHolder).bind(position)
        } else {
            (holder as PostItemViewHolder).bind(position)
        }
        holder.itemView.setOnClickListener {
            Log.d("POSITION", position.toString())
        }
    }

    override fun getItemCount(): Int {
        return postList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            USER_CARD_VIEW
        } else POST_VIEW
    }

    private inner class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        //var yourView: View  = itemView.findViewById(R.id.user_card)

        fun bind(position: Int) {
            //yourView.setImageResource(list[position])
        }
    }

    private inner class PostItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        //var yourView: TextView = itemView.findViewById(R.id.yourView)

        fun bind(position: Int) {
            //yourView.text = list[position]
        }
    }

    companion object {
        private const val USER_CARD_VIEW = 1
        private const val POST_VIEW = 2
    }

    fun setUserCard(userDetails: UserResponse) {
        this.userCardDetails = userDetails
    }

    fun addItem(post: PostResponse) {
        val size = postList.size
        postList.add(post)
        notifyItemInserted(size)
    }

    fun addAll(posts: List<PostResponse>) {
        val size = itemCount + posts.size
        postList.addAll(posts)
        notifyItemRangeInserted(size, posts.size)
    }

    interface PostListItemClickListener {
        fun onItemClick(post: PostResponse)
    }
}