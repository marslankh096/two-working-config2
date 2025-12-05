package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class InAppPurchaseProcess(
    private var activity: Activity?,
    private var billingCallbacks: BillingCallbacks? = null,
    private var skuListInApp: ArrayList<String>? = null,
    private var skuListSubscription: ArrayList<String>? = null,
    retryOnConnectionFailed: Boolean,
) : PurchasesUpdatedListener, BillingClientStateListener {
    private var billingClient: BillingClient? = null
    private var retryOnConnectionFailed = false
    private var subscriptionDetailList: List<ProductDetails?>? = null
    private var inAppDetailList: List<ProductDetails?>? = null

    val isBillingProcessReady
        get() = billingClient!!.isReady

    init {
        this.retryOnConnectionFailed = retryOnConnectionFailed
        initBillingProcess()
    }

    fun getBillingClient(): BillingClient? {
        return billingClient
    }

    //
    fun launchSubscriptionFlow(productDetails: ProductDetails) {
        val offers =
            productDetails.subscriptionOfferDetails?.let { offerDetailsList ->
                retrieveEligibleOffers(offerDetails = offerDetailsList)
            }

        val offerToken = offers?.let { leastPricedOfferToken(it) }
        val billingParams = offerToken?.let { token ->
            billingFlowParamsBuilder(
                productDetails = productDetails,
                offerToken = token
            )
        }
        billingParams?.let {
            launchBillingFlow(activity, it)
        }

    }

    fun launchInAppFlow(productDetails: ProductDetails) {
        val params = billingFlowParamsBuilder(productDetails)
        launchBillingFlow(activity, params)
    }

    private fun initBillingProcess() {
        activity?.let {
            billingClient = BillingClient.newBuilder(it)
                .setListener(this)
                .enablePendingPurchases()
                .build()
            establishConnection()
        }

    }

    private fun establishConnection() {
        billingClient?.startConnection(this)
    }

    private fun querySkuDetails() {

        skuListInApp?.let {
            val params = QueryProductDetailsParams.newBuilder()

            val productList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
            for (product in it) {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(product)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            }

            params.setProductList(productList).let { productDetailsParams ->
//                Log.i(TAG, "queryProductDetailsAsync")
                billingClient!!.queryProductDetailsAsync(
                    productDetailsParams.build()
                ) { billingResult, productList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        inAppDetailList = productList
                        billingCallbacks?.onQuerySkuDetailsInApp(productList)
                    }
                }
            }
        }




        skuListSubscription?.let { skulist ->
            val params = QueryProductDetailsParams.newBuilder()

            val productList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
            for (product in skulist) {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(product)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            }
            params.setProductList(productList).let { productDetailsParams ->
                billingClient!!.queryProductDetailsAsync(
                    productDetailsParams.build()
                ) { billingResult, productList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subscriptionDetailList = productList
                        billingCallbacks?.onQuerySkuDetailsSubscription(productList)
                    }
                }
            }
        }

    }

    private fun queryPurchases() {

        skuListInApp?.let { inappList ->
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { result, purchasesList ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (purchasesList.size > 0) {
                        billingCallbacks?.queryPurchaseResultInApp(purchasesList)
                    } else {
                        billingCallbacks?.queryPurchaseResultInApp(null)
                    }

                } else {
                    billingCallbacks?.queryPurchaseResultInApp(null)

                }
            }
        }


//
        skuListSubscription?.let {
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { result, purchasesList ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (purchasesList.size > 0) {
                        billingCallbacks?.queryPurchaseResultSubscription(purchasesList)
                    } else {
                        billingCallbacks?.queryPurchaseResultSubscription(null)
                    }

                } else {
                    billingCallbacks?.queryPurchaseResultSubscription(null)

                }
            }
        }

    }

    private fun launchBillingFlow(activity: Activity?, params: BillingFlowParams): Int {
        val billingResult = billingClient!!.launchBillingFlow(activity!!, params)
        return billingResult.responseCode
    }

    private fun billingFlowParamsBuilder(productDetails: ProductDetails): BillingFlowParams {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
        ).build()
    }


    private fun billingFlowParamsBuilder(productDetails: ProductDetails, offerToken: String):
            BillingFlowParams {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        ).build()
    }

    private fun retrieveEligibleOffers(
        offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>
    ):
            List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            eligibleOffers.add(offerDetail)
        }
        return eligibleOffers
    }

    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>
    ): String {
        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        if (!offerDetails.isNullOrEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros < lowestPrice) {
                        lowestPrice = price.priceAmountMicros.toInt()
                        leastPricedOffer = offer
                        offerToken = leastPricedOffer.offerToken
                    }
                }
            }
        }
        return offerToken

    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (list != null) {
                billingCallbacks?.billingPurchased(list as MutableList<Purchase>?)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> billingCallbacks?.billingCanceled()
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> billingCallbacks?.itemAlreadyOwned()
        }
    }

    override fun onBillingServiceDisconnected() {
        if (retryOnConnectionFailed) {
            establishConnection()
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                querySkuDetails()
                queryPurchases()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                billingCallbacks?.billingCanceled()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                billingCallbacks?.itemAlreadyOwned()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                billingCallbacks?.billingFailedToInitialize(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
            }
        }
    }

    fun disconnect() {
        billingClient?.endConnection()
        activity = null
        billingCallbacks = null
        skuListInApp = null
        skuListSubscription = null
        billingClient = null
        subscriptionDetailList = null
        inAppDetailList = null
    }
}

interface BillingCallbacks {
    fun billingFailedToInitialize(code: Int)
    fun billingPurchased(purchases: MutableList<Purchase>?)
    fun billingCanceled()
    fun itemAlreadyOwned()
    fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<ProductDetails>?)
    fun onQuerySkuDetailsInApp(skuDetailsList: MutableList<ProductDetails>?)
    fun queryPurchaseResultInApp(purchases: MutableList<Purchase>?)
    fun queryPurchaseResultSubscription(purchases: MutableList<Purchase>?)
}