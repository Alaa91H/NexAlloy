package io.github.nexalloy

import io.github.nexalloy.hoodles.morphe.alltrails.AllTrailsPatches
import io.github.nexalloy.morphe.music.YTMusicPatches
import io.github.nexalloy.morphe.reddit.RedditPatches
import io.github.nexalloy.morphe.youtube.YouTubePatches
import io.github.nexalloy.revanced.duolingo.DuolingoPatches
import io.github.nexalloy.revanced.facebook.FacebookPatches
import io.github.nexalloy.revanced.googlephotos.GooglePhotosPatches
import io.github.nexalloy.revanced.hexeditor.HexEditorPatches
import io.github.nexalloy.revanced.instagram.InstagramPatches
import io.github.nexalloy.revanced.inshorts.InshortsPatches
import io.github.nexalloy.revanced.messenger.MessengerPatches
import io.github.nexalloy.revanced.meta.MetaPatches
import io.github.nexalloy.revanced.photomath.PhotomathPatches
import io.github.nexalloy.revanced.protonvpn.ProtonVpnPatches
import io.github.nexalloy.revanced.spotify.SpotifyPatches
import io.github.nexalloy.revanced.strava.StravaPatches
import io.github.nexalloy.revanced.tiktok.TikTokPatches
import io.github.nexalloy.revanced.twitch.TwitchPatches
import io.github.nexalloy.revanced.twitter.TwitterPatches

class AppPatchInfo(val appName: String, val packageName: String, val patches: Array<Patch>)

val appPatchConfigurations = listOf(
    AppPatchInfo("YouTube", "com.google.android.youtube", YouTubePatches),
    AppPatchInfo("YT Music", "com.google.android.apps.youtube.music", YTMusicPatches),
    AppPatchInfo("Reddit", "com.reddit.frontpage", RedditPatches),
    AppPatchInfo("Google Photos", "com.google.android.apps.photos", GooglePhotosPatches),
    AppPatchInfo("Photomath", "com.microblink.photomath", PhotomathPatches),
    AppPatchInfo("Instagram", "com.instagram.android", InstagramPatches),
    AppPatchInfo("Threads", "com.instagram.barcelona", MetaPatches),
    AppPatchInfo("Messenger", "com.facebook.orca", MessengerPatches),
    AppPatchInfo("Facebook", "com.facebook.katana", FacebookPatches),
    AppPatchInfo("X (Twitter)", "com.twitter.android", TwitterPatches),
    AppPatchInfo("TikTok", "com.zhiliaoapp.musically", TikTokPatches),
    AppPatchInfo("TikTok (Trill)", "com.ss.android.ugc.trill", TikTokPatches),
    AppPatchInfo("Twitch", "tv.twitch.android.app", TwitchPatches),
    AppPatchInfo("Spotify", "com.spotify.music", SpotifyPatches),
    AppPatchInfo("Duolingo", "com.duolingo", DuolingoPatches),
    AppPatchInfo("ProtonVPN", "ch.protonvpn.android", ProtonVpnPatches),
    AppPatchInfo("Inshorts", "com.nis.app", InshortsPatches),
    AppPatchInfo("Hex Editor", "com.myprog.hexedit", HexEditorPatches),
    AppPatchInfo("Strava", "com.strava", StravaPatches),
    AppPatchInfo("AllTrails", "com.alltrails.alltrails", AllTrailsPatches),
)

val patchesByPackage = appPatchConfigurations.associate { it.packageName to it.patches }
