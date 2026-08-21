@file:Suppress("DEPRECATION")

package io.github.nexalloy.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.widget.Toast
import io.github.nexalloy.R
import io.github.nexalloy.bridge.CommunityBundle
import io.github.nexalloy.bridge.CommunityCatalog
import io.github.nexalloy.bridge.MorpheBridgeConfig
import io.github.nexalloy.bridge.MorpheCatalogClient
import io.github.nexalloy.bridge.MorpheCatalogStore
import io.github.nexalloy.bridge.MorpheProfileStore
import io.github.nexalloy.bridge.MorpheProfileValidator
import io.github.nexalloy.bridge.MorpheSourceReviewStore
import io.github.nexalloy.bridge.MULTI_APP_PROFILE_PACKAGE
import io.github.nexalloy.bridge.PatchProfile
import io.github.nexalloy.bridge.PatchSelection
import io.github.nexalloy.bridge.ProfileValidation
import io.github.nexalloy.bridge.ProfileValidationState
import io.github.nexalloy.bridge.SourceTrust
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

/**
 * Read-only catalog and profile manager for Morphe community bundles.
 *
 * This activity never executes .mpp contents. Applying a saved selection is handed
 * off to Morphe, which owns the compatible APK patching pipeline.
 */
class MorpheBundleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_morphe_bundle)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = getString(R.string.morphe_bundle_bridge_title)

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(R.id.morphe_bundle_container, MorpheBundleFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class MorpheBundleFragment : PreferenceFragment() {
        private val catalogStore by lazy { MorpheCatalogStore(context) }
        private val profileStore by lazy { MorpheProfileStore(context) }
        private val sourceReviewStore by lazy { MorpheSourceReviewStore(context) }
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        private var catalog: CommunityCatalog? = null

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            catalog = catalogStore.load()
            rebuildScreen()
        }

        @Deprecated("Deprecated in Java")
        override fun onDestroy() {
            scope.coroutineContext.cancel()
            super.onDestroy()
        }

        private fun rebuildScreen() {
            if (!isAdded) return
            val root = preferenceManager.createPreferenceScreen(context)
            preferenceScreen = root

            Preference(context).apply {
                title = getString(R.string.morphe_bundle_bridge_title)
                summary = getString(R.string.morphe_bundle_bridge_summary)
                isEnabled = false
                root.addPreference(this)
            }

            Preference(context).apply {
                title = getString(R.string.morphe_bundle_catalogue_open)
                summary = getString(R.string.morphe_bundle_catalogue_open_summary)
                setOnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(MorpheBridgeConfig.CATALOG_PAGE_URL)))
                    true
                }
                root.addPreference(this)
            }

            Preference(context).apply {
                title = getString(R.string.morphe_bundle_refresh)
                summary = getString(R.string.morphe_bundle_refresh_summary)
                setOnPreferenceClickListener {
                    refreshCatalog()
                    true
                }
                root.addPreference(this)
            }

            addCatalogStatus(root)
            addProfilePreferences(root)
            addBundlePreferences(root)
        }

        private fun addCatalogStatus(root: PreferenceScreen) {
            Preference(context).apply {
                title = getString(R.string.morphe_bundle_cache_title)
                val currentCatalog = catalog
                summary = if (currentCatalog == null) {
                    getString(R.string.morphe_bundle_cache_empty)
                } else {
                    getString(
                        R.string.morphe_bundle_cache_summary,
                        currentCatalog.bundles.size,
                        currentCatalog.patchCount,
                        currentCatalog.sha256.take(12),
                    )
                }
                isEnabled = false
                root.addPreference(this)
            }
        }

        private fun addProfilePreferences(root: PreferenceScreen) {
            val profiles = profileStore.loadAll().sortedBy { it.name.lowercase() }
            if (profiles.isEmpty()) return

            val currentCatalog = catalog
            val category = PreferenceCategory(context).apply {
                title = getString(R.string.morphe_bundle_profiles_title)
                root.addPreference(this)
            }
            profiles.forEach { profile ->
                val validation = currentCatalog?.let {
                    MorpheProfileValidator.validate(profile, it, sourceReviewStore::isApproved)
                }
                Preference(context).apply {
                    title = profile.name
                    summary = profileSummary(profile, validation)
                    setOnPreferenceClickListener {
                        showProfileDialog(profile, validation)
                        true
                    }
                    category.addPreference(this)
                }
            }
        }

        private fun profileSummary(profile: PatchProfile, validation: ProfileValidation?): String {
            val enabledCount = profile.selections.count { it.enabled }
            val state = validation?.state ?: ProfileValidationState.SOURCE_NOT_AVAILABLE
            return getString(
                R.string.morphe_bundle_profile_summary,
                profile.repository,
                enabledCount,
                profileValidationLabel(state),
            )
        }

        private fun profileValidationLabel(state: ProfileValidationState): String = when (state) {
            ProfileValidationState.CURRENT -> getString(R.string.morphe_bundle_profile_current)
            ProfileValidationState.CATALOG_UPDATED -> getString(R.string.morphe_bundle_profile_catalog_updated)
            ProfileValidationState.SOURCE_NOT_AVAILABLE -> getString(R.string.morphe_bundle_profile_source_unavailable)
            ProfileValidationState.SOURCE_BLOCKED -> getString(R.string.morphe_bundle_profile_source_blocked)
            ProfileValidationState.SOURCE_REVIEW_REQUIRED -> getString(R.string.morphe_bundle_profile_source_review_required)
            ProfileValidationState.TARGET_APP_NOT_AVAILABLE -> getString(R.string.morphe_bundle_profile_target_unavailable)
            ProfileValidationState.PATCHES_NOT_AVAILABLE -> getString(R.string.morphe_bundle_profile_patches_unavailable)
        }

        private fun addBundlePreferences(root: PreferenceScreen) {
            val currentCatalog = catalog ?: return
            val category = PreferenceCategory(context).apply {
                title = getString(R.string.morphe_bundle_sources_title)
                root.addPreference(this)
            }

            currentCatalog.bundles.forEach { bundle ->
                Preference(context).apply {
                    title = bundle.displayName
                    summary = bundleSummary(bundle)
                    isEnabled = true
                    setOnPreferenceClickListener {
                        showBundleDialog(bundle)
                        true
                    }
                    category.addPreference(this)
                }
            }
        }

        private fun bundleSummary(bundle: CommunityBundle): String {
            val appCount = bundle.packageNames.size
            val trust = when (sourceReviewStore.effectiveTrust(bundle)) {
                SourceTrust.APPROVED -> getString(R.string.morphe_bundle_trust_approved)
                SourceTrust.REVIEW_REQUIRED -> getString(R.string.morphe_bundle_trust_review_required)
                SourceTrust.BLOCKED -> getString(R.string.morphe_bundle_trust_blocked)
            }
            return getString(
                R.string.morphe_bundle_source_summary,
                bundle.repository,
                bundle.patches.size,
                appCount,
                trust,
            )
        }

        private fun refreshCatalog() {
            Toast.makeText(context, R.string.morphe_bundle_refresh_started, Toast.LENGTH_SHORT).show()
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        Result.success(MorpheCatalogClient(catalogStore).refresh())
                    } catch (error: Throwable) {
                        Result.failure(error)
                    }
                }
                result.onSuccess {
                    catalog = it
                    rebuildScreen()
                    Toast.makeText(context, R.string.morphe_bundle_refresh_success, Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(
                        context,
                        getString(R.string.morphe_bundle_refresh_failed, it.message ?: "unknown error"),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }

        private fun showProfileDialog(profile: PatchProfile, validation: ProfileValidation?) {
            val currentActivity = activity ?: return
            val resolvedValidation = validation ?: ProfileValidation(ProfileValidationState.SOURCE_NOT_AVAILABLE)
            val builder = AlertDialog.Builder(currentActivity)
                .setTitle(profile.name)
                .setMessage(
                    getString(
                        R.string.morphe_bundle_profile_dialog_summary,
                        profile.repository,
                        profileValidationLabel(resolvedValidation.state),
                        profile.selections.count { it.enabled },
                    )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.morphe_bundle_delete_profile) { _, _ ->
                    showDeleteProfileDialog(profile)
                }

            if (resolvedValidation.canHandoff) {
                builder.setPositiveButton(R.string.morphe_bundle_open_in_morphe) { _, _ ->
                    resolvedValidation.bundle?.let(::openInMorphe)
                }
            } else if (resolvedValidation.bundle != null) {
                builder.setPositiveButton(R.string.morphe_bundle_update_profile) { _, _ ->
                    showBundleDialog(resolvedValidation.bundle)
                }
            }
            builder.show()
        }

        private fun showDeleteProfileDialog(profile: PatchProfile) {
            val currentActivity = activity ?: return
            AlertDialog.Builder(currentActivity)
                .setTitle(R.string.morphe_bundle_delete_profile)
                .setMessage(getString(R.string.morphe_bundle_delete_profile_summary, profile.name))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    profileStore.delete(profile.id)
                    rebuildScreen()
                    Toast.makeText(context, R.string.morphe_bundle_profile_deleted, Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        private fun showBundleDialog(bundle: CommunityBundle) {
            val patches = bundle.patches.sortedBy { it.name.lowercase() }
            val profileId = "bundle:${bundle.id}"
            val savedProfile = profileStore.loadAll().firstOrNull { it.id == profileId }
            val selectedIds = savedProfile?.selections
                ?.filter { it.enabled }
                ?.mapTo(mutableSetOf()) { it.patchId }
                ?: mutableSetOf()
            val labels = patches.map { patch ->
                if (patch.description.isNullOrBlank()) patch.name else "${patch.name}\n${patch.description}"
            }.toTypedArray()
            val checked = patches.map { it.id in selectedIds }.toBooleanArray()

            val currentActivity = activity ?: return
            val dialog = AlertDialog.Builder(currentActivity)
                .setTitle(bundle.displayName)
                .setMessage(getString(R.string.morphe_bundle_dialog_summary, bundle.repository))
                .setMultiChoiceItems(labels, checked) { _, index, isChecked ->
                    if (isChecked) selectedIds += patches[index].id else selectedIds -= patches[index].id
                }
                .setNegativeButton(android.R.string.cancel, null)

            when (sourceReviewStore.effectiveTrust(bundle)) {
                SourceTrust.BLOCKED -> dialog.setPositiveButton(android.R.string.ok, null)
                SourceTrust.REVIEW_REQUIRED -> dialog.setPositiveButton(R.string.morphe_bundle_review_source) { _, _ ->
                    showSourceReviewDialog(bundle)
                }
                SourceTrust.APPROVED -> {
                    dialog.setNeutralButton(R.string.morphe_bundle_open_in_morphe) { _, _ ->
                        openInMorphe(bundle)
                    }
                    dialog.setPositiveButton(R.string.morphe_bundle_save_profile) { _, _ ->
                        saveProfile(bundle, profileId, patches, selectedIds)
                    }
                }
            }
            dialog.show()
        }

        private fun saveProfile(
            bundle: CommunityBundle,
            profileId: String,
            patches: List<io.github.nexalloy.bridge.CommunityPatch>,
            selectedIds: Set<String>,
        ) {
            val currentCatalog = catalog ?: return
            profileStore.save(
                PatchProfile(
                    id = profileId,
                    name = bundle.displayName,
                    repository = bundle.repository,
                    packageName = bundle.packageNames.singleOrNull() ?: MULTI_APP_PROFILE_PACKAGE,
                    catalogSha256 = currentCatalog.sha256,
                    selections = patches.map { patch ->
                        PatchSelection(
                            patchId = patch.id,
                            enabled = patch.id in selectedIds,
                            options = patch.options.mapNotNull { option ->
                                option.defaultValue?.let { option.key to it }
                            }.toMap(),
                        )
                    },
                )
            )
            Toast.makeText(context, R.string.morphe_bundle_profile_saved, Toast.LENGTH_SHORT).show()
        }

        private fun showSourceReviewDialog(bundle: CommunityBundle) {
            val currentActivity = activity ?: return
            AlertDialog.Builder(currentActivity)
                .setTitle(R.string.morphe_bundle_review_source)
                .setMessage(getString(R.string.morphe_bundle_review_source_summary, bundle.repository))
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.morphe_bundle_view_source) { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/${bundle.repository}")))
                }
                .setPositiveButton(R.string.morphe_bundle_trust_source) { _, _ ->
                    sourceReviewStore.approve(bundle.repository)
                    rebuildScreen()
                    showBundleDialog(bundle)
                }
                .show()
        }

        private fun openInMorphe(bundle: CommunityBundle) {
            startActivity(Intent(Intent.ACTION_VIEW, MorpheBridgeConfig.sourceHandoffUri(bundle.repository)))
        }

    }
}
