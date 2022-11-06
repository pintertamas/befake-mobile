package com.pintertamas.befake.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.UserItemBinding
import com.pintertamas.befake.fragment.FriendFragment
import com.pintertamas.befake.network.response.FriendshipResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import io.sulek.ssml.OnSwipeListener

class UsersRecyclerViewAdapter(
    private val listType: ListType,
    private val activity: FragmentActivity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userList = mutableListOf<UserResponse>()
    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPrefName = "user_shared_preference"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        sharedPreferences =
            parent.context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        return UserViewHolder(
            UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserViewHolder).bind(position)
        holder.itemView.setOnClickListener {
            Log.d("POSITION", position.toString())
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    enum class ListType {
        USER, PENDING, FRIEND
    }

    fun addAll(userList: List<UserResponse>) {
        println(userList)
        this.userList.addAll(userList)
        notifyItemChanged(0, userList.size)
    }

    fun add(user: UserResponse) {
        val size = userList.size
        this.userList.add(user)
        notifyItemInserted(size)
    }

    fun remove(user: UserResponse) {
        notifyDataSetChanged() // TODO: this is a bad workaround but I can't figure out where the notifications go sideways
        val userIndex = userList.indexOf(user)
        this.userList.remove(user)
        notifyItemRemoved(userIndex)
    }

    private inner class UserViewHolder(private val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val cache = CacheService.getInstance()
            cache?.cacheProfilePicture(
                userList[position].id,
                binding.civProfilePicture
            )
            binding.tvFullName.text = userList[position].fullName
            val usernameTag = "@${userList[position].username}"
            binding.tvUsername.text = usernameTag

            if (listType == ListType.USER) {
                binding.swipeContainer.isEnabled = false
                binding.confirmAddButton.setText(R.string.add)
                binding.removeFriendButton.visibility = View.GONE
                binding.confirmAddButton.visibility = View.VISIBLE
                binding.confirmAddButton.setOnClickListener {
                    addFriend(userList[position].id)
                }
                val searchBox: EditText = activity.findViewById(R.id.et_search)

                if (searchBox.text.isNotEmpty() && !userList[position].username.contains(searchBox.text)) {
                    binding.userItem.visibility = View.GONE
                    binding.userItem.layoutParams = RecyclerView.LayoutParams(0, 0)
                } else {
                    binding.userItem.visibility = View.VISIBLE
                }
            } else if (listType == ListType.PENDING) {
                binding.swipeContainer.isEnabled = true
                binding.confirmAddButton.setText(R.string.confirm)
                binding.removeFriendButton.visibility = View.GONE
                binding.confirmAddButton.visibility = View.VISIBLE
                binding.confirmAddButton.setOnClickListener {
                    acceptFriendRequest(userList[position].id)
                }
                binding.swipeContainer.setOnSwipeListener(object : OnSwipeListener {
                    override fun onSwipe(isExpanded: Boolean) {}
                })
                binding.rejectRequestButton.setOnClickListener {
                    removeFriend(userList[position].id)
                }
            } else if (listType == ListType.FRIEND) {
                binding.swipeContainer.isEnabled = false
                binding.confirmAddButton.visibility = View.GONE
                binding.removeFriendButton.visibility = View.VISIBLE
                binding.removeFriendButton.setOnClickListener {
                    removeFriend(userList[position].id)
                }
            }
        }
    }

    private fun addFriend(userId: Long) {
        network.addFriend(
            userId = userId,
            onSuccess = this::addFriendSuccess,
            onError = this::genericError,
        )
    }

    private fun addFriendSuccess(statusCode: Int, responseBody: FriendshipResponse, userId: Long) {
        Log.d(
            "FRIEND_LIST",
            "Successfully sent friend request to the user: $responseBody Status code: $statusCode"
        )
        val user: UserResponse = userList.first { it.id == userId }
        remove(user)
    }

    private fun acceptFriendRequest(userId: Long) {
        network.acceptRequest(
            userId = userId,
            onSuccess = this::acceptFriendRequestSuccess,
            onError = this::genericError,
        )
    }

    private fun acceptFriendRequestSuccess(
        statusCode: Int,
        responseBody: FriendshipResponse,
        userId: Long
    ) {
        Log.d(
            "FRIEND_LIST",
            "Successfully accepted friend request: $responseBody Status code: $statusCode"
        )
        val user: UserResponse = userList.first { it.id == userId }
        remove(user)
    }

    private fun removeFriend(userId: Long) {
        network.removeFriend(
            userId = userId,
            onSuccess = this::removeFriendSuccess,
            onError = this::genericError,
        )
    }

    private fun removeFriendSuccess(statusCode: Int, responseBody: Boolean, userId: Long) {
        Log.d(
            "FRIEND_LIST",
            "Successfully removed friend / friend request: $responseBody Status code: $statusCode"
        )
        val friend = userList.first { it.id == userId }
        remove(friend)
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }
}