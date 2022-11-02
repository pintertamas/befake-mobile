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

class NewPostFragment : Fragment(R.layout.fragment_new_post) {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        binding.postBefakeButton.setOnClickListener {
            Log.d("FRAGMENT", "Attached")
        }

        Log.d("NEW_POST", "Click")

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
