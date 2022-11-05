package com.pintertamas.befake.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.PostItemBinding
import com.pintertamas.befake.databinding.UserCardBinding
import com.pintertamas.befake.databinding.UserItemBinding
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso

class UsersRecyclerViewAdapter(private val listType: ListType) :
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
                binding.confirmAddButton.setText(R.string.add)
                binding.removeFriendButton.visibility = View.GONE
                binding.confirmAddButton.visibility = View.VISIBLE
                binding.confirmAddButton.setOnClickListener {
                    addFriend(userList[position].id)
                }
            } else if (listType == ListType.PENDING) {
                binding.confirmAddButton.setText(R.string.confirm)
                binding.removeFriendButton.visibility = View.GONE
                binding.confirmAddButton.visibility = View.VISIBLE
                binding.confirmAddButton.setOnClickListener {
                    acceptFriendRequest(userList[position].id)
                }
            } else if (listType == ListType.FRIEND) {
                binding.confirmAddButton.visibility = View.GONE
                binding.removeFriendButton.visibility = View.VISIBLE
                binding.removeFriendButton.setOnClickListener {
                    removeFriend(userList[position].id)
                }
            }
        }
    }

    private fun removeFriend(userId: Long) {
        TODO("Not yet implemented")
    }

    private fun acceptFriendRequest(userId: Long) {
        TODO("Not yet implemented")
    }

    private fun addFriend(userId: Long) {
        TODO("Not yet implemented")
    }
}