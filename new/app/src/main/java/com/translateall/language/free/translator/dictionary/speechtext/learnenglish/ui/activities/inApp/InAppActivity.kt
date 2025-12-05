package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.hm.admanagerx.isAppOpenAdShow
import com.limurse.iap.DataWrappers
import com.limurse.iap.IapConnector
import com.limurse.iap.SubscriptionServiceListener
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityInAppBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.premiumActive
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.IS_PREMIUM
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.billing.NetworkListener
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.billing.isActiveNetworkAvailable
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

private const val TAG = "InAppActivity"

//const val YEARLY_SUBS_ID = "yearly_sub"
//const val MONTHLY_SUBS_ID = "monthly_sub"
//const val LIFE_TIME_SUBS_ID = "lifetime_sub"

const val YEARLY_SUBS_ID = Constants.yearlyId
const val MONTHLY_SUBS_ID = Constants.monthlyId
const val LIFE_TIME_SUBS_ID = "lifetime_sub"
const val WEEKLY_SUBS_ID = Constants.weeklyId

class InAppActivity : BaseActivity() {

    private lateinit var binding: ActivityInAppBinding

    private var networkReceiver: NetworkListener? = null

    var networkStatus = false

    private var iapConnector: IapConnector? = null

    private var selectedSubscription = YEARLY_SUBS_ID

    var monthlyPrice = ""
    var yearlyPrice = ""
    var monthlyTrialPeriod = "0"
    var yearlyTrialPeriod = "0"
    var weeklyPrice = ""
    var weeklyTrialPeriod = "0"

    //    var lifeTimePrice = ""
    override fun onStart() {
        super.onStart()
        isAppOpenAdShow(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        // Set the content to appear under the system bars
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN

        binding = ActivityInAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

  /*      // ✅ Android 15+ edge-to-edge setup (only once, here)
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
        }*/

        init()

        setListeners()

    }

    private fun init() {

        initBilling()

        initNetworkListener()
    }

    var isFirstTime = true
    private fun initNetworkListener() {

        networkReceiver = NetworkListener(object : NetworkListener.NetworkStatusListener {

            override fun onNetworkStatusChange() {

                lifecycleScope.launch(Dispatchers.IO) {

                    networkStatus = isActiveNetworkAvailable()

                    if (isFirstTime) {
                        isFirstTime = false
                        return@launch
                    }

                    if (networkStatus) {

                        withContext(Dispatchers.Main) {
                            iapConnector?.let {
                                if (it.getBillingProcessReady().not()) {
                                    iapConnector?.closeAllConnection()
                                    initBilling()
                                }
                            } ?: run {
                                initBilling()
                            }
                        }
                    }
                }
            }
        })

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun initBilling() {
        iapConnector = IapConnector(
            context = this,
            subscriptionKeys = listOf(
                YEARLY_SUBS_ID,
                MONTHLY_SUBS_ID,
                LIFE_TIME_SUBS_ID,
                WEEKLY_SUBS_ID
            )
        )

        iapConnector?.addSubscriptionListener(object : SubscriptionServiceListener {

            override fun onSubscriptionNotPurchased() {}

            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    YEARLY_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }

                    MONTHLY_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }

                    LIFE_TIME_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }

                    WEEKLY_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }
                }
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    YEARLY_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }

                    MONTHLY_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }

                    LIFE_TIME_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }

                    WEEKLY_SUBS_ID -> {
                        activateSuccessfulPurchase()
                    }
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {

                iapKeyPrices.forEach {
                    when (it.key) {

                        YEARLY_SUBS_ID -> {

                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))

                            yearlyPrice = String.format(getString(R.string.yearly_data), priceTxt)

                            if (it.value.size == 1) {

                                binding.yearlyPriceTV.text = yearlyPrice

                                binding.detailTextView.text = getTextNoTrial(yearlyPrice)

                            } else if (it.value.size == 2) {

                                yearlyTrialPeriod =
                                    it.value.firstOrNull()?.billingPeriod?.getDaysFromTrialPeriod()
                                        .toString()

                                val des =
                                    yearlyTrialPeriod + " ${getString(R.string.free_trial_txt)}, ${priceTxt}/${
                                        getString(R.string.yearly_word)
                                    }"

                                binding.yearlyPriceTV.text = des

                                binding.detailTextView.text =
                                    getTextWithTrial(yearlyPrice, yearlyTrialPeriod)
                            }
                        }

                        MONTHLY_SUBS_ID -> {

                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))

                            monthlyPrice = String.format(getString(R.string.monthly_data), priceTxt)

                            if (it.value.size == 1) {

                                binding.monthlyPriceTV.text = monthlyPrice

                            } else if (it.value.size == 2) {

                                monthlyTrialPeriod =
                                    it.value.firstOrNull()?.billingPeriod?.getDaysFromTrialPeriod()
                                        .toString()

                                val des =
                                    monthlyTrialPeriod + " ${getString(R.string.free_trial_txt)}, ${priceTxt}/${
                                        getString(R.string.monthly_word)
                                    }"

                                binding.monthlyPriceTV.text = des
                            }
                        }

                        LIFE_TIME_SUBS_ID -> {

//                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
//                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))
//
//                            lifeTimePrice =
//                                String.format(getString(R.string.life_time_data), priceTxt)
//
//                            binding.lifeTimePriceTV.text = priceTxt
                        }

                        WEEKLY_SUBS_ID -> {

                            val priceTxt = it.value.lastOrNull()?.priceCurrencyCode + " " +
                                    ((DecimalFormat("0.##").format(it.value.lastOrNull()?.priceAmount)))

                            weeklyPrice = "$priceTxt ${getString(R.string.weekly)}"

                            if (it.value.size == 1) {

                                binding.lifeTimePriceTV.text = weeklyPrice

                            } else if (it.value.size == 2) {

                                weeklyTrialPeriod =
                                    it.value.firstOrNull()?.billingPeriod?.getDaysFromTrialPeriod()
                                        .toString()

                                val des =
                                    weeklyTrialPeriod + " ${getString(R.string.free_trial_txt)}, ${priceTxt}/${
                                        getString(R.string.weekly)
                                    }"

                                binding.lifeTimePriceTV.text = des
                            }
                        }
                    }
                }
            }

            override fun onPurchaseFailed(
                purchaseInfo: DataWrappers.PurchaseInfo?,
                billingResponseCode: Int?
            ) {

            }
        })
    }

    private fun setListeners() {

        /*onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            }
        )*/

        binding.closeIV.setOnClickListener {

            finish()
        }

        //this is weekly setup
        binding.lifeTimeCL.setOnClickListener {
            if (isDoubleClick()) {
                selectedSubscription = WEEKLY_SUBS_ID
                resetSelection()
                binding.lifeTimeCL.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_premium_selected)
                binding.lifeTimeCheckIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_premium_checked
                    )
                )
                binding.lifetTimeTitleTV.setTextColor(resources.getColor(R.color.black))
                binding.lifeTimePriceTV.setTextColor(resources.getColor(R.color.black))

                if (weeklyTrialPeriod != "0") {
                    binding.detailTextView.text = getTextWithTrial(weeklyPrice, weeklyTrialPeriod)
                } else {
                    binding.detailTextView.text = getTextNoTrial(weeklyPrice)
                }
            }
        }

        binding.monthlyCL.setOnClickListener {
            if (isDoubleClick()) {
                selectedSubscription = MONTHLY_SUBS_ID
                resetSelection()
                binding.monthlyCL.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_premium_selected)
                binding.monthlyCheckIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_premium_checked
                    )
                )
                binding.monthlyTitleTV.setTextColor(resources.getColor(R.color.black))
                binding.monthlyPriceTV.setTextColor(resources.getColor(R.color.black))

                if (monthlyTrialPeriod != "0") {
                    binding.detailTextView.text = getTextWithTrial(monthlyPrice, monthlyTrialPeriod)
                } else {
                    binding.detailTextView.text = getTextNoTrial(monthlyPrice)
                }
            }
        }

        binding.yearlyCL.setOnClickListener {
            if (isDoubleClick()) {
                selectedSubscription = YEARLY_SUBS_ID
                resetSelection()
                binding.yearlyCL.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_premium_selected)
                binding.yearlyCheckIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_premium_checked
                    )
                )
                binding.yearlyTitleTV.setTextColor(resources.getColor(R.color.black))
                binding.yearlyPriceTV.setTextColor(resources.getColor(R.color.black))

                if (yearlyTrialPeriod != "0") {
                    binding.detailTextView.text = getTextWithTrial(yearlyPrice, yearlyTrialPeriod)
                } else {
                    binding.detailTextView.text = getTextNoTrial(yearlyPrice)
                }
            }
        }

        binding.upgradeTV.setOnClickListener {
            if (isDoubleClick()) {
                startPurchaseProcess(selectedSubscription)
            }
        }
        AnimationUtils.loadAnimation(this, R.anim.purchase_btn_anim)
            .also { hyperspaceJumpAnimation ->
                binding.upgradeTV.startAnimation(hyperspaceJumpAnimation)
            }
    }

    private fun startPurchaseProcess(selectedSubscription: String) {

        if (networkStatus) {

            iapConnector?.let {
                if (it.getBillingProcessReady()) {

                    iapConnector?.subscribe(this, selectedSubscription)

                } else {
                    showToast(getString(R.string.google_service_not_ready))
                }
            } ?: run {
                showToast(getString(R.string.google_service_not_ready))
            }
        } else {
            showToast(getString(R.string.internet_toast))
        }

    }

    private var toastObj: Toast? = null
    fun showToast(string: String) {
        toastObj?.cancel()
        toastObj = Toast.makeText(this, string, Toast.LENGTH_SHORT)
        toastObj?.show()
    }

    private fun activateSuccessfulPurchase() {
        premiumActive.postValue(true)
        TinyDB.getInstance(this@InAppActivity).putBoolean(IS_PREMIUM, true)
        finish()
    }

    private fun resetSelection() {
        binding.lifeTimeCL.background =
            ContextCompat.getDrawable(this, R.drawable.bg_premium_unselected)
        binding.lifeTimeCheckIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_premium_unchecked
            )
        )
        binding.lifetTimeTitleTV.setTextColor(resources.getColor(R.color.subscription_unselected_color))
        binding.lifeTimePriceTV.setTextColor(resources.getColor(R.color.subscription_unselected_color))

        binding.monthlyCL.background =
            ContextCompat.getDrawable(this, R.drawable.bg_premium_unselected)
        binding.monthlyCheckIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_premium_unchecked
            )
        )
        binding.monthlyTitleTV.setTextColor(resources.getColor(R.color.subscription_unselected_color))
        binding.monthlyPriceTV.setTextColor(resources.getColor(R.color.subscription_unselected_color))

        binding.yearlyCL.background =
            ContextCompat.getDrawable(this, R.drawable.bg_premium_unselected)
        binding.yearlyCheckIV.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_premium_unchecked
            )
        )
        binding.yearlyTitleTV.setTextColor(resources.getColor(R.color.subscription_unselected_color))
        binding.yearlyPriceTV.setTextColor(resources.getColor(R.color.subscription_unselected_color))
    }

    fun getTextWithTrial(price: String, trialPeriod: String): String {

        return getString(R.string.subscription_detail_heading) + "\n" +
                String.format(getString(R.string.premium_text_1), trialPeriod) + "\n" +
                String.format(getString(R.string.premium_text_2), price) + "\n" +
                getString(R.string.premium_text_3) + "\n" +
                getString(R.string.premium_text_4) + "\n" +
                getString(R.string.premium_text_5) + "\n" +
                String.format(getString(R.string.premium_text_6), price) + "\n" +
                getString(R.string.premium_text_7) + "\n" +
                getString(R.string.premium_text_8) + "\n" +
                getString(R.string.premium_text_9) + "\n" +
                getString(R.string.premium_text_10) + "\n" +
                getString(R.string.premium_text_11) + "\n" +
                getString(R.string.premium_text_12) + "\n" + "\n" +
                getString(R.string.premium_text_13) + "\n" +
                getString(R.string.premium_text_14) + "\n" +
                getString(R.string.premium_text_15) + "\n" +
                getString(R.string.premium_text_16) + "\n" +
                getString(R.string.premium_text_17) + "\n"
    }

    fun getTextNoTrial(price: String): String {

        return getString(R.string.subscription_detail_heading) + "\n" +
                String.format(getString(R.string.premium_text_6), price) + "\n" +
                getString(R.string.premium_text_3) + "\n" +
                getString(R.string.premium_text_4) + "\n" +
                getString(R.string.premium_text_5) + "\n" +
                getString(R.string.premium_text_7) + "\n" +
                getString(R.string.premium_text_8) + "\n" +
                getString(R.string.premium_text_9) + "\n" +
                getString(R.string.premium_text_11) + "\n" +
                getString(R.string.premium_text_12) + "\n" + "\n" +
                getString(R.string.premium_text_13) + "\n" +
                getString(R.string.premium_text_14) + "\n" +
                getString(R.string.premium_text_15) + "\n" +
                getString(R.string.premium_text_16) + "\n" +
                getString(R.string.premium_text_17) + "\n"
    }

    fun String.getDaysFromTrialPeriod(): Int {
        return try {
            val multiple = substring(1, 2).toInt()
            when (substring(2, 3)) {
                "W" -> multiple * 7
                "D" -> multiple * 1
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        iapConnector?.closeAllConnection()

        networkReceiver?.let {
            unregisterReceiver(it)
        }
    }
}