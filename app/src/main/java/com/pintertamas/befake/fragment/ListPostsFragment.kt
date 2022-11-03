package com.pintertamas.befake.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.adapter.PostsRecyclerViewAdapter
import com.pintertamas.befake.databinding.FragmentListPostsBinding
import com.pintertamas.befake.network.response.PostResponse
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody

class ListPostsFragment(private val user: UserResponse) : Fragment(),
    PostsRecyclerViewAdapter.PostListItemClickListener {

    private var _binding: FragmentListPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var network: RetrofitService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var picasso: Picasso
    private var posts: List<PostResponse> = emptyList()

    private lateinit var postsRecyclerViewAdapter: PostsRecyclerViewAdapter

    private val sharedPrefName = "user_shared_preference"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListPostsBinding.inflate(inflater, container, false)

        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        picasso = Picasso.Builder(requireContext()).build()
        picasso.setIndicatorsEnabled(true)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)

        getTodaysPostByUser(user.id)

        setupRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    private fun getTodaysPostByUser(userId: Long) {
        network.getTodaysPostByUser(
            userId = userId,
            onSuccess = this::getTodaysPostByUserSuccess,
            onError = this::genericError
        )
    }

    private fun getTodaysPostByUserSuccess(statusCode: Int, responseBody: PostResponse) {
        Log.d(
            "GET_TODAYS_POST_BY_USER",
            "Successfully got today's post from user: $responseBody Status code: $statusCode"
        )
        postsRecyclerViewAdapter.setUserCard(user)
        postsRecyclerViewAdapter.setUserPost(responseBody)
        getPostsFromFriends()
    }

    private fun getPostsFromFriends() {
        network.getPostsFromFriends(
            onSuccess = this::getPostsFromFriendsSuccess,
            onError = this::genericError
        )
    }

    private fun getPostsFromFriendsSuccess(statusCode: Int, responseBody: List<PostResponse>) {
        Log.d(
            "GET_POSTS_FROM_FRIENDS",
            "Successfully got posts from friends: $responseBody Status code: $statusCode"
        )
        postsRecyclerViewAdapter.addAll(responseBody)
    }

    private fun getImageUrl(filename: String) {
        network.getImageUrl(
            filename = filename,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun getImageUrlSuccess(statusCode: Int, responseBody: ResponseBody) {
        Log.d(
            "GET_IMAGE_URL",
            "Successfully got image url: $responseBody Status code: $statusCode"
        )
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_CALL_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        postsRecyclerViewAdapter = PostsRecyclerViewAdapter()
        postsRecyclerViewAdapter.itemClickListener = this
        val list = binding.root.findViewById<RecyclerView>(R.id.posts_recycler_view)
        list.layoutManager = llm
        list.adapter = postsRecyclerViewAdapter
    }

    override fun onItemClick(post: PostResponse) {
        Log.d("POST_CLICK", "Clicked " + post.id)
    }

    companion object {
        @JvmStatic
        fun newInstance(user: UserResponse) = ListPostsFragment(user)
    }
}
