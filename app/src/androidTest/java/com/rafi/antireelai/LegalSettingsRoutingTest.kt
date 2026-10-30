package com.rafi.antireelai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real legal destinations/back stack; never opens a browser, sends mail or grants access. */
class LegalSettingsRoutingTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun helpRetainsLocalPrivacyControlsAndPublicSupportEntryWithoutChangingPreferences() {
        val store=TrackerStore(ui.activity)
        val before=store.prefs.all.toMap()
        ui.waitUntil(5000){ui.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithContentDescription("Settings").performClick()
        ui.onNode(hasText(SettingsSection.HELP.title) and hasClickAction()).performScrollTo().performClick()
        for(title in listOf("Private by design","Your data","Your controls","Help & Support"))
            ui.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Full guidance and public support page").assertDoesNotExist()
        ui.onNodeWithText("Legal & support").assertDoesNotExist()
        ui.runOnUiThread{ui.activity.onBackPressedDispatcher.onBackPressed()}
        ui.onNodeWithContentDescription("Settings").assertIsSelected()
        assertEquals(before.filterKeys{it.startsWith("count:") || it.startsWith("usage:")},store.prefs.all.filterKeys{it.startsWith("count:") || it.startsWith("usage:")})
        for(key in listOf("goal","dailyMinutes","platforms","themeMode","language","restartDay"))assertEquals(key,before[key],store.prefs.all[key])
    }
}
