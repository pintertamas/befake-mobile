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
import com.pintertamas.befake.adapter.UsersRecyclerViewAdapter
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.databinding.FragmentFriendBinding
import com.pintertamas.befake.databinding.FragmentMyFriendsBinding
import com.pintertamas.befake.databinding.FragmentUsersBinding
import com.pintertamas.befake.network.response.FriendshipResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService

class MyFriendsFragment : Fragment(R.layout.fragment_my_friends) {

    private var _binding: FragmentMyFriendsBinding? = null
    private val binding get() = _binding!!

    private lateinit var network: RetrofitService
    private lateinit var cache: CacheService
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedPrefName = "user_shared_preference"

    private lateinit var friendsRecyclerViewAdapter: UsersRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyFriendsBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)
        cache = CacheService.getInstance()!!
        cache.setNetworkService(network)

        setupFriendsRecyclerView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
    }

    private fun setupFriendsRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        friendsRecyclerViewAdapter =
            UsersRecyclerViewAdapter(UsersRecyclerViewAdapter.ListType.FRIEND, requireActivity())
        val list = binding.root.findViewById<RecyclerView>(R.id.friend_list_recycler_view)
        list.layoutManager = llm
        list.adapter = friendsRecyclerViewAdapter

        loadFriendList()
    }

    private fun loadFriendList() {
        network.loadFriendList(
            onSuccess = this::getFriendListSuccess,
            onError = this::generalError,
        )
    }

    private fun getFriendListSuccess(statusCode: Int, responseBody: List<Long>) {
        Log.d(
            "FRIEND_LIST",
            "Successfully queried friendlist: $responseBody Status code: $statusCode"
        )
        if (responseBody.isEmpty()) {
            binding.noFriends.visibility = View.VISIBLE
            binding.friendListRecyclerView.visibility = View.GONE
        } else {
            binding.noFriends.visibility = View.GONE
            binding.friendListRecyclerView.visibility = View.VISIBLE
        }
        responseBody.forEach {
            getUserByUserId(it)
        }
    }

    private fun getUserByUserId(userId: Long) {
        network.getUserByUserId(
            userId = userId,
            onSuccess = this::getUserByUserIdSuccess,
            onError = this::generalError,
        )
    }

    private fun getUserByUserIdSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "USER_BY_USERID",
            "Successfully queried user by userId: $responseBody Status code: $statusCode"
        )
        val user: UserResponse = responseBody
        friendsRecyclerViewAdapter.add(user)
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyFriendsFragment()
    }
}