package com.translateall.language.free.translator.dictionary.speechtext.learnenglish

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.hm.admanagerx.AdsManagerX
import com.limurse.iap.DataWrappers
import com.limurse.iap.IapConnector
import com.limurse.iap.SubscriptionServiceListener
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di.dictionaryDatabaseModule
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di.dictionaryRepositoryModule
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di.phraseDatabaseModule
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di.phraseModules
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di.suggestedWordsDatabaseModule
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di.viewModelModule
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.LIFE_TIME_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.MONTHLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.WEEKLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.YEARLY_SUBS_ID
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.IS_PREMIUM
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TranslateApplication : Application() {
    var backFromOnboarding = false
    var noConnectionDialogShown = false
    var homePremiumShown = false
    var conversationPremiumShown = false
    var shouldReloadAds = false

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        settingIAP()
        AdsManagerX.init(this, false)

        noConnectionDialogShown = false
        startKoin {
            androidContext(this@TranslateApplication)
            androidLogger()
            modules(
                dictionaryDatabaseModule,
                dictionaryRepositoryModule,
                suggestedWordsDatabaseModule,
                viewModelModule,
                phraseDatabaseModule,
                phraseModules
            )
        }


    }

    private var iapConnector: IapConnector? = null
    private fun settingIAP() {

        iapConnector = IapConnector(
            context = this,
            subscriptionKeys = listOf(
                YEARLY_SUBS_ID,
                MONTHLY_SUBS_ID,
                LIFE_TIME_SUBS_ID,
                WEEKLY_SUBS_ID
            )
        )
        TinyDB.getInstance(this@TranslateApplication).putBoolean(IS_PREMIUM, false)
        iapConnector?.addSubscriptionListener(object : SubscriptionServiceListener {

            override fun onSubscriptionNotPurchased() {
                Log.d("subLog", "onSubscriptionNotPurchased: ")
                TinyDB.getInstance(this@TranslateApplication).putBoolean(IS_PREMIUM, false)
            }

            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                Log.d("subLog", "onSubscriptionRestored: ${purchaseInfo.sku}")
                when (purchaseInfo.sku) {
                    YEARLY_SUBS_ID -> {
                        TinyDB.getInstance(this@TranslateApplication).putBoolean(IS_PREMIUM, true)
                    }

                    MONTHLY_SUBS_ID -> {
                        TinyDB.getInstance(this@TranslateApplication).putBoolean(IS_PREMIUM, true)
                    }

                    LIFE_TIME_SUBS_ID -> {
                        TinyDB.getInstance(this@TranslateApplication).putBoolean(IS_PREMIUM, true)
                    }

                    WEEKLY_SUBS_ID -> {
                        TinyDB.getInstance(this@TranslateApplication).putBoolean(IS_PREMIUM, true)
                    }
                }
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                Log.d("subLog", "onSubscriptionPurchased")
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
                Log.d("subLog", "onPricesUpdated")
            }

            override fun onPurchaseFailed(
                purchaseInfo: DataWrappers.PurchaseInfo?,
                billingResponseCode: Int?
            ) {
                Log.d("subLog", "onPurchaseFailed")
            }
        })
    }
}