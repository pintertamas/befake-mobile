package com.pintertamas.befake.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pintertamas.befake.R
import com.pintertamas.befake.adapter.PostsRecyclerViewAdapter
import com.pintertamas.befake.databinding.FragmentListPostsBinding
import com.pintertamas.befake.network.response.PostResponse

class ListPostsFragment : Fragment(), PostsRecyclerViewAdapter.PostListItemClickListener {

    private var _binding: FragmentListPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var postsRecyclerViewAdapter: PostsRecyclerViewAdapter
    private var itemDetailFragmentContainer: View? = null

    companion object {

        fun newInstance(): ListPostsFragment {
            return ListPostsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        /*binding.postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.userCard.userCardWidget.visibility == View.VISIBLE) {
                    //Hide
                } else if (dy < 0 && binding.userCard.userCardWidget.visibility != View.VISIBLE) {
                    //Show
                }
            }
        })*/
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val demoData = mutableListOf(
            PostResponse(
                1L,
                1L,
                "2022-Oct-23_22:32:24_main.jpg",
                "2022-Oct-31_15:24:48_selfie.png",
                "description1",
                location = "Budapest",
                postingTime = "2022.11.01.",
                beFakeTime = "2011.11.01.",
                deleted = false
            ),
            PostResponse(
                2L,
                2L,
                "2022-Oct-23_22:32:24_selfie.jpg",
                "2022-Oct-25_23:29:53_selfie.jpg",
                "description2",
                location = "Bukarest",
                postingTime = "2022.11.01.",
                beFakeTime = "2011.11.01.",
                deleted = false
            ),
            PostResponse(
                3L,
                3L,
                "2022-Oct-30_20:16:46_main.jpg",
                "2022-Oct-31_15:24:48_selfie.png",
                "description3",
                location = "Vienna",
                postingTime = "2022.11.01.",
                beFakeTime = "2011.11.01.",
                deleted = false
            )
        )
        val llm = LinearLayoutManager(this.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        postsRecyclerViewAdapter = PostsRecyclerViewAdapter()
        postsRecyclerViewAdapter.itemClickListener = this
        postsRecyclerViewAdapter.addAll(demoData)
        val list = binding.root.findViewById<RecyclerView>(R.id.posts_recycler_view)
        list.layoutManager = llm
        list.adapter = postsRecyclerViewAdapter
    }

    override fun onItemClick(post: PostResponse) {
        Log.d("POST_CLICK", "Clicked " + post.id)
    }
}
