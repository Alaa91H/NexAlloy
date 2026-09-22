package io.github.nexalloy.revanced.twitter.privacy

import android.content.Intent
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch
import io.github.nexalloy.revanced.shared.links.sanitizeUrlsInText

private val xLiteFlags = setOf(
    "x_lite_in_tfa_for_existing_users_enabled",
    "existing_user_redirected_to_x_lite",
    "x_lite_in_tfa_for_existing_users_exit_enabled",
)

val BlockRedirectToXLite = patch(
    name = "Block redirecting to X Lite",
    description = "Keeps X from enabling the X Lite migration flags used for existing-user redirects.",
) {
    val preferencesClass = Class.forName("android.app.SharedPreferencesImpl")
    preferencesClass.getDeclaredMethod(
        "getBoolean",
        String::class.java,
        Boolean::class.javaPrimitiveType!!,
    ).hookMethod {
        before { param ->
            if ((param.args.firstOrNull() as? String) in xLiteFlags) {
                param.result = false
            }
        }
    }

    runCatching { Class.forName("android.app.SharedPreferencesImpl\$EditorImpl") }
        .getOrNull()
        ?.declaredMethods
        ?.filter {
            it.name == "putBoolean" &&
                it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == String::class.java &&
                it.parameterTypes[1] == Boolean::class.javaPrimitiveType
        }
        ?.forEach { method ->
            method.hookMethod {
                before { param ->
                    if ((param.args.firstOrNull() as? String) in xLiteFlags) {
                        param.args[1] = false
                    }
                }
            }
        }
}

val SanitizeXSharingLinks = patch(
    name = "Sanitize X sharing links",
    description = "Removes common X/Twitter tracking parameters from URLs placed in Android share intents.",
    use = false,
) {
    val hosts = setOf("x.com", "twitter.com")
    val tracking = setOf(
        "s",
        "t",
        "ref_src",
        "ref_url",
        "src",
        "cn",
        "cxt",
        "twclid",
    )

    Intent::class.java.declaredMethods
        .filter {
            it.name == "putExtra" &&
                it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == String::class.java &&
                CharSequence::class.java.isAssignableFrom(it.parameterTypes[1])
        }
        .forEach { method ->
            method.hookMethod {
                before { param ->
                    if (param.args[0] != Intent.EXTRA_TEXT) return@before
                    val text = param.args[1] as? CharSequence ?: return@before
                    param.args[1] = sanitizeUrlsInText(text, hosts, tracking)
                }
            }
        }
}
