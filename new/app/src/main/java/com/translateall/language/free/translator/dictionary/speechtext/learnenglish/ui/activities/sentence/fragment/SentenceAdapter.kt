package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.sentence.fragment

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ItemPhraseSentenceBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ItemPhraseTranslationBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhrasesSentences
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClickForCopy
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class SentenceAdapter(
    private val speakCallBack: (sentence: PhrasesSentences, position: Int) -> Unit,
    private val favouriteCallBack: (sentence: PhrasesSentences, position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }

    var previousPosition = -1
    var animationPosition = -1
    private var sentenceList: List<PhrasesSentences> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<PhrasesSentences>, position: Int?) {
        sentenceList = list
        position?.let {
            animationPosition = it
            notifyItemChanged(it)
        } ?: notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TWO) {
            val binding = ItemPhraseTranslationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            PhraseTranslationSentence(binding, speakCallBack)
        } else {
            val binding = ItemPhraseSentenceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            PhraseSentence(binding)
        }
    }

    override fun getItemCount(): Int {
        return sentenceList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (sentenceList[position].open) {
            (holder as PhraseTranslationSentence).bind(
                position,
                sentenceList[position]
            )

        } else {
            (holder as PhraseSentence).bind(
                position,
                sentenceList[position]
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (sentenceList[position].open) {
            VIEW_TYPE_TWO
        } else {
            VIEW_TYPE_ONE
        }
    }

    inner class PhraseSentence(val binding: ItemPhraseSentenceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, phraseSentence: PhrasesSentences) {
            binding.sentenceTV.text = phraseSentence.phraseSentences
            if (position == sentenceList.size - 1) {
                binding.lineView.gone()
            } else {
                binding.lineView.visible()
            }
            if (phraseSentence.favourite) {
                binding.ivFavorite.visible()
            } else {
                binding.ivFavorite.gone()
            }

            binding.ivFavorite.setOnClickListener {
                favouriteCallBack.invoke(phraseSentence, position)
            }
            binding.parentCL.setOnClickListener {
                if (position >= 0 && position < sentenceList.size) {
                    sentenceList[position].open = !sentenceList[position].open

                    if (previousPosition != -1 && previousPosition != position
                        && previousPosition < sentenceList.size
                    ) {
                        sentenceList[previousPosition].open = false
                        notifyItemChanged(previousPosition)
                    }

                    notifyItemChanged(position)
                    previousPosition = position
                }
            }
        }
    }

    inner class PhraseTranslationSentence(
        val binding: ItemPhraseTranslationBinding,
        private val speakPhrase: (sentence: PhrasesSentences, position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, phraseSentence: PhrasesSentences) {
            if (phraseSentence.phraseSentences != binding.sentenceTV.text)
                binding.sentenceTV.text = phraseSentence.phraseSentences
            if (phraseSentence.translation != binding.translationTV.text)
                binding.translationTV.text = phraseSentence.translation

            binding.ivFavorite.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    getStarImageId(phraseSentence.favourite)
                )
            )

            binding.ivCopyTranslated.setOnClickListener {
                if (isDoubleClickForCopy()) {
                    val clipboard =
                        (binding.root.context).getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(
                        "ocr_copy",
                        binding.translationTV.text.toString()
                    )
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        binding.root.context,
                        binding.root.context.getString(R.string.text_copied_successfully),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            binding.ivShareTranslation.setOnClickListener {
                if (isDoubleClick()) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, binding.translationTV.text.toString())
                    binding.root.context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Share via"
                        )
                    )
                }
            }
            binding.ivFavorite.setOnClickListener {
                favouriteCallBack.invoke(phraseSentence, position)
            }

            binding.ivSpeakTranslated.setOnClickListener {
                if (isDoubleClick()) {
                    speakPhrase.invoke(phraseSentence, position)
                }
            }

            binding.parentCL.setOnClickListener {
                sentenceList[position].open = false
                notifyItemChanged(position)
            }

        }

    }

    private fun getStarImageId(favourite: Boolean): Int {
        return if (favourite) {
            R.drawable.ic_favorite_fill
        } else {
            R.drawable.ic_favorite
        }
    }

}