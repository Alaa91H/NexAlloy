package io.github.nexalloy

import io.github.nexalloy.hoodles.morphe.alltrails.AllTrailsPatches
import io.github.nexalloy.morphe.music.YTMusicPatches
import io.github.nexalloy.morphe.reddit.RedditPatches
import io.github.nexalloy.morphe.youtube.YouTubePatches
import io.github.nexalloy.revanced.googlephotos.GooglePhotosPatches
import io.github.nexalloy.revanced.meta.MetaPatches
import io.github.nexalloy.revanced.photomath.PhotomathPatches
import io.github.nexalloy.revanced.strava.StravaPatches
import io.github.nexalloy.runtime.RuntimeLayerRegistry

class AppPatchInfo(
    val appName: String,
    val packageName: String,
    private val builtInPatches: Array<Patch>,
) {
    /** Built-in patches plus compiled runtime layers for this host package. */
    val patches: Array<Patch>
        get() = (builtInPatches.asList() + RuntimeLayerRegistry.layersFor(packageName).asList())
            .distinct()
            .toTypedArray()
}

val appPatchConfigurations = listOf(
    AppPatchInfo("YouTube", "com.google.android.youtube", YouTubePatches),
    AppPatchInfo("YT Music", "com.google.android.apps.youtube.music", YTMusicPatches),
    AppPatchInfo("Reddit", "com.reddit.frontpage", RedditPatches),
    AppPatchInfo("Google Photos", "com.google.android.apps.photos", GooglePhotosPatches),
    AppPatchInfo("Photomath", "com.microblink.photomath", PhotomathPatches),
    AppPatchInfo("Instagram", "com.instagram.android", MetaPatches),
    AppPatchInfo("Threads", "com.instagram.barcelona", MetaPatches),
    AppPatchInfo("Strava", "com.strava", StravaPatches),
    AppPatchInfo("AllTrails", "com.alltrails.alltrails", AllTrailsPatches),
    AppPatchInfo("Gboard", "com.google.android.inputmethod.latin", emptyArray()),
    AppPatchInfo("Truecaller", "com.truecaller", emptyArray()),
    AppPatchInfo("Telegram", "org.telegram.messenger", emptyArray()),
    AppPatchInfo("Messenger", "com.facebook.orca", emptyArray()),
    AppPatchInfo("Proton VPN", "ch.protonvpn.android", emptyArray()),
    AppPatchInfo("X", "com.twitter.android", emptyArray()),
)

val patchesByPackage = appPatchConfigurations.associate { it.packageName to it.patches }
