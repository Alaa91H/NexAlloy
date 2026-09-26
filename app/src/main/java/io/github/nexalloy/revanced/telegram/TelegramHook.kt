package io.github.nexalloy.revanced.telegram

import io.github.nexalloy.revanced.telegram.ads.RemoveSponsoredAds
import io.github.nexalloy.revanced.telegram.forward.ForwardWithoutAttribution

val TelegramPatches = arrayOf(
    RemoveSponsoredAds,
    ForwardWithoutAttribution,
)
