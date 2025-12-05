package com.hm.admanagerx.adsanalysis

import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.admanagerx.AdsManagerX.getAllAdsAnalysis
import com.hm.admanagerx.R
import com.hm.admanagerx.databinding.ActivityMainBinding

@Keep
data class AdAnalyticsTracker(
    var adsTitle: String = "",
    var totalRequests: Int = 0,
    var totalLoaded: Int = 0,
    var totalLoadFailed: Int = 0,
    var totalShow: Int = 0,
    var totalShowFailed: Int = 0,
    var totalImpression: Int = 0,
    var totalClicked: Int = 0,
) {
    fun trackAdRequest() = ++totalRequests
    fun trackAdLoaded() = ++totalLoaded
    fun trackAdLoadFailed() = ++totalLoadFailed
    fun trackAdShow() = ++totalShow
    fun trackAdShowFailed() = ++totalShowFailed
    fun trackAdImpression() = ++totalImpression
    fun trackAdClicked() = ++totalClicked

    fun clear() {
        totalRequests = 0
        totalLoaded = 0
        totalLoadFailed = 0
        totalShow = 0
        totalShowFailed = 0
    }


    override fun toString(): String {
        return "$adsTitle: \n totalRequests = $totalRequests\n totalLoaded = $totalLoaded\n " +
                "totalShow = $totalShow\n totalImpression = $totalImpression\n " +
                "totalClicked = $totalClicked\n totalLoadFailed = $totalLoadFailed\n" +
                " totalShowFailed = $totalShowFailed"
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdAnalyticsTrackerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRecyclerView()
/*
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
        }*/
    }

    private fun initRecyclerView() {
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter
        adapter = AdAnalyticsTrackerAdapter(getAllAdsAnalysis())
        recyclerView.adapter = adapter
    }
}