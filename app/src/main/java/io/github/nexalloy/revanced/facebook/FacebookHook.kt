package io.github.nexalloy.revanced.facebook

import io.github.nexalloy.revanced.facebook.ads.HideStoryAds
import io.github.nexalloy.revanced.meta.privacy.SanitizeMetaSharingLinks

val FacebookPatches = arrayOf(
    HideStoryAds,
    SanitizeMetaSharingLinks,
)
