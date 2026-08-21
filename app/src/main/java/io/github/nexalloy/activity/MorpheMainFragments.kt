@file:Suppress("DEPRECATION")

package io.github.nexalloy.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import io.github.nexalloy.AppPatchInfo
import io.github.nexalloy.BuildConfig
import io.github.nexalloy.R
import io.github.nexalloy.appPatchConfigurations
import io.github.nexalloy.bridge.CommunityCatalog
import io.github.nexalloy.bridge.MorpheBridgeConfig
import io.github.nexalloy.bridge.MorpheCatalogClient
import io.github.nexalloy.bridge.MorpheCatalogStore
import io.github.nexalloy.common.UpdateChecker
import io.github.nexalloy.runtime.BuiltInRuntimeLayerState
import io.github.nexalloy.runtime.ImportedBooleanRuntimeLayerSpec
import io.github.nexalloy.runtime.ImportedRuntimeLayerStore
import io.github.nexalloy.runtime.RuntimeLayer
import io.github.nexalloy.runtime.RuntimeLayerRegistry
import io.github.nexalloy.runtime.RuntimeLayerSpecCodec
import io.github.nexalloy.runtime.RuntimeStoreAvailability
import io.github.nexalloy.runtime.RuntimeStoreClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CARD_CORNER_RADIUS_DP = 26
private const val CARD_MARGIN_BOTTOM_DP = 12

/** Shared Material card renderer used by all three home destinations. */
abstract class MorpheCardFragment : Fragment() {
    private var cardIndex = 0

    /** Platform fragments do not expose the AndroidX requireContext helper. */
    protected fun requireContext(): Context = activity
        ?: throw IllegalStateException("Morphe card page is not attached to an Activity")
    protected lateinit var cardList: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_morphe_cards, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardList = view.findViewById(R.id.morphe_card_list)
        renderCards()
    }

    protected fun rebuildCards() {
        if (!isAdded) return
        cardIndex = 0
        cardList.removeAllViews()
        renderCards()
    }

    protected abstract fun renderCards()

    protected fun addCard(
        title: String,
        summary: String? = null,
        status: String? = null,
        block: LinearLayout.() -> Unit = {},
    ): LinearLayout {
        val context = requireContext()
        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(CARD_MARGIN_BOTTOM_DP) }
            radius = dp(CARD_CORNER_RADIUS_DP).toFloat()
            cardElevation = dp(2).toFloat()
            strokeWidth = dp(1)
            strokeColor = color(R.color.morphe_card_stroke)
            setCardBackgroundColor(
                color(if (cardIndex++ % 2 == 0) R.color.morphe_card_light else R.color.morphe_card_dark),
            )
            isClickable = true
            isFocusable = true
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }
        titleView(title).also(content::addView)
        if (!summary.isNullOrBlank()) {
            summaryView(summary).also {
                it.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(6) }
                content.addView(it)
            }
        }
        if (!status.isNullOrBlank()) {
            statusView(status).also {
                it.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(12) }
                content.addView(it)
            }
        }
        content.block()
        card.addView(content)
        cardList.addView(card)
        return content
    }

    protected fun addSwitch(
        container: LinearLayout,
        label: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit,
    ) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(14) }
        }
        val text = TextView(requireContext()).apply {
            this.text = label
            setTextColor(color(R.color.morphe_on_surface))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val toggle = MaterialSwitch(requireContext()).apply {
            isChecked = checked
            isEnabled = enabled
            contentDescription = label
            setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        }
        row.addView(text)
        row.addView(toggle)
        container.addView(row)
    }

    protected fun addButton(
        container: LinearLayout,
        label: String,
        onClick: () -> Unit,
    ) {
        val button = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = label
            isAllCaps = false
            contentDescription = label
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(14) }
            setOnClickListener { onClick() }
        }
        container.addView(button)
    }

    protected fun color(resource: Int): Int = requireContext().getColor(resource)
    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun titleView(value: String) = TextView(requireContext()).apply {
        text = value
        setTextColor(color(R.color.morphe_on_surface))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        typeface = Typeface.DEFAULT_BOLD
    }

    private fun summaryView(value: String) = TextView(requireContext()).apply {
        text = value
        setTextColor(color(R.color.morphe_on_surface_variant))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setLineSpacing(dp(2).toFloat(), 1f)
    }

    private fun statusView(value: String) = TextView(requireContext()).apply {
        text = value.uppercase()
        setTextColor(color(R.color.morphe_primary))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        typeface = Typeface.DEFAULT_BOLD
    }
}

private data class PatchCard(
    val appName: String,
    val packageName: String,
    val patchName: String,
    val description: String,
    val defaultEnabled: Boolean,
)

/** Displays every local patch as an individual, directly controllable Material card. */
class ActivePatchesFragment : MorpheCardFragment() {
    @SuppressLint("WorldReadableFiles")
    override fun renderCards() {
        val context = requireContext()
        val patches = appPatchConfigurations.flatMap { app -> app.visiblePatchCards() }
            .sortedWith(
                compareByDescending<PatchCard> { patch ->
                    context.getSharedPreferences(patch.packageName, Context.MODE_PRIVATE)
                        .getBoolean(patch.patchName, patch.defaultEnabled)
                }.thenBy { it.appName }.thenBy { it.patchName },
            )

        addCard(
            title = getString(R.string.patches_page_intro_title),
            summary = getString(R.string.patches_page_intro_summary, patches.size),
            status = getString(R.string.patches_page_intro_status),
        )

        if (patches.isEmpty()) {
            addCard(
                title = getString(R.string.patches_empty_title),
                summary = getString(R.string.patches_empty_summary),
            )
            return
        }

        patches.forEach { patch ->
            val preferences = context.getSharedPreferences(patch.packageName, Context.MODE_PRIVATE)
            val isEnabled = preferences.getBoolean(patch.patchName, patch.defaultEnabled)
            addCard(
                title = patch.patchName,
                summary = "${patch.appName} · ${patch.description.ifBlank { getString(R.string.patch_description_unavailable) }}",
                status = if (isEnabled) getString(R.string.status_enabled) else getString(R.string.status_disabled),
            ) {
                addSwitch(
                    container = this,
                    label = getString(R.string.patch_switch_label),
                    checked = isEnabled,
                ) { enabled ->
                    context.getSharedPreferences(patch.packageName, Context.MODE_WORLD_READABLE)
                        .edit()
                        .putBoolean(patch.patchName, enabled)
                        .commit()
                    Toast.makeText(context, R.string.patch_state_saved, Toast.LENGTH_SHORT).show()
                    rebuildCards()
                }
            }
        }
    }

    private fun AppPatchInfo.visiblePatchCards(): List<PatchCard> = patches
        .asSequence()
        .filter { it.name.isNotBlank() && !it.name.startsWith("<") }
        .map {
            PatchCard(
                appName = appName,
                packageName = packageName,
                patchName = it.name,
                description = it.description.orEmpty(),
                defaultEnabled = it.use,
            )
        }
        .toList()
}

/** Runtime catalog and compiled local layers, rendered as install/enable cards. */
class RuntimeStoreFragment : MorpheCardFragment() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var catalog: CommunityCatalog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        catalog = MorpheCatalogStore(requireContext()).load()
        super.onViewCreated(view, savedInstanceState)
        if (catalog == null) refreshCatalog()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun renderCards() {
        addCard(
            title = getString(R.string.store_page_intro_title),
            summary = getString(R.string.store_page_intro_summary),
            status = catalog?.let { getString(R.string.store_catalog_loaded, it.patchCount) }
                ?: getString(R.string.store_catalog_loading),
        ) {
            addButton(this, getString(R.string.store_refresh_action)) { refreshCatalog() }
            addButton(this, getString(R.string.store_open_catalog_action)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MorpheBridgeConfig.CATALOG_PAGE_URL)))
            }
        }

        val layers = RuntimeLayerRegistry.all()
        layers.forEach { layer -> addBuiltInLayerCard(layer) }

        val currentCatalog = catalog ?: return
        val items = RuntimeStoreClassifier().classify(currentCatalog)
        items.filter { it.availability == RuntimeStoreAvailability.READY && it.runtimeLayer == null }
            .forEach { item ->
                val recipe = item.recipe ?: return@forEach
                val installed = ImportedRuntimeLayerStore.loadFromContext(requireContext())
                    .firstOrNull { it.id == recipe.spec.id }
                val enabled = installed?.enabled ?: false
                addCard(
                    title = item.sourcePatchName,
                    summary = getString(
                        R.string.store_recipe_summary,
                        item.sourceRepository,
                        item.primaryPackageName.orEmpty(),
                    ),
                    status = if (installed == null) getString(R.string.store_recipe_status)
                    else if (enabled) getString(R.string.status_enabled) else getString(R.string.status_disabled),
                ) {
                    addSwitch(
                        container = this,
                        label = getString(R.string.store_recipe_switch_label),
                        checked = enabled,
                    ) { shouldEnable ->
                        val context = requireContext()
                        if (installed == null) {
                            ImportedRuntimeLayerStore.save(context, recipe.spec.copy(enabled = shouldEnable))
                        } else {
                            ImportedRuntimeLayerStore.setEnabled(context, installed.id, shouldEnable)
                        }
                        Toast.makeText(context, R.string.runtime_restart_required, Toast.LENGTH_LONG).show()
                        rebuildCards()
                    }
                    addButton(this, getString(R.string.store_view_source_action)) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/${item.sourceRepository}"),
                            ),
                        )
                    }
                }
            }

        val needsAdapter = items.count { it.availability == RuntimeStoreAvailability.NEEDS_RUNTIME_ADAPTER }
        if (needsAdapter > 0) {
            addCard(
                title = getString(R.string.store_needs_adapter_title),
                summary = getString(R.string.store_needs_adapter_summary, needsAdapter),
                status = getString(R.string.store_metadata_only_status),
            )
        }
    }

    private fun addBuiltInLayerCard(layer: RuntimeLayer) {
        val context = requireContext()
        val enabled = BuiltInRuntimeLayerState.isEnabled(context, layer)
        addCard(
            title = layer.patch.name,
            summary = getString(
                R.string.store_layer_summary,
                layer.sourceRepository,
                layer.sourcePatchName,
                layer.packageNames.joinToString(),
            ),
            status = if (enabled) getString(R.string.status_enabled) else getString(R.string.status_disabled),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.store_layer_switch_label),
                checked = enabled,
            ) { isEnabled ->
                BuiltInRuntimeLayerState.setEnabled(context, layer, isEnabled)
                Toast.makeText(context, R.string.runtime_restart_required, Toast.LENGTH_LONG).show()
                rebuildCards()
            }
        }
    }

    private fun refreshCatalog() {
        if (!isAdded) return
        Toast.makeText(requireContext(), R.string.store_refresh_started, Toast.LENGTH_SHORT).show()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { MorpheCatalogClient(MorpheCatalogStore(requireContext())).refresh() }
            }
            result.onSuccess {
                catalog = it
                rebuildCards()
                Toast.makeText(requireContext(), R.string.store_refresh_success, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.store_refresh_failed, it.message ?: "unknown error"),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }
}

/** Module controls and support links, using the same alternating card system. */
class ModuleSettingsFragment : MorpheCardFragment() {
    @SuppressLint("WorldReadableFiles")
    override fun renderCards() {
        val context = requireContext()
        addCard(
            title = getString(R.string.settings_page_intro_title),
            summary = getString(R.string.settings_page_intro_summary),
            status = getString(R.string.settings_page_intro_status),
        )

        val preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val autoCheckDisabled = preferences.getBoolean("disable_auto_check_update", false)
        addCard(
            title = getString(R.string.settings_updates_title),
            summary = getString(R.string.settings_updates_summary),
            status = if (autoCheckDisabled) getString(R.string.status_disabled) else getString(R.string.status_enabled),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.settings_auto_update_switch),
                checked = !autoCheckDisabled,
            ) { enabled ->
                context.getSharedPreferences("prefs", Context.MODE_WORLD_READABLE)
                    .edit()
                    .putBoolean("disable_auto_check_update", !enabled)
                    .commit()
                rebuildCards()
            }
            addButton(this, getString(R.string.settings_check_update_action)) {
                UpdateChecker().apply {
                    setActivity(activity)
                    checkUpdate(silent = false)
                }
            }
        }

        val aliasName = ComponentName(context, SettingsActivity::class.java.name + "Alias")
        val isHidden = context.packageManager.getComponentEnabledSetting(aliasName) ==
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        addCard(
            title = getString(R.string.settings_launcher_title),
            summary = getString(R.string.settings_launcher_summary),
            status = if (isHidden) getString(R.string.status_disabled) else getString(R.string.status_enabled),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.settings_launcher_switch),
                checked = !isHidden,
            ) { visible ->
                context.packageManager.setComponentEnabledSetting(
                    aliasName,
                    if (visible) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
                rebuildCards()
            }
        }

        addCard(
            title = getString(R.string.settings_info_title),
            summary = getString(
                R.string.settings_info_summary,
                BuildConfig.VERSION_NAME,
                BuildConfig.COMMIT_HASH,
                DateUtils.getRelativeTimeSpanString(BuildConfig.COMMIT_DATE * 1000),
            ),
        ) {
            addButton(this, getString(R.string.settings_faq_action)) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Alaa91H/Morphe-LSPosed/wiki/Frequently-Asked-Questions"),
                    ),
                )
            }
            addButton(this, getString(R.string.settings_licenses_action)) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Alaa91H/Morphe-LSPosed/blob/main/LICENSE"),
                    ),
                )
            }
        }
    }
}
