package io.github.nexalloy.revanced.telegram.forward

import io.github.nexalloy.hookMethod
import io.github.nexalloy.patch

private const val DROP_AUTHOR_FLAG = 1 shl 11

private fun ClassLoader.findClassOrNull(name: String): Class<*>? =
    runCatching { loadClass(name) }.getOrNull()

val ForwardWithoutAttribution = patch(
    name = "Forward without attribution",
    description = "Forwards Telegram messages without the original sender or source attribution by setting the native drop_author flag.",
    use = false,
) {
    val connectionsManager =
        classLoader.findClassOrNull("org.telegram.tgnet.ConnectionsManager")

    connectionsManager
        ?.declaredMethods
        ?.filter {
            it.name == "sendRequest" &&
                it.parameterTypes.isNotEmpty()
        }
        ?.forEach { method ->
            method.hookMethod {
                before { param ->
                    val request = param.args.firstOrNull() ?: return@before
                    if (!request.javaClass.name.endsWith("\$TL_messages_forwardMessages")) {
                        return@before
                    }

                    runCatching {
                        request.javaClass.getField("drop_author").setBoolean(request, true)
                    }.recoverCatching {
                        request.javaClass.getDeclaredField("drop_author").apply {
                            isAccessible = true
                            setBoolean(request, true)
                        }
                    }

                    runCatching {
                        val flags = request.javaClass.getField("flags")
                        flags.setInt(request, flags.getInt(request) or DROP_AUTHOR_FLAG)
                    }.recoverCatching {
                        val flags = request.javaClass.getDeclaredField("flags").apply {
                            isAccessible = true
                        }
                        flags.setInt(request, flags.getInt(request) or DROP_AUTHOR_FLAG)
                    }
                }
            }
        }

    // Keep Telegram's forward preview consistent with the request that will be sent.
    classLoader.findClassOrNull("org.telegram.messenger.MessagePreviewParams")
        ?.declaredMethods
        ?.filter {
            it.name == "updateForward" &&
                it.returnType == Void.TYPE
        }
        ?.forEach { method ->
            method.hookMethod {
                after { param ->
                    runCatching {
                        param.thisObject.javaClass
                            .getField("hideForwardSendersName")
                            .setBoolean(param.thisObject, true)
                    }.recoverCatching {
                        param.thisObject.javaClass
                            .getDeclaredField("hideForwardSendersName")
                            .apply {
                                isAccessible = true
                                setBoolean(param.thisObject, true)
                            }
                    }
                }
            }
        }
}
