package com.pintertamas.befake.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import com.pintertamas.befake.R
import com.pintertamas.befake.databinding.FragmentNewPostBinding

class NewPostFragment : Fragment(R.layout.fragment_new_post) {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

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

    private val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val toolbar: View? = requireActivity().findViewById(R.id.toolbar)
                if (toolbar?.visibility == View.VISIBLE) {
                    requireActivity().finish()
                } else {
                    toolbar?.visibility = View.VISIBLE
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewPostFragment()
    }
}
