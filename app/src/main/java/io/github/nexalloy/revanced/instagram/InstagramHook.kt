package io.github.nexalloy.revanced.instagram

import io.github.nexalloy.revanced.instagram.download.EnableNativeDownloads
import io.github.nexalloy.revanced.instagram.media.DisableVideoAutoplay
import io.github.nexalloy.revanced.instagram.privacy.DisableScreenshotDetection
import io.github.nexalloy.revanced.instagram.stories.DisableStoryFlipping
import io.github.nexalloy.revanced.meta.ads.HideAds
import io.github.nexalloy.revanced.meta.privacy.SanitizeMetaSharingLinks

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics

val InstagramPatches = arrayOf(
    DisableCommonAnalytics,
    HideAds,
    EnableNativeDownloads,
    DisableScreenshotDetection,
    DisableStoryFlipping,
    DisableVideoAutoplay,
    SanitizeMetaSharingLinks,
)
