package io.github.nexalloy.revanced.telegram.history

import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private fun ClassLoader.findClassOrNull(name: String): Class<*>? =
    runCatching { loadClass(name) }.getOrNull()

private fun Any.readUpdatesList(): MutableList<Any>? =
    runCatching {
        @Suppress("UNCHECKED_CAST")
        javaClass.getField("updates").get(this) as? MutableList<Any>
    }.recoverCatching {
        @Suppress("UNCHECKED_CAST")
        javaClass.getDeclaredField("updates").apply { isAccessible = true }
            .get(this) as? MutableList<Any>
    }.getOrNull()

private fun ClassLoader.filterIncomingUpdates(predicate: (Any) -> Boolean) {
    val controller =
        findClassOrNull("org.telegram.messenger.MessagesController") ?: return

    controller.declaredMethods
        .filter {
            it.name == "processUpdates" &&
                it.parameterTypes.isNotEmpty() &&
                it.parameterTypes[0].name == "org.telegram.tgnet.TLRPC\$Updates"
        }
        .forEach { method ->
            method.hookMethod {
                before { param ->
                    val updates = param.args.firstOrNull() ?: return@before
                    val list = updates.readUpdatesList() ?: return@before
                    list.removeAll(predicate)
                }
            }
        }
}

val KeepDeletedMessages = patch(
    name = "Keep deleted messages locally",
    description = "Ignores incoming Telegram delete-message updates so messages already present on this device remain visible locally.",
    use = false,
) {
    classLoader.filterIncomingUpdates { update ->
        val name = update.javaClass.name
        name.endsWith("\$TL_updateDeleteMessages") ||
            name.endsWith("\$TL_updateDeleteChannelMessages")
    }
}

val KeepOriginalEditedMessages = patch(
    name = "Keep original edited messages",
    description = "Ignores incoming edit updates so the original message text already stored on this device remains visible locally.",
    use = false,
) {
    classLoader.filterIncomingUpdates { update ->
        val name = update.javaClass.name
        name.endsWith("\$TL_updateEditMessage") ||
            name.endsWith("\$TL_updateEditChannelMessage")
    }
}
