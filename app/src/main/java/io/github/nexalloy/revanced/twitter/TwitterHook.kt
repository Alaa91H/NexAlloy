package io.github.nexalloy.revanced.twitter

import io.github.nexalloy.revanced.twitter.links.BlockRedirectToXLite
import io.github.nexalloy.revanced.twitter.links.ClearTrackingParams
import io.github.nexalloy.revanced.twitter.timeline.HidePromotedTimelineEntries

val TwitterPatches = arrayOf(
    HidePromotedTimelineEntries,
    ClearTrackingParams,
    BlockRedirectToXLite,
)
