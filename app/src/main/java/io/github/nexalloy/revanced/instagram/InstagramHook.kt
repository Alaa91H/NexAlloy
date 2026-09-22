package io.github.nexalloy.revanced.instagram

import io.github.nexalloy.revanced.instagram.download.EnableNativeDownloads
import io.github.nexalloy.revanced.instagram.links.SanitizeShareLinks
import io.github.nexalloy.revanced.instagram.privacy.DisableScreenshotDetection
import io.github.nexalloy.revanced.instagram.stories.DisableStoryFlipping
import io.github.nexalloy.revanced.meta.ads.HideAds

val InstagramPatches = arrayOf(
    HideAds,
    EnableNativeDownloads,
    SanitizeShareLinks,
    DisableScreenshotDetection,
    DisableStoryFlipping,
)
