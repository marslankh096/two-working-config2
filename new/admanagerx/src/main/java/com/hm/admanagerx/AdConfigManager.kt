package com.hm.admanagerx


enum class AdConfigManager(var adConfig: AdConfig) {
    APP_OPEN(
        AdConfig(
            adId = R.string.app_open_ad,
            isAdLoadAgain = true,
            adType = APP_OPEN_AD,
            isAppOpenAdAppLevel = true
        )
    ),
    INTER_AD_SPLASH_FIRST_OPEN(
        AdConfig(
            isAdShow = true,
            adId = R.string.inter_ad_splash_first_open,
            adType = INTER_AD,
            isAdLoadAgain = false,
            isShowLoadingBeforeAd = true
        )
    ),
    INTER_AD_SPLASH_SECOND_OPEN(
        AdConfig(
            isAdShow = true,
            adId = R.string.inter_ad_splash_second_open,
            adType = INTER_AD,
            isAdLoadAgain = false,
            isShowLoadingBeforeAd = true
        )
    ),
    INTER_AD_LANGUAGE(
        AdConfig(
            adType = INTER_AD,
            adId = R.string.inter_ad_language,
            isAdShow = true,
            isShowLoadingBeforeAd = true
        )
    ),
    INTER_AD_VOICE_RESULT_FINDING(
        AdConfig(
            adType = INTER_AD,
            adId = R.string.inter_ad_voice_result_finding,
            isAdShow = true,
            isShowLoadingBeforeAd = true
        )
    ),
    INTER_AD_TRANSLATE_BUTTON(
        AdConfig(
            adType = INTER_AD,
            adId = R.string.inter_ad_translate_done,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
            isAdLoadAgain = true,
            fullScreenAdSessionCount = 5
        )
    ),
    INTER_AD_CONVERSATION(
        AdConfig(
            adType = INTER_AD,
            adId = R.string.inter_ad_conversation,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
            isAdLoadAgain = true
        )
    ),
    INTER_AD_CAMERA_TRANSLATION(
        AdConfig(
            adType = INTER_AD,
            adId = R.string.inter_ad_camera_translation,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
            isAdLoadAgain = true,
            fullScreenAdSessionCount = 5
        )
    ),
    NATIVE_AD_LANGUAGE(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_language,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    BANNER_AD_MAIN(
        AdConfig(
            adType = BANNER_AD,
            adId = R.string.banner_ad_main,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    NATIVE_AD_MAIN(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_main,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    NATIVE_AD_SETTINGS(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_settings,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    NATIVE_AD_ON_BOARDING(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_on_boarding,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),

    NATIVE_AD_EXIT_DIALOG(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_exit_dialog,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),

    NATIVE_AD_HISTORY(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_history,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    NATIVE_AD_FAVOURITES(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_favourite,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    NATIVE_AD_LANGUAGE_SELECTION(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_language_selection,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    NATIVE_AD_TRANSLATE(
        AdConfig(
            adType = NATIVE_AD,
            adId = R.string.native_ad_translation,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    BANNER_AD_VOICE_RESULT(
        AdConfig(
            adType = BANNER_AD,
            adId = R.string.banner_ad_voice_result,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
    BANNER_AD_CAMERA(
        AdConfig(
            adType = BANNER_AD,
            adId = R.string.banner_ad_camera,
            nativeAdLayout = R.layout.native_add_banner_view,
            isAdShow = true,
            isShowLoadingBeforeAd = true,
        )
    ),
}




