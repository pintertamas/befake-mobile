package com.pintertamas.befake.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private var userId: Long = -1L

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
        userId = sharedPreferences.getLong("userId", -1)
        network = RetrofitService(token!!)
        cache = CacheService.getInstance()!!
        cache.setNetworkService(network)

        setupUsersRecyclerView()
        setupPendingRecyclerView()

        binding.etSearch.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    // we don't know the new length of the recycler list after filtering, so using notifyDataSetChanged() is not a bad practice here
                    usersRecyclerViewAdapter.notifyDataSetChanged()
                }
            }
        )

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
    }

    private fun setupUsersRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        usersRecyclerViewAdapter =
            UsersRecyclerViewAdapter(UsersRecyclerViewAdapter.ListType.USER, requireActivity())
        val list = binding.root.findViewById<RecyclerView>(R.id.user_list_recycler_view)
        //println(userList)
        //val filteredUserList = userList.filter { it.id != userId }
        //usersRecyclerViewAdapter.addAll(filteredUserList)
        list.layoutManager = llm
        list.adapter = usersRecyclerViewAdapter

        loadFriendList()
    }

    private fun setupPendingRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        pendingRecyclerViewAdapter =
            UsersRecyclerViewAdapter(UsersRecyclerViewAdapter.ListType.PENDING, requireActivity())
        val list = binding.root.findViewById<RecyclerView>(R.id.pending_list_recycler_view)
        list.layoutManager = llm
        list.adapter = pendingRecyclerViewAdapter

        loadPendingRequests()
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
        println(userList)
        val filteredUserList = userList.filter { !responseBody.contains(it.id) && it.id != userId }
        print(filteredUserList)
        usersRecyclerViewAdapter.addAll(filteredUserList)
    }

    private fun loadPendingRequests() {
        network.loadPendingRequests(
            onSuccess = this::getPendingRequestsSuccess,
            onError = this::generalError,
        )
    }

    private fun getPendingRequestsSuccess(statusCode: Int, responseBody: List<FriendshipResponse>) {
        Log.d(
            "PENDING_REQUESTS",
            "Successfully queried pending requests: $responseBody Status code: $statusCode"
        )
        val friendships = responseBody
        val incoming = friendships.filter { it.user1Id != userId }
        val outgoing = friendships.filter { it.user2Id != userId }

        incoming.forEach {
            getUserByUserId(it.user1Id, this::getIncomingRequestUsersSuccess)
            binding.friendRequests.visibility = View.VISIBLE
            binding.pendingListRecyclerView.visibility = View.VISIBLE
        }

        outgoing.forEach {
            getUserByUserId(it.user2Id, this::getOutgoingRequestUsersSuccess)
            binding.friendRequests.visibility = View.VISIBLE
            binding.pendingListRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun getUserByUserId(userId: Long, onSuccess: (Int, UserResponse) -> Unit) {
        network.getUserByUserId(
            userId = userId,
            onSuccess = onSuccess,
            onError = this::generalError,
        )
    }

    private fun getIncomingRequestUsersSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "USER_BY_USERID",
            "Successfully queried user by userId: $responseBody Status code: $statusCode"
        )
        val user: UserResponse = responseBody
        pendingRecyclerViewAdapter.add(user)
        usersRecyclerViewAdapter.remove(user)
    }

    private fun getOutgoingRequestUsersSuccess(statusCode: Int, responseBody: UserResponse) {
        Log.d(
            "USER_BY_USERID",
            "Successfully queried user by userId: $responseBody Status code: $statusCode"
        )
        val user: UserResponse = responseBody
        usersRecyclerViewAdapter.remove(user)
    }

    private fun generalError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call!")
        e.printStackTrace()
    }

    companion object {
        @JvmStatic
        fun newInstance(userList: List<UserResponse>) = UsersFragment(userList)
    }
}