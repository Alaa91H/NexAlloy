package io.github.nexalloy.revanced.facebook

import io.github.nexalloy.revanced.facebook.ads.HideStoryAds
import io.github.nexalloy.revanced.shared.privacy.BlockCommonDisplayAds
import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val FacebookPatches = arrayOf(
    HideStoryAds,
    DisableCommonAnalytics,
    BlockCommonDisplayAds,
)
