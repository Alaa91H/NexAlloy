package io.github.nexalloy.bridge

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fuel.Fuel
import fuel.get
import kotlinx.io.readString
import java.io.File
import java.security.MessageDigest

/**
 * Read-only bridge for Morphe community bundle metadata.
 *
 * Community .mpp archives are intentionally never loaded or executed by Morphe LSPosed.
 * Morphe applies those archives during APK patching; this bridge only indexes metadata,
 * stores profiles, and generates a handoff URI for the Morphe source flow.
 */
object MorpheBridgeConfig {
    const val CATALOG_URL = "https://morphe-patches.software/data/bundles.json"
    const val CATALOG_PAGE_URL = "https://morphe-patches.software/"
    private const val MORPHE_ADD_SOURCE_URL = "https://morphe.software/add-source"

    fun sourceHandoffUri(repository: String): Uri = Uri.parse(MORPHE_ADD_SOURCE_URL).buildUpon()
        .appendQueryParameter("github", repository)
        .build()
}

enum class SourceTrust {
    APPROVED,
    REVIEW_REQUIRED,
    BLOCKED,
}

data class BundleOption(
    val key: String,
    val title: String,
    val description: String?,
    val required: Boolean,
    val defaultValue: String?,
)

data class CommunityPatch(
    val id: String,
    val name: String,
    val description: String?,
    val enabledByDefault: Boolean,
    val options: List<BundleOption>,
    val compatiblePackages: Set<String>,
)

data class CommunityBundle(
    val id: String,
    val source: String,
    val repository: String,
    val displayName: String,
    val author: String?,
    val donateUrl: String?,
    val patches: List<CommunityPatch>,
    val trust: SourceTrust,
) {
    val packageNames: Set<String>
        get() = patches.flatMapTo(linkedSetOf()) { it.compatiblePackages }
}

data class CommunityCatalog(
    val fetchedAtMillis: Long,
    val sha256: String,
    val bundles: List<CommunityBundle>,
) {
    val patchCount: Int
        get() = bundles.sumOf { it.patches.size }
}

data class PatchSelection(
    val patchId: String,
    val enabled: Boolean,
    val options: Map<String, String> = emptyMap(),
)

data class PatchProfile(
    val id: String,
    val name: String,
    val repository: String,
    val packageName: String,
    val catalogSha256: String,
    val selections: List<PatchSelection>,
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

const val MULTI_APP_PROFILE_PACKAGE = "multiple-apps"

enum class ProfileValidationState {
    CURRENT,
    CATALOG_UPDATED,
    SOURCE_NOT_AVAILABLE,
    SOURCE_BLOCKED,
    SOURCE_REVIEW_REQUIRED,
    TARGET_APP_NOT_AVAILABLE,
    PATCHES_NOT_AVAILABLE,
}

data class ProfileValidation(
    val state: ProfileValidationState,
    val bundle: CommunityBundle? = null,
    val missingPatchIds: Set<String> = emptySet(),
) {
    val canHandoff: Boolean
        get() = state == ProfileValidationState.CURRENT
}

/**
 * Validates a saved selection against the catalog snapshot currently on the device.
 * A changed catalog does not silently change a profile; it requires the user to review
 * and save the profile again before handoff.
 */
object MorpheProfileValidator {
    fun validate(
        profile: PatchProfile,
        catalog: CommunityCatalog,
        isSourceApproved: (String) -> Boolean,
    ): ProfileValidation {
        val bundle = catalog.bundles.firstOrNull { it.repository == profile.repository }
            ?: return ProfileValidation(ProfileValidationState.SOURCE_NOT_AVAILABLE)
        if (bundle.trust == SourceTrust.BLOCKED) {
            return ProfileValidation(ProfileValidationState.SOURCE_BLOCKED, bundle)
        }
        if (!isSourceApproved(bundle.repository)) {
            return ProfileValidation(ProfileValidationState.SOURCE_REVIEW_REQUIRED, bundle)
        }
        if (
            profile.packageName != MULTI_APP_PROFILE_PACKAGE &&
            profile.packageName !in bundle.packageNames
        ) {
            return ProfileValidation(ProfileValidationState.TARGET_APP_NOT_AVAILABLE, bundle)
        }

        val availablePatchIds = bundle.patches.mapTo(linkedSetOf()) { it.id }
        val missingPatchIds = profile.selections
            .asSequence()
            .filter { it.enabled }
            .map { it.patchId }
            .filterNot { it in availablePatchIds }
            .toSet()
        if (missingPatchIds.isNotEmpty()) {
            return ProfileValidation(
                state = ProfileValidationState.PATCHES_NOT_AVAILABLE,
                bundle = bundle,
                missingPatchIds = missingPatchIds,
            )
        }
        if (profile.catalogSha256 != catalog.sha256) {
            return ProfileValidation(ProfileValidationState.CATALOG_UPDATED, bundle)
        }
        return ProfileValidation(ProfileValidationState.CURRENT, bundle)
    }
}

/**
 * A community GitHub source is readable but requires an explicit local approval before
 * Morphe LSPosed can save a profile or hand it to Morphe. Non-GitHub and malformed sources
 * remain blocked because the bridge has no verified handoff route for them.
 */
object MorpheSourceTrustPolicy {
    private val repositoryPattern = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")

    fun evaluate(source: String, repository: String): SourceTrust {
        if (source.lowercase() != "github") return SourceTrust.BLOCKED
        if (!repository.matches(repositoryPattern)) return SourceTrust.BLOCKED
        return SourceTrust.REVIEW_REQUIRED
    }
}

/** Stores explicit source approvals locally. Approval never executes a bundle. */
class MorpheSourceReviewStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun effectiveTrust(bundle: CommunityBundle): SourceTrust = when (bundle.trust) {
        SourceTrust.REVIEW_REQUIRED -> if (isApproved(bundle.repository)) {
            SourceTrust.APPROVED
        } else {
            SourceTrust.REVIEW_REQUIRED
        }
        else -> bundle.trust
    }

    fun approve(repository: String) {
        val approved = loadApproved() + repository
        preferences.edit().putStringSet(KEY_APPROVED_REPOSITORIES, approved).apply()
    }

    fun revoke(repository: String) {
        preferences.edit().putStringSet(
            KEY_APPROVED_REPOSITORIES,
            loadApproved() - repository,
        ).apply()
    }

    fun isApproved(repository: String): Boolean = repository in loadApproved()

    private fun loadApproved(): Set<String> = preferences
        .getStringSet(KEY_APPROVED_REPOSITORIES, emptySet())
        ?.toSet()
        .orEmpty()

    companion object {
        private const val PREFERENCES_NAME = "morphe_bundle_bridge"
        private const val KEY_APPROVED_REPOSITORIES = "approved_source_repositories"
    }
}

class MorpheCatalogParser {
    fun parse(payload: String, fetchedAtMillis: Long = System.currentTimeMillis()): CommunityCatalog {
        val root = JsonParser.parseString(payload).asJsonObject
        val compatibility = root["compatibilities"]?.asJsonArray ?: JsonArray()
        val bundleArray = root["bundles"]?.asJsonArray
            ?: throw IllegalArgumentException("Morphe catalog does not contain bundles")

        val bundles = bundleArray.mapNotNull { bundleElement ->
            val bundle = bundleElement.asJsonObject
            val source = bundle.stringOrNull("source") ?: return@mapNotNull null
            val repository = bundle.stringOrNull("repo") ?: return@mapNotNull null
            val displayName = bundle.stringOrNull("name") ?: repository
            val patches = bundle["patches"]?.asJsonArray?.mapNotNull { patchElement ->
                parsePatch(patchElement.asJsonObject, compatibility)
            }.orEmpty()

            if (patches.isEmpty()) return@mapNotNull null
            CommunityBundle(
                id = "$source:$repository:$displayName",
                source = source,
                repository = repository,
                displayName = displayName,
                author = bundle.stringOrNull("author"),
                donateUrl = bundle.stringOrNull("donate"),
                patches = patches,
                trust = MorpheSourceTrustPolicy.evaluate(source, repository),
            )
        }.sortedWith(compareBy<CommunityBundle> { it.trust }.thenBy { it.displayName.lowercase() })

        return CommunityCatalog(
            fetchedAtMillis = fetchedAtMillis,
            sha256 = payload.sha256(),
            bundles = bundles,
        )
    }

    private fun parsePatch(patch: JsonObject, compatibility: JsonArray): CommunityPatch? {
        val name = patch.stringOrNull("name") ?: return null
        val packageKey = patch["compatiblePackagesKey"]?.takeIf { it.isJsonPrimitive }
            ?.asInt
        val packages = packageKey?.let { key ->
            if (key in 0 until compatibility.size()) {
                extractPackageNames(compatibility[key])
            } else {
                emptySet()
            }
        }.orEmpty()

        val options = patch["options"]?.asJsonArray?.mapNotNull { optionElement ->
            optionElement.asJsonObject.let { option ->
                val key = option.stringOrNull("key") ?: return@let null
                val title = option.stringOrNull("title") ?: key
                BundleOption(
                    key = key,
                    title = title,
                    description = option.stringOrNull("description"),
                    required = option["required"]?.takeIf { it.isJsonPrimitive }?.asBoolean ?: false,
                    defaultValue = option["default"]?.takeIf { it.isJsonPrimitive }?.asString,
                )
            }
        }.orEmpty()

        return CommunityPatch(
            id = name.slugify(),
            name = name,
            description = patch.stringOrNull("description"),
            enabledByDefault = patch["default"]?.takeIf { it.isJsonPrimitive }?.asBoolean ?: false,
            options = options,
            compatiblePackages = packages,
        )
    }

    private fun extractPackageNames(element: JsonElement): Set<String> {
        val result = linkedSetOf<String>()
        fun visit(current: JsonElement?) {
            when {
                current == null || current.isJsonNull -> Unit
                current.isJsonArray -> current.asJsonArray.forEach(::visit)
                current.isJsonObject -> current.asJsonObject.entrySet().forEach { (key, value) ->
                    if (key.isPackageName()) result.add(key)
                    visit(value)
                }
                current.isJsonPrimitive && current.asJsonPrimitive.isString -> {
                    current.asString.takeIf(String::isPackageName)?.let { result.add(it) }
                }
            }
        }
        visit(element)
        return result
    }

    private fun JsonObject.stringOrNull(key: String): String? = this[key]
        ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
        ?.asString
}

class MorpheCatalogStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val catalogFile = File(context.filesDir, CATALOG_FILE_NAME)
    private val parser = MorpheCatalogParser()

    fun load(): CommunityCatalog? {
        if (!catalogFile.exists()) return null
        val fetchedAt = preferences.getLong(KEY_CATALOG_FETCHED_AT, 0L)
        return runCatching { parser.parse(catalogFile.readText(), fetchedAt) }.getOrNull()
    }

    fun save(payload: String, fetchedAtMillis: Long = System.currentTimeMillis()): CommunityCatalog {
        val catalog = parser.parse(payload, fetchedAtMillis)
        catalogFile.writeText(payload)
        preferences.edit().putLong(KEY_CATALOG_FETCHED_AT, fetchedAtMillis).apply()
        return catalog
    }

    fun clear() {
        catalogFile.delete()
        preferences.edit().remove(KEY_CATALOG_FETCHED_AT).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "morphe_bundle_bridge"
        private const val CATALOG_FILE_NAME = "morphe-community-catalog.json"
        private const val KEY_CATALOG_FETCHED_AT = "catalog_fetched_at"
    }
}

class MorpheProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadAll(): List<PatchProfile> {
        val payload = preferences.getString(KEY_PROFILES, null) ?: return emptyList()
        return runCatching {
            gson.fromJson(payload, Array<PatchProfile>::class.java)?.toList().orEmpty()
        }.getOrDefault(emptyList())
    }

    fun save(profile: PatchProfile) {
        val profiles = loadAll().filterNot { it.id == profile.id } + profile
        preferences.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply()
    }

    fun delete(profileId: String) {
        preferences.edit().putString(
            KEY_PROFILES,
            gson.toJson(loadAll().filterNot { it.id == profileId })
        ).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "morphe_bundle_bridge"
        private const val KEY_PROFILES = "patch_profiles"
    }
}

class MorpheCatalogClient(
    private val store: MorpheCatalogStore,
) {
    private companion object {
        const val MAX_CATALOG_BYTES = 2_000_000
    }

    /**
     * Fetches only catalog metadata. It never downloads, loads, or executes a .mpp archive.
     */
    suspend fun refresh(): CommunityCatalog {
        val response = Fuel.get(
            MorpheBridgeConfig.CATALOG_URL,
            headers = mapOf(
                "Accept" to "application/json, text/plain, */*",
                "Referer" to MorpheBridgeConfig.CATALOG_PAGE_URL,
                "User-Agent" to "Morphe-LSPosed-MorpheBundleBridge/1.0",
            )
        )
        if (response.statusCode !in 200..299) {
            throw IllegalStateException("Morphe catalog request failed: HTTP ${response.statusCode}")
        }
        val payload = response.source.readString()
        if (payload.toByteArray().size > MAX_CATALOG_BYTES) {
            throw IllegalStateException("Morphe catalog exceeds the local safety limit")
        }
        if (!payload.trimStart().startsWith('{')) {
            throw IllegalStateException("Morphe catalog response is not JSON")
        }
        return store.save(payload)
    }
}

private fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString("") { "%02x".format(it) }

private fun String.isPackageName(): Boolean =
    matches(Regex("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+"))

private fun String.slugify(): String = lowercase()
    .replace(Regex("[^a-z0-9]+"), "-")
    .trim('-')
