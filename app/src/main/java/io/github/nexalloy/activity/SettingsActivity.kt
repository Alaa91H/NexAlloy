package io.github.nexalloy.activity

import android.app.Activity
import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import app.morphe.extension.shared.Utils
import io.github.nexalloy.R
import io.github.nexalloy.common.MorphePreferences
import io.github.nexalloy.common.UpdateChecker

/**
 * Material home for Morphe LSPosed. The three destinations are intentionally local:
 * patch controls, Runtime Store, and module settings.
 */
class SettingsActivity : Activity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Utils.setContext(this)

        toolbar = findViewById(R.id.morphe_toolbar)
        navigation = findViewById(R.id.morphe_bottom_navigation)
        navigation.setOnItemSelectedListener { item ->
            showDestination(item.itemId)
            true
        }

        if (savedInstanceState == null) {
            navigation.selectedItemId = R.id.navigation_patches
        } else {
            showDestination(navigation.selectedItemId)
        }

        if (MorphePreferences.shouldCheckApplicationUpdate(this)) {
            UpdateChecker().apply {
                setActivity(this@SettingsActivity)
                autoCheckUpdate()
            }
        }
    }

    private fun showDestination(destinationId: Int) {
        val (titleRes, fragment) = when (destinationId) {
            R.id.navigation_store -> R.string.navigation_store to RuntimeStoreFragment()
            R.id.navigation_settings -> R.string.navigation_settings to ModuleSettingsFragment()
            else -> R.string.navigation_patches to ActivePatchesFragment()
        }
        toolbar.title = getString(titleRes)
        fragmentManager.beginTransaction()
            .replace(R.id.settings_container, fragment)
            .commit()
    }
}
