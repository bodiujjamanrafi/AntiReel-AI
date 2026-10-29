package com.rafi.antireelai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Public settings pages only. Permission approval always stays with the user. */
internal object DeviceSettings {
    fun appInfoIntent(packageName:String)=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package",packageName,null))
    fun accessibilityIntent()=Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun open(context:Context,intent:Intent):Boolean {
        if(runCatching{context.startActivity(intent);true}.getOrDefault(false))return true
        // Some manufacturers do not expose the standard destination. Opening
        // general Settings is a useful fallback and never changes a permission.
        return runCatching{context.startActivity(Intent(Settings.ACTION_SETTINGS));true}.getOrDefault(false)
    }
}
