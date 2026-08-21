package io.github.nexalloy.runtime

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import de.robv.android.xposed.XSharedPreferences
import io.github.nexalloy.BuildConfig

/**
 * On-device import format for the small set of runtime operations that Morphe LSPosed can
 * validate safely. It is data only: no Java/Kotlin/Smali code, class names, method
 * names, reflection expressions, URLs, or bytecode can be provided by an import.
 */
data class ImportedBooleanRuntimeLayerSpec(
    val schemaVersion: Int = SCHEMA_VERSION,
    val id: String,
    val sourceRepository: String,
    val sourcePatchName: String,
    val packageName: String,
    val patchName: String,
    val description: String,
    val fingerprintStrings: List<String>,
    val replacementValue: Boolean,
    val enabled: Boolean = false,
) {
    fun toDefinition(): BooleanReturnOverrideLayerDefinition =
        BooleanReturnOverrideLayerDefinition(
            id = id,
            sourceRepository = sourceRepository,
            sourcePatchName = sourcePatchName,
            packageNames = setOf(packageName),
            patchName = patchName,
            description = description,
            fingerprintStrings = fingerprintStrings,
            replacementValue = replacementValue,
            enabledByDefault = enabled,
        )

    companion object {
        const val SCHEMA_VERSION = 1
    }
}

class RuntimeLayerImportException(message: String) : IllegalArgumentException(message)

object RuntimeLayerSpecCodec {
    private val gson = Gson()
    private val idPattern = Regex("^[a-z0-9][a-z0-9.-]{2,119}$")
    private val repositoryPattern = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")
    private val packagePattern = Regex("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+")

    fun parse(raw: String): ImportedBooleanRuntimeLayerSpec {
        if (raw.toByteArray().size > MAX_SPEC_BYTES) {
            throw RuntimeLayerImportException("Runtime specification exceeds the size limit")
        }
        val spec = try {
            gson.fromJson(raw, ImportedBooleanRuntimeLayerSpec::class.java)
        } catch (error: JsonParseException) {
            throw RuntimeLayerImportException("Runtime specification is not valid JSON")
        } ?: throw RuntimeLayerImportException("Runtime specification is empty")
        validate(spec)
        return spec
    }

    fun encode(spec: ImportedBooleanRuntimeLayerSpec): String {
        validate(spec)
        return gson.toJson(spec)
    }

    fun validate(spec: ImportedBooleanRuntimeLayerSpec) {
        if (spec.schemaVersion != ImportedBooleanRuntimeLayerSpec.SCHEMA_VERSION) {
            throw RuntimeLayerImportException("Unsupported runtime specification schema")
        }
        if (!spec.id.matches(idPattern)) {
            throw RuntimeLayerImportException("Runtime layer id is invalid")
        }
        if (!spec.sourceRepository.matches(repositoryPattern)) {
            throw RuntimeLayerImportException("Source repository must be an owner/repository pair")
        }
        if (!spec.packageName.matches(packagePattern)) {
            throw RuntimeLayerImportException("Target package name is invalid")
        }
        if (spec.packageName !in RuntimeLayerTargetRegistry.packageNames) {
            throw RuntimeLayerImportException("Target package is not registered by Morphe LSPosed")
        }
        if (spec.sourcePatchName.isBlank() || spec.sourcePatchName.length > MAX_TEXT_LENGTH) {
            throw RuntimeLayerImportException("Source patch name is invalid")
        }
        if (spec.patchName.isBlank() || spec.patchName.length > MAX_TEXT_LENGTH) {
            throw RuntimeLayerImportException("Runtime patch name is invalid")
        }
        if (spec.description.length > MAX_DESCRIPTION_LENGTH) {
            throw RuntimeLayerImportException("Runtime patch description is too long")
        }
        if (spec.fingerprintStrings.isEmpty() || spec.fingerprintStrings.size > MAX_FINGERPRINT_STRINGS) {
            throw RuntimeLayerImportException("Runtime layer needs between 1 and $MAX_FINGERPRINT_STRINGS fingerprint strings")
        }
        if (spec.fingerprintStrings.any { it.isBlank() || it.length > MAX_FINGERPRINT_LENGTH || it.hasControlCharacter() }) {
            throw RuntimeLayerImportException("Fingerprint strings contain unsupported characters")
        }
    }

    private fun String.hasControlCharacter(): Boolean = any { it.code < 32 }

    private const val MAX_SPEC_BYTES = 8 * 1024
    private const val MAX_TEXT_LENGTH = 160
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MAX_FINGERPRINT_STRINGS = 8
    private const val MAX_FINGERPRINT_LENGTH = 160
}

/** Persists approved data-only specs and exposes them to the Xposed host process. */
object ImportedRuntimeLayerStore {
    private const val PREFERENCES_NAME = "runtime_layers"
    private const val KEY_SPECS = "boolean_override_specs"

    @SuppressLint("WorldReadableFiles")
    fun save(context: Context, spec: ImportedBooleanRuntimeLayerSpec) {
        val existing = loadFromContext(context).filterNot { it.id == spec.id } + spec
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE)
            .edit()
            .putString(KEY_SPECS, Gson().toJson(existing))
            .commit()
    }

    fun setEnabled(context: Context, id: String, enabled: Boolean) {
        val updated = loadFromContext(context).map { spec ->
            if (spec.id == id) spec.copy(enabled = enabled) else spec
        }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE)
            .edit()
            .putString(KEY_SPECS, Gson().toJson(updated))
            .commit()
    }

    fun delete(context: Context, id: String) {
        val remaining = loadFromContext(context).filterNot { it.id == id }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE)
            .edit()
            .putString(KEY_SPECS, Gson().toJson(remaining))
            .commit()
    }

    fun loadFromContext(context: Context): List<ImportedBooleanRuntimeLayerSpec> =
        parseList(context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SPECS, null))

    fun loadForHooking(): List<RuntimeLayer> {
        val preferences = XSharedPreferences(BuildConfig.APPLICATION_ID, PREFERENCES_NAME)
        preferences.reload()
        return parseList(preferences.getString(KEY_SPECS, null)).mapNotNull { spec ->
            runCatching { spec.toDefinition().compile() }.getOrNull()
        }
    }

    private fun parseList(raw: String?): List<ImportedBooleanRuntimeLayerSpec> {
        if (raw.isNullOrBlank() || raw.toByteArray().size > MAX_STORE_BYTES) return emptyList()
        val specs = runCatching {
            Gson().fromJson(raw, Array<ImportedBooleanRuntimeLayerSpec>::class.java)?.toList().orEmpty()
        }.getOrDefault(emptyList())
        return specs.mapNotNull { spec -> runCatching { RuntimeLayerSpecCodec.validate(spec); spec }.getOrNull() }
            .take(MAX_STORED_SPECS)
    }

    private const val MAX_STORE_BYTES = 64 * 1024
    private const val MAX_STORED_SPECS = 32
}
