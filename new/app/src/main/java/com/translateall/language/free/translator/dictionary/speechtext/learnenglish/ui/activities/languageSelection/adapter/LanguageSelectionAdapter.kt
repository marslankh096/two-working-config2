package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.LiLanguageSelectorBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.onSelectLanguage
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick

class LanguageSelectionAdapter :
    RecyclerView.Adapter<LanguageSelectionAdapter.LanguageItemHolder>() {

    var languagesList: List<LanguageModel> = ArrayList()
    var mSelectedPosition: Int = 0

    companion object {
        private val VIEW_HEADER = 0
        private val VIEW_DATA = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageItemHolder {
        return LanguageItemHolder(
            LiLanguageSelectorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LanguageItemHolder, position: Int) {
        val langModel = languagesList[position]
        holder.binding.apply {
            tvLanguageName.text = langModel.languageName

            if (langModel.langMean.isNotEmpty()) {
                tvLangMeaning.text = "(${langModel.langMean})"
            } else {
                tvLangMeaning.text = ""
            }

            when (position) {

                languagesList.size - 1 -> {
                    lineLang.visibility = View.GONE
                }

                else -> {
                    lineLang.visibility = View.VISIBLE
                }
            }
            if (position == 0) {
                tvHeaderRecyclerLang.visibility = View.VISIBLE
            } else {
                tvHeaderRecyclerLang.visibility = View.GONE
            }

            languageItem.setOnClickListener {
                if (isDoubleClick()) {
                    if (langModel != null) {
                        if (langModel.languageCode != null && langModel.languageName != null && langModel.languageName != null)
                            onSelectLanguage?.invoke(langModel, position)
                    }
                }
            }
            if (position == mSelectedPosition) {
                ivLangSelected.visibility = View.VISIBLE
                tvLanguageName.setTextColor(Color.parseColor("#4286f5"))
                tvLangMeaning.setTextColor(Color.parseColor("#4286f5"))
            } else {
                ivLangSelected.visibility = View.GONE
                tvLanguageName.setTextColor(Color.parseColor("#000000"))
                tvLangMeaning.setTextColor(Color.parseColor("#5B6D80"))
            }

        }
    }

    override fun getItemCount(): Int {
        return languagesList.size
    }

    fun setData(data: List<LanguageModel>, selectedPosition: Int) {
        this.languagesList = data
        this.mSelectedPosition = selectedPosition
        notifyDataSetChanged()
    }


    class LanguageItemHolder(val binding: LiLanguageSelectorBinding) :
        RecyclerView.ViewHolder(binding.root)

//    inner class LanguageHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//    }

}