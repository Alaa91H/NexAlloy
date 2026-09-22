package io.github.nexalloy.revanced.shared.links

import android.net.Uri

private val urlRegex = Regex("""https?://[^\s<>"']+""")

fun sanitizeUrlsInText(
    text: CharSequence,
    hosts: Set<String>,
    trackingParameters: Set<String>,
): String {
    val normalizedHosts = hosts.map { it.lowercase() }.toSet()
    val normalizedParameters = trackingParameters.map { it.lowercase() }.toSet()

    return urlRegex.replace(text.toString()) { match ->
        sanitizeUri(
            raw = match.value,
            hosts = normalizedHosts,
            trackingParameters = normalizedParameters,
        )
    }
}

private fun sanitizeUri(
    raw: String,
    hosts: Set<String>,
    trackingParameters: Set<String>,
): String {
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return raw
    val host = uri.host?.lowercase() ?: return raw

    val hostMatches = hosts.any { allowed ->
        host == allowed || host.endsWith(".$allowed")
    }
    if (!hostMatches) return raw

    val parameterNames = runCatching { uri.queryParameterNames }.getOrNull() ?: return raw
    if (parameterNames.none { it.lowercase() in trackingParameters }) return raw

    val builder = uri.buildUpon().clearQuery()
    parameterNames.forEach { name ->
        if (name.lowercase() !in trackingParameters) {
            uri.getQueryParameters(name).forEach { value ->
                builder.appendQueryParameter(name, value)
            }
        }
    }
    return builder.build().toString()
}
