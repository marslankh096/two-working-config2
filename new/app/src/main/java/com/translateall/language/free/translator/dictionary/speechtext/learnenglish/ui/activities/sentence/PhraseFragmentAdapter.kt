package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class PhraseFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = mutableListOf<Fragment>()
    private val titles = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        titles.add(title)
        notifyDataSetChanged()
    }
    fun remove(index: Int) {
        fragments.removeAt(index)
        notifyItemChanged(index)
    }
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getPageTitle(position: Int): CharSequence = titles[position]

    fun getFragment(index: Int) = fragments[index]

}