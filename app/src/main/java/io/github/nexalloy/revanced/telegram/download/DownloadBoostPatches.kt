package io.github.nexalloy.revanced.telegram.download

import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private fun ClassLoader.findClassOrNull(name: String): Class<*>? =
    runCatching { loadClass(name) }.getOrNull()

private fun Any.setIntField(name: String, value: Int) {
    runCatching {
        javaClass.getField(name).setInt(this, value)
    }.recoverCatching {
        javaClass.getDeclaredField(name).apply {
            isAccessible = true
            setInt(this@setIntField, value)
        }
    }
}

private fun ClassLoader.installDownloadProfile(
    normalChunk: Int,
    bigChunk: Int,
    maxRequests: Int,
) {
    val operation =
        findClassOrNull("org.telegram.messenger.FileLoadOperation") ?: return

    operation.declaredConstructors.forEach { constructor ->
        constructor.hookMethod {
            after { param ->
                val instance = param.thisObject
                instance.setIntField("downloadChunkSize", normalChunk)
                instance.setIntField("downloadChunkSizeBig", bigChunk)
                instance.setIntField("currentDownloadChunkSize", bigChunk)
                instance.setIntField("currentMaxDownloadRequests", maxRequests)
            }
        }
    }
}

val DownloadBoostBalanced = patch(
    name = "Download boost: balanced",
    description = "Uses larger Telegram file chunks and up to 8 parallel file requests. Do not enable together with maximum mode.",
    use = false,
) {
    classLoader.installDownloadProfile(
        normalChunk = 128 * 1024,
        bigChunk = 512 * 1024,
        maxRequests = 8,
    )
}

val DownloadBoostMaximum = patch(
    name = "Download boost: maximum",
    description = "Uses aggressive 1 MiB chunks and up to 12 parallel file requests. May increase RAM, battery, or server throttling. Do not enable together with balanced mode.",
    use = false,
) {
    classLoader.installDownloadProfile(
        normalChunk = 256 * 1024,
        bigChunk = 1024 * 1024,
        maxRequests = 12,
    )
}
