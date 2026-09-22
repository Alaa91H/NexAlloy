package io.github.nexalloy.revanced.meta

import io.github.nexalloy.revanced.meta.ads.HideAds
import io.github.nexalloy.revanced.meta.privacy.SanitizeMetaSharingLinks

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val MetaPatches = arrayOf(
    DisableCommonAnalytics,
    HideAds,
    SanitizeMetaSharingLinks,
)
