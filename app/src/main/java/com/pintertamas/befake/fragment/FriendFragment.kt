package com.pintertamas.befake.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pintertamas.befake.R
import com.pintertamas.befake.databinding.FragmentFriendBinding
import com.pintertamas.befake.databinding.UserItemBinding
import com.pintertamas.befake.network.response.UserResponse

class FriendFragment(private val userList: List<UserResponse>) :
    Fragment(R.layout.fragment_friend) {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        val fragment: Fragment = UsersFragment.newInstance(userList)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.friend_fragment_container, fragment)
            .commit()
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
        }
        binding.myFriendsButton.setOnClickListener {
            binding.addFriendsButton.setBackgroundResource(R.drawable.underline_dark)
            binding.myFriendsButton.setBackgroundResource(R.drawable.underline_thick)

            binding.addFriendsButton.typeface = Typeface.DEFAULT
            binding.myFriendsButton.typeface = Typeface.DEFAULT_BOLD

            val myFriendsFragment: Fragment = MyFriendsFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.friend_fragment_container, myFriendsFragment)
                .commit()
        }

        binding.addFriendsButton.setOnClickListener {
            binding.myFriendsButton.setBackgroundResource(R.drawable.underline_dark)
            binding.addFriendsButton.setBackgroundResource(R.drawable.underline_thick)

            binding.myFriendsButton.typeface = Typeface.DEFAULT
            binding.addFriendsButton.typeface = Typeface.DEFAULT_BOLD

            val myFriendsFragment: Fragment = UsersFragment.newInstance(userList)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.friend_fragment_container, myFriendsFragment)
                .commit()
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    companion object {
        @JvmStatic
        fun newInstance(userList: List<UserResponse>) = FriendFragment(userList)
    }
}