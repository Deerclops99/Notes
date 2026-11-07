package com.sweetyani.misnotas.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.sweetyani.misnotas.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adWidth = LocalConfiguration.current.screenWidthDp

    val adView = remember(adWidth) {
        AdView(context).apply {
            setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    context,
                    adWidth
                )
            )
            adUnitId = BuildConfig.ADMOB_BANNER_ID
        }
    }

    DisposableEffect(adView) {
        val handler = Handler(Looper.getMainLooper())
        var loaded = false
        var loading = false

        fun load() {
            if (loaded || loading) return
            loading = true
            adView.loadAd(AdRequest.Builder().build())
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                loaded = true
                loading = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loading = false
                // Reintento a los 30 s por si la red sigue inestable
                handler.postDelayed({ load() }, 30_000)
            }
        }

        // Cuando vuelve la conexión, pide el anuncio si todavía no cargó
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handler.post { load() }
            }
        }
        cm.registerDefaultNetworkCallback(callback)

        load()

        onDispose {
            handler.removeCallbacksAndMessages(null)
            runCatching { cm.unregisterNetworkCallback(callback) }
            adView.adListener = object : AdListener() {}
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { adView }
    )
}