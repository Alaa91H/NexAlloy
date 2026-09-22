package io.github.nexalloy.revanced.twitter

import io.github.nexalloy.revanced.twitter.privacy.BlockRedirectToXLite
import io.github.nexalloy.revanced.twitter.privacy.SanitizeXSharingLinks
import io.github.nexalloy.revanced.twitter.timeline.HidePromotedTimelineEntries

val TwitterPatches = arrayOf(
    HidePromotedTimelineEntries,
    BlockRedirectToXLite,
    SanitizeXSharingLinks,
)
