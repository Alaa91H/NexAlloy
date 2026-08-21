package io.github.nexalloy.runtime

import android.annotation.SuppressLint
import android.content.Context

/**
 * Controls a built-in runtime layer through the same package preference file read by
 * PatchExecutor's XSharedPreferences. The target process reads the change on its next
 * process start, so the store never injects a hook into a running process.
 */
object BuiltInRuntimeLayerState {
    fun isEnabled(context: Context, layer: RuntimeLayer): Boolean {
        val packageName = layer.packageNames.singleOrNull() ?: return layer.patch.use
        return context.getSharedPreferences(packageName, Context.MODE_PRIVATE)
            .getBoolean(layer.patch.name, layer.patch.use)
    }

    @SuppressLint("WorldReadableFiles")
    fun setEnabled(context: Context, layer: RuntimeLayer, enabled: Boolean) {
        val packageName = layer.packageNames.singleOrNull() ?: return
        context.getSharedPreferences(packageName, Context.MODE_WORLD_READABLE)
            .edit()
            .putBoolean(layer.patch.name, enabled)
            .commit()
    }
}
