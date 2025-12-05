package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.TranslateApplication
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.DlgNoInternetBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick

class NoConnectionDialog : DialogFragment() {

    private var mContext: Context? = null
    private lateinit var binding: DlgNoInternetBinding
    private var mListener: NoConnectionListener? = null

    companion object {
        fun newInstance(): NoConnectionDialog {
            return NoConnectionDialog()
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


    fun initListener(listener: NoConnectionListener) {
        this.mListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.customDialog)
    }

    override fun onResume() {
        // Set the width of the dialog proportional to 90% of the screen width
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * 0.96).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        if (window != null) {
            //            window.requestFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawableResource(android.R.color.transparent)

            val windowLayoutParams = window.attributes
            windowLayoutParams.dimAmount = 0.9f
        }
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DlgNoInternetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext?.let { ctx ->
            (ctx.applicationContext as TranslateApplication).noConnectionDialogShown = true
            binding.mobileDataLL.setOnClickListener {
                if (isDoubleClick()) {
                    dismiss()
                    mListener?.turnOnMobileData()
                }
            }
            binding.wifiLL.setOnClickListener {
                if (isDoubleClick()) {
                    dismiss()
                    mListener?.turnOnWifi()
                }
            }
            binding.closeIV.setOnClickListener {
                if (isDoubleClick())
                    dismiss()
            }
        }

    }

    interface NoConnectionListener {
        fun turnOnMobileData()
        fun turnOnWifi()
    }

}