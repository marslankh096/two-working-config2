package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.LanguageItemLayoutBinding

class LanguageAdapter(
    private val languages: List<String>,
    private val flagList: ArrayList<Int>,
    private val callBack: () -> Unit
) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    //    var selectedLanguage = "English"
    var selectedLanguage = ""
    private var newSelected: Int = 0
    private var oldSelected: Int = 0

    class ViewHolder(var binding: LanguageItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LanguageItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.languageNameTV.text = languages[position]
        /*Glide.with(holder.itemView.context)
            .load(flagList[position])
            .into(holder.binding.languageIV)*/
        holder.binding.flagIV.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                flagList[position]
            )
        )

        holder.binding.langRB.isChecked = selectedLanguage == languages[position]
        if (selectedLanguage == languages[position]) {
            oldSelected = newSelected
            newSelected = holder.adapterPosition
            holder.binding.mainCL.background = ContextCompat.getDrawable(
                holder.binding.root.context,
                R.drawable.bg_app_lang_selected
            )
        } else {
            holder.binding.mainCL.background = ContextCompat.getDrawable(
                holder.binding.root.context,
                R.drawable.bg_app_lang_unselected
            )
        }

        holder.binding.langRB.isEnabled = false
        holder.binding.langRB.isClickable = false
        holder.binding.mainCL.setOnClickListener {
            oldSelected = newSelected
            newSelected = holder.adapterPosition
            selectedLanguage = languages[position]
            notifyItemChanged(oldSelected)
            notifyItemChanged(newSelected)
            if (newSelected != oldSelected) {
                callBack.invoke()
            }
//            Toast.makeText(holder.binding.root.context, selectedLanguage, Toast.LENGTH_SHORT).show()
//            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return languages.size
    }

}