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
        disableStoryFlippingDefinition.compile(),
    )
}
