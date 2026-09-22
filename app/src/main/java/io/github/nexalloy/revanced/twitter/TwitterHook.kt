package io.github.nexalloy.revanced.twitter

import io.github.nexalloy.revanced.twitter.privacy.BlockRedirectToXLite
import io.github.nexalloy.revanced.twitter.privacy.SanitizeXSharingLinks
import io.github.nexalloy.revanced.twitter.timeline.HidePromotedTimelineEntries

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val TwitterPatches = arrayOf(
    DisableCommonAnalytics,
    HidePromotedTimelineEntries,
    BlockRedirectToXLite,
    SanitizeXSharingLinks,
)
