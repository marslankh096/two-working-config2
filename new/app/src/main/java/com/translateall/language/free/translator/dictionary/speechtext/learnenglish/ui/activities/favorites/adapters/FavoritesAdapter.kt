package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.favorites.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils

class FavoritesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataList: List<TranslationHistory> = ArrayList()
    private var inputWord: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.li_favorites, parent, false)

        return FavoriteItemHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val historyHolder = holder as FavoriteItemHolder
        val model = dataList[position]
        historyHolder.bindData(model, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(data: List<TranslationHistory>) {
        this.dataList = data
        notifyDataSetChanged()
    }

    inner class FavoriteItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvInputWord = itemView.findViewById<TextView>(R.id.tv_history_input_word)
        var tvTranslatedWord = itemView.findViewById<TextView>(R.id.tv_history_translated_word)
        var line = itemView.findViewById<View>(R.id.view_line_history)
        var rootItem = itemView.findViewById<ConstraintLayout>(R.id.root_favorite)
        var ivFavorite = itemView.findViewById<ImageView>(R.id.iv_star_history)


        fun bindData(model: TranslationHistory, position: Int) {
            tvInputWord.text = model.inputWord
            tvTranslatedWord.text = model.translatedWord
            inputWord = model.inputWord

            val isFavorite: Boolean = !model.isFavorite
            if (model.isFavorite) {
                ivFavorite.setImageResource(R.drawable.ic_star_history_fill)
            } else {
                ivFavorite.setImageResource(R.drawable.ic_star_history_unfil)

            }

            ivFavorite.setOnClickListener {
                AppUtils.favoriteHistoryItem?.invoke(model, isFavorite)
            }


            if (position == dataList.size - 1) {
                line.visibility = View.GONE
            } else {
                View.VISIBLE
            }
            if (position == 0 && dataList.size == 1) {
                rootItem.setBackgroundResource(R.drawable.bg_favorite_single)
            } else if (position == 0 && dataList.size > 1) {
                rootItem.setBackgroundResource(R.drawable.bg_favorite_top_cornored)
            } else if (position == dataList.size - 1 && dataList.size > 1) {
                rootItem.setBackgroundResource(R.drawable.bg_favorite_bottom_cornored)
            } else if (position > 0 && dataList.size > 1 && position != dataList.size - 1) {
                rootItem.setBackgroundResource(R.drawable.bg_favorite_middle)
            }

            itemView.setOnClickListener {
                AppUtils.onClickFavoriteItem?.invoke(model)
            }

        }

    }


}