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

private fun Any.clearMessageIds(): Boolean =
    runCatching {
        @Suppress("UNCHECKED_CAST")
        val ids = javaClass.getField("messages").get(this) as? MutableList<Int>
            ?: return@runCatching false
        ids.clear()
        true
    }.recoverCatching {
        @Suppress("UNCHECKED_CAST")
        val ids = javaClass.getDeclaredField("messages").apply { isAccessible = true }
            .get(this) as? MutableList<Int>
            ?: return@recoverCatching false
        ids.clear()
        true
    }.getOrDefault(false)

private fun ClassLoader.hookIncomingUpdates(block: (MutableList<Any>) -> Unit) {
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
                    block(list)
                }
            }
        }
}

val KeepDeletedMessages = patch(
    name = "Keep deleted messages locally",
    description = "Keeps messages already present on this device by neutralizing incoming delete-message ID lists while preserving the update object and pts state.",
    use = false,
) {
    classLoader.hookIncomingUpdates { updates ->
        updates.forEach { update ->
            val name = update.javaClass.name
            if (
                name.endsWith("\$TL_updateDeleteMessages") ||
                name.endsWith("\$TL_updateDeleteChannelMessages")
            ) {
                update.clearMessageIds()
            }
        }
    }
}

val KeepOriginalEditedMessages = patch(
    name = "Keep original edited messages",
    description = "Keeps the locally stored original text by ignoring incoming edit updates. This is intentionally separate from anti-delete because edit-state synchronization is more version-sensitive.",
    use = false,
) {
    classLoader.hookIncomingUpdates { updates ->
        updates.removeAll { update ->
            val name = update.javaClass.name
            name.endsWith("\$TL_updateEditMessage") ||
                name.endsWith("\$TL_updateEditChannelMessage")
        }
    }
}
