package io.github.nexalloy.revanced.tiktok.misc.login

import io.github.nexalloy.morphe.fingerprint
import org.luckypray.dexkit.query.enums.StringMatchType

val enableForcedLoginFingerprint = fingerprint {
    methodMatcher { name = "enableForcedLogin" }
    classMatcher { className(".MandatoryLoginService", StringMatchType.EndsWith) }
}

val shouldShowForcedLoginFingerprint = fingerprint {
    methodMatcher { name = "shouldShowForcedLogin" }
    classMatcher { className(".MandatoryLoginService", StringMatchType.EndsWith) }
}
