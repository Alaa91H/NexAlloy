package io.github.nexalloy.revanced.telegram.privacy

import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private fun ClassLoader.findClassOrNull(name: String): Class<*>? =
    runCatching { loadClass(name) }.getOrNull()

private fun ClassLoader.blockOutgoingRequests(
    predicate: (Any) -> Boolean,
) {
    val connectionsManager =
        findClassOrNull("org.telegram.tgnet.ConnectionsManager") ?: return

    connectionsManager.declaredMethods
        .filter {
            it.name == "sendRequest" &&
                it.parameterTypes.isNotEmpty() &&
                it.returnType == Int::class.javaPrimitiveType
        }
        .forEach { method ->
            method.hookMethod {
                before { param ->
                    val request = param.args.firstOrNull() ?: return@before
                    if (predicate(request)) {
                        // Returning 0 matches Telegram's no-request / cancelled token convention.
                        param.result = 0
                    }
                }
            }
        }
}

private fun Any.classNameEndsWith(vararg names: String): Boolean {
    val name = javaClass.name
    return names.any(name::endsWith)
}

private fun Any.fieldOrNull(name: String): Any? =
    runCatching { javaClass.getField(name).get(this) }
        .recoverCatching {
            javaClass.getDeclaredField(name).apply { isAccessible = true }.get(this)
        }
        .getOrNull()

val HideReadReceipts = patch(
    name = "Hide message read receipts",
    description = "Prevents Telegram from sending normal, channel, and secret-chat read-history acknowledgements.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        request.classNameEndsWith(
            "\$TL_messages_readHistory",
            "\$TL_channels_readHistory",
            "\$TL_messages_readEncryptedHistory",
        )
    }
}

val HideContentReadReceipts = patch(
    name = "Hide listened / content-read receipts",
    description = "Prevents read-content acknowledgements used for voice notes and other content-specific read state.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        request.classNameEndsWith(
            "\$TL_messages_readMessageContents",
            "\$TL_channels_readMessageContents",
        )
    }
}

val HideTypingStatus = patch(
    name = "Hide typing status",
    description = "Prevents the typing indicator from being sent to chats.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        if (!request.classNameEndsWith("\$TL_messages_setTyping")) {
            return@blockOutgoingRequests false
        }
        val actionName = request.fieldOrNull("action")?.javaClass?.name.orEmpty()
        actionName.contains("TypingAction")
    }
}

val HideRecordingAndUploadStatus = patch(
    name = "Hide recording and upload status",
    description = "Hides voice/video recording and media/file upload activity indicators.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        if (!request.classNameEndsWith("\$TL_messages_setTyping")) {
            return@blockOutgoingRequests false
        }
        val actionName = request.fieldOrNull("action")?.javaClass?.name.orEmpty()
        actionName.contains("Record") || actionName.contains("Upload")
    }
}

val HideEmojiInteractions = patch(
    name = "Hide emoji interaction status",
    description = "Prevents Telegram from broadcasting animated-emoji interaction activity.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        if (!request.classNameEndsWith("\$TL_messages_setTyping")) {
            return@blockOutgoingRequests false
        }
        request.fieldOrNull("action")
            ?.javaClass
            ?.name
            ?.contains("EmojiInteraction") == true
    }
}

val HideGroupCallSpeakingStatus = patch(
    name = "Hide speaking status in group calls",
    description = "Prevents the speaking activity indicator from being sent through Telegram's typing/action channel.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        if (!request.classNameEndsWith("\$TL_messages_setTyping")) {
            return@blockOutgoingRequests false
        }
        request.fieldOrNull("action")
            ?.javaClass
            ?.name
            ?.contains("speakingInGroupCallAction") == true
    }
}

val HideStoryViews = patch(
    name = "Hide story views",
    description = "Prevents story read and story-view increment requests from being sent.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        request.classNameEndsWith(
            "\$TL_stories_readStories",
            "\$TL_stories_incrementStoryViews",
        )
    }
}

val HideScreenshotNotifications = patch(
    name = "Hide screenshot notifications",
    description = "Prevents Telegram from sending screenshot-notification requests from supported secret-chat flows.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        request.classNameEndsWith("\$TL_messages_sendScreenshotNotification")
    }
}

val HideAllChatActivity = patch(
    name = "Hide all chat activity (master)",
    description = "Blocks every Telegram setTyping/activity request, including typing, recording, uploads, sticker/contact/location selection, emoji interactions, and group-call speaking indicators.",
    use = false,
) {
    classLoader.blockOutgoingRequests { request ->
        request.classNameEndsWith("\$TL_messages_setTyping")
    }
}
