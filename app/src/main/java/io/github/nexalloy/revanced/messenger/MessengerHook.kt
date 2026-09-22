package io.github.nexalloy.revanced.messenger

import io.github.nexalloy.revanced.messenger.ads.HideInboxAds
import io.github.nexalloy.revanced.meta.privacy.SanitizeMetaSharingLinks

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val MessengerPatches = arrayOf(
    DisableCommonAnalytics,
    HideInboxAds,
    SanitizeMetaSharingLinks,
)
