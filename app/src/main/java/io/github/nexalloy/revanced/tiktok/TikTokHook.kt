package io.github.nexalloy.revanced.tiktok

import io.github.nexalloy.revanced.tiktok.ad.HideAds
import io.github.nexalloy.revanced.tiktok.interaction.downloads.EnableDownloads
import io.github.nexalloy.revanced.tiktok.interaction.seekbar.ShowSeekbar
import io.github.nexalloy.revanced.tiktok.interaction.speed.EnablePlaybackSpeed
import io.github.nexalloy.revanced.tiktok.misc.login.DisableLoginRequirement
import io.github.nexalloy.revanced.tiktok.misc.login.FixGoogleLogin

val TikTokPatches = arrayOf(
    HideAds,
    ShowSeekbar,
    EnablePlaybackSpeed,
    EnableDownloads,
    DisableLoginRequirement,
    FixGoogleLogin,
)
