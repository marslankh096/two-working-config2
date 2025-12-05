package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.BuildConfig
import java.security.NoSuchAlgorithmException

class MobileAdsConsentHandler private constructor(context: Context) {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)

    /** Interface definition for a callback to be invoked when consent gathering is complete. */
    interface OnConsentGatherListeners {
        fun onConsent()
        fun onError(error: FormError?)
    }

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */
    fun gatherConsent(
        activity: Activity,
        listener: OnConsentGatherListeners
    ) {
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(activity.getAdmobHashId())
                .build()

        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(if (BuildConfig.DEBUG) debugSettings else null)
            .setTagForUnderAgeOfConsent(false)
            .build()

        var isConsentProcessed = false
        // Start the CountDownTimer for an 8-second timeout
        val consentTimer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                cmpLog("g:: Time left: ${millisUntilFinished / 1000} seconds")
            }

            override fun onFinish() {
                if (!isConsentProcessed) {
                    cmpLog("adsConsentTimeOutTimer: Consent flow timed out.")
                    isConsentProcessed = true
                    listener.onError(null)
                }
            }
        }

        consentTimer.start()

        // Requesting an update to consent information should be called on every app launch.
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                consentTimer.cancel()
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError == null && canRequestAds) {
                        Log.d("CONSENT", "CONSENT FORM AVAILABLE STOPPING WAIT TIME")
                        listener.onConsent()
                    } else {
                        cmpLog("onConsentInfoUpdatedListener: else Ads can be requested directly.")
                        listener.onError(formError)
                    }
                }
            },
            { formError: FormError? ->
                cmpLog("OnConsentInfoUpdateFailureListener: Error fetching consent info.")
                consentTimer.cancel()
                listener.onError(formError)
            }
        )
    }

    /** Helper method to call the UMP SDK method to show the privacy options form. */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    companion object {
        @Volatile
        private var instance: MobileAdsConsentHandler? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: MobileAdsConsentHandler(context).also { instance = it }
        }
    }
}

fun Context.getAdmobHashId(): String {
    try {
        // Create MD5 Hash
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val digest = java.security.MessageDigest.getInstance("MD5")
        digest.update(androidId.toByteArray())
        val messageDigest = digest.digest()

        // Create Hex String
        val hexString = StringBuilder()
        for (i in messageDigest.indices) {
            var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
            while (h.length < 2)
                h = "0$h"
            hexString.append(h)
        }
        return hexString.toString().uppercase()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""

}


fun cmpLog(message: String) {
    Log.d("showCmpAndInitAds", "$message")
}