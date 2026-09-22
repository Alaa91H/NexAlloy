package io.github.nexalloy.revanced.messenger

import io.github.nexalloy.revanced.messenger.ads.HideInboxAds
import io.github.nexalloy.revanced.shared.privacy.BlockCommonDisplayAds
import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val MessengerPatches = arrayOf(
    HideInboxAds,
    DisableCommonAnalytics,
    BlockCommonDisplayAds,
)
