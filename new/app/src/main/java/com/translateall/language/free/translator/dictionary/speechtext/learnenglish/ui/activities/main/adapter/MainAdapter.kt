package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainAdapter(fragment: FragmentActivity, private val fragments: List<Fragment>) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getFragment(index: Int): Fragment {
        return fragments[index]
    }
}