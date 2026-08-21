package io.github.nexalloy.runtime

/**
 * Runtime adapters for community patches targeting Instagram.
 *
 * The adapter definition is compiled into NexAlloy. Community .mpp archives are never
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

    val layers: List<RuntimeLayer> = listOf(
        disableVideoAutoplayDefinition.compile(),
    )
}
