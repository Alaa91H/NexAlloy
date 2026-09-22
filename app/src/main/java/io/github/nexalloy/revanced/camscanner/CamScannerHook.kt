package io.github.nexalloy.revanced.camscanner

import io.github.nexalloy.revanced.camscanner.telemetry.DisableTelemetry
import io.github.nexalloy.revanced.shared.privacy.BlockCommonDisplayAds
import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val CamScannerPatches = arrayOf(
    DisableTelemetry,
    DisableCommonAnalytics,
    BlockCommonDisplayAds,
)
