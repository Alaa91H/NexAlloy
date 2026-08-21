package io.github.nexalloy.runtime

import io.github.nexalloy.morphe.reddit.ad.HideAds
import io.github.nexalloy.morphe.reddit.misc.privacy.SanitizeSharingLinks
import io.github.nexalloy.revanced.googlephotos.misc.backup.EnableDCIMFoldersBackupControl

/**
 * Catalog-attributed Runtime layers backed exclusively by reviewed LSPosed patches
 * already compiled into Morphe LSPosed.
 */
object CommunityRuntimeLayers {
    private val hideRedditAdsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.reddit.hide-ads.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Hide Ads",
        packageNames = setOf("com.reddit.frontpage"),
        patch = HideAds,
    )

    private val sanitizeRedditSharingDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "adobo.reddit.sanitize-share-links.runtime",
        sourceRepository = "jkennethcarino/adobo",
        sourcePatchName = "Sanitize share links",
        packageNames = setOf("com.reddit.frontpage"),
        patch = SanitizeSharingLinks,
    )

    private val blockGboardTelemetryDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "kveld.gboard.block-telemetry.runtime",
        sourceRepository = "kveld9/kveld-morphe-patches",
        sourcePatchName = "Block Telemetry",
        packageNames = setOf("com.google.android.inputmethod.latin"),
        patchName = "Runtime · Block Telemetry",
        description = "Disables reviewed Gboard metrics, event logging, crash-report and diagnostic dispatch paths.",
        targets = listOf(
            VoidMethodTarget(
                definingClass = "Lcom/google/android/libraries/performance/primes/transmitter/LifeboatReceiver;",
                methodName = "onReceive",
                parameterTypes = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
            ),
            VoidMethodTarget("Lvpy;", "n", listOf("Lvpt;")),
            VoidMethodTarget("Lvpy;", "p"),
            VoidMethodTarget("Lvpy;", "s"),
            VoidMethodTarget("Loge;", "b", listOf("Loii;")),
            VoidMethodTarget("Lrzl;", "dB", listOf("Landroid/content/Context;", "Lvsp;")),
        ),
    )

    private val openMessengerLinksExternallyDefinition = BooleanReturnOverrideLayerDefinition(
        id = "morphe.messenger.open-links-externally.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Open links externally",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Open links externally",
        description = "Routes supported Messenger links to the system browser instead of the in-app browser.",
        fingerprintStrings = listOf("iab_skipped_reason", "user_prefers_external"),
        replacementValue = false,
        parameterTypes = listOf(
            "Landroid/net/Uri;",
            "Lcom/facebook/auth/usersession/FbUserSession;",
        ),
    )

    private val removeTelegramSponsoredMessagesDefinition = MultiBooleanReturnOverrideLayerDefinition(
        id = "morphe.telegram.remove-sponsored-messages.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Remove ads",
        packageNames = setOf("org.telegram.messenger"),
        patchName = "Runtime · Remove sponsored messages",
        description = "Disables reviewed Telegram sponsored-message state checks in supported builds.",
        targets = listOf(
            BooleanMethodTarget(
                definingClass = "Lorg/telegram/messenger/MessagesController;",
                methodName = "isSponsoredDisabled",
                replacementValue = true,
            ),
            BooleanMethodTarget(
                definingClass = "Lorg/telegram/messenger/MessageObject;",
                methodName = "isSponsored",
                replacementValue = false,
            ),
        ),
    )

    private val disableTruecallerTelemetryDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "paresh.truecaller.disable-telemetry.runtime",
        sourceRepository = "Paresh-Maheshwari/paresh-patches",
        sourcePatchName = "Disable telemetry",
        packageNames = setOf("com.truecaller"),
        patchName = "Runtime · Disable telemetry",
        description = "Prevents the reviewed AppStartTracker telemetry activation path from running.",
        targets = listOf(
            VoidMethodTarget(
                definingClass = "Lcom/truecaller/analytics/technical/AppStartTracker;",
                methodName = "enableTracking",
            ),
        ),
    )

    private val enableDcimFoldersBackupControlDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "devanced.google-photos.dcim-backup-control.runtime",
        sourceRepository = "RookieEnough/De-Vanced",
        sourcePatchName = "Enable DCIM folders backup control",
        packageNames = setOf("com.google.android.apps.photos"),
        patch = EnableDCIMFoldersBackupControl,
    )

    /**
     * A narrowly matched Instagram touch-interception adapter. It is intentionally scoped to
     * the identified Reels gesture method and does not load any upstream bytecode or payload.
     */
    private val disableReelsScrollingDefinition = BooleanReturnOverrideLayerDefinition(
        id = "piko.instagram.disable-reels-scrolling.runtime",
        sourceRepository = "crimera/piko",
        sourcePatchName = "Disable Reels scrolling",
        packageNames = setOf("com.instagram.android"),
        patchName = "Runtime · Disable Reels scrolling",
        description = "Blocks the reviewed Reels swipe-interception path when enabled.",
        fingerprintStrings = emptyList(),
        replacementValue = false,
        definingClass = "Linstagram/features/clips/viewer/ui/ClipsSwipeRefreshLayout;",
        parameterTypes = listOf("Landroid/view/MotionEvent;"),
    )

    val layers: List<RuntimeLayer> = listOf(
        hideRedditAdsDefinition.compile(),
        sanitizeRedditSharingDefinition.compile(),
        blockGboardTelemetryDefinition.compile(),
        openMessengerLinksExternallyDefinition.compile(),
        removeTelegramSponsoredMessagesDefinition.compile(),
        disableTruecallerTelemetryDefinition.compile(),
        enableDcimFoldersBackupControlDefinition.compile(),
        disableReelsScrollingDefinition.compile(),
    )
}
