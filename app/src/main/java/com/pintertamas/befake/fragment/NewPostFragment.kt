package com.pintertamas.befake.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pintertamas.befake.R
import com.pintertamas.befake.databinding.FragmentNewPostBinding
import java.util.*

class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    companion object {

        fun newInstance(): NewPostFragment {
            return NewPostFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        binding.postBefakeButton.setOnClickListener {
            Log.d("FRAGMENT", "Attached")
            val fr = fragmentManager?.beginTransaction()
            fr?.replace(R.id.new_post_fragment, newInstance(), "newPost")
            fr?.commit()
        }

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
