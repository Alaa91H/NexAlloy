package io.github.nexalloy.revanced.telegram.behavior

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private fun ClassLoader.findClassOrNull(name: String): Class<*>? =
    runCatching { loadClass(name) }.getOrNull()

val DisableMediaAutoplay = patch(
    name = "Disable media autoplay",
    description = "Disables Telegram autoplay for videos and GIFs while keeping manual playback available.",
    use = false,
) {
    classLoader.findClassOrNull("org.telegram.messenger.SharedConfig")
        ?.declaredMethods
        ?.filter {
            (it.name == "isAutoplayVideo" || it.name == "isAutoplayGifs") &&
                it.returnType == Boolean::class.javaPrimitiveType
        }
        ?.forEach {
            it.hookMethod(XC_MethodReplacement.returnConstant(false))
        }
}

val OpenLinksExternally = patch(
    name = "Open links externally",
    description = "Prefers the system/external browser over Telegram's in-app browser for regular web links.",
    use = false,
) {
    classLoader.findClassOrNull("org.telegram.messenger.browser.Browser")
        ?.declaredMethods
        ?.filter { it.name == "openUrl" }
        ?.forEach { method ->
            val types = method.parameterTypes
            if (
                types.size >= 9 &&
                types[8] == Boolean::class.javaPrimitiveType
            ) {
                method.hookMethod {
                    before { param ->
                        // allowInAppBrowser
                        param.args[8] = false
                    }
                }
            }
        }
}
