package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdView
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.IS_UNLOCK_CAMERA
import com.hm.admanagerx.getBooleanRemoteConfigValue
import com.hm.admanagerx.isAppOpenAdShow
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.FragmentHomeBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.Controller
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.InAppActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inputscreen.InputActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.generic.LanguageSelectionActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.viewmodel.MainViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.settings.SettingsActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.DEFAULT_SRC_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.DEFAULT_TAR_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INPUT_TYPE_KEY
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_TYPE_SOURCE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.LANGUAGE_TYPE_TARGET
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.SOURCE_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.TARGET_LANG_CODE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.TARGET_LANG_NAME
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.TARGET_LANG_POSITION
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.invisible
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isAppInstalled
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isLanguageSupportedForMic
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchCameraScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchConversationScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = _binding!!
    private var mContext: Context? = null
    private var srcLangCode: String = ""
    private var srcLangName: String = ""
    private var targetLangCode: String = ""
    private var targetLangName: String = ""

    private var srcLangPosition: Int = DEFAULT_SRC_LANG_POSITION
    private var targetLangPosition: Int = DEFAULT_TAR_LANG_POSITION

    var bannerAd: AdView? = null

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private val settingsResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        }

    private val inputResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            mContext?.let { ctx ->
                (ctx as MainActivity).showPremiumDialog()
            }
        }

    private val micResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            mContext?.let { ctx ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val recognizedResult =
                        intent?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (recognizedResult != null) {
                        val inputWord = recognizedResult[0]
                        showVoiceInputInterAd(inputWord)
//                        setInputFieldText(inputWord)
                    }
                }
            }
        }

    private val languageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            mContext?.let { ctx ->

                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data

                    intent?.apply {
                        val langModel = getParcelableExtra("language_model") as LanguageModel?
                        val langType = getStringExtra("language_type")
                        val langPosition = getIntExtra("language_position", 0)
                        setLanguages(langType, langModel, langPosition)
                    }
                }
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentHomeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onPause() {
        bannerAd?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onResume: HomeFragment")
        bannerAd?.resume()
        mContext?.let { ctx ->
            if (ctx.isPremium())
                hideViewOnPremium()
            else {
                binding.layoutMain.proIV.visible()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
    }

    private fun init() {
        mContext?.let { ctx ->

            getLanguagesData()
            Handler().postDelayed({
                (ctx as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                //  showHomeNativeAd()
                loadInterAd()
//            checkUpdate()
            }, 500)

            AppUtils.onLanguageChange = { langType, langModel, langPosition ->
                setLanguages(langType, langModel, langPosition)
            }

            AppUtils.onSwitchLanguages = {
                switchLanguages()
            }

//            IKBillingController.reCheckIAP(object : IKBillingListener {
//
//                override fun onBillingFail() {
//                    if (ctx.isPremium()) {
//                        binding.layoutMain.proIV.visible()
//                        binding.layoutMain.nativeAd.visible()
//                    }
//                    ctx.putBoolean(IS_PREMIUM, false)
//                }
//
//                override fun onBillingSuccess() {
//                    ctx.putBoolean(IS_PREMIUM, true)
//                }
//
//            }, false)
        }

    }

    private fun loadInterAd() {
//        mContext?.let { ctx ->
//            (ctx as MainActivity).loadInterAd(AdConfigManager.HOME_OPEN_TRANSLATOR)
//        }
    }

    private fun setListeners() {
        binding.layoutMain.layoutMic.setOnClickListener {
            if (isDoubleClick())
                checkAudioPermission()
        }

        binding.layoutMain.viewInputTouch.setOnClickListener {
            if (isDoubleClick())
            //showInputInterAd()
                mContext?.let {
                    launchInputScreen(null)
                }
        }

        binding.layoutMain.layoutSwapMain.setOnClickListener {
            switchLanguages()
        }
        binding.layoutMain.layoutLanguagesMainSrc.setOnClickListener {
            if (isDoubleClick()) {
                openLanguageSheet(LANGUAGE_TYPE_SOURCE)
            }
        }
        binding.layoutMain.layoutLanguagesMainTarget.setOnClickListener {
            if (isDoubleClick()) {
                openLanguageSheet(LANGUAGE_TYPE_TARGET)
            }
        }
        binding.layoutMain.proIV.setOnClickListener {
            if (isDoubleClick()) {
                mContext?.let {
                    startActivity(Intent(it, InAppActivity::class.java))
                }
            }
        }
        binding.layoutMain.ivSetting.setOnClickListener {
            if (isDoubleClick()) {
                mContext?.let { ctx ->
                    settingsResult.launch(Intent(ctx, SettingsActivity::class.java))
                }
            }
        }

        binding.layoutMain.layoutCamera.setOnClickListener {
            if (isDoubleClick()) {
                mContext?.let {
                    if (it.isPremium()) {
                        openCameraScreen()
                    } else {
                        val unlockCamera =
                            IS_UNLOCK_CAMERA.getBooleanRemoteConfigValue()
                        Log.d("TAG", "unlockCamera:$unlockCamera ")
                        if (unlockCamera) {
                            (it as MainActivity).isShowPayWallBackFromPremiumScreen = true
                            startActivity(Intent(it, InAppActivity::class.java))
                        } else {
                            openCameraScreen()
                        }
                    }
                }
            }
        }

        binding.layoutMain.layoutConversation.setOnClickListener {
            if (isDoubleClick()) {
                mContext?.let { ctx ->
                    if (micClickable) {
                        if (Controller.isOnline(ctx)) {
                            startConversationScreen()
                        } else {
                            val msg = getString(R.string.this_feature_is_not_available_offline)
                            ctx.showToast(msg, 0)
                        }
                    } else {
                        val language = if (!isLanguageSupportedForMic(srcLangCode)) {
                            srcLangName
                        } else {
                            targetLangName
                        }
                        val msg = getString(
                            R.string.speech_input_is_not_available_for_selected_language,
                            language
                        )
                        ctx.showToast(msg, 0)
                    }
                }
            }

        }

    }

    private fun openLanguageSheet(type: String) {
        mContext?.let {
//            (it as Activity).launchLanguageScreen(type, "main")
            val intent = Intent(it, LanguageSelectionActivity::class.java)
            intent.putExtra("language_type", type)
            intent.putExtra("language_source", "main")
            languageResult.launch(intent)
        }
    }

    private fun openCameraScreen() {
        mContext?.let { ctx ->
            val permissions = arrayOf(Manifest.permission.CAMERA)
            Permissions.check(ctx, permissions, null, null, object : PermissionHandler() {
                override fun onGranted() {
                    mContext?.let {
                        (it as Activity).launchCameraScreen()
                    }
                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
                    super.onDenied(context, deniedPermissions)
                }
            })
        }

    }

    private fun startConversationScreen() {
        mContext?.let { ctx ->
            if (Build.VERSION.SDK_INT >= 30) {
                (ctx as Activity).launchConversationScreen()
            } else {
                if (isAppInstalled("com.google.android.googlequicksearchbox", ctx as Activity)
                    ||
                    isAppInstalled("com.google.android.apps.searchlite", ctx as Activity)
                ) {
                    (ctx as Activity).launchConversationScreen()
                } else {
                    ctx.showToast(getString(R.string.message_app_install), 1)
                }
            }
        }

    }

    private fun checkAudioPermission() {
        mContext?.let { ctx ->
            if (Build.VERSION.SDK_INT >= 30) {
                startMic()
            } else {
                if (isAppInstalled("com.google.android.googlequicksearchbox", ctx as Activity)
                    ||
                    isAppInstalled("com.google.android.apps.searchlite", ctx as Activity)
                ) {
                    startMic()
                } else {
                    ctx.showToast(getString(R.string.message_app_install), 1)
                }
            }
        }

    }

    private fun startMic() {
        loadVoiceInputInterAd()
        showLaunchMicAd()
    }

    private fun speakIn(code: String) {
        val micIntent = viewModel.getRecognizerIntent(code)
        try {
            micResult.launch(micIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            mContext?.showToast(resources.getString(R.string.stt_error_device), 0)
        }
    }

    private fun getLanguagesData() {
        srcLangCode = viewModel.getLangData(SOURCE_LANG_CODE)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(SOURCE_LANG_CODE, srcLangCode)
        }
        srcLangName = viewModel.getLangData(SOURCE_LANG_NAME)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
        }

        srcLangPosition = viewModel.getLangPosition(SOURCE_LANG_POSITION)

        if (srcLangPosition == -1) {
            srcLangPosition = DEFAULT_SRC_LANG_POSITION
            viewModel.putLangPosition(SOURCE_LANG_POSITION, srcLangPosition)
        }

        targetLangCode = viewModel.getLangData(TARGET_LANG_CODE)
        if (targetLangCode == "") {
            targetLangCode = "fr"
            viewModel.setLangData(TARGET_LANG_CODE, targetLangCode)
        }

        targetLangName = viewModel.getLangData(TARGET_LANG_NAME)
        if (targetLangName == "") {
            targetLangName = "French"
            viewModel.setLangData(TARGET_LANG_NAME, targetLangName)
        }

        targetLangPosition = viewModel.getLangPosition(TARGET_LANG_POSITION)
        if (targetLangPosition == -1) {
            targetLangPosition = DEFAULT_TAR_LANG_POSITION
            viewModel.putLangPosition(TARGET_LANG_POSITION, targetLangPosition)
        }

        binding.layoutMain.tvSrcNameMain.text = srcLangName
        binding.layoutMain.tvTargetNameMain.text = targetLangName

        checkClickableTab()
    }

    private var micClickable = false
    private fun checkClickableTab() {
        if (isLanguageSupportedForMic(srcLangCode) && isLanguageSupportedForMic(targetLangCode)) {
            micClickable = true
            binding.layoutMain.conversationIV.alpha = 1f
            binding.layoutMain.conversationTV.alpha = 1f
            binding.layoutMain.micIV.alpha = 1f
            binding.layoutMain.micTV.alpha = 1f
        } else {
            micClickable = false
            binding.layoutMain.conversationIV.alpha = 0.5f
            binding.layoutMain.conversationTV.alpha = 0.5f
            binding.layoutMain.micIV.alpha = 0.5f
            binding.layoutMain.micTV.alpha = 0.5f
        }

    }

    private fun setLanguages(type: String?, langModel: LanguageModel?, position: Int) {

        if (type == LANGUAGE_TYPE_SOURCE) {
            srcLangName = langModel?.languageName!!
            srcLangCode = langModel.languageCode!!
            srcLangPosition = position

            viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
            viewModel.setLangData(SOURCE_LANG_CODE, srcLangCode)
            viewModel.putLangPosition(SOURCE_LANG_POSITION, srcLangPosition)
            binding.layoutMain.tvSrcNameMain.text = srcLangName

        } else if (type == LANGUAGE_TYPE_TARGET) {
            targetLangName = langModel?.languageName!!
            targetLangCode = langModel.languageCode!!
            targetLangPosition = position

            viewModel.setLangData(TARGET_LANG_NAME, targetLangName)
            viewModel.setLangData(TARGET_LANG_CODE, targetLangCode)
            viewModel.putLangPosition(TARGET_LANG_POSITION, targetLangPosition)
            binding.layoutMain.tvTargetNameMain.text = targetLangName

        }
        checkClickableTab()

    }

    private var isSwitched = false

    private fun switchLanguages() {

        if (isSwitched) {
            isSwitched = false
            binding.layoutMain.ivLangArrowsMain.animate().rotation(-180f).start()
        } else {
            isSwitched = true
            binding.layoutMain.ivLangArrowsMain.animate().rotation(180f).start()
        }
        val tempSrcCode = srcLangCode
        val temSrcName = srcLangName
        val temSrcPosition = srcLangPosition

        val temTarCode = targetLangCode
        val temTarName = targetLangName
        val temTarPosition = targetLangPosition

        srcLangCode = temTarCode
        srcLangName = temTarName
        srcLangPosition = temTarPosition

        targetLangCode = tempSrcCode
        targetLangName = temSrcName
        targetLangPosition = temSrcPosition

        viewModel.setLangData(SOURCE_LANG_CODE, srcLangCode)
        viewModel.setLangData(SOURCE_LANG_NAME, srcLangName)
        viewModel.putLangPosition(SOURCE_LANG_POSITION, srcLangPosition)

        viewModel.setLangData(TARGET_LANG_CODE, targetLangCode)
        viewModel.setLangData(TARGET_LANG_NAME, targetLangName)
        viewModel.putLangPosition(TARGET_LANG_POSITION, targetLangPosition)
        binding.layoutMain.tvSrcNameMain.text = srcLangName
        binding.layoutMain.tvTargetNameMain.text = targetLangName

        checkClickableTab()
    }

    /*private fun showHomeNativeAd() {
        mContext?.let { ctx ->

            bannerAd = ctx.loadBannerAd(binding.layoutMain.nativeAd,
                BannerPlacements.BANNER_AD,
                RemoteConfigValues.IS_SHOW_BANNER_HOME_SCREEN,
                onLoaded = { binding.layoutMain.nativeAd.root.visible() },
                onFailed = { binding.layoutMain.nativeAd.root.gone() },
                onPremium = { binding.layoutMain.nativeAd.root.gone() }
            )
        }

    }*/

    private fun showLaunchMicAd() {
        /* mContext?.let { ctx ->

             (ctx as? MainActivity)?.showInterstitialAdFragment(
                 RemoteConfigValues.OPEN_MIC_INTER_AD,
                 preAdShow = {

                 },
                 onClosed = {
                     srcLangCode.let { code ->
                         viewModel.getMicCode(code)?.let {
                             speakIn(it)
                         }
                     }
                 },
                 onFailedToShow = {
                     srcLangCode.let { code ->
                         viewModel.getMicCode(code)?.let {
                             speakIn(it)
                         }
                     }
                 },
                 onToShow = {}
             )
         }*/
        mContext?.isAppOpenAdShow(false)
        srcLangCode.let { code ->
            viewModel.getMicCode(code)?.let {
                speakIn(it)
            }
        }
    }

    /*   private fun showCameraInterAd() {
           mContext?.let { ctx ->


               (ctx as? MainActivity)?.showInterstitialAdFragment(
                   RemoteConfigValues.OPEN_CAMERA_INTER_AD,
                   preAdShow = {

                   },
                   onClosed = {
                       mContext?.let {
                           (it as Activity).launchCameraScreen()
                       }
                   },
                   onFailedToShow = {
                       mContext?.let {
                           (it as Activity).launchCameraScreen()
                       }
                   },
                   onToShow = {}
               )
           }

       }*/

    /*private fun showConversationInterAd() {
        mContext?.let { ctx ->

            (ctx as? MainActivity)?.showInterstitialAdFragment(
                RemoteConfigValues.OPEN_CONVERSATION_INTER_AD,
                preAdShow = {

                },
                onClosed = {
                    startConversationScreen()
                },
                onFailedToShow = {
                    startConversationScreen()
                },
                onToShow = {}
            )
        }
    }
*/
    /*
        private fun showInputInterAd(inputText: String? = null) {
            mContext?.let { ctx ->

                (ctx as? MainActivity)?.showInterstitialAdFragment(
                    RemoteConfigValues.OPEN_INPUT_INTER_AD,
                    preAdShow = {

                    },
                    onClosed = {
                        mContext?.let {
                            launchInputScreen(inputText)
                        }
                    },
                    onFailedToShow = {
                        mContext?.let {
                            launchInputScreen(inputText)
                        }
                    },
                    onToShow = {}
                )
            }
        }
    */

    private fun launchInputScreen(inputText: String?, isVoiceResultInterAdShown: Boolean = false) {
        mContext?.let {
            val intent = Intent(it, InputActivity::class.java)
            inputText?.let {
                intent.putExtra("has_data", "yes")
                intent.putExtra("from_mic", "yes")
            } ?: run {
                intent.putExtra("has_data", "no")
            }
            intent.putExtra("isVoiceResultInterShow", isVoiceResultInterAdShown)
            intent.putExtra("from_docs", false)
            intent.putExtra(INPUT_TYPE_KEY, inputText)
            inputResult.launch(intent)
        }
    }

    private fun hideLoadingDialog() {
        mContext?.let { ctx ->
            (ctx as MainActivity).hideLoadingDialog()
        }
    }

    private fun showLoadingDialog() {
        mContext?.let { ctx ->
            (ctx as MainActivity).showLoadingDialog()
        }
    }

    private fun hideViewOnPremium() {
        binding.layoutMain.proIV.invisible()
        //  binding.layoutMain.nativeAd.root.gone()
    }

    /*fun hideBannerNative() {
        if (isAdded && mContext != null)
            binding.layoutMain.nativeAd.root.gone()
    }

    fun showBannerNative() {
        if (isAdded && mContext != null)
            binding.layoutMain.nativeAd.root.visible()
    }*/

    override fun onDestroy() {
        bannerAd?.destroy()
        super.onDestroy()
        _binding = null
        mContext = null
    }

    private fun showVoiceInputInterAd(inputText: String? = null) {
        mContext?.let { ctx ->
            (ctx as? MainActivity)?.let {
                if (AdsManagerX.isAppOpenAdShowing(AdConfigManager.APP_OPEN)){
                    mContext?.let {
                        launchInputScreen(inputText, true)
                    }
                }else{
                    AdsManagerX.showInterAd(
                        it,
                        AdConfigManager.INTER_AD_VOICE_RESULT_FINDING.apply {
                            adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                        },
                        onAdClose = {
                            mContext?.let {
                                launchInputScreen(inputText, true)
                            }
                        }, onAdShow = {
                        },
                        funBlock = {
                            mContext?.let {
                                launchInputScreen(inputText, false)
                            }
                        })
                }

            }
        }
    }

    private fun loadVoiceInputInterAd() {
        AdsManagerX.loadInterAd(this, AdConfigManager.INTER_AD_VOICE_RESULT_FINDING)
    }

}