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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.constant.Constants
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.PostItemBinding
import com.pintertamas.befake.databinding.UserCardBinding
import com.pintertamas.befake.fragment.CommentsFragment
import com.pintertamas.befake.network.response.CommentResponse
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.ReactionResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import java.sql.Timestamp
import java.util.*

class PostsRecyclerViewAdapter(
    private val reactionListener: ReactionListener,
    private val activity: FragmentActivity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userCardDetails: UserResponse? = null
    private var userPost: PostResponse? = null
    private val postList = mutableListOf<PostResponse>()
    private var commentsOnPosts: HashMap<Long, List<CommentResponse>> = HashMap()
    private var reactionsOnPosts: HashMap<Long, List<ReactionResponse>> = HashMap()
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPrefName = "user_shared_preference"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        sharedPreferences =
            parent.context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

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
        var comments: List<CommentResponse> = emptyList()
        var reactions: List<ReactionResponse> = emptyList()
        private var inputMethodManager: InputMethodManager? = null

        fun bind(position: Int) {
            comments = commentsOnPosts[userPost?.id] ?: emptyList()
            reactions = reactionsOnPosts[userPost?.id] ?: emptyList()

            Log.d("USER_CARD_BINDING", "binding")
            if (userPost == null) return

            binding.userDetailInclude.tvUsername.text = userCardDetails?.username
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

            if (comments.isNotEmpty()) {
                val commentCount = comments.size
                var commentCountText = "See ${comments.size} comment"
                if (commentCount > 1) commentCountText += "s"
                binding.comments.text = commentCountText
            } else {
                val noCommentsText = "Add a comment..."
                binding.comments.text = noCommentsText
            }

            binding.comments.setOnClickListener {
                showFullscreenCommentsFragment(userPost!!, comments, reactions)
            }

            binding.reaction3.visibility = View.GONE
            binding.reaction2.visibility = View.GONE
            binding.reaction1.visibility = View.GONE

            if (reactions.isNotEmpty()) {
                cache?.cacheReactionImage(
                    filename = reactions[0].imageName,
                    target = binding.reaction1
                )
                binding.reaction1.visibility = View.VISIBLE
            }
            if (reactions.size > 1) {
                cache?.cacheReactionImage(
                    filename = reactions[1].imageName,
                    target = binding.reaction2
                )
                binding.reaction2.visibility = View.VISIBLE
            }
            if (reactions.size > 2) {
                cache?.cacheReactionImage(
                    filename = reactions[2].imageName,
                    target = binding.reaction3
                )
                binding.reaction3.visibility = View.VISIBLE
            }

            val lateTimeText = calculateLateness(userPost!!.beFakeTime, userPost!!.postingTime)
            binding.userDetailInclude.tvPostTime.text = lateTimeText

            binding.etDescription.setText(userPost!!.description ?: "")
            binding.etDescription.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val newDescription: String = binding.etDescription.text.toString()
                    editDescription(userPost!!.id, newDescription)
                    if (inputMethodManager == null) {
                        inputMethodManager = binding.root.context.getSystemService(
                            INPUT_METHOD_SERVICE
                        ) as InputMethodManager
                    }
                    inputMethodManager!!.hideSoftInputFromWindow(
                        binding.etDescription.windowToken,
                        0
                    )
                    binding.etDescription.isCursorVisible = false

                    Log.d("DESCRIPTION", "UPDATING DESCRIPTION")
                    true
                } else {
                    false
                }
            }
        }
    }

    private inner class PostItemViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var isSwapped: Boolean = false
        var comments: List<CommentResponse> = emptyList()
        var reactions: List<ReactionResponse> = emptyList()

        fun bind(position: Int) {
            comments = commentsOnPosts[postList[position - 1].id] ?: emptyList()
            reactions = reactionsOnPosts[postList[position - 1].id] ?: emptyList()

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

            binding.selfiePhoto.imageHolder.setOnClickListener {
                cache?.cachePostImage(
                    filename = postList[position - 1].mainPhoto,
                    target = if (isSwapped) binding.mainPhoto.imageHolder else binding.selfiePhoto.imageHolder
                )
                cache?.cachePostImage(
                    filename = postList[position - 1].selfiePhoto,
                    target = if (isSwapped) binding.selfiePhoto.imageHolder else binding.mainPhoto.imageHolder
                )
                isSwapped = !isSwapped
            }

            if (comments.isNotEmpty()) {
                val commentCount = comments.size
                var commentCountText = "See ${comments.size} comment"
                if (commentCount > 1) commentCountText += "s"
                binding.comments.text = commentCountText
            } else {
                val noCommentsText = "Add a comment..."
                binding.comments.text = noCommentsText
            }

            binding.reaction3.visibility = View.GONE
            binding.reaction2.visibility = View.GONE
            binding.reaction1.visibility = View.GONE

            if (reactions.isNotEmpty()) {
                cache?.cacheReactionImage(
                    filename = reactions[0].imageName,
                    target = binding.reaction1
                )
                binding.reaction1.visibility = View.VISIBLE
            }
            if (reactions.size > 1) {
                cache?.cacheReactionImage(
                    filename = reactions[1].imageName,
                    target = binding.reaction2
                )
                binding.reaction2.visibility = View.VISIBLE
            }
            if (reactions.size > 2) {
                cache?.cacheReactionImage(
                    filename = reactions[2].imageName,
                    target = binding.reaction3
                )
                binding.reaction3.visibility = View.VISIBLE
            }

            binding.reactionIcon.setOnClickListener {
                reactionListener.reaction(postList[position - 1].id, position - 1)
            }

            val lateTimeText = calculateLateness(
                postList[position - 1].beFakeTime,
                postList[position - 1].postingTime
            )
            binding.userDetailInclude.tvPostTime.text = lateTimeText

            binding.comments.setOnClickListener {
                showFullscreenCommentsFragment(postList[position - 1], comments, reactions)
            }

            binding.commentIcon.setOnClickListener {
                showFullscreenCommentsFragment(postList[position - 1], comments, reactions)
            }
        }
    }

    private fun replaceCommentFragment(
        post: PostResponse,
        comments: List<CommentResponse>,
        reactions: List<ReactionResponse>
    ) {
        val fragment: Fragment = CommentsFragment.newInstance(post, comments, reactions)
        val tag = "COMMENTS_FRAGMENT"
        println("Adding fragment ${fragment.id} with tag $tag")
        val fragmentManager = activity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.fragment_container_view, fragment,
            tag
        )
        fragmentTransaction.addToBackStack(fragment.id.toString())
        fragmentTransaction.commit()
    }

    private fun showFullscreenCommentsFragment(
        post: PostResponse,
        comments: List<CommentResponse>,
        reactions: List<ReactionResponse>
    ) {
        activity.findViewById<View>(R.id.toolbar).visibility = View.GONE
        replaceCommentFragment(post, comments, reactions)
    }

    interface ReactionListener {
        fun reaction(postId: Long, position: Int)
    }

    companion object {
        private const val USER_CARD_VIEW = 1
        private const val POST_VIEW = 2
    }

    private fun editDescription(postId: Long, description: String) {
        network.addDescription(
            postId = postId,
            description = description,
            onSuccess = this::editDescriptionSuccess,
            onError = this::generalError,
        )
    }

    private fun editDescriptionSuccess(statusCode: Int, responseBody: PostResponse) {
        Log.d("REACTION_SUCCESSFUL", "Status code: $statusCode")
    }

    private fun getBeFakeTime() {
        network.getBeFakeTime(
            onSuccess = this::getBeFakeTimeSuccess,
            onError = this::generalError,
        )
    }

    private fun getBeFakeTimeSuccess(statusCode: Int, responseBody: String) {
        Log.d("BEFAKE_TIME", "Status code: $statusCode")
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    private fun calculateLateness(time1: String, time2: String): String {
        val beFakeTime: Timestamp = Constants.convertStringToTimestamp(time1)
        val postingTime: Timestamp = Constants.convertStringToTimestamp(time2)
        val diff: Long = postingTime.time - beFakeTime.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        var lateTimeText: String =
            if (days > 1) "$days days"
            else if (days > 0) "$days day"
            else if (hours > 1) "$hours hours"
            else if (hours > 0) "$hours hour"
            else if (minutes > 1) "$minutes minutes"
            else if (minutes > 0) "$minutes minute"
            else if (seconds > 1) "$seconds seconds"
            else "$seconds second"
        lateTimeText += " late"
        return lateTimeText
    }

    fun setUserPost(post: PostResponse) {
        userPost = post
        notifyItemChanged(0)
    }

    fun setUserCard(userDetails: UserResponse) {
        this.userCardDetails = userDetails
        notifyItemInserted(0)
    }

    fun addComments(postId: Long, comments: List<CommentResponse>) {
        commentsOnPosts[postId] = comments
        val post: PostResponse
        if (userPost?.id == postId) {
            notifyItemChanged(0)
        } else {
            post = postList.first { it.id == postId }
            notifyItemChanged(postList.indexOf(post) + 1)
        }
        Log.d("COMMENTS", "Comments added: $comments")
    }

    fun addReactions(postId: Long, reactions: List<ReactionResponse>) {
        reactionsOnPosts[postId] = reactions
        val post: PostResponse
        if (userPost?.id == postId) {
            notifyItemChanged(0)
        } else {
            post = postList.first { it.id == postId }
            notifyItemChanged(postList.indexOf(post) + 1)
        }
        Log.d("REACTIONS", "Reactions added: $reactions")
    }

    fun addItem(post: PostResponse) {
        val size = postList.size
        postList.add(post)
        notifyItemInserted(size)
    }

    fun updateUser(user: UserResponse) {
        this.userCardDetails = user
        println("Updating user on feed")
        notifyItemChanged(0)
    }

    fun addAll(posts: List<PostResponse>) {
        println(posts)
        val size = itemCount + posts.size
        postList.addAll(posts.sortedByDescending { it.id })
        notifyItemRangeInserted(size, posts.size)
    }
}