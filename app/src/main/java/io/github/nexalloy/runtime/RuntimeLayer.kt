package io.github.nexalloy.runtime

import de.robv.android.xposed.XC_MethodReplacement
import io.github.nexalloy.Patch
import io.github.nexalloy.morphe.AccessFlags
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
/**
 * Compile-time definition for a reviewed static method whose original bytecode replacement
 * returns DEX register p0, the first method argument. The hook proceeds unchanged unless the
 * target is static and its first argument is a String, keeping this adapter fail-closed.
 */
data class StaticFirstStringArgumentReturnLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val fingerprintStrings: List<String>,
    val parameterTypes: List<String>,
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val fingerprint = FingerprintDsl {
            strings(*fingerprintStrings.toTypedArray())
            parameters(*parameterTypes.toTypedArray())
            returns("Ljava/lang/String;")
        }.build()
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            fingerprint.hookMethod {
                before { hook ->
                    val firstArgument = hook.args.firstOrNull()
                    if (hook.thisObject == null && firstArgument is String) {
                        hook.result = firstArgument
                    }
                }
            }
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
data class BooleanMethodTarget(
    val definingClass: String,
    val methodName: String,
    val replacementValue: Boolean,
    val parameterTypes: List<String> = emptyList(),
)

data class MultiBooleanReturnOverrideLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val targets: List<BooleanMethodTarget>,
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            targets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns("Z")
                }.build().hookMethod(XC_MethodReplacement.returnConstant(target.replacementValue))
            }
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

data class VoidMethodTarget(
    val definingClass: String,
    val methodName: String,
    val parameterTypes: List<String> = emptyList(),
)

/** A reviewed Object-returning target that must fail closed with a null result. */
data class ObjectNullMethodTarget(
    val definingClass: String,
    val methodName: String,
    val returnType: String,
    val parameterTypes: List<String> = emptyList(),
)

/**
 * Combines exact Boolean, Object-null, and Void substitutions for a single reviewed local
 * source patch. This definition never imports bytecode or an upstream patch archive.
 */
data class CompositeRuntimeLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val booleanTargets: List<BooleanMethodTarget> = emptyList(),
    val objectNullTargets: List<ObjectNullMethodTarget> = emptyList(),
    val voidTargets: List<VoidMethodTarget> = emptyList(),
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            booleanTargets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns("Z")
                }.build().hookMethod(XC_MethodReplacement.returnConstant(target.replacementValue))
            }
            objectNullTargets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns(target.returnType)
                }.build().hookMethod {
                    before { hook -> hook.result = null }
                }
            }
            voidTargets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns("V")
                }.build().hookMethod(XC_MethodReplacement.DO_NOTHING)
            }
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

data class MultiVoidMethodSkipLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val targets: List<VoidMethodTarget>,
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            targets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns("V")
                }.build().hookMethod(XC_MethodReplacement.DO_NOTHING)
            }
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
 * A reviewed target that returns Kotlin's Unit object through an Object-typed suspend bridge.
 * The target is local source metadata, not an imported patch payload.
 */
data class KotlinUnitMethodTarget(
    val definingClass: String? = null,
    val methodName: String? = null,
    val parameterTypes: List<String> = emptyList(),
    val returnType: String = "Ljava/lang/Object;",
    val accessFlags: List<AccessFlags> = emptyList(),
)

/**
 * Replaces selected Kotlin Unit-returning methods with Unit before their body executes.
 * This is limited to exact, locally reviewed telemetry and observability targets.
 */
data class MultiKotlinUnitReturnOverrideLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val targets: List<KotlinUnitMethodTarget>,
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            targets.forEach { target ->
                val kotlinUnitInstance by lazy(LazyThreadSafetyMode.NONE) {
                    classLoader.loadClass("kotlin.Unit").getField("INSTANCE").get(null)
                }
                FingerprintDsl {
                    target.definingClass?.let(::definingClass)
                    target.methodName?.let(::name)
                    if (target.parameterTypes.isNotEmpty()) {
                        parameters(*target.parameterTypes.toTypedArray())
                    }
                    if (target.accessFlags.isNotEmpty()) {
                        accessFlags(*target.accessFlags.toTypedArray())
                    }
                    returns(target.returnType)
                }.build().hookMethod {
                    before { it.result = kotlinUnitInstance }
                }
            }
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
 * A target whose reviewed bytecode replacement returns DEX register p0. At runtime p0 is
 * the receiver for an instance method and the first argument for a static method.
 */
data class DexP0ObjectReturnTarget(
    val definingClass: String,
    val methodName: String,
    val parameterTypes: List<String> = emptyList(),
    val returnType: String = "Ljava/lang/Object;",
)

/**
 * Replaces selected Object-returning methods with their DEX p0 value. This narrowly mirrors
 * reviewed local adapters that use `return-object p0` and is never imported from a bundle.
 */
data class MultiDexP0ObjectReturnOverrideLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val targets: List<DexP0ObjectReturnTarget>,
    val voidTargets: List<VoidMethodTarget> = emptyList(),
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            targets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns(target.returnType)
                }.build().hookMethod {
                    before { hook ->
                        hook.result = hook.thisObject ?: hook.args.firstOrNull()
                    }
                }
            }
            voidTargets.forEach { target ->
                FingerprintDsl {
                    definingClass(target.definingClass)
                    name(target.methodName)
                    parameters(*target.parameterTypes.toTypedArray())
                    returns("V")
                }.build().hookMethod(XC_MethodReplacement.DO_NOTHING)
            }
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
 * Matches every reviewed Void method carrying one exact name and parameter signature.
 * This supports sources that deliberately patch every implementation of a stable delegate
 * method; missing results yield an empty list and therefore no hook.
 */
data class AllMatchingVoidMethodSkipLayerDefinition(
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageNames: Set<String>,
    val patchName: String,
    val description: String,
    val methodName: String,
    val parameterTypes: List<String> = emptyList(),
    val enabledByDefault: Boolean = false,
) {
    fun compile(): RuntimeLayer {
        val compiledPatch = patch(
            name = patchName,
            description = description,
            use = enabledByDefault,
        ) {
            hookMethodList(
                cacheKey = "$id|$methodName|${parameterTypes.joinToString(",")}",
                findMethods = {
                    findMethod {
                        matcher {
                            name = methodName
                            returnType = "void"
                            paramTypes(*parameterTypes.toTypedArray())
                        }
                    }.filter { it.isMethod }
                },
                callback = XC_MethodReplacement.DO_NOTHING,
            )
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
        "com.instagram.android",
        "com.instagram.barcelona",
        "com.strava",
        "com.soundcloud.android",
        "jp.pxv.android",
        "com.sonyliv",
        "com.vimtv",
        "com.sofascore.results",
        "com.amazon.mp3",
        "com.alltrails.alltrails",
        "com.google.android.inputmethod.latin",
        "com.truecaller",
        "org.telegram.messenger",
        "com.facebook.orca",
        "ch.protonvpn.android",
        "com.twitter.android",
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
