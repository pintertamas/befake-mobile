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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
    }

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment: Fragment? =
                        requireActivity().supportFragmentManager.findFragmentByTag("NEW_POST_FRAGMENT")
                    if (requireActivity()
                            .supportFragmentManager
                            .getBackStackEntryAt(
                                requireActivity()
                                    .supportFragmentManager.backStackEntryCount - 1
                            )
                            .name!!.toString() == fragment!!.id.toString()
                    )
                        requireActivity().finish()
                    else requireActivity().supportFragmentManager.popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }*/

    companion object {
        @JvmStatic
        fun newInstance() = NewPostFragment()
    }
}
