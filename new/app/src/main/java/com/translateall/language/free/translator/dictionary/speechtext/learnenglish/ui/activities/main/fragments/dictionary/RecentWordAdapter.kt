package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.RecentWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.RecentWordItemBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class RecentWordAdapter(
    private val callbacks: RecentWordCallbacks
) : RecyclerView.Adapter<RecentWordAdapter.ViewHolder>() {
    private var recentList: ArrayList<RecentWord> = ArrayList()
    private var selection = false

    class ViewHolder(var binding: RecentWordItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecentWordItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return recentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recentList[position]

        holder.binding.wordTV.text = item.word
        holder.binding.detailTV.text = item.displayMeaning
        if (item.bookmark) {
            holder.binding.bookmarkIV.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_dictionary_bookmark_selected
                )
            )
        } else {
            holder.binding.bookmarkIV.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_dictionary_bookmark_unselected
                )
            )
        }

        if (selection) {
            holder.binding.actionsGroup.gone()
            holder.binding.checkIV.visible()
            if (item.isSelected) {
                holder.binding.checkIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_recent_chcek
                    )
                )
                holder.binding.parentCL.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.bg_recent_word_selected
                )
            } else {
                holder.binding.checkIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_recent_unchcek
                    )
                )
                holder.binding.parentCL.background =
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_recent_word)
            }
        } else {
            holder.binding.checkIV.gone()
            holder.binding.actionsGroup.visible()
            holder.binding.parentCL.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_recent_word)
        }

        holder.binding.parentCL.setOnClickListener {
                callbacks.onWordCalled(item.word, position)
        }
        /*holder.binding.copyIV.setOnClickListener {
            if (isDoubleClick()) {
                callbacks.onCopyCalled(item)
            }
        }*/
        holder.binding.bookmarkIV.setOnClickListener {
            if (isDoubleClick()) {
                callbacks.onBookmarkCalled(item)
            }
        }
        holder.binding.shareIV.setOnClickListener {
            if (isDoubleClick()) {
                callbacks.onShareCalled(item)
            }
        }
        holder.binding.root.setOnLongClickListener {
            callbacks.onLongClick(position)
            true
        }

    }

    fun updateList(list: List<RecentWord>) {
        recentList.clear()
        recentList.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelection(): Boolean {
        return selection
    }

    fun changeSelectionState(state: Boolean) {
        selection = state
        notifyDataSetChanged()
    }

    val data: List<RecentWord>
        get() = recentList
    val isAnyItemSelected: Boolean
        get() {
            for (obj in data) {
                if (obj.isSelected) return true
            }
            return false
        }
    val allSelectedItems: ArrayList<String>
        get() {
            val selectedObjects = ArrayList<String>()
            if (data.isNotEmpty()) {
                for (item in data) {
                    if (item.isSelected) selectedObjects.add(item.word)
                }
            }
            return selectedObjects
        }

    fun clearSelection() {
        for (obj in data) {
            obj.isSelected = false
        }
        setSelection(false)
    }

    fun unselectAll() {
        for (obj in data) {
            obj.isSelected = false
        }
        callbacks.onSelectionChange(selection, allSelectedItems.size)
        notifyDataSetChanged()
    }

    fun setChecked(position: Int) {
        data[position].isSelected = !data[position].isSelected
        if (!isAnyItemSelected && !selection) {
            setSelection(false)
        } else {
            callbacks.onSelectionChange(true, allSelectedItems.size)
            if (allSelectedItems.size >= recentList.size) {
                callbacks.onSelectAll(true, allSelectedItems.size)
            } else {
                callbacks.onSelectAll(false, 0)
            }
        }
        notifyItemChanged(position)
    }

    fun setCheckedAll(selection: Boolean) {
        for (obj in data) {
            obj.isSelected = selection
        }
        this.selection = selection
        if (allSelectedItems.size >= recentList.size) {
            callbacks.onSelectAll(true, allSelectedItems.size)
        } else {
            callbacks.onSelectAll(false, 0)
        }
        notifyDataSetChanged()
    }

    fun isSelection(): Boolean {
        return selection
    }

    fun setSelection(selection: Boolean) {
        this.selection = selection
        callbacks.onSelectionChange(selection, allSelectedItems.size)
        notifyDataSetChanged()
    }

    interface RecentWordCallbacks {
        fun onWordCalled(word: String, position: Int)
        fun onCopyCalled(item: RecentWord)
        fun onBookmarkCalled(item: RecentWord)
        fun onShareCalled(item: RecentWord)
        fun onSelectionChange(selection: Boolean, count: Int)
        fun onSelectAll(isAllSelected: Boolean, count: Int)
        fun onLongClick(position: Int)
    }

}