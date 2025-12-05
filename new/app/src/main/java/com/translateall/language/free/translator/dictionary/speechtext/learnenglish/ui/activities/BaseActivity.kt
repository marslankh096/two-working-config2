package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_PREF
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.languageCode
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    var loadingDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = setupLoadingDialog(R.layout.loading_layout, false)
    }

    private fun setupLoadingDialog(layout: Int, isCancelable: Boolean = true): Dialog {
        val dialog = Dialog(this, R.style.customDialogSize)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(isCancelable)
        dialog.setContentView(layout)
        return dialog
    }

    private fun dismissLoader(time: Long = 500) {
        Handler(Looper.getMainLooper()).postDelayed({
            loadingDialog?.dismiss()
        }, time)
    }

    ///base context

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(updateBaseContextLocale(newBase!!))
    }

    private fun updateBaseContextLocale(context: Context): Context {

        var isLangCodeFind = false
        var languages = TinyDB.getInstance(context).getString(LANGUAGE_PREF)

        if (languages.isNullOrEmpty()) {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales[0]
            } else {
                Resources.getSystem().configuration.locale
            }
            // en_Us to split en
            val defaultSystemLang = locale.toString()
            languages = if (defaultSystemLang.contains("_")) {
                defaultSystemLang.split("_")[0]
            } else {
                locale.toString()
            }

            isLangCodeFind = context.languageCode().contains(languages)

        } else {
            isLangCodeFind = true
        }

        val locale = if (languages.contains("-")) {
            Locale(languages.split("-")[0])
        } else {
            Locale(languages)
        }
        if (isLangCodeFind) {
            Locale.setDefault(locale)
        } else {
            Locale.setDefault(Locale("en"))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesLocale(context, locale)
        }
        return updateResourcesLocaleLegacy(context, locale)
    }

    private fun updateResourcesLocale(
        context: Context,
        locale: Locale
    ): Context {
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLocaleLegacy(
        context: Context,
        locale: Locale
    ): Context {
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.locale = locale

        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}