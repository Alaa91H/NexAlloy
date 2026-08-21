package io.github.nexalloy.runtime

import io.github.nexalloy.bridge.CommunityCatalog
import io.github.nexalloy.bridge.CommunityPatch

/** Classification shown by the LSPosed runtime store. */
enum class RuntimeStoreAvailability {
    READY,
    NEEDS_RUNTIME_ADAPTER,
    UNSUPPORTED_TARGET,
}

data class RuntimeStoreItem(
    val sourceHost: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val description: String?,
    /** Target packages supported by the local LSPosed build. */
    val packageNames: Set<String>,
    /** Target packages declared by the public catalog, retained for visible metadata cards. */
    val catalogPackageNames: Set<String> = packageNames,
    val availability: RuntimeStoreAvailability,
    val runtimeLayer: RuntimeLayer? = null,
    val recipe: RuntimeStoreRecipe? = null,
) {
    val primaryPackageName: String?
        get() = packageNames.firstOrNull()
}

/**
 * Converts community catalog metadata into an LSPosed-oriented store index.
 *
 * This classifier does not inspect or execute .mpp files. A catalog patch is READY
 * only if Morphe LSPosed already has a compiled runtime adapter for the same source patch
 * and target package. All other records stay visible with an explicit limitation.
 */
class RuntimeStoreClassifier(
    private val targetPackages: Set<String> = RuntimeLayerTargetRegistry.packageNames,
    private val runtimeLayers: List<RuntimeLayer> = RuntimeLayerRegistry.all(),
    private val recipes: List<RuntimeStoreRecipe> = RuntimeStoreRecipeRegistry.all(),
) {
    fun classify(catalog: CommunityCatalog): List<RuntimeStoreItem> = catalog.bundles
        .flatMap { bundle ->
            bundle.patches.map { patch -> classifyPatch(bundle.source, bundle.repository, patch) }
        }
        .sortedWith(
            compareBy<RuntimeStoreItem> { it.availability.ordinal }
                .thenBy { it.sourcePatchName.lowercase() }
        )

    private fun classifyPatch(
        sourceHost: String,
        sourceRepository: String,
        patch: CommunityPatch,
    ): RuntimeStoreItem {
        val supportedPackages = patch.compatiblePackages.intersect(targetPackages)
        val layer = supportedPackages.asSequence().mapNotNull { packageName ->
            runtimeLayers.firstOrNull {
                it.sourceRepository == sourceRepository &&
                    it.sourcePatchName == patch.name &&
                    packageName in it.packageNames
            }
        }.firstOrNull()
        val recipe = supportedPackages.asSequence().mapNotNull { packageName ->
            recipes.firstOrNull {
                it.sourceRepository == sourceRepository &&
                    it.sourcePatchName == patch.name &&
                    it.packageName == packageName
            }
        }.firstOrNull()
        val installableRecipe = recipe?.takeIf { layer == null }
        val availability = when {
            layer != null || installableRecipe != null -> RuntimeStoreAvailability.READY
            supportedPackages.isNotEmpty() -> RuntimeStoreAvailability.NEEDS_RUNTIME_ADAPTER
            else -> RuntimeStoreAvailability.UNSUPPORTED_TARGET
        }
        return RuntimeStoreItem(
            sourceHost = sourceHost,
            sourceRepository = sourceRepository,
            sourcePatchName = patch.name,
            description = patch.description,
            packageNames = supportedPackages,
            catalogPackageNames = patch.compatiblePackages,
            availability = availability,
            runtimeLayer = layer,
            recipe = installableRecipe,
        )
    }
}
