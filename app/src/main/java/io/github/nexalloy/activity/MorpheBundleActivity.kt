package io.github.nexalloy.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import io.github.nexalloy.R
import io.github.nexalloy.appPatchConfigurations
import io.github.nexalloy.bridge.CommunityCatalog
import io.github.nexalloy.bridge.MorpheBridgeConfig
import io.github.nexalloy.bridge.MorpheCatalogClient
import io.github.nexalloy.bridge.MorpheCatalogStore
import io.github.nexalloy.runtime.BuiltInRuntimeLayerState
import io.github.nexalloy.runtime.ImportedBooleanRuntimeLayerSpec
import io.github.nexalloy.runtime.ImportedRuntimeLayerStore
import io.github.nexalloy.runtime.RuntimeLayer
import io.github.nexalloy.runtime.RuntimeLayerSpecCodec
import io.github.nexalloy.runtime.RuntimeLayerRegistry
import io.github.nexalloy.runtime.RuntimeStoreAvailability
import io.github.nexalloy.runtime.RuntimeStoreClassifier
import io.github.nexalloy.runtime.RuntimeStoreItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Read-only Morphe catalogue view plus a launcher for compiled Morphe LSPosed runtime layers.
 *
 * Community .mpp archives are never downloaded or executed here. Each listed runtime
 * layer is compiled into Morphe LSPosed and opened through the ordinary app patch settings.
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
                summary = getString(R.string.runtime_catalog_summary)
                isEnabled = false
                root.addPreference(this)
            }

            Preference(context).apply {
                title = getString(R.string.morphe_bundle_catalogue_open)
                summary = getString(R.string.morphe_bundle_catalogue_open_summary)
                setOnPreferenceClickListener {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse(MorpheBridgeConfig.CATALOG_PAGE_URL),
                        )
                    )
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
            addRuntimeStorePreferences(root)
            addRuntimeLayerPreferences(root)
            addImportedRuntimeLayerPreferences(root)
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

        private fun addRuntimeStorePreferences(root: PreferenceScreen) {
            val currentCatalog = catalog ?: return
            val items = RuntimeStoreClassifier().classify(currentCatalog)
            val ready = items.filter { it.availability == RuntimeStoreAvailability.READY }
            val needsAdapter = items.filter { it.availability == RuntimeStoreAvailability.NEEDS_RUNTIME_ADAPTER }

            if (ready.isNotEmpty()) {
                val category = PreferenceCategory(context).apply {
                    title = getString(R.string.runtime_store_ready_title)
                    root.addPreference(this)
                }
                ready.forEach { item ->
                    Preference(context).apply {
                        title = item.sourcePatchName
                        summary = getString(
                            R.string.runtime_store_ready_summary,
                            item.sourceRepository,
                            item.primaryPackageName ?: "",
                        )
                        setOnPreferenceClickListener {
                            showReadyStoreItemDialog(item)
                            true
                        }
                        category.addPreference(this)
                    }
                }
            }

            if (needsAdapter.isNotEmpty()) {
                val category = PreferenceCategory(context).apply {
                    title = getString(R.string.runtime_store_needs_adapter_title)
                    root.addPreference(this)
                }
                needsAdapter.forEach { item ->
                    Preference(context).apply {
                        title = item.sourcePatchName
                        summary = getString(
                            R.string.runtime_store_needs_adapter_summary,
                            item.sourceRepository,
                            item.primaryPackageName ?: "",
                        )
                        isEnabled = false
                        category.addPreference(this)
                    }
                }
            }

            val unsupportedCount = items.count { it.availability == RuntimeStoreAvailability.UNSUPPORTED_TARGET }
            if (unsupportedCount > 0) {
                Preference(context).apply {
                    title = getString(R.string.runtime_store_unsupported_title)
                    summary = getString(R.string.runtime_store_unsupported_summary, unsupportedCount)
                    isEnabled = false
                    root.addPreference(this)
                }
            }
        }

        private fun showReadyStoreItemDialog(item: RuntimeStoreItem) {
            val currentActivity = activity ?: return
            val builtInLayer = item.runtimeLayer
            if (builtInLayer != null) {
                showBuiltInRuntimeStoreDialog(item, builtInLayer)
                return
            }
            val recipe = item.recipe ?: return
            val existing = ImportedRuntimeLayerStore.loadFromContext(context)
                .firstOrNull { it.id == recipe.spec.id }
            val isEnabled = existing?.enabled ?: false
            AlertDialog.Builder(currentActivity)
                .setTitle(item.sourcePatchName)
                .setMessage(
                    getString(
                        R.string.runtime_store_recipe_dialog_summary,
                        item.sourceRepository,
                        item.primaryPackageName ?: "",
                    )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.morphe_bundle_view_source) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/${item.sourceRepository}"),
                        )
                    )
                }
                .setPositiveButton(
                    when {
                        existing == null -> R.string.runtime_store_add_action
                        isEnabled -> R.string.runtime_disable_action
                        else -> R.string.runtime_enable_action
                    }
                ) { _, _ ->
                    when {
                        existing == null -> ImportedRuntimeLayerStore.save(context, recipe.spec)
                        else -> ImportedRuntimeLayerStore.setEnabled(context, existing.id, !existing.enabled)
                    }
                    rebuildScreen()
                    Toast.makeText(context, R.string.runtime_restart_required, Toast.LENGTH_LONG).show()
                }
                .show()
        }

        private fun showBuiltInRuntimeStoreDialog(item: RuntimeStoreItem, layer: RuntimeLayer) {
            val currentActivity = activity ?: return
            val isEnabled = BuiltInRuntimeLayerState.isEnabled(context, layer)
            AlertDialog.Builder(currentActivity)
                .setTitle(item.sourcePatchName)
                .setMessage(
                    getString(
                        R.string.runtime_store_built_in_dialog_summary,
                        item.sourceRepository,
                        item.primaryPackageName ?: "",
                    )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.morphe_bundle_view_source) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/${item.sourceRepository}"),
                        )
                    )
                }
                .setPositiveButton(
                    if (isEnabled) R.string.runtime_disable_action else R.string.runtime_enable_action
                ) { _, _ ->
                    BuiltInRuntimeLayerState.setEnabled(context, layer, !isEnabled)
                    rebuildScreen()
                    Toast.makeText(context, R.string.runtime_restart_required, Toast.LENGTH_LONG).show()
                }
                .show()
        }

        private fun addRuntimeLayerPreferences(root: PreferenceScreen) {
            val layers = RuntimeLayerRegistry.all()
            if (layers.isEmpty()) return

            val category = PreferenceCategory(context).apply {
                title = getString(R.string.runtime_layers_title)
                root.addPreference(this)
            }
            layers.forEach { layer ->
                Preference(context).apply {
                    title = layer.patch.name
                    summary = getString(
                        R.string.runtime_layer_summary,
                        layer.sourceRepository,
                        layer.sourcePatchName,
                    )
                    setOnPreferenceClickListener {
                        showRuntimeLayerDialog(layer)
                        true
                    }
                    category.addPreference(this)
                }
            }
        }

        private fun addImportedRuntimeLayerPreferences(root: PreferenceScreen) {
            Preference(context).apply {
                title = getString(R.string.runtime_import_title)
                summary = getString(R.string.runtime_import_summary)
                setOnPreferenceClickListener {
                    showRuntimeSpecImportDialog()
                    true
                }
                root.addPreference(this)
            }

            val importedSpecs = ImportedRuntimeLayerStore.loadFromContext(context)
            if (importedSpecs.isEmpty()) return
            val category = PreferenceCategory(context).apply {
                title = getString(R.string.runtime_imported_title)
                root.addPreference(this)
            }
            importedSpecs.forEach { spec ->
                Preference(context).apply {
                    title = spec.patchName
                    summary = getString(
                        R.string.runtime_imported_summary,
                        spec.sourceRepository,
                        spec.packageName,
                        if (spec.enabled) getString(R.string.runtime_status_enabled) else getString(R.string.runtime_status_disabled),
                    )
                    setOnPreferenceClickListener {
                        showImportedRuntimeLayerDialog(spec)
                        true
                    }
                    category.addPreference(this)
                }
            }
        }

        private fun showRuntimeSpecImportDialog() {
            val currentActivity = activity ?: return
            val input = EditText(currentActivity).apply {
                hint = getString(R.string.runtime_import_hint)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 8
                maxLines = 12
            }
            AlertDialog.Builder(currentActivity)
                .setTitle(R.string.runtime_import_title)
                .setMessage(R.string.runtime_import_dialog_summary)
                .setView(input)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.runtime_import_action) { _, _ ->
                    val result = runCatching { RuntimeLayerSpecCodec.parse(input.text.toString()) }
                    result.onSuccess { spec ->
                        ImportedRuntimeLayerStore.save(context, spec)
                        rebuildScreen()
                        Toast.makeText(context, R.string.runtime_import_success, Toast.LENGTH_LONG).show()
                    }.onFailure { error ->
                        Toast.makeText(
                            context,
                            getString(R.string.runtime_import_failed, error.message ?: "unknown error"),
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
                .show()
        }

        private fun showImportedRuntimeLayerDialog(spec: ImportedBooleanRuntimeLayerSpec) {
            val currentActivity = activity ?: return
            AlertDialog.Builder(currentActivity)
                .setTitle(spec.patchName)
                .setMessage(
                    getString(
                        R.string.runtime_imported_dialog_summary,
                        spec.sourceRepository,
                        spec.sourcePatchName,
                        spec.packageName,
                    )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.runtime_delete_action) { _, _ ->
                    ImportedRuntimeLayerStore.delete(context, spec.id)
                    rebuildScreen()
                    Toast.makeText(context, R.string.runtime_deleted, Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton(
                    if (spec.enabled) R.string.runtime_disable_action else R.string.runtime_enable_action
                ) { _, _ ->
                    ImportedRuntimeLayerStore.setEnabled(context, spec.id, !spec.enabled)
                    rebuildScreen()
                    Toast.makeText(context, R.string.runtime_restart_required, Toast.LENGTH_LONG).show()
                }
                .show()
        }

        private fun showRuntimeLayerDialog(layer: RuntimeLayer) {
            val currentActivity = activity ?: return
            val packageName = layer.packageNames.firstOrNull()
            val targetApp = packageName?.let { target ->
                appPatchConfigurations.firstOrNull { it.packageName == target }
            }
            AlertDialog.Builder(currentActivity)
                .setTitle(layer.patch.name)
                .setMessage(
                    getString(
                        R.string.runtime_layer_dialog_summary,
                        layer.sourceRepository,
                        layer.sourcePatchName,
                    )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.morphe_bundle_view_source) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/${layer.sourceRepository}"),
                        )
                    )
                }
                .setPositiveButton(R.string.runtime_layer_open_settings) { _, _ ->
                    if (targetApp == null) {
                        Toast.makeText(context, R.string.runtime_layer_target_unavailable, Toast.LENGTH_LONG).show()
                    } else {
                        startActivity(
                            Intent(context, AppPatchSettingsActivity::class.java)
                                .putExtra(AppPatchSettingsActivity.ARGUMENT_APP_NAME, targetApp.appName)
                        )
                    }
                }
                .show()
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
    }
}
