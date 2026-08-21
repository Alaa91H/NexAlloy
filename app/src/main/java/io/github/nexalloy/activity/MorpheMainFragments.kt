package io.github.nexalloy.activity

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import io.github.nexalloy.bridge.RuntimeCatalogClient
import io.github.nexalloy.bridge.RuntimeCatalogConfig
import io.github.nexalloy.bridge.RuntimeCatalogStore
import io.github.nexalloy.common.MorphePreferences
import io.github.nexalloy.common.UpdateChecker
import io.github.nexalloy.runtime.BuiltInRuntimeLayerState
import io.github.nexalloy.runtime.ImportedRuntimeLayerStore
import io.github.nexalloy.runtime.RuntimeLayer
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

private const val CARD_CORNER_RADIUS_DP = 26
private const val CARD_MARGIN_BOTTOM_DP = 12
private const val DEVELOPER_PROFILE_URL = "https://github.com/Alaa91H"
private const val DEVELOPER_DONATION_URL = "https://ko-fi.com/alaa91h"
private const val PROJECT_RELEASES_URL = "https://github.com/Alaa91H/Morphe-LSPosed/releases"

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
        applicationPackage: String? = null,
        onClick: (() -> Unit)? = null,
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
            if (onClick != null) {
                isClickable = true
                isFocusable = true
                setOnClickListener { onClick() }
            }
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }
        addCardTitle(content, title, applicationPackage)
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
        summary: String? = null,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit,
    ) {
        val context = requireContext()
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(14) }
        }
        val labels = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        TextView(context).apply {
            text = label
            setTextColor(color(R.color.morphe_on_surface))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            labels.addView(this)
        }
        if (!summary.isNullOrBlank()) {
            TextView(context).apply {
                text = summary
                setTextColor(color(R.color.morphe_on_surface_variant))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setLineSpacing(dp(1).toFloat(), 1f)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(3) }
                labels.addView(this)
            }
        }
        val toggle = MaterialSwitch(context).apply {
            isChecked = checked
            isEnabled = enabled
            contentDescription = label
            setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        }
        row.addView(labels)
        row.addView(toggle)
        container.addView(row)
    }

    protected fun addButton(
        container: LinearLayout,
        label: String,
        onClick: () -> Unit,
    ) {
        val button = MaterialButton(
            requireContext(),
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle,
        ).apply {
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

    private fun addCardTitle(container: LinearLayout, value: String, applicationPackage: String?) {
        if (applicationPackage.isNullOrBlank()) {
            titleView(value).also(container::addView)
            return
        }
        val context = requireContext()
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val icon = ImageView(context).apply {
            val size = dp(40)
            layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = dp(12) }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = value
            val applicationIcon = runCatching {
                context.packageManager.getApplicationIcon(applicationPackage)
            }.getOrNull()
            if (applicationIcon == null) setImageResource(R.drawable.ic_morphe_patches)
            else setImageDrawable(applicationIcon)
        }
        header.addView(icon)
        header.addView(titleView(value).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        container.addView(header)
    }

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

private data class PatchToggle(
    val name: String,
    val description: String,
    val defaultEnabled: Boolean,
)

private data class AppPatchGroup(
    val appName: String,
    val packageName: String,
    val patches: List<PatchToggle>,
)

/** Displays all local patches grouped into one controllable Material card per target app. */
class ActivePatchesFragment : MorpheCardFragment() {
    private var expandedPackageName: String? = null

    @SuppressLint("WorldReadableFiles")
    override fun renderCards() {
        val context = requireContext()
        val groups = appPatchConfigurations.mapNotNull { it.toPatchGroupOrNull() }
            .sortedWith(
                compareByDescending<AppPatchGroup> { it.enabledPatchCount(context) }
                    .thenBy { it.appName.lowercase() },
            )
        val patchCount = groups.sumOf { it.patches.size }

        addCard(
            title = getString(R.string.patches_page_intro_title),
            summary = getString(R.string.patches_page_intro_summary, groups.size, patchCount),
            status = getString(R.string.patches_page_intro_status),
        )

        if (groups.isEmpty()) {
            addCard(
                title = getString(R.string.patches_empty_title),
                summary = getString(R.string.patches_empty_summary),
            )
            return
        }

        groups.forEach { group ->
            val enabledCount = group.enabledPatchCount(context)
            val expanded = expandedPackageName == group.packageName
            addCard(
                title = group.appName,
                summary = getString(
                    R.string.patches_app_summary,
                    group.packageName,
                    group.patches.size,
                    getString(
                        if (expanded) R.string.patches_collapse_hint
                        else R.string.patches_expand_hint,
                    ),
                ),
                status = getString(R.string.patches_app_status, enabledCount, group.patches.size),
                applicationPackage = group.packageName,
                onClick = {
                    expandedPackageName = if (expanded) null else group.packageName
                    rebuildCards()
                },
            ) {
                if (!expanded) return@addCard
                group.patches.sortedBy { it.name.lowercase() }.forEach { patch ->
                    val enabled = context.getSharedPreferences(group.packageName, Context.MODE_PRIVATE)
                        .getBoolean(patch.name, patch.defaultEnabled)
                    addSwitch(
                        container = this,
                        label = patch.name,
                        summary = patch.description.ifBlank {
                            getString(R.string.patch_description_unavailable)
                        },
                        checked = enabled,
                    ) { shouldEnable ->
                        context.getSharedPreferences(
                            group.packageName,
                            Context.MODE_WORLD_READABLE,
                        ).edit().putBoolean(patch.name, shouldEnable).commit()
                        Toast.makeText(context, R.string.patch_state_saved, Toast.LENGTH_SHORT).show()
                        rebuildCards()
                    }
                }
            }
        }
    }

    private fun AppPatchInfo.toPatchGroupOrNull(): AppPatchGroup? {
        val visiblePatches = patches.asSequence()
            .filter { it.name.isNotBlank() && !it.name.startsWith("<") }
            .map {
                PatchToggle(
                    name = it.name,
                    description = it.description.orEmpty(),
                    defaultEnabled = it.use,
                )
            }
            .toList()
        return visiblePatches.takeIf { it.isNotEmpty() }?.let {
            AppPatchGroup(appName, packageName, it)
        }
    }

    private fun AppPatchGroup.enabledPatchCount(context: Context): Int {
        val preferences = context.getSharedPreferences(packageName, Context.MODE_PRIVATE)
        return patches.count { preferences.getBoolean(it.name, it.defaultEnabled) }
    }
}

/** Runtime catalog, all compatible metadata, and compiled local layers in one visible store. */
class RuntimeStoreFragment : MorpheCardFragment() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var catalog: CommunityCatalog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        catalog = RuntimeCatalogStore(context).load()
        super.onViewCreated(view, savedInstanceState)
        if (catalog == null || MorphePreferences.shouldRefreshCatalog(context, catalog?.fetchedAtMillis)) {
            refreshCatalog(automatic = true)
        }
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
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RuntimeCatalogConfig.CATALOG_PAGE_URL)))
            }
        }

        val layers = RuntimeLayerRegistry.all()
        val currentCatalog = catalog
        if (currentCatalog == null) {
            if (layers.isEmpty()) {
                addCard(
                    title = getString(R.string.store_layers_empty_title),
                    summary = getString(R.string.store_layers_empty_summary),
                )
            } else {
                layers.forEach(::addBuiltInLayerCard)
            }
            return
        }

        val items = RuntimeStoreClassifier().classify(currentCatalog)
        if (items.isEmpty()) {
            addCard(
                title = getString(R.string.store_catalog_empty_title),
                summary = getString(R.string.store_catalog_empty_summary),
            )
            return
        }

        val installable = items.filter { it.availability == RuntimeStoreAvailability.READY }

        addCard(
            title = getString(R.string.store_catalogue_title),
            summary = getString(R.string.store_catalogue_summary, installable.size),
            status = getString(R.string.store_catalogue_status),
        )

        installable.forEach { item ->
            when {
                item.runtimeLayer != null -> addBuiltInLayerCard(item.runtimeLayer, item)
                item.recipe != null -> addInstallableRecipeCard(item)
            }
        }
        val catalogLayerIds = installable.mapNotNull { it.runtimeLayer?.id }.toSet()
        layers.filterNot { it.id in catalogLayerIds }.forEach(::addBuiltInLayerCard)
    }

    private fun addInstallableRecipeCard(item: RuntimeStoreItem) {
        val recipe = item.recipe ?: return
        val context = requireContext()
        val installed = ImportedRuntimeLayerStore.loadFromContext(context)
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
            applicationPackage = item.primaryPackageName,
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.store_recipe_switch_label),
                summary = item.description.orEmpty(),
                checked = enabled,
            ) { shouldEnable ->
                val saved = if (installed == null) {
                    ImportedRuntimeLayerStore.save(context, recipe.spec.copy(enabled = shouldEnable))
                } else {
                    ImportedRuntimeLayerStore.setEnabled(context, installed.id, shouldEnable)
                }
                val message = if (saved) R.string.runtime_restart_required else R.string.runtime_activation_failed
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (saved) rebuildCards()
            }
            addSourceButton(this, item.sourceRepository)
        }
    }

    private fun addMetadataCard(item: RuntimeStoreItem, availability: RuntimeStoreAvailability) {
        val targetPackages = (item.packageNames.ifEmpty { item.catalogPackageNames })
            .joinToString()
            .ifBlank { getString(R.string.store_target_unknown) }
        val status = when (availability) {
            RuntimeStoreAvailability.NEEDS_RUNTIME_ADAPTER -> getString(R.string.store_needs_adapter_status)
            RuntimeStoreAvailability.UNSUPPORTED_TARGET -> getString(R.string.store_unsupported_status)
            RuntimeStoreAvailability.READY -> return
        }
        addCard(
            title = item.sourcePatchName,
            summary = getString(
                R.string.store_catalog_item_summary,
                item.sourceRepository,
                targetPackages,
                item.description.orEmpty(),
            ),
            status = status,
            applicationPackage = item.catalogPackageNames.firstOrNull(),
        ) {
            addSourceButton(this, item.sourceRepository)
        }
    }

    private fun addSourceButton(container: LinearLayout, sourceRepository: String) {
        addButton(container, getString(R.string.store_view_source_action)) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/$sourceRepository"),
                ),
            )
        }
    }

    private fun addBuiltInLayerCard(layer: RuntimeLayer, catalogItem: RuntimeStoreItem? = null) {
        val context = requireContext()
        val enabled = BuiltInRuntimeLayerState.isEnabled(context, layer)
        val targetPackages = catalogItem?.catalogPackageNames
            ?.ifEmpty { layer.packageNames }
            ?.joinToString()
            ?: layer.packageNames.joinToString()
        addCard(
            title = catalogItem?.sourcePatchName ?: layer.patch.name,
            summary = getString(
                R.string.store_layer_summary,
                layer.sourceRepository,
                layer.sourcePatchName,
                targetPackages,
            ),
            status = if (enabled) getString(R.string.status_enabled) else getString(R.string.status_disabled),
            applicationPackage = layer.packageNames.singleOrNull(),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.store_layer_switch_label),
                checked = enabled,
            ) { isEnabled ->
                val saved = BuiltInRuntimeLayerState.setEnabled(context, layer, isEnabled)
                val message = if (saved) R.string.runtime_restart_required else R.string.runtime_activation_failed
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (saved) rebuildCards()
            }
            addSourceButton(this, layer.sourceRepository)
        }
    }

    private fun refreshCatalog(automatic: Boolean = false) {
        if (!isAdded) return
        val context = requireContext()
        if (!automatic) {
            Toast.makeText(context, R.string.store_refresh_started, Toast.LENGTH_SHORT).show()
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { RuntimeCatalogClient(RuntimeCatalogStore(context)).refresh() }
            }
            result.onSuccess {
                if (!isAdded) return@onSuccess
                catalog = it
                rebuildCards()
                val message = if (automatic) {
                    R.string.store_auto_refresh_success
                } else {
                    R.string.store_refresh_success
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }.onFailure {
                if (!isAdded) return@onFailure
                val error = catalogErrorMessage(it)
                val message = if (automatic) {
                    getString(R.string.store_auto_refresh_failed, error)
                } else {
                    getString(R.string.store_refresh_failed, error)
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun catalogErrorMessage(error: Throwable): String = when {
        error.message?.startsWith("Morphe catalog request failed") == true ->
            getString(R.string.store_error_network)
        error.message?.contains("exceeds the local safety limit") == true ->
            getString(R.string.store_error_size)
        error.message?.contains("response is not JSON") == true ->
            getString(R.string.store_error_response)
        else -> getString(R.string.store_error_network)
    }
}

/** Module controls, update preferences, privacy controls, and project information. */
class ModuleSettingsFragment : MorpheCardFragment() {
    override fun renderCards() {
        val context = requireContext()
        addCard(
            title = getString(R.string.settings_page_intro_title),
            summary = getString(R.string.settings_page_intro_summary),
            status = getString(R.string.settings_page_intro_status),
        )

        val catalogAutoUpdateEnabled = MorphePreferences.catalogAutoUpdateEnabled(context)
        addCard(
            title = getString(R.string.settings_catalog_updates_title),
            summary = getString(R.string.settings_catalog_updates_summary),
            status = if (catalogAutoUpdateEnabled) getString(R.string.status_enabled)
            else getString(R.string.status_disabled),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.settings_catalog_auto_update_switch),
                summary = getString(R.string.settings_catalog_auto_update_switch_summary),
                checked = catalogAutoUpdateEnabled,
            ) { enabled ->
                MorphePreferences.setCatalogAutoUpdateEnabled(context, enabled)
                rebuildCards()
            }
        }

        val appUpdateCheckEnabled = MorphePreferences.appUpdateCheckEnabled(context)
        addCard(
            title = getString(R.string.settings_app_updates_title),
            summary = getString(R.string.settings_app_updates_summary),
            status = if (appUpdateCheckEnabled) getString(R.string.status_enabled)
            else getString(R.string.status_disabled),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.settings_app_auto_update_switch),
                summary = getString(R.string.settings_app_auto_update_switch_summary),
                checked = appUpdateCheckEnabled,
            ) { enabled ->
                MorphePreferences.setAppUpdateCheckEnabled(context, enabled)
                rebuildCards()
            }
            addButton(this, getString(R.string.settings_check_update_action)) {
                UpdateChecker().apply {
                    setActivity(activity)
                    checkUpdate(silent = false)
                }
            }
            addButton(this, getString(R.string.settings_open_releases_action)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_RELEASES_URL)))
            }
        }

        val aliasName = ComponentName(context, SettingsActivity::class.java.name + "Alias")
        val launcherHidden = context.packageManager.getComponentEnabledSetting(aliasName) ==
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        addCard(
            title = getString(R.string.settings_launcher_title),
            summary = getString(R.string.settings_launcher_summary),
            status = if (launcherHidden) getString(R.string.status_enabled)
            else getString(R.string.status_disabled),
        ) {
            addSwitch(
                container = this,
                label = getString(R.string.settings_launcher_switch),
                summary = getString(R.string.settings_launcher_switch_summary),
                checked = launcherHidden,
            ) { hidden ->
                context.packageManager.setComponentEnabledSetting(
                    aliasName,
                    if (hidden) PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    PackageManager.DONT_KILL_APP,
                )
                rebuildCards()
            }
        }

        addCard(
            title = getString(R.string.settings_about_title),
            summary = getString(
                R.string.settings_about_summary,
                BuildConfig.VERSION_NAME,
                BuildConfig.COMMIT_HASH,
            ),
            status = getString(R.string.settings_about_status),
        ) {
            addButton(this, getString(R.string.settings_open_profile_action)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_PROFILE_URL)))
            }
            addButton(this, getString(R.string.settings_donate_action)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_DONATION_URL)))
            }
        }
    }
}
