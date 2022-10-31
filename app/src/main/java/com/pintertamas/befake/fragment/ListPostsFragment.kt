package com.pintertamas.befake.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pintertamas.befake.databinding.FragmentListPostsBinding
import java.util.*

class ListPostsFragment : Fragment() {

    private var _binding: FragmentListPostsBinding? = null
    private val binding get() = _binding!!

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
        binding.editMe.text = UUID(10, 1).toString()



        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
