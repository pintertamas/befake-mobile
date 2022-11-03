package com.pintertamas.befake.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import okhttp3.internal.notify


class PostsRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userCardDetails: UserResponse? = null
    private var userPost: PostResponse? = null
    private val postList = mutableListOf<PostResponse>()
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var picasso: Picasso

    private val sharedPrefName = "user_shared_preference"

    var itemClickListener: PostListItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        sharedPreferences =
            parent.context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        picasso = Picasso.Builder(parent.context).build()
        //picasso.setIndicatorsEnabled(true)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

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
        return postList.size + if (userPost == null) 0 else 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            USER_CARD_VIEW
        } else POST_VIEW
    }

    private inner class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var username: TextView = itemView.findViewById(R.id.tv_username)
        var description: TextView = itemView.findViewById(R.id.description)
        var profilePictureView: ImageView = itemView.findViewById(R.id.civ_profile_picture)
        var mainImageView: ImageView = itemView.findViewById(R.id.main_photo)
        var selfieImageView: ImageView = itemView.findViewById(R.id.selfie_photo)

        fun bind(position: Int) {
            if (userPost == null) return
            username.text = userCardDetails?.username
            description.text = userPost?.description ?: "Add description"
            loadProfilePictureUrlIntoView(userCardDetails!!.id, profilePictureView)
            loadPostImageUrlIntoView(userPost!!.mainPhoto, mainImageView)
            loadPostImageUrlIntoView(userPost!!.selfiePhoto, selfieImageView)
        }
    }

    private inner class PostItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var username: TextView = itemView.findViewById(R.id.tv_username)
        var descriptionView: TextView = itemView.findViewById(R.id.description)
        var profilePictureView: ImageView = itemView.findViewById(R.id.civ_profile_picture)
        var mainImageView: ImageView = itemView.findViewById(R.id.main_photo)
        var selfieImageView: ImageView = itemView.findViewById(R.id.selfie_photo)

        fun bind(position: Int) {
            if (userPost == null) return
            if (postList[position - 1].deleted) {
                itemView.visibility = View.GONE
            }
            username.text = postList[position - 1].username
            val description: String = postList[position - 1].description ?: ""
            if (description == "") descriptionView.visibility = View.GONE
            else descriptionView.text = description
            loadProfilePictureUrlIntoView(postList[position - 1].id, profilePictureView)
            loadPostImageUrlIntoView(postList[position - 1].mainPhoto, mainImageView)
            loadPostImageUrlIntoView(postList[position - 1].selfiePhoto, selfieImageView)
        }
    }

    companion object {
        private const val USER_CARD_VIEW = 1
        private const val POST_VIEW = 2
    }

    private fun loadProfilePictureUrlIntoView(userId: Long, view: ImageView) {
        network.loadProfilePictureUrlIntoView(
            userId = userId,
            view = view,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun loadPostImageUrlIntoView(filename: String, view: ImageView) {
        network.loadPostImageUrlIntoView(
            filename = filename,
            view = view,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun getImageUrlSuccess(statusCode: Int, responseBody: ResponseBody, view: ImageView) {
        Log.d(
            "GET_IMAGE_URL",
            "Successfully got image url: $responseBody Status code: $statusCode"
        )
        picasso.load(responseBody.string()).placeholder(R.color.primaryAccent).into(view)
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
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

    fun addAll(posts: List<PostResponse>) {
        println(posts)
        val size = itemCount + posts.size
        postList.addAll(posts)
        notifyItemRangeInserted(size, posts.size)
    }

    interface PostListItemClickListener {
        fun onItemClick(post: PostResponse)
    }
}