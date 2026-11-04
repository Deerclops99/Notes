package com.deerclops99.notesbyvicsign99.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.deerclops99.notesbyvicsign99.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAds {
    private var ad: RewardedAd? = null
    private var loading = false

    // Deja un anuncio listo para cuando el usuario lo pida
    fun preload(context: Context) {
        if (ad != null || loading) return
        loading = true
        RewardedAd.load(
            context.applicationContext,
            BuildConfig.ADMOB_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(loaded: RewardedAd) {
                    ad = loaded
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad = null
                    loading = false
                }
            }
        )
    }

    // onReward se llama solo si el usuario ganó la recompensa
    fun show(
        activity: Activity,
        onReward: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        val current = ad
        if (current == null) {
            preload(activity)
            onUnavailable()
            return
        }
        var earned = false
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                preload(activity)
                if (earned) onReward()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad = null
                preload(activity)
                onUnavailable()
            }
        }
        current.show(activity) { earned = true }
    }
}

// Busca la Activity dentro del Context de Compose
fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
