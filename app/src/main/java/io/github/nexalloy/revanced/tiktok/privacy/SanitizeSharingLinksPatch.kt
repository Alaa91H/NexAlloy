package io.github.nexalloy.revanced.tiktok.privacy

import android.content.Intent
import android.net.Uri
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private val urlRegex = Regex("""https?://[^\s<>"']+""")

private fun sanitizeTikTokUrls(text: CharSequence): String {
    val hosts = setOf("tiktok.com", "vm.tiktok.com", "vt.tiktok.com")
    val tracking = setOf(
        "_r",
        "_t",
        "is_from_webapp",
        "sender_device",
        "sender_web_id",
        "share_app_id",
        "share_iid",
        "share_link_id",
        "share_item_id",
        "social_share_type",
        "timestamp",
        "u_code",
    )

    return urlRegex.replace(text.toString()) { match ->
        val raw = match.value
        val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return@replace raw
        val host = uri.host?.lowercase() ?: return@replace raw
        if (hosts.none { host == it || host.endsWith(".$it") }) return@replace raw

        val names = runCatching { uri.queryParameterNames }.getOrNull() ?: return@replace raw
        if (names.none { it.lowercase() in tracking }) return@replace raw

        val builder = uri.buildUpon().clearQuery()
        names.forEach { name ->
            if (name.lowercase() !in tracking) {
                uri.getQueryParameters(name).forEach { value ->
                    builder.appendQueryParameter(name, value)
                }
            }
        }
        builder.build().toString()
    }
}

val SanitizeTikTokSharingLinks = patch(
    name = "Sanitize TikTok sharing links",
    description = "Removes common TikTok tracking parameters from URLs placed in Android share intents.",
    use = false,
) {
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
                    param.args[1] = sanitizeTikTokUrls(text)
                }
            }
        }
}
