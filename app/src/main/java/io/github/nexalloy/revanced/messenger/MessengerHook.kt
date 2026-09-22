package io.github.nexalloy.revanced.messenger

import io.github.nexalloy.revanced.messenger.ads.HideInboxAds
import io.github.nexalloy.revanced.meta.privacy.SanitizeMetaSharingLinks

val MessengerPatches = arrayOf(
    HideInboxAds,
    SanitizeMetaSharingLinks,
)
