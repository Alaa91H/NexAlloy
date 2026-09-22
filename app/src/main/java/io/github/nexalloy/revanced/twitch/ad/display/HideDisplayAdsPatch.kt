package io.github.nexalloy.revanced.twitch.ad.display

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

val HideDisplayAds = patch(
    name = "Hide Twitch display ads",
    description = "Returns Twitch's no-ad response object from the display-ad parser. Currently fingerprinted from Twitch 29.9.1.",
    use = false,
) {
    val noAd = runCatching {
        val clazz = classLoader.loadClass("gq")
        clazz.getDeclaredField("a").apply { isAccessible = true }.get(null)
    }.getOrNull()

    if (noAd != null) {
        ::displayAdResponseParserFingerprint.memberOrNull
            ?.hookMethod(XC_MethodReplacement.returnConstant(noAd))
    }
}
