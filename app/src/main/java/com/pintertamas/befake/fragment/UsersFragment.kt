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
import com.pintertamas.befake.databinding.FragmentUsersBinding
import com.pintertamas.befake.network.response.FriendshipResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService

class UsersFragment(private val userList: List<UserResponse>) :
    Fragment(R.layout.fragment_users) {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var pendingRecyclerViewAdapter: UsersRecyclerViewAdapter
    private lateinit var usersRecyclerViewAdapter: UsersRecyclerViewAdapter

    private lateinit var network: RetrofitService
    private lateinit var cache: CacheService
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt", "")
        val userId = sharedPreferences.getLong("userId", 0)
        network = RetrofitService(token!!)
        cache = CacheService.getInstance()!!
        cache.setNetworkService(network)

        setupUsersRecyclerView()
        setupPendingRecyclerView()
        return binding.root
    }

    private fun setupUsersRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        usersRecyclerViewAdapter = UsersRecyclerViewAdapter(UsersRecyclerViewAdapter.ListType.USER)
        val list = binding.root.findViewById<RecyclerView>(R.id.user_list_recycler_view)
        println(userList)
        usersRecyclerViewAdapter.addAll(userList)
        list.layoutManager = llm
        list.adapter = usersRecyclerViewAdapter
    }

    private fun setupPendingRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        pendingRecyclerViewAdapter = UsersRecyclerViewAdapter(UsersRecyclerViewAdapter.ListType.PENDING)
        val list = binding.root.findViewById<RecyclerView>(R.id.pending_list_recycler_view)
        list.layoutManager = llm
        list.adapter = pendingRecyclerViewAdapter

        loadPendingRequests()
    }

    private fun loadPendingRequests() {
        network.loadPendingRequests(
            onSuccess = this::getPendingRequestsSuccess,
            onError = this::genericError,
        )
    }

    private fun getPendingRequestsSuccess(statusCode: Int, responseBody: List<FriendshipResponse>) {
        Log.d(
            "PENDING_REQUESTS",
            "Successfully queried pending requests: $responseBody Status code: $statusCode"
        )
        responseBody.forEach {
            getUserByUserId(it.user2Id)
            binding.friendRequests.visibility = View.VISIBLE
            binding.pendingListRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun getUserByUserId(userId: Long) {
        network.getUserByUserId(
            userId = userId,
            onSuccess = this::getUserByUserIdSuccess,
            onError = this::genericError,
        )
    }

    private fun getUserByUserIdSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "USER_BY_USERID",
            "Successfully queried user by userId: $responseBody Status code: $statusCode"
        )
        val user: UserResponse = responseBody
        pendingRecyclerViewAdapter.add(user)
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }

    companion object {
        @JvmStatic
        fun newInstance(userList: List<UserResponse>) = UsersFragment(userList)
    }
}