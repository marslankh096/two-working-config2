package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.DlgGoToPageBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GoToDialog : DialogFragment() {

    private var mContext: Context? = null
    private lateinit var binding: DlgGoToPageBinding
    private lateinit var mListener: GoToPageListener
    private var numLimit = -1

    companion object {
        fun newInstance(): GoToDialog {
            return GoToDialog()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    fun setPagesLimit(num: Int) {
        numLimit = num
        if (numLimit == null)
            numLimit = 0
    }

    fun initListener(mListener: GoToPageListener) {
        this.mListener = mListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.customDialog)
    }

    override fun onResume() {
        // Set the width of the dialog proportional to 90% of the screen width
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        if (window != null) {
            //            window.requestFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawableResource(android.R.color.transparent)

            val windowLayoutParams = window.attributes
            windowLayoutParams.dimAmount = 0.7f
        }
        super.onResume()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DlgGoToPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext?.let { ctx ->
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            binding.pageNumberET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isEmpty()) {
                        binding.doneTV.background =
                            ContextCompat.getDrawable(ctx, R.drawable.bg_page_done_btn)
                        binding.doneTV.isClickable = false
                    } else {
                        binding.doneTV.background =
                            ContextCompat.getDrawable(ctx, R.drawable.bg_page_done_btn_blue)
                        binding.doneTV.isClickable = true
                    }
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            binding.cancelTV.setOnClickListener {
                if (isDoubleClick()) {
                    hideSoftKeyboard()
                    dismiss()
                }
            }

            binding.doneTV.setOnClickListener {
                if (isDoubleClick()) {
                    val num = binding.pageNumberET.text.toString().trim()
                    if (num.isEmpty()) {
                        ctx.showToast(getString(R.string.enter_a_valid_number), 0)
                    } else if (num.toInt() > numLimit || num.toInt() <= 0) {
                        ctx.showToast(getString(R.string.enter_a_valid_number), 0)
                    } else {
                        mListener.pageNumber(num.toInt())
                        hideSoftKeyboard()
                        dismiss()
                    }
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                if (isAdded && isVisible) {
                    binding.pageNumberET.setText("")
                    binding.pageNumberET.requestFocus()
                    val imm =
                        ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.pageNumberET, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

    }

    private fun hideSoftKeyboard() {
        mContext?.let {
            val imm =
                it.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    interface GoToPageListener {
        fun pageNumber(num: Int)
    }

}