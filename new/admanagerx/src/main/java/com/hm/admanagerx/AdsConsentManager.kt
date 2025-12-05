/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hm.admanagerx

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.security.NoSuchAlgorithmException

class AdsConsentManager private constructor(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun init(
        activity: Activity,
        consentCompleteListener: ConsentCompleteListener,
    ) {
        if (activity.isPremium()) {
            consentCompleteListener.onConsentProcessed()
            return
        }

        //For debugging
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(activity.getAdmobHashId())
                .build()
        val params = ConsentRequestParameters
            .Builder()
          //  .setConsentDebugSettings(if (BuildConfig.DEBUG) debugSettings else null)
            .setTagForUnderAgeOfConsent(false)
            .build()

        var isConsentProcessed = false
        // Start the CountDownTimer for an 6-second timeout
        val consentTimer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                cmpLog("adsConsentTimeOutTimer: Time left: ${millisUntilFinished / 1000} seconds")
            }

            override fun onFinish() {
                if (!isConsentProcessed) {
                    cmpLog("adsConsentTimeOutTimer: Consent flow timed out.")
                    isConsentProcessed = true
                    consentCompleteListener.onConsentProcessed()
                }
            }
        }

        consentTimer.start()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params, {
                cmpLog("onConsentInfoUpdatedListener: $canRequestAds")
                if (!isConsentProcessed) {
                    isConsentProcessed = true
                    consentTimer.cancel()
                    if (!canRequestAds) {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            cmpLog("OnConsentFormDismissedListener: $canRequestAds")
                            consentCompleteListener.onConsentProcessed()
                        }
                    } else {
                        cmpLog("onConsentInfoUpdatedListener: else Ads can be requested directly.")
                        consentCompleteListener.onConsentProcessed()
                    }
                }
            },
            {
                cmpLog("OnConsentInfoUpdateFailureListener: Error fetching consent info.")
                if (!isConsentProcessed) {
                    isConsentProcessed = true
                    consentTimer.cancel()
                    consentCompleteListener.onConsentProcessed()
                }
            }
        )
    }

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    fun interface ConsentCompleteListener {
        fun onConsentProcessed()
    }

    fun interface ConsentFailedListener {
        fun onConsentFailed()
    }

    companion object {
        @Volatile
        private var instance: AdsConsentManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: AdsConsentManager(context).also { instance = it }
                }
    }

    private fun Context.getAdmobHashId(): String {
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
}
