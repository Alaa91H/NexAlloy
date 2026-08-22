package io.github.nexalloy.runtime

/**
 * Runtime adapters for community patches targeting Instagram.
 *
 * The adapter definition is compiled into Morphe LSPosed. Community .mpp archives are never
 * downloaded, loaded, or interpreted by this module.
 */
object InstagramRuntimeLayers {
    private val disableVideoAutoplayDefinition = BooleanReturnOverrideLayerDefinition(
        id = "piko.instagram.disable-video-autoplay.runtime",
        sourceRepository = "crimera/piko",
        sourcePatchName = "Disable video autoplay",
        packageNames = setOf("com.instagram.android"),
        patchName = "Runtime · Disable video autoplay",
        description = "Runtime layer adapted from Piko's Disable video autoplay patch.",
        fingerprintStrings = listOf(
            "ig_olympus_disable_video_autoplay",
            "ig_disable_video_autoplay",
            "ig_video_setting",
        ),
        replacementValue = true,
    )

    /**
     * The upstream source uses the same uniquely anchored Boolean feature gate as the
     * already-reviewed Piko adapter. Keeping a distinct compiled definition preserves
     * catalog attribution without parsing or loading the upstream patch archive.
     */
    private val disableVideoAutoplayFromBrossshDefinition = BooleanReturnOverrideLayerDefinition(
        id = "morphe.instagram.disable-video-autoplay.runtime",
        sourceRepository = "brosssh/morphe-patches",
        sourcePatchName = "Disable video autoplay",
        packageNames = setOf("com.instagram.android"),
        patchName = "Runtime · Disable video autoplay",
        description = "Disables the reviewed Instagram feed video autoplay feature gate.",
        fingerprintStrings = listOf("ig_disable_video_autoplay"),
        replacementValue = true,
    )

    /**
     * This catalog source resolves the same reviewed story-timeout method as the existing Piko
     * definition. A separate local definition preserves catalog attribution without importing
     * source archives or expanding the target beyond the anchored ReelViewerFragment method.
     */
    private val disableStoryAutoFlippingFromBrossshDefinition = VoidMethodSkipLayerDefinition(
        id = "morphe.instagram.disable-story-auto-flipping.runtime",
        sourceRepository = "brosssh/morphe-patches",
        sourcePatchName = "Disable story auto flipping",
        packageNames = setOf("com.instagram.android"),
        patchName = "Runtime · Disable story auto flipping",
        description = "Skips the reviewed Instagram story timeout action.",
        definingClass = "Linstagram/features/stories/fragment/ReelViewerFragment;",
        fingerprintStrings = listOf("userSession"),
        parameterTypes = listOf("Ljava/lang/Object;"),
    )

    private val disableStoryFlippingDefinition = VoidMethodSkipLayerDefinition(
        id = "piko.instagram.disable-story-flipping.runtime",
        sourceRepository = "crimera/piko",
        sourcePatchName = "Disable story flipping",
        packageNames = setOf("com.instagram.android"),
        patchName = "Runtime · Disable story flipping",
        description = "Runtime layer adapted from Piko's Disable story flipping patch.",
        definingClass = "Linstagram/features/stories/fragment/ReelViewerFragment;",
        fingerprintStrings = listOf("userSession"),
        parameterTypes = listOf("Ljava/lang/Object;"),
    )

    val layers: List<RuntimeLayer> = listOf(
        disableVideoAutoplayDefinition.compile(),
        disableVideoAutoplayFromBrossshDefinition.compile(),
        disableStoryAutoFlippingFromBrossshDefinition.compile(),
        disableStoryFlippingDefinition.compile(),
    )
}
