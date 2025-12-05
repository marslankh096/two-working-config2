package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ItemPhraseCategoryBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseCategory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getResourceIconId
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class PhraseMainAdapter(
    private val list: List<PhraseCategory>,
    private val callBack: (Int, String, String) -> Unit
) : RecyclerView.Adapter<PhraseMainAdapter.ViewHolder>() {
    var previousPosition = -1
    class ViewHolder(var binding: ItemPhraseCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPhraseCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            if (data.isExpanded) {
                subcategoryRV.visible()
                categoryArrowIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        root.context,
                        R.drawable.ic_category_arrow_up
                    )
                )
            } else {
                subcategoryRV.gone()
                categoryArrowIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        root.context,
                        R.drawable.ic_category_arrow_down
                    )
                )
            }
            titleTV.text = data.name
            subtitleTV.text = data.subText
            categoryIV.setImageResource(root.context.getResourceIconId(data.icon))

            subcategoryRV.layoutManager = LinearLayoutManager(root.context)
            subcategoryRV.adapter = PhraseSubAdapter(data.subcategoryList) {
                callBack.invoke(data.id,data.name, it)
            }

            framePhraseType.setOnClickListener {
                if (position >= 0 && position < list.size) {
                    list[position].isExpanded = !list[position].isExpanded

                    if (previousPosition != -1 && previousPosition != position
                        && previousPosition < list.size
                    ) {
                        list[previousPosition].isExpanded = false
                        notifyItemChanged(previousPosition)
                    }

                    notifyItemChanged(position)
                    previousPosition = position
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}