package io.github.nexalloy.runtime

/**
 * A reviewed, data-only conversion recipe for a community catalog patch.
 *
 * Recipes are shipped with NexAlloy and use only the constrained runtime operation
 * schema. They are not remote code and never contain a .mpp archive or bytecode.
 */
data class RuntimeStoreRecipe(
    val spec: ImportedBooleanRuntimeLayerSpec,
) {
    val sourceRepository: String
        get() = spec.sourceRepository
    val sourcePatchName: String
        get() = spec.sourcePatchName
    val packageName: String
        get() = spec.packageName
}

object RuntimeStoreRecipeRegistry {
    private val recipes = listOf(
        RuntimeStoreRecipe(
            ImportedBooleanRuntimeLayerSpec(
                id = "piko.instagram.disable-video-autoplay.runtime",
                sourceRepository = "crimera/piko",
                sourcePatchName = "Disable video autoplay",
                packageName = "com.instagram.android",
                patchName = "Runtime · Disable video autoplay",
                description = "Runtime layer adapted from Piko's Disable video autoplay patch.",
                fingerprintStrings = listOf(
                    "ig_olympus_disable_video_autoplay",
                    "ig_disable_video_autoplay",
                    "ig_video_setting",
                ),
                replacementValue = true,
                enabled = false,
            )
        )
    )

    fun find(
        sourceRepository: String,
        sourcePatchName: String,
        packageName: String,
    ): RuntimeStoreRecipe? = recipes.firstOrNull {
        it.sourceRepository == sourceRepository &&
            it.sourcePatchName == sourcePatchName &&
            it.packageName == packageName
    }

    fun all(): List<RuntimeStoreRecipe> = recipes.toList()
}
