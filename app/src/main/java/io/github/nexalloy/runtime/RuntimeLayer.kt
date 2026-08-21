package io.github.nexalloy.runtime

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.Patch
import io.github.nexalloy.morphe.FingerprintDsl
import io.github.nexalloy.patch

/**
 * A compiled Morphe LSPosed runtime layer.
 *
 * The [patch] is authored and packaged with Morphe LSPosed. Metadata is retained only to
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
 * [sourcePatchName], but executes exclusively through Morphe LSPosed's existing
 * PatchExecutor and Xposed/DexKit APIs.
 */
/**
 * Declarative, compile-time safe definition for the narrow class of layers that only
 * replace a uniquely matched Boolean method result. The definition is still packaged
 * with Morphe LSPosed; it is never parsed from a remote bundle.
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
    val definingClass: String? = null,
    val parameterTypes: List<String> = emptyList(),
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val fingerprint = FingerprintDsl {
            definingClass?.let(::definingClass)
            if (fingerprintStrings.isNotEmpty()) strings(*fingerprintStrings.toTypedArray())
            if (parameterTypes.isNotEmpty()) parameters(*parameterTypes.toTypedArray())
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

/**
 * Maps a reviewed local LSPosed patch to a community catalog item. The patch is already
 * compiled into this module; this definition only supplies store attribution and discovery.
 */
data class ExistingPatchRuntimeLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patch: Patch,
) {
    fun compile(): RuntimeLayer = RuntimeLayer(
        id = id,
        sourceRepository = sourceRepository,
        sourcePatchName = sourcePatchName,
        packageNames = packageNames,
        patch = patch,
    )
}

/**
 * Declarative, compile-time safe definition for a uniquely matched Void method that
 * must be skipped entirely. This operation is limited to reviewed built-in layers.
 */
data class VoidMethodSkipLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val definingClass: String,
    val fingerprintStrings: List<String>,
    val parameterTypes: List<String>,
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val fingerprint = FingerprintDsl {
            definingClass(definingClass)
            strings(*fingerprintStrings.toTypedArray())
            parameters(*parameterTypes.toTypedArray())
            returns("V")
        }.build()
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            fingerprint.hookMethod(XC_MethodReplacement.DO_NOTHING)
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

object RuntimeLayerTargetRegistry {
    val packageNames = setOf(
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.reddit.frontpage",
        "com.google.android.apps.photos",
        "com.microblink.photomath",
        "com.instagram.android",
        "com.instagram.barcelona",
        "com.strava",
        "com.alltrails.alltrails",
    )
}

object RuntimeLayerRegistry {
    private val builtInLayers: List<RuntimeLayer> by lazy {
        InstagramRuntimeLayers.layers + CommunityRuntimeLayers.layers
    }

    /** Layers permanently compiled into the module and visible in normal settings. */
    fun layersFor(packageName: String): Array<Patch> = builtInLayers
        .asSequence()
        .filter { packageName in it.packageNames }
        .map { it.patch }
        .toList()
        .toTypedArray()

    fun find(sourceRepository: String, sourcePatchName: String, packageName: String): RuntimeLayer? =
        builtInLayers.firstOrNull {
            it.sourceRepository == sourceRepository &&
                it.sourcePatchName == sourcePatchName &&
                packageName in it.packageNames
        }

    /** Built-in layers are available even before an imported specification is added. */
    fun all(): List<RuntimeLayer> = builtInLayers.toList()

    /** Called only by the Xposed host process after it can read shared module state. */
    fun importedLayersForHooking(packageName: String): Array<Patch> =
        ImportedRuntimeLayerStore.loadForHooking()
            .asSequence()
            .filter { packageName in it.packageNames }
            .map { it.patch }
            .toList()
            .toTypedArray()
}
