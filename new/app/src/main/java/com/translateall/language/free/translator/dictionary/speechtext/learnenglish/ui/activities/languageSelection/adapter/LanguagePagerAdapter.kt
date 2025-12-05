package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class LanguagePagerAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {

    private val mFragments: ArrayList<Fragment> = ArrayList()
    override fun getCount(): Int {
        return mFragments.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    fun addFragment(fragment: Fragment) {
        mFragments.add(fragment)

    }
}