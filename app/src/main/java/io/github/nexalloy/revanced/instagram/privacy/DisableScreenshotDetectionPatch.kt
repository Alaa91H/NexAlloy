package io.github.nexalloy.revanced.instagram.privacy

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

val DisableScreenshotDetection = patch(
    name = "Disable Instagram screenshot detection",
    description = "Blocks Instagram DM screenshot-detection trigger paths without changing media access.",
    use = false,
) {
    ::addFlagsToWindowFingerprint.memberOrNull
        ?.hookMethod(XC_MethodReplacement.DO_NOTHING)

    ::directScreenshotCaptureTriggerFingerprint.memberOrNull
        ?.hookMethod(XC_MethodReplacement.DO_NOTHING)
}
