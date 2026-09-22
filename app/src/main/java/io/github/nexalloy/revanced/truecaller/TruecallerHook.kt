package io.github.nexalloy.revanced.truecaller

import io.github.nexalloy.revanced.truecaller.ads.HideAds
import io.github.nexalloy.revanced.truecaller.analytics.DisableAnalytics
import io.github.nexalloy.revanced.truecaller.misc.DisableAppStartTelemetry
import io.github.nexalloy.revanced.truecaller.misc.DisableUpdateCheck
import io.github.nexalloy.revanced.truecaller.premium.HideUpgradePrompts
import io.github.nexalloy.revanced.truecaller.premium.RemovePremiumUi
import io.github.nexalloy.revanced.shared.restrictions.AllowScreenCapture

val TruecallerPatches = arrayOf(
    AllowScreenCapture,
    HideAds,
    DisableAnalytics,
    DisableAppStartTelemetry,
    HideUpgradePrompts,
    RemovePremiumUi,
    DisableUpdateCheck,
)
