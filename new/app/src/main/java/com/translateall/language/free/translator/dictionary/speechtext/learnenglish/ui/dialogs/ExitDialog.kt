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
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.DlgExitNewBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium


class ExitDialog : DialogFragment() {

    private var mContext: Context? = null
    private lateinit var binding: DlgExitNewBinding
    private var mListener: ExitClickListener? = null

    companion object {
        fun newInstance(): ExitDialog {
            return ExitDialog()
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

    fun initListener(mListener: ExitClickListener) {
        this.mListener = mListener
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
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
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
        binding = DlgExitNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext?.let { ctx ->

            if (!ctx.isPremium() && ctx.isOnline())
                showExitNativeAd()
            else {
                binding.nativeAdContainer.gone()
               // binding.exitAdView.visibility = View.GONE
            }
            binding.exitTV.setOnClickListener {
                if (isDoubleClick()) {
                    dismiss()
                    mListener?.onExitClick()
                }
            }
            binding.cancelTV.setOnClickListener {
                if (isDoubleClick()) {
                    dismiss()
                }
            }
        }
    }

    private fun showExitNativeAd() {

       /* nativeAd?.let {
            context.bindNativeAM(
                binding.exitAdView,
                CustomNativeBigLayoutBinding.inflate(layoutInflater),
                nativeAd,
                remoteConfigValue = RemoteConfigValues.IS_SHOW_EXIT_DIALOG_NATIVE,
                viewBound = {
                    Log.d("hello", "loadNativeAd: 116")
                    context.loadNativeAd()
                },
                onPremium = {
                    binding.adsCV.gone()
                }
            )
        } ?: run {
            binding.adsCV.gone()
            Log.d("hello", "loadNativeAd: 125")
            context.loadNativeAd()
        }*/
        AdsManagerX.showNativeAd(
            AdConfigManager.NATIVE_AD_EXIT_DIALOG.apply {
                adConfig.nativeAdLayout =
                    R.layout.custom_native_medium_layout
            }, binding.nativeAdContainer
        )
    }
    interface ExitClickListener {
        fun onExitClick()
    }
}