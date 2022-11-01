package com.pintertamas.befake.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pintertamas.befake.fragment.ListPostsFragment
import com.pintertamas.befake.fragment.NewPostFragment

class PostFragmentAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) NewPostFragment()
        else ListPostsFragment()
    }
}