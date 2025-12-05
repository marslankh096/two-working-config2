package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryBookmark

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.BookmarkWord
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityDictionaryBookmarkBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryDetail.DictionaryDetailActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.copyWordData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getBottomDialogs
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWordData
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class DictionaryBookmarkActivity : BaseActivity() {
    private lateinit var binding: ActivityDictionaryBookmarkBinding
    private lateinit var adapter: BookmarkAdapter
    private val viewModel: BookmarkViewModel by viewModel()
    private var isAllSelection = false
    private var deleteDialog: Dialog? = null
    private var trackRespond = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDictionaryBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Android 15+ edge-to-edge setup (only once, here)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ViewCompat.setOnApplyWindowInsetsListener(requireNotNull(binding).root) { view, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

                // Apply padding to your view using KTX extension
                view.updatePadding(
                    left = systemBarInsets.left,
                    top = systemBarInsets.top,
                    right = systemBarInsets.right,
                    bottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)
                )

                // Consume the insets if you've handled them
                WindowInsetsCompat.CONSUMED
            }
        }

        init()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        if (trackRespond) {
            trackRespond = false
        }
    }

    private fun init() {

        setRecentAdapter()
        viewModel.getBookmarkList()
        viewModel.bookmarkList.observe(this) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.noBookmarkGroup.visibility = GONE
                    binding.bookmarkRV.visibility = VISIBLE
                    adapter.updateList(it)
                } else {
                    binding.bookmarkRV.visibility = GONE
                    binding.noBookmarkGroup.visibility = VISIBLE
                }
            } else {
                binding.bookmarkRV.visibility = GONE
                binding.noBookmarkGroup.visibility = VISIBLE
            }
        }
        setDeleteDialog()
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
        )

        binding.ivBack.setOnClickListener {
            backPressed()
        }

        binding.selectionIV.setOnClickListener {
            if (isDoubleClick()) {
                if (isAllSelection) {
                    isAllSelection = false
                    adapter.unselectAll()
                } else {
                    adapter.setCheckedAll(true)
                }
            }
        }

        binding.deleteIV.setOnClickListener {
            if (isDoubleClick()) {
                deleteDialog?.show()
            }
        }

    }

    private fun setRecentAdapter() {
        adapter = BookmarkAdapter(object : BookmarkAdapter.BookmarkWordCallbacks {
            override fun onWordCalled(word: String, position: Int) {
                if (adapter.isSelection()) {
                    adapter.setChecked(position)
                } else {
                    launchDetailScreen(word)
                }
            }

            override fun onCopyCalled(item: BookmarkWord) {
                copyWordData(Gson().fromJson(item.response, WordResponse::class.java))
            }

            override fun onBookmarkCalled(item: BookmarkWord) {
                viewModel.deleteBookmark(item)
            }

            override fun onShareCalled(item: BookmarkWord) {
                shareWordData(Gson().fromJson(item.response, WordResponse::class.java))
            }

            override fun onLongClick(position: Int) {
                adapter.let {
                    if (!it.isSelection()) {
                        it.setSelection(true)
                    }
                    it.setChecked(position)
                }
            }

            override fun onSelectionChange(selection: Boolean, count: Int) {
//                isAllSelection = selection
                if (selection) {
                    binding.titleTV.text = getString(R.string.label_selected, count)
                    binding.deleteIV.visible()
                    binding.selectionIV.visible()
                } else {
//                    binding.deleteIV.gone()
//                    binding.selectionIV.gone()
                }

                if (count == viewModel.bookmarkList.value?.size) {
                    binding.selectionIV.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DictionaryBookmarkActivity,
                            R.drawable.ic_selection_check
                        )
                    )
                } else {
                    binding.selectionIV.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DictionaryBookmarkActivity,
                            R.drawable.ic_selection_uncheck
                        )
                    )
                }

                /*if (selection) {
                    binding.deleteIV.visible()
                    binding.selectionIV.visible()
                } else {
                    binding.titleTV.text = getString(R.string.bookmarked)
                    binding.deleteIV.gone()
                    binding.selectionIV.gone()
                }*/
            }

            override fun onSelectAll(isAllSelected: Boolean, count: Int) {
                isAllSelection = isAllSelected
                if (isAllSelected) {
                    binding.selectionIV.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DictionaryBookmarkActivity,
                            R.drawable.ic_selection_check
                        )
                    )
                } else {
                    binding.selectionIV.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DictionaryBookmarkActivity,
                            R.drawable.ic_selection_uncheck
                        )
                    )
                }
                if (isAllSelected) {
                    val counted = getString(R.string.label_selected, count)
                    binding.titleTV.text = counted
                }
            }

        })

        binding.bookmarkRV.layoutManager = LinearLayoutManager(this)
        binding.bookmarkRV.adapter = adapter
    }

    private fun setDeleteDialog() {
        deleteDialog = getBottomDialogs(
            R.layout.dlg_clear_history,
            getString(R.string.delete_permanently),
            getString(R.string.are_you_sure_you_want_to_delete_selected_words),
            onCancel = {
                deleteDialog?.dismiss()
            },
            onDelete = {
                viewModel.deleteBookmarkList(adapter.allSelectedItems)
                adapter.clearSelection()
                binding.titleTV.text = getString(R.string.bookmarked)
                binding.deleteIV.gone()
                binding.selectionIV.gone()
                deleteDialog?.dismiss()
            })
    }

    private fun backPressed() {
        if (isDoubleClick()) {
            if (adapter.isSelection()) {
                adapter.clearSelection()
                binding.titleTV.text = getString(R.string.bookmarked)
                binding.deleteIV.gone()
                binding.selectionIV.gone()
            } else {
                adapter.clearSelection()
                finish()
            }
        }
    }

    private fun launchDetailScreen(word: String) {
        trackRespond = true
        startActivity(
            Intent(
                this@DictionaryBookmarkActivity,
                DictionaryDetailActivity::class.java
            ).putExtra("detail", word)
                .putExtra("isWOD", false)
        )
    }
}