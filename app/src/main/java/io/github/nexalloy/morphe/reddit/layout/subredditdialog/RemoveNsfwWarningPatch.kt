package io.github.nexalloy.morphe.reddit.layout.subredditdialog

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private val subredditModelClasses = arrayOf(
    "com.reddit.domain.model.Subreddit",
    "com.reddit.domain.model.UserSubreddit",
)

val RemoveNsfwWarning = patch(
    name = "Remove Reddit NSFW warning",
    description = "Treats subreddit models as already visited so Reddit skips the repeat NSFW community warning.",
    use = false,
) {
    subredditModelClasses.forEach { className ->
        runCatching { classLoader.loadClass(className) }
            .getOrNull()
            ?.declaredMethods
            ?.filter {
                it.name == "getHasBeenVisited" &&
                    it.parameterTypes.isEmpty() &&
                    it.returnType == Boolean::class.javaPrimitiveType
            }
            ?.forEach { method ->
                method.hookMethod(XC_MethodReplacement.returnConstant(true))
            }
    }
}
