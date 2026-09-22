package io.github.nexalloy.revanced.instagram.links

import io.github.nexalloy.patch
import java.lang.reflect.Modifier

private val trackingParameters = listOf(
    "igsh",
    "utm_source",
    "utm_medium",
    "utm_content",
    "fbclid",
    "si",
)

private fun sanitizeInstagramUrl(value: String): String {
    if (!value.startsWith("http://") && !value.startsWith("https://")) return value

    var sanitized = value
    trackingParameters.forEach { key ->
        sanitized = sanitized.replace(
            Regex("([&?])" + Regex.escape(key) + "=[^&]*"),
            "",
        )
    }

    sanitized = sanitized
        .replace("?&", "?")
        .replace(Regex("[?&]$"), "")

    return sanitized
}

private fun sanitizeUrlFields(target: Any) {
    var clazz: Class<*>? = target.javaClass

    while (clazz != null && clazz != Any::class.java) {
        clazz.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .forEach { field ->
                runCatching {
                    field.isAccessible = true
                    val original = field.get(target) as? String ?: return@runCatching
                    val sanitized = sanitizeInstagramUrl(original)
                    if (sanitized != original) field.set(target, sanitized)
                }
            }
        clazz = clazz.superclass
    }
}

val SanitizeShareLinks = patch(
    name = "Sanitize Instagram share links",
    description = "Removes igsh, UTM, fbclid and si tracking parameters from Instagram share URLs.",
) {
    listOf(
        ::storyUrlResponseImplFingerprint,
        ::liveUrlResponseImplFingerprint,
    ).forEach { fingerprint ->
        fingerprint.memberOrNull?.hookMethod {
            after { param ->
                val original = param.result as? String ?: return@after
                param.result = sanitizeInstagramUrl(original)
            }
        }
    }

    listOf(
        ::permalinkResponseJsonParserFingerprint,
        ::profileUrlResponseJsonParserFingerprint,
    ).forEach { fingerprint ->
        fingerprint.memberOrNull?.hookMethod {
            after { param ->
                val result = param.result ?: return@after
                sanitizeUrlFields(result)
            }
        }
    }
}
