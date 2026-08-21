package io.github.nexalloy.common

import android.annotation.SuppressLint
import android.content.Context

/**
 * Central settings for Morphe LSPosed. Defaults are deliberately conservative for the
 * launcher, and active for foreground-only catalog and application update checks.
 */
object MorphePreferences {
    const val PREFERENCES_NAME = "morphe_lsposed_settings"
    const val KEY_CATALOG_AUTO_UPDATE = "catalog_auto_update"
    const val KEY_APP_UPDATE_CHECK = "app_update_check"
    private const val KEY_LAST_APP_UPDATE_CHECK = "last_app_update_check"

    const val CATALOG_AUTO_UPDATE_INTERVAL_MILLIS = 6 * 60 * 60 * 1000L
    const val APP_UPDATE_CHECK_INTERVAL_MILLIS = 12 * 60 * 60 * 1000L

    fun catalogAutoUpdateEnabled(context: Context): Boolean = preferences(context)
        .getBoolean(KEY_CATALOG_AUTO_UPDATE, true)

    fun appUpdateCheckEnabled(context: Context): Boolean = preferences(context)
        .getBoolean(KEY_APP_UPDATE_CHECK, true)

    @SuppressLint("WorldReadableFiles")
    fun setCatalogAutoUpdateEnabled(context: Context, enabled: Boolean) {
        writablePreferences(context).edit().putBoolean(KEY_CATALOG_AUTO_UPDATE, enabled).commit()
    }

    @SuppressLint("WorldReadableFiles")
    fun setAppUpdateCheckEnabled(context: Context, enabled: Boolean) {
        writablePreferences(context).edit().putBoolean(KEY_APP_UPDATE_CHECK, enabled).commit()
    }

    fun shouldRefreshCatalog(context: Context, fetchedAtMillis: Long?): Boolean {
        if (!catalogAutoUpdateEnabled(context)) return false
        val lastRefresh = fetchedAtMillis ?: 0L
        return System.currentTimeMillis() - lastRefresh >= CATALOG_AUTO_UPDATE_INTERVAL_MILLIS
    }

    @SuppressLint("WorldReadableFiles")
    fun shouldCheckApplicationUpdate(context: Context): Boolean {
        if (!appUpdateCheckEnabled(context)) return false
        val prefs = writablePreferences(context)
        val lastCheck = prefs.getLong(KEY_LAST_APP_UPDATE_CHECK, 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheck < APP_UPDATE_CHECK_INTERVAL_MILLIS) return false
        prefs.edit().putLong(KEY_LAST_APP_UPDATE_CHECK, now).commit()
        return true
    }

    private fun preferences(context: Context) = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    @SuppressLint("WorldReadableFiles")
    private fun writablePreferences(context: Context) = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_WORLD_READABLE,
    )
}
