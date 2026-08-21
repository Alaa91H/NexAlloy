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
        enableDcimFoldersBackupControlDefinition.compile(),
        disableReelsScrollingDefinition.compile(),
    )
}
