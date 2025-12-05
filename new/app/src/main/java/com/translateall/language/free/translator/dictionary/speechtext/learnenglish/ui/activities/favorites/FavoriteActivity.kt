package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.favorites

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.MyDataBase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityFavoriteBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.favorites.adapters.FavoritesAdapter
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inputscreen.InputActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.onClickFavoriteItem
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INTENT_FAVORITE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INTENT_HISTORY
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import kotlinx.coroutines.runBlocking

class FavoriteActivity : BaseActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private var isFrom: String? = null
    private var favoriteAdapter: FavoritesAdapter? = null
    private var myDataBase: MyDataBase? = null
    private var mLastClickTime: Long = 0
    private var adCalled = false

    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
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

        isFrom = intent.getStringExtra("from")
        if (isFrom == INTENT_HISTORY) {
            val title = getString(R.string.my_history)
            binding.tvTitleHistory.text = title

        }

        myDataBase = MyDataBase.getInstance(this)
        setListeners()

        favoriteAdapter = FavoritesAdapter()
        binding.rvFavorite.adapter = favoriteAdapter
        binding.rvFavorite.isNestedScrollingEnabled = false

        isFrom?.let { type ->

            if (type == INTENT_FAVORITE) {
                myDataBase?.translationDao()?.getFavorites(true)?.apply {
                    this.observe(this@FavoriteActivity) {
                        if (it.size > 0) {
                            binding.rvFavorite.visibility = View.VISIBLE

                            it.reverse()
                            favoriteAdapter?.setData(it)
                            if (!adCalled)
                                showFavoriteNativeAd()
                        } else {
                            binding.tvFavoriteNo.text = getString(R.string.no_favorite_found_)
                            binding.ivHistoryFavorite.setImageResource(R.drawable.ic_star_history_unfil)
                            binding.rvFavorite.visibility = View.GONE
                            binding.nativeAd.gone()
                        }
                    }


                }
            } else {
                myDataBase?.translationDao()?.allHistory?.apply {
                    this.observe(this@FavoriteActivity) {
                        if (it.size > 0) {
                            binding.rvFavorite.visibility = View.VISIBLE
                            it.reverse()
                            favoriteAdapter?.setData(it)
                            if (!adCalled)
                                showHistoryNativeAd()
                        } else {
                            binding.tvFavoriteNo.text = getString(R.string.no_history_found_)
                            binding.ivHistoryFavorite.setImageResource(R.drawable.ic_history)
                            binding.rvFavorite.visibility = View.GONE
                            binding.nativeAd.gone()
                        }
                    }
                }
            }
        }

        onClickFavoriteItem = {
            val intent = Intent(this@FavoriteActivity, InputActivity::class.java)
            intent.putExtra("history_model", it)
            startActivity(intent)
            finish()
//            setResult(Activity.RESULT_OK, intent)
        }

        AppUtils.favoriteHistoryItem = { model, favorite ->
            runBlocking {
                model.apply {
                    isFavorite = favorite
                    updateFavorite(this)
                }


            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (isPremium())
            binding.nativeAd.gone()
    }

    private fun setListeners() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })

        binding.ivBackFavorite.setOnClickListener {
            finish()
        }

    }

    private fun updateFavorite(translationHistory: TranslationHistory) {
        kotlin.runCatching {
            myDataBase!!.translationDao().update(translationHistory)
        }

    }

    private fun showHistoryNativeAd() {
        adCalled = true
        if (isPremium().not() && isOnline(this@FavoriteActivity)) {
            AdsManagerX.loadNativeAd(
                this@FavoriteActivity,
                AdConfigManager.NATIVE_AD_HISTORY.apply {
                    adConfig.nativeAdLayout =
                        R.layout.custom_native_seventy
                }, binding.nativeAd, onAdFailed = {
                    binding.nativeAd.gone()
                }
            )
        } else {
            binding.nativeAd.gone()
        }
    }

    private fun showFavoriteNativeAd() {
        adCalled = true
        if (isPremium().not() && isOnline(this@FavoriteActivity)) {
            AdsManagerX.loadNativeAd(
                this@FavoriteActivity,
                AdConfigManager.NATIVE_AD_FAVOURITES.apply {
                    adConfig.nativeAdLayout =
                        R.layout.custom_native_seventy
                }, binding.nativeAd, onAdFailed = {
                    binding.nativeAd.gone()
                }
            )
        } else {
            binding.nativeAd.gone()
        }
    }
}