package io.github.nexalloy.morphe.reddit.misc.openlink

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private fun redirectTarget(uri: Uri): Uri? {
    if (uri.scheme != "http" && uri.scheme != "https") return null
    val raw = runCatching { uri.getQueryParameter("url") }.getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: return null
    val target = runCatching { Uri.parse(raw) }.getOrNull() ?: return null
    return target.takeIf { it.scheme == "http" || it.scheme == "https" }
}

val OpenLinksDirectly = patch(
    name = "Open Reddit links directly",
    description = "Skips Reddit redirect URLs by opening their embedded destination URL.",
    use = false,
) {
    Activity::class.java.declaredMethods
        .filter {
            it.name == "startActivity" &&
                it.parameterTypes.isNotEmpty() &&
                it.parameterTypes[0] == Intent::class.java
        }
        .forEach { method ->
            method.hookMethod {
                before { param ->
                    val intent = param.args.firstOrNull() as? Intent ?: return@before
                    val uri = intent.data ?: return@before
                    val target = redirectTarget(uri) ?: return@before
                    intent.data = target
                }
            }
        }
}

val OpenLinksExternally = patch(
    name = "Open Reddit links externally",
    description = "Opens Reddit custom-tab links with the system ACTION_VIEW handler instead of the in-app browser.",
    use = false,
) {
    val customTabsIntent = runCatching {
        classLoader.loadClass("androidx.browser.customtabs.CustomTabsIntent")
    }.getOrNull()

    if (customTabsIntent != null) {
        customTabsIntent.declaredMethods
            .filter {
                it.name == "launchUrl" &&
                    it.returnType == Void.TYPE &&
                    it.parameterTypes.size == 2 &&
                    Context::class.java.isAssignableFrom(it.parameterTypes[0]) &&
                    it.parameterTypes[1] == Uri::class.java
            }
            .forEach { method ->
                method.hookMethod {
                    before { param ->
                        val context = param.args.getOrNull(0) as? Context ?: return@before
                        val uri = param.args.getOrNull(1) as? Uri ?: return@before
                        if (uri.scheme != "http" && uri.scheme != "https") return@before

                        val intent = Intent(Intent.ACTION_VIEW, redirectTarget(uri) ?: uri)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                        if (context !is Activity) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        runCatching { context.startActivity(intent) }
                            .onSuccess { param.result = null }
                    }
                }
            }
    }
}
