package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ItemPhraseSubcategoryBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick

class PhraseSubAdapter(
    private val list: ArrayList<Pair<String, Int>>,
    private val callback: (String) -> Unit
) : RecyclerView.Adapter<PhraseSubAdapter.ViewHolder>() {
    class ViewHolder(var binding: ItemPhraseSubcategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPhraseSubcategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            subcategoryTV.text = data.first
            subcategorySizeTV.text = data.second.toString()

            subcategoryCL.setOnClickListener {
                if (isDoubleClick()) {
                    callback.invoke(data.first)
                }
            }
        }

    }

}