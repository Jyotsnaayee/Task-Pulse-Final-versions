package com.example.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

/**
 * Manages App Open and Interstitial ads.
 */
class AdManager(private val application: Application) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAppOpenAd = false
    private var isShowingAppOpenAd = false
    private var appOpenLoadTime: Long = 0

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitialAd = false

    private var currentActivity: Activity? = null

    // TEST IDs - Replace with real IDs in production
    private val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395915"
    private val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadAppOpenAd()
        loadInterstitialAd()
    }

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showAppOpenAdIfAvailable()
    }

    /** Request an app open ad. */
    fun loadAppOpenAd() {
        if (isLoadingAppOpenAd || isAdAvailable()) return

        isLoadingAppOpenAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAppOpenAd = false
                    appOpenLoadTime = Date().time
                    Log.d("AdManager", "App Open Ad Loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAppOpenAd = false
                    Log.d("AdManager", "App Open Ad Failed to Load: ${loadAdError.message}")
                }
            }
        )
    }

    /** Request an interstitial ad. */
    fun loadInterstitialAd() {
        if (isLoadingInterstitialAd || interstitialAd != null) return

        isLoadingInterstitialAd = true
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            application,
            INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitialAd = false
                    Log.d("AdManager", "Interstitial Ad Loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoadingInterstitialAd = false
                    Log.d("AdManager", "Interstitial Ad Failed to Load: ${loadAdError.message}")
                }
            }
        )
    }

    /** Shows the app open ad. */
    fun showAppOpenAdIfAvailable() {
        if (isShowingAppOpenAd) return

        if (!isAdAvailable()) {
            loadAppOpenAd()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }

        currentActivity?.let {
            appOpenAd?.show(it)
        }
    }

    /** Shows an interstitial ad. */
    fun showInterstitialAd(onAdDismissed: () -> Unit = {}) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdDismissed()
                }
            }
            currentActivity?.let {
                interstitialAd?.show(it)
            } ?: run {
                onAdDismissed()
            }
        } else {
            loadInterstitialAd()
            onAdDismissed()
        }
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - appOpenLoadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
