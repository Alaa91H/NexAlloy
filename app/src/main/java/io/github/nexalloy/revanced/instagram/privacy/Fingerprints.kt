package io.github.nexalloy.revanced.instagram.privacy

import io.github.nexalloy.morphe.fingerprint

val addFlagsToWindowFingerprint = fingerprint {
    returns("V")
    strings("Inconsistency in window FLAG_SECURE state detected! window state: ")
}

val directScreenshotCaptureTriggerFingerprint = fingerprint {
    returns("V")
    strings("igd_screenshot_capture")
}
