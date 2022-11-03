package com.pintertamas.befake.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.databinding.PostItemBinding
import com.pintertamas.befake.databinding.UserCardBinding
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso


class PostsRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userCardDetails: UserResponse? = null
    private var userPost: PostResponse? = null
    private val postList = mutableListOf<PostResponse>()
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var picasso: Picasso

    private val sharedPrefName = "user_shared_preference"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        sharedPreferences =
            parent.context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        picasso = Picasso.Builder(parent.context).build()
        //picasso.setIndicatorsEnabled(true)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        return if (viewType == USER_CARD_VIEW) {
            UserViewHolder(
                UserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else PostItemViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        return postList.size + if (userPost == null) 0 else 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            USER_CARD_VIEW
        } else POST_VIEW
    }

    private inner class UserViewHolder(private val binding: UserCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            Log.d("USER_CARD_BINDING", "binding")
            if (userPost == null) return

            binding.userDetailInclude.tvUsername.text = userCardDetails?.username
            binding.description.text = userPost?.description ?: "Add description"
            val cache = CacheService.getInstance()
            cache?.cacheProfilePicture(
                userCardDetails!!,
                binding.userDetailInclude.civProfilePicture
            )
            cache?.cachePostImage(
                userPost!!.mainPhoto,
                binding.mainPhoto.imageHolder
            )
            cache?.cachePostImage(userPost!!.selfiePhoto, binding.selfiePhoto.imageHolder)
        }
    }

    private inner class PostItemViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            if (userPost == null) return
            if (postList[position - 1].deleted) {
                itemView.visibility = View.GONE
            }
            binding.userDetailInclude.tvUsername.text = postList[position - 1].username
            val description: String = postList[position - 1].description ?: ""
            if (description == "") binding.description.visibility = View.GONE
            else binding.description.text = description
            val cache = CacheService.getInstance()
            cache?.cacheProfilePicture(
                userId = postList[position - 1].userId,
                target = binding.userDetailInclude.civProfilePicture
            )
            cache?.cachePostImage(
                filename = postList[position - 1].mainPhoto,
                target = binding.mainPhoto.imageHolder
            )
            cache?.cachePostImage(
                filename = postList[position - 1].selfiePhoto,
                target = binding.selfiePhoto.imageHolder
            )
        }
    }

    companion object {
        private const val USER_CARD_VIEW = 1
        private const val POST_VIEW = 2
    }

    fun setUserPost(post: PostResponse) {
        userPost = post
        notifyItemChanged(0)
    }

    fun setUserCard(userDetails: UserResponse) {
        this.userCardDetails = userDetails
        notifyItemInserted(0)
    }

    fun addItem(post: PostResponse) {
        val size = postList.size
        postList.add(post)
        notifyItemInserted(size)
    }

    fun updateUser(user: UserResponse) {
        /*this.userCardDetails = user
        println("Updating user on feed")
        notifyItemChanged(0)*/
    }

    fun addAll(posts: List<PostResponse>) {
        println(posts)
        val size = itemCount + posts.size
        postList.addAll(posts)
        notifyItemRangeInserted(size, posts.size)
    }
}