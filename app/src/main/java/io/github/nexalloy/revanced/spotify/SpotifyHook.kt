package io.github.nexalloy.revanced.spotify

import io.github.nexalloy.revanced.shared.privacy.DisableCommonAnalytics
import io.github.nexalloy.revanced.spotify.audio.RelaxAudioCapturePolicy

val SpotifyPatches = arrayOf(
    RelaxAudioCapturePolicy,
    DisableCommonAnalytics,
)
