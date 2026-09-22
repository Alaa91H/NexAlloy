package io.github.nexalloy.revanced.tiktok

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics
import io.github.nexalloy.revanced.tiktok.ad.HideAds
import io.github.nexalloy.revanced.tiktok.interaction.downloads.EnableDownloads
import io.github.nexalloy.revanced.tiktok.interaction.seekbar.ShowSeekbar
import io.github.nexalloy.revanced.tiktok.interaction.speed.EnablePlaybackSpeed
import io.github.nexalloy.revanced.tiktok.misc.login.DisableLoginRequirement
import io.github.nexalloy.revanced.tiktok.misc.login.FixGoogleLogin
import io.github.nexalloy.revanced.tiktok.privacy.SanitizeTikTokSharingLinks

val TikTokPatches = arrayOf(
    DisableCommonAnalytics,
    HideAds,
    ShowSeekbar,
    EnablePlaybackSpeed,
    EnableDownloads,
    DisableLoginRequirement,
    FixGoogleLogin,
    SanitizeTikTokSharingLinks,
)
