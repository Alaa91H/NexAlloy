package io.github.nexalloy.revanced.meta.privacy

import android.content.Intent
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch
import io.github.nexalloy.revanced.shared.links.sanitizeUrlsInText

val SanitizeMetaSharingLinks = patch(
    name = "Sanitize Meta sharing links",
    description = "Removes common Instagram, Threads and Facebook tracking parameters from URLs placed in Android share intents.",
    use = false,
) {
    val hosts = setOf(
        "instagram.com",
        "threads.net",
        "facebook.com",
        "fb.com",
    )
    val tracking = setOf(
        "igsh",
        "utm_source",
        "utm_medium",
        "utm_campaign",
        "utm_content",
        "fbclid",
        "mibextid",
        "__cft__",
        "__tn__",
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
