package com.pintertamas.befake.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import com.pintertamas.befake.LoginActivity
import com.pintertamas.befake.R
import com.pintertamas.befake.databinding.FragmentProfileBinding
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.database.repository.CacheService
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso


class ProfileFragment(
    private var user: UserResponse,
) :
    Fragment(R.layout.fragment_profile), EditProfileFragment.EditedUserListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var network: RetrofitService
    private lateinit var cache: CacheService
    private var picasso: Picasso = Picasso.get()

    private val sharedPrefName = "user_shared_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        picasso = Picasso.Builder(requireContext()).build()
        picasso.setIndicatorsEnabled(true)

        val token = sharedPreferences.getString("jwt", "")
        network = RetrofitService(token!!)
        cache = CacheService.getInstance()!!
        println(user.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.backButton.setOnClickListener {
            Log.d("BACK_ARROW", "Click")
            activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            val listPostsFragment: Fragment =
                requireActivity().supportFragmentManager.findFragmentByTag("LIST_POST_FRAGMENT")!!
            val fragment: Fragment = EditProfileFragment.newInstance(
                user,
                listOf(
                    this,
                    listPostsFragment as EditProfileFragment.EditedUserListener
                )
            )
            replaceFragment(fragment)
        }

        binding.logoutButton.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.remove("userId")
            editor.remove("username")
            editor.remove("email")
            editor.remove("jwt")
            editor.apply()
            cache.clear()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            requireActivity().finish()
            startActivity(intent)
        }

        rebuildView()

        return binding.root
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container_view, fragment)
        fragmentTransaction.addToBackStack(fragment.id.toString())
        fragmentTransaction.commit()
    }

    override fun onDetach() {
        super.onDetach()
        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
    }

    override fun updateUserDetails(user: UserResponse) {
        this.user = user
        refreshView()
    }

    private fun refreshView() {
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
        rebuildView()
        println("Refreshed")
    }

    private fun rebuildView() {
        binding.etFullName.text = user.fullName ?: ""
        val usernameTag = "@${user.username}"
        binding.etUsername.text = usernameTag
        binding.etBiography.text = user.biography ?: ""
        binding.etLocation.text = user.location ?: ""
        cache.cacheProfilePicture(user, binding.civProfilePicture)
    }

    companion object {
        @JvmStatic
        fun newInstance(user: UserResponse) = ProfileFragment(user)
    }
}