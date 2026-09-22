package io.github.nexalloy.morphe.reddit

import io.github.nexalloy.morphe.reddit.ad.HideAds
import io.github.nexalloy.morphe.reddit.layout.subredditdialog.RemoveNsfwWarning
import io.github.nexalloy.morphe.reddit.misc.openlink.OpenLinksDirectly
import io.github.nexalloy.morphe.reddit.misc.openlink.OpenLinksExternally
import io.github.nexalloy.morphe.reddit.misc.privacy.SanitizeSharingLinks
import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val RedditPatches = arrayOf(
    DisableCommonAnalytics,
    HideAds,
    SanitizeSharingLinks,
    OpenLinksDirectly,
    OpenLinksExternally,
    RemoveNsfwWarning,
)
