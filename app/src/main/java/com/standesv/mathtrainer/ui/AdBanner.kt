package com.standesv.mathtrainer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.standesv.mathtrainer.BuildConfig
import com.standesv.mathtrainer.R

/**
 * Banniere AdMob ancree en bas d'ecran.
 *
 * [enabled] doit refleter `ConsentManager.canRequestAds` : tant qu'il est
 * faux, aucune AdView n'est construite, donc aucune requete publicitaire
 * n'est emise. C'est ce qui rend l'integration conforme au RGPD.
 *
 * En build debug, l'identifiant de test officiel de Google est utilise :
 * cliquer sur ses propres annonces reelles est le motif classique de
 * suspension d'un compte AdMob.
 */
@Composable
fun AdBanner(enabled: Boolean, modifier: Modifier = Modifier) {
    if (!enabled || LocalInspectionMode.current) return

    val widthDp = LocalConfiguration.current.screenWidthDp
    val unitId = stringResource(
        if (BuildConfig.DEBUG) R.string.admob_banner_test_unit_id
        else R.string.admob_banner_unit_id
    )

    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = unitId
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, widthDp)
                    )
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
