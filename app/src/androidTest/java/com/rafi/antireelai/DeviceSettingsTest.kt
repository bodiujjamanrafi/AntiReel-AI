package com.rafi.antireelai

import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class DeviceSettingsTest {
    @Test fun restrictedAccessHelpOpensOnlyThisAppsPublicInfoPage() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val intent=DeviceSettings.appInfoIntent(context.packageName)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,intent.action)
        assertEquals("package:${context.packageName}",intent.data.toString())
        assertNull(intent.component)
        assertNull(intent.extras)
        assertEquals(Settings.ACTION_ACCESSIBILITY_SETTINGS,DeviceSettings.accessibilityIntent().action)
    }
}
