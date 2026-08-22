package io.github.nexalloy.runtime

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.hookMethod
import io.github.nexalloy.morphe.AccessFlags
import io.github.nexalloy.morphe.Fingerprint
import io.github.nexalloy.morphe.methodCall
import io.github.nexalloy.morphe.string
import io.github.nexalloy.patch
import android.net.Uri
import java.lang.reflect.Method
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

    private val disableGboardDiagnosticsDefinition = MultiDexP0ObjectReturnOverrideLayerDefinition(
        id = "kveld.gboard.disable-diagnostics.runtime",
        sourceRepository = "kveld9/kveld-morphe-patches",
        sourcePatchName = "Disable Diagnostics",
        packageNames = setOf("com.google.android.inputmethod.latin"),
        patchName = "Runtime · Disable Diagnostics",
        description = "Disables reviewed Gboard diagnostics initialization and recovery telemetry receiving paths.",
        targets = listOf(
            DexP0ObjectReturnTarget(
                definingClass = "Lcom/google/android/libraries/inputmethod/appdoctor/initializer/AppDoctorInitializer;",
                methodName = "a",
                parameterTypes = listOf("Landroid/content/Context;"),
            ),
        ),
        voidTargets = listOf(
            VoidMethodTarget(
                definingClass = "Lcom/google/android/libraries/appdoctor/AppDoctorReceiver;",
                methodName = "onReceive",
                parameterTypes = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
            ),
        ),
    )

    private val forceGboardIncognitoModeDefinition = MultiBooleanReturnOverrideLayerDefinition(
        id = "kveld.gboard.force-incognito-mode.runtime",
        sourceRepository = "kveld9/kveld-morphe-patches",
        sourcePatchName = "Force Incognito Mode",
        packageNames = setOf("com.google.android.inputmethod.latin"),
        patchName = "Runtime · Force Incognito Mode",
        description = "Returns true only from the reviewed Gboard incognito-state checks.",
        targets = listOf(
            BooleanMethodTarget(
                definingClass = "Lsew;",
                methodName = "H",
                parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;"),
                replacementValue = true,
            ),
            BooleanMethodTarget(
                definingClass = "Lfoh;",
                methodName = "F",
                replacementValue = true,
            ),
        ),
    )

    private val disableGboardRemoteConfigurationDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "kveld.gboard.disable-remote-configuration.runtime",
        sourceRepository = "kveld9/kveld-morphe-patches",
        sourcePatchName = "Disable Remote Configuration",
        packageNames = setOf("com.google.android.inputmethod.latin"),
        patchName = "Runtime · Disable Remote Configuration",
        description = "Disables reviewed Gboard remote experiment synchronization and periodic background updates.",
        targets = listOf(
            VoidMethodTarget(
                definingClass = "Lcom/google/android/libraries/phenotype/client/stable/PhenotypeUpdateBackgroundBroadcastReceiver;",
                methodName = "onReceive",
                parameterTypes = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
            ),
            VoidMethodTarget(
                definingClass = "Lcom/google/android/libraries/phenotype/client/stable/AccountRemovedBroadcastReceiver;",
                methodName = "onReceive",
                parameterTypes = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
            ),
            VoidMethodTarget("Lwhh;", "dB", listOf("Landroid/content/Context;", "Lvsp;")),
            VoidMethodTarget("Lwhh;", "e"),
            VoidMethodTarget("Lwhh;", "g"),
        ),
    )

    private val disableGboardSuperpacksEagerSyncDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "kveld.gboard.disable-superpacks-eager-sync.runtime",
        sourceRepository = "kveld9/kveld-morphe-patches",
        sourcePatchName = "Disable Superpacks Eager Sync",
        packageNames = setOf("com.google.android.inputmethod.latin"),
        patchName = "Runtime · Disable Superpacks Eager Sync",
        description = "Disables reviewed Gboard startup Superpacks synchronization while preserving on-demand downloads.",
        targets = listOf(
            VoidMethodTarget("Lgvk;", "n"),
            VoidMethodTarget("Lgrp;", "n"),
        ),
    )

    private val disableGboardTenorShareTrackingDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "kveld.gboard.disable-tenor-share-tracking.runtime",
        sourceRepository = "kveld9/kveld-morphe-patches",
        sourcePatchName = "Disable Tenor Share Tracking",
        packageNames = setOf("com.google.android.inputmethod.latin"),
        patchName = "Runtime · Disable Tenor Share Tracking",
        description = "Disables the reviewed Gboard Tenor GIF selection and sharing telemetry path.",
        targets = listOf(
            VoidMethodTarget("Limg;", "K", listOf("Lafsc;", "Lidb;")),
        ),
    )

    private val messengerMetaAiFabRenderFingerprint = Fingerprint(
        accessFlags = listOf(AccessFlags.PUBLIC),
        strings = listOf("fab_expanded", "AiFabComponent"),
    )

    private val messengerMetaAiCreationFolderFingerprint = Fingerprint(
        returnType = "Z",
        accessFlags = listOf(AccessFlags.PRIVATE),
        parameters = emptyList(),
        filters = listOf(
            methodCall(
                definingClass = "Lcom/facebook/messaging/navigation/plugins/aicreationfolder/folderitem/AiCreationFolderItem;",
                name = "A00",
            ),
        ),
        strings = listOf(
            "com.facebook.messaging.navigation.plugins.aicreationfolder.folderitem.AiCreationFolderItem",
        ),
    )

    private val messengerMetaAiHomeFolderFingerprint = Fingerprint(
        returnType = "Z",
        accessFlags = listOf(AccessFlags.PRIVATE),
        parameters = emptyList(),
        filters = listOf(
            methodCall(
                definingClass = "Lcom/facebook/messaging/navigation/plugins/aihomefolder/folderitem/AiHomeFolderItem;",
                name = "A00",
            ),
        ),
        strings = listOf(
            "com.facebook.messaging.navigation.plugins.aihomefolder.folderitem.AiHomeFolderItem",
        ),
    )

    private val messengerMetaAiSearchFingerprint = Fingerprint(
        returnType = "Z",
        accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
        filters = listOf(
            methodCall(
                definingClass = "Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;",
                name = "Afy",
            ),
        ),
        strings = listOf(
            "messaging.search.aiagent.implementations.SearchAiagentImplementationsKillSwitch",
        ),
    )

    private val camScannerLogAgentInitFingerprints = listOf(
        Fingerprint(
            definingClass = "Lcom/intsig/logagent/LogAgent;",
            name = "Init",
            returnType = "I",
            parameters = listOf(
                "Landroid/app/Application;", "I", "Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/intsig/logagent/LogAgent;",
            name = "Init",
            returnType = "I",
            parameters = listOf(
                "Landroid/app/Application;", "Lcom/intsig/logagent/SocketInterface;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/intsig/logagent/LogAgent;",
            name = "Init",
            returnType = "I",
            parameters = listOf(
                "Landroid/app/Application;", "Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;",
            ),
        ),
    )

    private val disableCamScannerTelemetryDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.camscanner.disable-telemetry.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable telemetry",
        packageNames = setOf("com.intsig.camscanner"),
        patch = patch(
            name = "Runtime · Disable telemetry",
            description = "Prevents the reviewed CamScanner LogAgent initialization overloads from starting telemetry.",
        ) {
            camScannerLogAgentInitFingerprints.forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod { before { param -> param.result = 0 } }
            }
        },
    )

    private val esExplorerAnalyticsInitFingerprint = Fingerprint(
        definingClass = "Lcom/estrongs/android/pop/FexApplication;",
        name = "M",
        returnType = "V",
        parameters = emptyList(),
        strings = listOf("China"),
    )

    private val disableEsExplorerTrackingDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.esexplorer.disable-tracking.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable Tracking",
        packageNames = setOf("com.estrongs.android.pop"),
        patch = patch(
            name = "Runtime · Disable Tracking",
            description = "Skips the reviewed ES File Explorer UMeng analytics and crash-report initialization method.",
        ) {
            esExplorerAnalyticsInitFingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
        },
    )

    private val amazonSearchSuggestionsSetEventFingerprint = Fingerprint(
        definingClass = "Lcom/amazon/search/resources/dependency/client/suggestions/SearchSuggestionsV2Request\$Builder;",
        name = "setEvent",
        returnType = "Lcom/amazon/search/resources/dependency/client/suggestions/SearchSuggestionsV2Request\$Builder;",
        parameters = listOf("Lcom/amazon/search/resources/dependency/client/suggestions/Event;"),
    )

    private val disableAmazonSearchSuggestionsTrackingDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.amazon.disable-search-suggestions-tracking.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable search suggestions tracking",
        packageNames = setOf("com.amazon.mShop.android.shopping", "in.amazon.mShop.android.shopping"),
        patch = patch(
            name = "Runtime · Disable search suggestions tracking",
            description = "Prevents the reviewed Amazon search-suggestions builder from recording a keypress or focus event.",
        ) {
            amazonSearchSuggestionsSetEventFingerprint.memberOrNull?.hookMethod { before { param -> param.result = param.thisObject } }
        },
    )

    private val telegramPlusAnalyticsFingerprints = listOf(
        Fingerprint(
            definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
            name = "enableAnalytics",
            returnType = "V",
            parameters = listOf("Landroid/app/Application;"),
        ),
        Fingerprint(
            definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
            name = "trackEvent",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;"),
        ),
        Fingerprint(
            definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
            name = "trackEvent",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;", "Ljava/util/HashMap;"),
        ),
    )

    private val disableTelegramPlusAnalyticsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.telegram-plus.disable-analytics.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable analytics",
        packageNames = setOf("org.telegram.plus"),
        patch = patch(
            name = "Runtime · Disable analytics",
            description = "Skips the reviewed Telegram Plus Firebase analytics initialization and event-dispatch methods.",
        ) {
            telegramPlusAnalyticsFingerprints.forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
            }
        },
    )

    private val disablePlusMessengerAnalyticsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "paresh.plus-messenger.disable-analytics.runtime",
        sourceRepository = "Paresh-Maheshwari/paresh-patches",
        sourcePatchName = "Disable analytics",
        packageNames = setOf("org.telegram.plus"),
        patch = patch(
            name = "Runtime · Disable analytics",
            description = "Skips only the reviewed Plus Messenger analytics start and event-dispatch methods.",
        ) {
            listOf(
                Fingerprint(
                    definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
                    name = "start",
                    returnType = "V",
                ),
                Fingerprint(
                    definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
                    name = "trackEvent",
                    returnType = "V",
                    parameters = listOf("Ljava/lang/String;"),
                ),
                Fingerprint(
                    definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
                    name = "trackEvent",
                    returnType = "V",
                    parameters = listOf("Ljava/lang/String;", "Ljava/util/HashMap;"),
                ),
            ).forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
            }
        },
    )

    private val telegramPlusAdsDisabledFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/plus/ads/AdsController;",
        name = "adsDisabled",
        returnType = "Z",
    )

    private val telegramPlusLoadAdsFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/plus/ads/AdsInstance;",
        name = "loadAds",
        returnType = "V",
    )

    private val telegramPlusLoadNativeAdFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/plus/ads/AdsInstance;",
        name = "loadNativeAd",
        parameters = listOf(
            "Landroid/content/Context;",
            "Z",
            "Lorg/telegram/plus/ads/AdsInstance\$AdsInstanceInterface;",
        ),
    )

    private val telegramPlusSponsoredDisabledFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/messenger/MessagesController;",
        name = "isSponsoredDisabled",
        returnType = "Z",
    )

    private val telegramPlusMessageIsSponsoredFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/messenger/MessageObject;",
        name = "isSponsored",
        returnType = "Z",
    )

    private val telegramPlusVideoAdsLoadFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/messenger/video/VideoAds;",
        name = "load",
        returnType = "V",
        accessFlags = listOf(AccessFlags.PRIVATE),
    )

    private val removeTelegramPlusAdsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.telegram-plus.remove-ads.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Remove ads",
        packageNames = setOf("org.telegram.plus"),
        patch = patch(
            name = "Runtime · Remove ads",
            description = "Blocks only the reviewed Telegram Plus banner, native, sponsored-message, and video-ad paths.",
        ) {
            telegramPlusAdsDisabledFingerprint.memberOrNull?.hookMethod { before { param -> param.result = true } }
            telegramPlusLoadAdsFingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
            telegramPlusLoadNativeAdFingerprint.memberOrNull?.hookMethod {
                before { param ->
                    when ((param.method as? Method)?.returnType) {
                        java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> param.result = false
                        java.lang.Void.TYPE, java.lang.Void::class.java -> param.result = null
                    }
                }
            }
            telegramPlusSponsoredDisabledFingerprint.memberOrNull?.hookMethod { before { param -> param.result = true } }
            telegramPlusMessageIsSponsoredFingerprint.memberOrNull?.hookMethod { before { param -> param.result = false } }
            telegramPlusVideoAdsLoadFingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
        },
    )

    private val telegramPlusSendTypingFingerprint = Fingerprint(
        definingClass = "Lorg/telegram/messenger/MessagesController;",
        name = "sendTyping",
        returnType = "Z",
        parameters = listOf("J", "J", "I", "I"),
    )

    private val hideTelegramPlusTypingIndicatorDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.telegram-plus.hide-typing-indicator.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Hide typing indicator",
        packageNames = setOf("org.telegram.plus"),
        patch = patch(
            name = "Runtime · Hide typing indicator",
            description = "Prevents the reviewed Telegram Plus typing-state dispatch and matching UI delegates from running.",
        ) {
            telegramPlusSendTypingFingerprint.memberOrNull?.hookMethod { before { param -> param.result = false } }
            hookMethodList(
                cacheKey = "morphe.telegram-plus.hide-typing-indicator.runtime|needSendTyping|",
                findMethods = {
                    findMethod {
                        matcher {
                            name = "needSendTyping"
                            returnType = "void"
                            paramTypes()
                        }
                    }.filter { it.isMethod }
                },
                callback = XC_MethodReplacement.DO_NOTHING,
            )
        },
    )

    private val removeMessengerMetaAiDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.messenger.remove-meta-ai.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Remove Meta AI",
        packageNames = setOf("com.facebook.orca"),
        patch = patch(
            name = "Runtime · Remove Meta AI",
            description = "Hides the reviewed Messenger Meta AI button, drawer items, and search suggestions.",
        ) {
            messengerMetaAiFabRenderFingerprint.memberOrNull?.hookMethod {
                before { it.result = null }
            }
            messengerMetaAiCreationFolderFingerprint.memberOrNull?.hookMethod {
                before { it.result = false }
            }
            messengerMetaAiHomeFolderFingerprint.memberOrNull?.hookMethod {
                before { it.result = false }
            }
            messengerMetaAiSearchFingerprint.memberOrNull?.hookMethod {
                before { it.result = false }
            }
        },
    )

    private val messengerDisableMediaTranscodingFingerprint = Fingerprint(
        accessFlags = listOf(AccessFlags.PUBLIC),
        returnType = "Lcom/facebook/fbservice/service/OperationResult;",
        parameters = listOf("L"),
        strings = listOf("transcode", "should_transcode", "transcoded_video_larger"),
    )

    private val disableMessengerMediaTranscodingDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.messenger.disable-media-transcoding.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable media transcoding",
        packageNames = setOf("com.facebook.orca"),
        patch = patch(
            name = "Runtime · Disable media transcoding",
            description = "Returns Messenger's reviewed default operation result before the media transcoding workflow begins.",
        ) {
            messengerDisableMediaTranscodingFingerprint.memberOrNull?.hookMethod {
                before { param ->
                    val resultType = (param.method as? Method)?.returnType ?: return@before
                    val defaultResult = runCatching {
                        resultType.getDeclaredField("A00").apply { isAccessible = true }.get(null)
                    }.getOrNull() ?: return@before
                    param.result = defaultResult
                }
            }
        },
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

    /**
     * De-Vanced targets the same reviewed in-app-browser Boolean decision. Separate metadata
     * keeps catalog attribution explicit while the compiled adapter remains unchanged in scope.
     */
    private val openMessengerLinksExternallyFromDevancedDefinition = BooleanReturnOverrideLayerDefinition(
        id = "devanced.messenger.open-links-externally.runtime",
        sourceRepository = "RookieEnough/De-Vanced",
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

    private val hideMessengerInboxSubtabsDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "morphe.messenger.hide-inbox-subtabs.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Hide inbox subtabs",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Hide inbox subtabs",
        description = "Prevents the reviewed Messenger inbox-subtabs supplier Runnable from signalling that its tabs are ready.",
        targets = listOf(
            VoidMethodTarget("LX/2Je;", "run"),
        ),
    )

    /**
     * De-Vanced resolves the same InboxSubtabs supplier through a retained Redex original name.
     * This alternate catalog attribution retains the existing concrete, fail-closed Runtime target.
     */
    private val hideMessengerInboxSubtabsFromDevancedDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "devanced.messenger.hide-inbox-subtabs.runtime",
        sourceRepository = "RookieEnough/De-Vanced",
        sourcePatchName = "Hide inbox subtabs",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Hide inbox subtabs",
        description = "Prevents the reviewed Messenger inbox-subtabs supplier Runnable from signalling that its tabs are ready.",
        targets = listOf(
            VoidMethodTarget("LX/2Je;", "run"),
        ),
    )

    private val messengerInboxAdsLoadFingerprint = Fingerprint(
        definingClass = "Lcom/facebook/messaging/business/inboxads/plugins/inboxads/itemsupplier/InboxAdsItemSupplierImplementation;",
        returnType = "V",
        strings = listOf("ads_load_begin", "inbox_ads_fetch_start"),
    )

    private val hideMessengerInboxAdsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "devanced.messenger.hide-inbox-ads.runtime",
        sourceRepository = "RookieEnough/De-Vanced",
        sourcePatchName = "Hide inbox ads",
        packageNames = setOf("com.facebook.orca"),
        patch = patch(
            name = "Runtime · Hide inbox ads",
            description = "Skips the reviewed Messenger inbox-ad load method when the complete source fingerprint resolves.",
        ) {
            messengerInboxAdsLoadFingerprint.memberOrNull?.hookMethod {
                before { param -> param.result = null }
            }
        },
    )

    private val disableMessengerTypingIndicatorDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "morphe.messenger.disable-typing-indicator.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable typing indicator",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Disable typing indicator",
        description = "Prevents the reviewed Messenger composer Runnable from sending the typing state.",
        targets = listOf(
            VoidMethodTarget("LX/Ay7;", "run"),
        ),
    )

    /**
     * The De-Vanced source resolves this logical Runnable through its retained Redex original
     * name. The local adapter intentionally keeps the previously reviewed concrete target and
     * exposes separate source attribution for catalog discovery.
     */
    private val disableMessengerTypingIndicatorFromDevancedDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "devanced.messenger.disable-typing-indicator.runtime",
        sourceRepository = "RookieEnough/De-Vanced",
        sourcePatchName = "Disable typing indicator",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Disable typing indicator",
        description = "Prevents the reviewed Messenger composer Runnable from sending the typing state.",
        targets = listOf(
            VoidMethodTarget("LX/Ay7;", "run"),
        ),
    )
    private val hideMessengerInboxStoriesNotesTrayDefinition = BooleanReturnOverrideLayerDefinition(
        id = "morphe.messenger.hide-inbox-stories-notes-tray.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Hide inbox stories and notes tray",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Hide inbox stories and notes tray",
        description = "Hides the reviewed Messenger inbox stories and notes tray visibility decision.",
        fingerprintStrings = listOf(
            "com.facebook.messaging.friendsinboxunit.plugins.inboxunit.FriendsInboxUnitKillSwitch",
        ),
        replacementValue = false,
    )

    /**
     * De-Vanced uses the same reviewed FriendsInboxTray Boolean gate. A separate compiled
     * definition preserves catalog attribution while retaining the existing fail-closed match.
     */
    private val hideMessengerInboxStoriesNotesTrayFromDevancedDefinition = BooleanReturnOverrideLayerDefinition(
        id = "devanced.messenger.hide-inbox-stories-notes-tray.runtime",
        sourceRepository = "RookieEnough/De-Vanced",
        sourcePatchName = "Hide inbox stories and notes tray",
        packageNames = setOf("com.facebook.orca"),
        patchName = "Runtime · Hide inbox stories and notes tray",
        description = "Hides the reviewed Messenger inbox stories and notes tray visibility decision.",
        fingerprintStrings = listOf(
            "com.facebook.messaging.friendsinboxunit.plugins.inboxunit.FriendsInboxUnitKillSwitch",
        ),
        replacementValue = false,
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

    private val disableTelegramAutoUpdateDefinition = CompositeRuntimeLayerDefinition(
        id = "morphe.telegram.disable-auto-update.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable auto-update",
        packageNames = setOf("org.telegram.messenger"),
        patchName = "Runtime · Disable auto-update",
        description = "Disables the reviewed Telegram in-app update checks and update UI only; Android system updates are unchanged.",
        booleanTargets = listOf(
            BooleanMethodTarget(
                definingClass = "Lorg/telegram/messenger/SharedConfig;",
                methodName = "isAppUpdateAvailable",
                replacementValue = false,
            ),
            BooleanMethodTarget(
                definingClass = "Lorg/telegram/messenger/SharedConfig;",
                methodName = "setNewAppVersionAvailable",
                replacementValue = false,
                parameterTypes = listOf("Lorg/telegram/tgnet/TLRPC\$TL_help_appUpdate;"),
            ),
        ),
        voidTargets = listOf(
            VoidMethodTarget(
                definingClass = "Lorg/telegram/ui/LaunchActivity;",
                methodName = "checkAppUpdate",
                parameterTypes = listOf("Z", "Lorg/telegram/messenger/browser/Browser\$Progress;"),
            ),
            VoidMethodTarget(
                definingClass = "Lorg/telegram/ui/Components/BlockingUpdateView;",
                methodName = "show",
                parameterTypes = listOf("I", "Lorg/telegram/tgnet/TLRPC\$TL_help_appUpdate;", "Z"),
            ),
            VoidMethodTarget(
                definingClass = "Lorg/telegram/messenger/MessagesController;",
                methodName = "checkPromoInfoInternal",
                parameterTypes = listOf("Z"),
            ),
        ),
    )

    private val hideTelegramTypingIndicatorDefinition = AllMatchingVoidMethodSkipLayerDefinition(
        id = "morphe.telegram.hide-typing-indicator.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Hide typing indicator",
        packageNames = setOf("org.telegram.messenger"),
        patchName = "Runtime · Hide typing indicator",
        description = "Skips every reviewed Telegram needSendTyping delegate before a typing-state request is dispatched.",
        methodName = "needSendTyping",
    )

    private val disableTelegramChannelSwitchingDefinition = CompositeRuntimeLayerDefinition(
        id = "morphe.telegram.disable-channel-switching.runtime",
        sourceRepository = "rushiranpise/morphe-patches",
        sourcePatchName = "Disable channel switching",
        packageNames = setOf("org.telegram.messenger"),
        patchName = "Runtime · Disable channel switching",
        description = "Disables the reviewed Telegram pull-down gesture that switches to the next unread channel.",
        booleanTargets = listOf(
            BooleanMethodTarget(
                definingClass = "Lorg/telegram/ui/ChatPullingDownDrawable;",
                methodName = "needDrawBottomPanel",
                replacementValue = false,
            ),
        ),
        objectNullTargets = listOf(
            ObjectNullMethodTarget(
                definingClass = "Lorg/telegram/ui/ChatPullingDownDrawable;",
                methodName = "getNextUnreadDialog",
                returnType = "Lorg/telegram/tgnet/TLRPC\$Dialog;",
            ),
        ),
        voidTargets = listOf(
            VoidMethodTarget(
                definingClass = "Lorg/telegram/ui/ChatPullingDownDrawable;",
                methodName = "drawBottomPanel",
            ),
        ),
    )

    private val xNavbarBadgeNumberFingerprint = Fingerprint(
        definingClass = "/BadgeableTabView;",
        name = "setBadgeNumber",
    )

    private val hideXNavigationBadgesDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.x.hide-navigation-badges.runtime",
        sourceRepository = "crimera/piko",
        sourcePatchName = "Hide badges from navigation bar icons",
        packageNames = setOf("com.twitter.android"),
        patch = patch(
            name = "Runtime · Hide navigation badges",
            description = "Replaces the reviewed X navigation badge number with zero before the tab view renders it.",
        ) {
            xNavbarBadgeNumberFingerprint.memberOrNull?.hookMethod {
                before { param ->
                    if (param.args.size == 1 && param.args[0] is Int) {
                        param.args[0] = 0
                    }
                }
            }
        },
    )

    private val clearXTrackingParamsDefinition = StaticFirstStringArgumentReturnLayerDefinition(
        id = "piko.x.clear-tracking-params.runtime",
        sourceRepository = "crimera/piko",
        sourcePatchName = "Clear tracking params",
        packageNames = setOf("com.twitter.android"),
        patchName = "Runtime · Clear tracking params",
        description = "Returns the original shared URL before the reviewed X session-tracking parameter is added.",
        fingerprintStrings = listOf("<this>", "shareParam", "sessionToken"),
        parameterTypes = listOf("Ljava/lang/String;", "L", "Ljava/lang/String;"),
    )

    private val protonVpnLongDelayFingerprint = Fingerprint(
        name = "getChangeServerLongDelayInSeconds",
    )

    private val protonVpnShortDelayFingerprint = Fingerprint(
        name = "getChangeServerShortDelayInSeconds",
    )

    private val removeProtonVpnServerChangeDelayDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.proton-vpn.remove-server-change-delay.runtime",
        sourceRepository = "hoo-dles/morphe-patches",
        sourcePatchName = "Remove delay",
        packageNames = setOf("ch.protonvpn.android"),
        patch = patch(
            name = "Runtime · Remove server-change delay",
            description = "Returns zero from the reviewed Proton VPN server-change delay accessors.",
        ) {
            listOf(protonVpnLongDelayFingerprint, protonVpnShortDelayFingerprint).forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod {
                    before { param ->
                        if (param.args.isNotEmpty()) return@before
                        val returnType = (param.method as? Method)?.returnType ?: return@before
                        val zero = when (returnType) {
                            Int::class.javaPrimitiveType, Int::class.javaObjectType -> 0
                            Long::class.javaPrimitiveType, Long::class.javaObjectType -> 0L
                            Short::class.javaPrimitiveType, Short::class.javaObjectType -> 0.toShort()
                            Byte::class.javaPrimitiveType, Byte::class.javaObjectType -> 0.toByte()
                            Float::class.javaPrimitiveType, Float::class.javaObjectType -> 0f
                            Double::class.javaPrimitiveType, Double::class.javaObjectType -> 0.0
                            else -> return@before
                        }
                        param.result = zero
                    }
                }
            }
        },
    )

    private val disableProtonVpnTelemetryDefinition = MultiKotlinUnitReturnOverrideLayerDefinition(
        id = "paresh.proton-vpn.disable-telemetry.runtime",
        sourceRepository = "Paresh-Maheshwari/paresh-patches",
        sourcePatchName = "Disable telemetry",
        packageNames = setOf("ch.protonvpn.android"),
        patchName = "Runtime · Disable telemetry",
        description = "Blocks reviewed Proton VPN telemetry scheduling, observability upload, and local event recording paths.",
        targets = listOf(
            KotlinUnitMethodTarget(
                definingClass = "Lme/proton/core/telemetry/data/worker/TelemetryWorkerManagerImpl;",
                methodName = "enqueueOrKeep-HG0u8IE",
                returnType = "V",
            ),
            KotlinUnitMethodTarget(
                definingClass = "Lme/proton/core/observability/data/usecase/SendObservabilityEventsImpl;",
                methodName = "invoke",
                parameterTypes = listOf(
                    "Ljava/util/List;",
                    "Lkotlin/coroutines/Continuation;",
                ),
            ),
            KotlinUnitMethodTarget(
                parameterTypes = listOf(
                    "Lcom/protonvpn/android/telemetry/TelemetryEvent;",
                    "Z",
                    "Lkotlin/coroutines/Continuation;",
                ),
                accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
            ),
        ),
    )

    private val xSearchSuggestionFingerprint = Fingerprint(
        definingClass = "/search/provider/",
        returnType = "Ljava/util/Collection;",
        strings = listOf("type", "query_id"),
    )

    private val removeXSearchSuggestionsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "piko.x.remove-search-suggestions.runtime",
        sourceRepository = "crimera/piko",
        sourcePatchName = "Remove search suggestions",
        packageNames = setOf("com.twitter.android"),
        patch = patch(
            name = "Runtime · Remove search suggestions",
            description = "Returns no reviewed X search suggestions when the constrained provider target is present.",
        ) {
            xSearchSuggestionFingerprint.memberOrNull?.hookMethod {
                before { param -> param.result = null }
            }
        },
    )

    private val tiktokShareUrlTrackerFingerprint = Fingerprint(
        accessFlags = listOf(AccessFlags.STATIC),
        returnType = "Ljava/lang/String;",
        strings = listOf("utm_campaign", "share_link_id"),
    )

    private val sanitizeTikTokSharingLinksDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "morphe.tiktok.sanitize-sharing-links.runtime",
        sourceRepository = "icysymmetra/tiktok-patches-for-morphe",
        sourcePatchName = "Sanitize sharing links",
        packageNames = setOf("com.zhiliaoapp.musically"),
        patch = patch(
            name = "Runtime · Sanitize sharing links",
            description = "Removes query tracking parameters from the reviewed TikTok share URL before it is returned.",
        ) {
            tiktokShareUrlTrackerFingerprint.memberOrNull?.hookMethod {
                before { param ->
                    val stringArgumentCount = param.args.count { it is String }
                    val url = param.args.firstOrNull() as? String
                        ?: return@before
                    if (stringArgumentCount < 2) return@before
                    val sanitized = runCatching {
                        Uri.parse(url).buildUpon().clearQuery().build().toString()
                    }.getOrNull() ?: return@before
                    param.result = sanitized
                }
            }
        },
    )

    private val disableZee5CleverTapDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "wzse.zee5.disable-clevertap.runtime",
        sourceRepository = "WZSE/aapam-patches",
        sourcePatchName = "Disable CleverTap",
        packageNames = setOf("com.graymatrix.did"),
        patch = patch(
            name = "Runtime · Disable CleverTap",
            description = "Skips only the reviewed Zee5 CleverTap initialization method.",
        ) {
            Fingerprint(
                definingClass = "Lcom/zee5/android/analytics/data/trackers/clevertap/DefaultCleverTapAnalytics;",
                name = "initCleverTap",
                accessFlags = listOf(AccessFlags.PUBLIC),
                returnType = "V",
                parameters = emptyList(),
            ).memberOrNull?.hookMethod { before { param -> param.result = null } }
        },
    )

    private val disableZee5AnalyticsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "wzse.zee5.disable-analytics.runtime",
        sourceRepository = "WZSE/aapam-patches",
        sourcePatchName = "Disable analytics",
        packageNames = setOf("com.graymatrix.did"),
        patch = patch(
            name = "Runtime · Disable analytics",
            description = "Skips only the reviewed Zee5 central analytics event dispatcher.",
        ) {
            Fingerprint(
                definingClass = "Lcom/zee5/android/analytics/data/DefaultAnalytics;",
                name = "trackEvent",
                returnType = "V",
                parameters = listOf(
                    "Lcom/zee5/android/analytics/data/trackers/mixpanel/data/models/AnalyticEvent;",
                ),
            ).memberOrNull?.hookMethod { before { param -> param.result = null } }
        },
    )

    private val myTelenorTrackerInitFingerprints = listOf(
        Fingerprint(
            returnType = "V",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
            parameters = listOf("Landroid/app/Application;", "Ljava/lang/String;", "L"),
            strings = listOf("partnerName", "notificationCallback"),
        ),
        Fingerprint(
            returnType = "V",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
            parameters = listOf("Landroid/content/Context;"),
            filters = listOf(
                methodCall(definingClass = "Lcom/tiktok/TikTokBusinessSdk;", name = "initializeSdk"),
            ),
        ),
        Fingerprint(
            returnType = "V",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
            parameters = listOf("Landroid/app/Application;"),
            strings = listOf("Failed to initialize Mixpanel SDK"),
        ),
    )

    private val blockMyTelenorTrackersDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "totsiaw.mytelenor.block-trackers.runtime",
        sourceRepository = "totsiaw/proxma-patches",
        sourcePatchName = "Block trackers",
        packageNames = setOf("com.telenor.pakistan.mytelenor"),
        patch = patch(
            name = "Runtime · Block trackers",
            description = "Skips only the reviewed Insider, TikTok Business, and Mixpanel tracker initialization paths.",
        ) {
            myTelenorTrackerInitFingerprints.forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
            }
        },
    )

    private val avitoTelemetryFingerprints = listOf(
        Fingerprint(
            definingClass = "Lcom/avito/android/analytics/clickstream/",
            returnType = "V",
            parameters = listOf("Lcom/avito/android/analytics/"),
            filters = listOf(
                methodCall(definingClass = "Ljava/util/concurrent/Executor;", name = "execute"),
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/avito/android/analytics/clickstream/",
            returnType = "V",
            filters = listOf(
                string("Sending event on main thread. May cause ANR"),
                methodCall(
                    definingClass = "Lcom/avito/android/analytics/inhouse_transport/",
                    name = "add",
                ),
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/avito/android/analytics_adjust/",
            returnType = "V",
            strings = listOf("Adjust initialized"),
            filters = listOf(
                methodCall(definingClass = "Lcom/adjust/sdk/Adjust;", name = "initSdk"),
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/avito/android/analytics_adjust/",
            returnType = "V",
            parameters = listOf("Lcom/adjust/sdk/AdjustEvent;"),
            filters = listOf(
                methodCall(definingClass = "Lcom/adjust/sdk/Adjust;", name = "trackEvent"),
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/avito/android/analytics_adjust/",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;"),
            filters = listOf(
                methodCall(definingClass = "Lcom/adjust/sdk/Adjust;", name = "addGlobalPartnerParameter"),
                methodCall(definingClass = "Lcom/adjust/sdk/Adjust;", name = "removeGlobalPartnerParameter"),
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/avito/android/analytics_adjust/",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;"),
            filters = listOf(
                methodCall(definingClass = "Lcom/adjust/sdk/Adjust;", name = "setPushToken"),
            ),
        ),
    )

    private val disableAvitoTelemetryDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "xob0t.avito.disable-telemetry.runtime",
        sourceRepository = "xob0t/morphe-patches",
        sourcePatchName = "Disable telemetry",
        packageNames = setOf("com.avito.android"),
        patch = patch(
            name = "Runtime · Disable telemetry",
            description = "Skips only the reviewed Avito clickstream and Adjust telemetry dispatch paths.",
        ) {
            avitoTelemetryFingerprints.forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
            }
        },
    )

    private val truecallerScamFeedEnabledFingerprint = Fingerprint(
        returnType = "Z",
        parameters = emptyList(),
        filters = listOf(
            methodCall(definingClass = "Lhc2/a;", name = "a"),
            methodCall(definingClass = "Lhc2/baz;", name = "a"),
        ),
    )

    private val hideTruecallerScamsTabDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "paresh.truecaller.hide-scams-tab.runtime",
        sourceRepository = "Paresh-Maheshwari/paresh-patches",
        sourcePatchName = "Hide Scams tab",
        packageNames = setOf("com.truecaller"),
        patch = patch(
            name = "Runtime · Hide Scams tab",
            description = "Returns false from the reviewed scam-feed enablement gate before the navigation tab is added.",
        ) {
            truecallerScamFeedEnabledFingerprint.memberOrNull?.hookMethod {
                before { param -> param.result = false }
            }
        },
    )

    private val hideTruecallerAssistantTabDefinition = BooleanReturnOverrideLayerDefinition(
        id = "paresh.truecaller.hide-assistant-tab.runtime",
        sourceRepository = "Paresh-Maheshwari/paresh-patches",
        sourcePatchName = "Hide Assistant tab",
        packageNames = setOf("com.truecaller"),
        patchName = "Runtime · Hide Assistant tab",
        description = "Hides the reviewed Truecaller Assistant tab from the bottom navigation.",
        fingerprintStrings = listOf("featureCallAssistant"),
        replacementValue = false,
        parameterTypes = emptyList(),
    )

    private val disableTruecallerUpdateCheckDefinition = BooleanReturnOverrideLayerDefinition(
        id = "paresh.truecaller.disable-update-check.runtime",
        sourceRepository = "Paresh-Maheshwari/paresh-patches",
        sourcePatchName = "Disable update check",
        packageNames = setOf("com.truecaller"),
        patchName = "Runtime · Disable update check",
        description = "Disables the reviewed Truecaller in-app update decision without changing system updates.",
        fingerprintStrings = listOf("playAppUpdateManager", "configsInventory"),
        replacementValue = false,
        parameterTypes = listOf("Lcom/truecaller/inappupdate/UpdateTrigger;"),
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

    private val hideTruecallerAfterCallAdsDefinition = MultiVoidMethodSkipLayerDefinition(
        id = "bufferk.truecaller.hide-ads.runtime",
        sourceRepository = "bufferk/morphe-patches",
        sourcePatchName = "Hide ads",
        packageNames = setOf("com.truecaller"),
        patchName = "Runtime · Hide ads",
        description = "Skips only the reviewed after-call and Neo after-call ad-update methods.",
        targets = listOf(
            VoidMethodTarget(
                definingClass = "Lcom/truecaller/acs/ui/baz;",
                methodName = "Rh",
                parameterTypes = listOf("Z"),
            ),
            VoidMethodTarget(
                definingClass = "Ltw1/f;",
                methodName = "Th",
                parameterTypes = listOf("Z"),
            ),
        ),
    )

    private val truecallerCleverTapPushFingerprints = listOf(
        Fingerprint(
            definingClass = "Lkr0/k;",
            name = "push",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;"),
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
        ),
        Fingerprint(
            definingClass = "Lkr0/k;",
            name = "push",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
        ),
    )

    private val disableTruecallerCleverTapAnalyticsDefinition = ExistingPatchRuntimeLayerDefinition(
        id = "bufferk.truecaller.disable-analytics.runtime",
        sourceRepository = "bufferk/morphe-patches",
        sourcePatchName = "Disable analytics",
        packageNames = setOf("com.truecaller"),
        patch = patch(
            name = "Runtime · Disable analytics",
            description = "Skips only the reviewed Truecaller CleverTap behavioural-event dispatch overloads.",
        ) {
            truecallerCleverTapPushFingerprints.forEach { fingerprint ->
                fingerprint.memberOrNull?.hookMethod { before { param -> param.result = null } }
            }
        },
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
        disableGboardDiagnosticsDefinition.compile(),
        forceGboardIncognitoModeDefinition.compile(),
        disableGboardRemoteConfigurationDefinition.compile(),
        disableGboardSuperpacksEagerSyncDefinition.compile(),
        disableGboardTenorShareTrackingDefinition.compile(),
        disableCamScannerTelemetryDefinition.compile(),
        disableEsExplorerTrackingDefinition.compile(),
        disableAmazonSearchSuggestionsTrackingDefinition.compile(),
        disableTelegramPlusAnalyticsDefinition.compile(),
        disablePlusMessengerAnalyticsDefinition.compile(),
        removeTelegramPlusAdsDefinition.compile(),
        hideTelegramPlusTypingIndicatorDefinition.compile(),
        removeMessengerMetaAiDefinition.compile(),
        disableMessengerMediaTranscodingDefinition.compile(),
        openMessengerLinksExternallyDefinition.compile(),
        openMessengerLinksExternallyFromDevancedDefinition.compile(),
        hideMessengerInboxSubtabsDefinition.compile(),
        hideMessengerInboxSubtabsFromDevancedDefinition.compile(),
        hideMessengerInboxAdsDefinition.compile(),
        disableMessengerTypingIndicatorDefinition.compile(),
        disableMessengerTypingIndicatorFromDevancedDefinition.compile(),
        hideMessengerInboxStoriesNotesTrayDefinition.compile(),
        hideMessengerInboxStoriesNotesTrayFromDevancedDefinition.compile(),
        removeTelegramSponsoredMessagesDefinition.compile(),
        disableTelegramAutoUpdateDefinition.compile(),
        hideTelegramTypingIndicatorDefinition.compile(),
        disableTelegramChannelSwitchingDefinition.compile(),
        removeProtonVpnServerChangeDelayDefinition.compile(),
        disableProtonVpnTelemetryDefinition.compile(),
        hideXNavigationBadgesDefinition.compile(),
        clearXTrackingParamsDefinition.compile(),
        removeXSearchSuggestionsDefinition.compile(),
        sanitizeTikTokSharingLinksDefinition.compile(),
        disableZee5CleverTapDefinition.compile(),
        disableZee5AnalyticsDefinition.compile(),
        blockMyTelenorTrackersDefinition.compile(),
        disableAvitoTelemetryDefinition.compile(),
        hideTruecallerScamsTabDefinition.compile(),
        hideTruecallerAssistantTabDefinition.compile(),
        disableTruecallerUpdateCheckDefinition.compile(),
        disableTruecallerTelemetryDefinition.compile(),
        hideTruecallerAfterCallAdsDefinition.compile(),
        disableTruecallerCleverTapAnalyticsDefinition.compile(),
        enableDcimFoldersBackupControlDefinition.compile(),
        disableReelsScrollingDefinition.compile(),
    )
}
