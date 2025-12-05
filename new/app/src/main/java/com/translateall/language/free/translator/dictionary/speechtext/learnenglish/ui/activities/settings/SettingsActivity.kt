package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.settings

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivitySettingsBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.language.AppLanguageActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.settings.viewmodel.SettingsViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.CLIP_BOARD_SERVICE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INTENT_FAVORITE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INTENT_HISTORY
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_PREF
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.PRIVACY_URL
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.REQUEST_CODE_HISTORY_FAVORITE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getBottomDialogs
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getFirstBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.getSelectedLanguageName
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchClipboardService
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchFavorite
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.loadExitRatingDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.openPrivacyPolicy
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.hide
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.putBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.shareWithFriends
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.stopClipboardService
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import java.util.Locale

private const val TAG = "SettingsActivity"

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var clearHistoryDialog: Dialog? = null
    private var hasHistoryContent = false
    private var hasFavoriteContent = false
    private var isClipboardOn = false
    private val viewModel: SettingsViewModel by lazy {
        ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }
    private var mLastClickTime: Long = 0
    private fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
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
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.containerAdditionalSettings.gone()
            binding.tvHeaderSettingAdditional.gone()
        } else {
            binding.containerAdditionalSettings.visible()
            binding.tvHeaderSettingAdditional.visible()
        }

        binding.tvLanguageName.text =
            getSelectedLanguageName(TinyDB.getInstance(this).getString(LANGUAGE_PREF))
        handleClipboard()
        initClearHistory()
        loadShowNativeAd()

        viewModel.getFavoriteList()?.observe(this) {
            hasFavoriteContent = if (it != null) {
                it.size > 0
            } else {
                false
            }
        }

        viewModel.getHistoryList()?.observe(this) {
            hasHistoryContent = if (it != null) {
                it.size > 0
            } else {
                false
            }
        }

        binding.ivBackSettings.setOnClickListener {
            onBackPressed()
        }

        binding.layoutLanguage.setOnClickListener {
            if (isDoubleClick()) {
                startActivity(Intent(this, AppLanguageActivity::class.java))
            }
        }

        binding.layoutClearHistory.setOnClickListener {
            clearHistoryDialog?.show()

        }
        binding.layoutFavorite.setOnClickListener {
            if (isDoubleClick()) {
                /*if (hasFavoriteContent)
                    showLaunchFavoriteAd()
                else*/
                launchFavorite(INTENT_FAVORITE)
            }
        }
        binding.layoutRateUs.setOnClickListener {
            loadExitRatingDialog(false)
        }

        binding.layoutShare.setOnClickListener {
            if (isDoubleClick())
                shareWithFriends()
        }

        binding.layoutPrivacy.setOnClickListener {
            if (isDoubleClick())
                openPrivacyPolicy(this, PRIVACY_URL)
        }

        binding.layoutHistory.setOnClickListener {
            if (isDoubleClick()) {
                /*if (hasHistoryContent)
                    showLaunchHistoryAd()
                else*/
                launchFavorite(INTENT_HISTORY)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (isPremium())
            binding.nativeAdView.gone()
    }

    private fun initClearHistory() {
        clearHistoryDialog = getBottomDialogs(
            R.layout.dlg_clear_history,
            onCancel = {
                clearHistoryDialog?.dismiss()
            }, onDelete = {
                MyDataBase.getInstance(this).translationDao().deleteAll()
                clearHistoryDialog?.dismiss()
            })
    }

    private fun handleClipboard() {
        AppUtils.onStopClipBoard = {
            binding.switchCopy.isChecked = it
            isClipboardOn = it
        }
        isClipboardOn = getFirstBoolean(CLIP_BOARD_SERVICE)
        binding.switchCopy.changeState(isClipboardOn)

        binding.switchViewCopy.setOnClickListener {
            if (isClipboardOn) {
                isClipboardOn = false
                putBoolean(CLIP_BOARD_SERVICE, isClipboardOn)
                binding.switchCopy.changeState(isClipboardOn)
                stopClipboardService()
            } else {
                checkPopupPermission()
            }
        }

    }

    private fun SwitchCompat.changeState(state: Boolean) {
        isChecked = state
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_HISTORY_FAVORITE) {
            if (resultCode == RESULT_OK && null != data) {
                data.apply {
                    val translation = getParcelableExtra("history_model") as TranslationHistory?
                    intent.putExtra("history_model", translation)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun checkPopupPermission() {
        if (checkIsXiaomi()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                    intent.setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    intent.putExtra("extra_pkgname", packageName)
                    AlertDialog.Builder(this)
                        .setTitle("Please Enable the additional permissions")
                        .setMessage("You will not be able to use copy to translate feature while the app is in background if you disable these permissions")
                        .setPositiveButton(
                            "Go to Settings"
                        ) { dialog, which -> startActivity(intent) }
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setCancelable(false)
                        .show()
                } else {
                    startClipBoard()
                }
            } else {
                startClipBoard()
            }
        } else {
            startClipBoard()

        }

    }

    private fun startClipBoard() {
        isClipboardOn = true
        putBoolean(CLIP_BOARD_SERVICE, isClipboardOn)
        binding.switchCopy.changeState(isClipboardOn)
        launchClipboardService()
    }

    private fun checkIsXiaomi(): Boolean {
        return "xiaomi" == Build.MANUFACTURER.toLowerCase(Locale.ROOT)
    }

    private fun loadShowNativeAd() {
        if (isPremium().not() && isOnline(this@SettingsActivity)) {
            AdsManagerX.loadNativeAd(
                this@SettingsActivity,
                AdConfigManager.NATIVE_AD_SETTINGS.apply {
                    adConfig.nativeAdLayout =
                        R.layout.custom_native_big_layout
                }, binding.nativeAdView, onAdFailed = {
                    binding.nativeAdView.hide()
                }
            )
        } else {
            binding.nativeAdView.hide()
        }
    }
}