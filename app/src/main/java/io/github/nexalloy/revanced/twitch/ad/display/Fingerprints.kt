package io.github.nexalloy.revanced.twitch.ad.display

import io.github.nexalloy.morphe.findMethodDirect
import io.github.nexalloy.morphe.fingerprint

private val displayAdParserClassFingerprint = fingerprint {
    strings(
        "failed to parse display ad response: ",
        "could not parse content type: ",
    )
}

val displayAdResponseParserFingerprint = findMethodDirect {
    val clazz = displayAdParserClassFingerprint.invoke(this).declaredClass
        ?: error("Twitch display-ad parser class not found")

    clazz.findMethod {
        matcher {
            name = "a"
            returnType = "nq"
            paramTypes = listOf(
                "retrofit2.adapter.rxjava2.Result",
                "boolean",
            )
        }
    }.single()
}
