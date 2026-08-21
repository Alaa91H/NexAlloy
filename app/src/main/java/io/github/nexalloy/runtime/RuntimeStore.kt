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
    val sourceRepository: String,
    val sourcePatchName: String,
    val description: String?,
    val packageNames: Set<String>,
    val availability: RuntimeStoreAvailability,
    val runtimeLayer: RuntimeLayer? = null,
) {
    val primaryPackageName: String?
        get() = packageNames.firstOrNull()
}

/**
 * Converts community catalog metadata into an LSPosed-oriented store index.
 *
 * This classifier does not inspect or execute .mpp files. A catalog patch is READY
 * only if NexAlloy already has a compiled runtime adapter for the same source patch
 * and target package. All other records stay visible with an explicit limitation.
 */
class RuntimeStoreClassifier(
    private val targetPackages: Set<String> = RuntimeLayerTargetRegistry.packageNames,
    private val runtimeLayers: List<RuntimeLayer> = RuntimeLayerRegistry.all(),
) {
    fun classify(catalog: CommunityCatalog): List<RuntimeStoreItem> = catalog.bundles
        .flatMap { bundle ->
            bundle.patches.map { patch -> classifyPatch(bundle.repository, patch) }
        }
        .sortedWith(
            compareBy<RuntimeStoreItem> { it.availability.ordinal }
                .thenBy { it.sourcePatchName.lowercase() }
        )

    private fun classifyPatch(sourceRepository: String, patch: CommunityPatch): RuntimeStoreItem {
        val supportedPackages = patch.compatiblePackages.intersect(targetPackages)
        val layer = supportedPackages.asSequence().mapNotNull { packageName ->
            runtimeLayers.firstOrNull {
                it.sourceRepository == sourceRepository &&
                    it.sourcePatchName == patch.name &&
                    packageName in it.packageNames
            }
        }.firstOrNull()
        val availability = when {
            layer != null -> RuntimeStoreAvailability.READY
            supportedPackages.isNotEmpty() -> RuntimeStoreAvailability.NEEDS_RUNTIME_ADAPTER
            else -> RuntimeStoreAvailability.UNSUPPORTED_TARGET
        }
        return RuntimeStoreItem(
            sourceRepository = sourceRepository,
            sourcePatchName = patch.name,
            description = patch.description,
            packageNames = supportedPackages,
            availability = availability,
            runtimeLayer = layer,
        )
    }
}
