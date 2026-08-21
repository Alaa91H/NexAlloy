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
import io.github.nexalloy.appPatchConfigurations
import io.github.nexalloy.bridge.CommunityCatalog
import io.github.nexalloy.bridge.MorpheBridgeConfig
import io.github.nexalloy.bridge.MorpheCatalogClient
import io.github.nexalloy.bridge.MorpheCatalogStore
import io.github.nexalloy.runtime.RuntimeLayer
import io.github.nexalloy.runtime.RuntimeLayerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Read-only Morphe catalogue view plus a launcher for compiled NexAlloy runtime layers.
 *
 * Community .mpp archives are never downloaded or executed here. Each listed runtime
 * layer is compiled into NexAlloy and opened through the ordinary app patch settings.
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
            addRuntimeLayerPreferences(root)
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
