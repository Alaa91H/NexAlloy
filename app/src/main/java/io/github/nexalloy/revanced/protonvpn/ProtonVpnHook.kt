package io.github.nexalloy.revanced.protonvpn

import io.github.nexalloy.revanced.protonvpn.delay.RemoveServerChangeDelay
import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val ProtonVpnPatches = arrayOf(
    RemoveServerChangeDelay,
    DisableCommonAnalytics,
)
