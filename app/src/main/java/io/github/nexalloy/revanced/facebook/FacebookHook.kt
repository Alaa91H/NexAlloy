package io.github.nexalloy.revanced.facebook

import io.github.nexalloy.revanced.facebook.ads.HideStoryAds
import io.github.nexalloy.revanced.meta.privacy.SanitizeMetaSharingLinks

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val FacebookPatches = arrayOf(
    DisableCommonAnalytics,
    HideStoryAds,
    SanitizeMetaSharingLinks,
)
