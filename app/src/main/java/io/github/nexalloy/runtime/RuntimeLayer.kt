package io.github.nexalloy.runtime

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.Patch
import io.github.nexalloy.morphe.FingerprintDsl
import io.github.nexalloy.patch

/**
 * A compiled NexAlloy runtime layer.
 *
 * The [patch] is authored and packaged with NexAlloy. Metadata is retained only to
 * credit and trace the community source that inspired the adapter. This type must
 * never contain a class name, bytecode, or executable payload downloaded from a
 * community bundle.
 */
data class RuntimeLayer(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patch: Patch,
)

/**
 * Registry of runtime layers compiled into this module.
 *
 * A layer is discoverable from catalog metadata by [sourceRepository] and
 * [sourcePatchName], but executes exclusively through NexAlloy's existing
 * PatchExecutor and Xposed/DexKit APIs.
 */
/**
 * Declarative, compile-time safe definition for the narrow class of layers that only
 * replace a uniquely matched Boolean method result. The definition is still packaged
 * with NexAlloy; it is never parsed from a remote bundle.
 */
data class BooleanReturnOverrideLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val fingerprintStrings: List<String>,
    val replacementValue: Boolean,
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val fingerprint = FingerprintDsl {
            strings(*fingerprintStrings.toTypedArray())
            returns("Z")
        }.build()
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            fingerprint.hookMethod(XC_MethodReplacement.returnConstant(replacementValue))
        }
        return RuntimeLayer(
            id = id,
            sourceRepository = sourceRepository,
            sourcePatchName = sourcePatchName,
            packageNames = packageNames,
            patch = compiledPatch,
        )
    }
}

object RuntimeLayerRegistry {
    private val layers: List<RuntimeLayer> by lazy {
        InstagramRuntimeLayers.layers
    }

    fun layersFor(packageName: String): Array<Patch> = layers
        .asSequence()
        .filter { packageName in it.packageNames }
        .map { it.patch }
        .toList()
        .toTypedArray()

    fun find(sourceRepository: String, sourcePatchName: String, packageName: String): RuntimeLayer? =
        layers.firstOrNull {
            it.sourceRepository == sourceRepository &&
                it.sourcePatchName == sourcePatchName &&
                packageName in it.packageNames
        }

    fun all(): List<RuntimeLayer> = layers.toList()
}
