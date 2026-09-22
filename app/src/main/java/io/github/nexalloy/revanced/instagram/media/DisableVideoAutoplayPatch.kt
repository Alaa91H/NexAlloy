package io.github.nexalloy.revanced.instagram.media

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.hookMethod
import io.github.nexalloy.morphe.fingerprint
import io.github.nexalloy.patch

val disableVideoAutoplayMethodFingerprint = fingerprint {
    returns("Z")
    strings(
        "ig_olympus_disable_video_autoplay",
        "ig_disable_video_autoplay",
        "ig_video_setting",
    )
}

val DisableVideoAutoplay = patch(
    name = "Disable Instagram video autoplay",
    description = "Forces Instagram's native disable-video-autoplay feature gate on.",
    use = false,
) {
    ::disableVideoAutoplayMethodFingerprint.memberOrNull
        ?.hookMethod(XC_MethodReplacement.returnConstant(true))
}
