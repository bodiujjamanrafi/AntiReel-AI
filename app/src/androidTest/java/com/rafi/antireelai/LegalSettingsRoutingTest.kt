package com.rafi.antireelai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real legal destinations/back stack; never opens a browser, sends mail or grants access. */
class LegalSettingsRoutingTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun fourLegalDestinationsReturnToPrivacyWithAndroidBackAndPreserveData() {
        val store=TrackerStore(ui.activity)
        val before=store.prefs.all.toMap()
        ui.onNodeWithContentDescription("Settings").performClick()
        ui.onNode(hasText(SettingsSection.PRIVACY.title) and hasClickAction()).performScrollTo().performClick()
        for(document in LegalDocument.entries) {
            ui.onNode(hasText(document.title) and hasClickAction()).performScrollTo().performClick()
            ui.waitUntil(5000){ui.onAllNodesWithText("Updated 29 October 2026").fetchSemanticsNodes().isNotEmpty()}
            ui.onNodeWithText("Open public page").performScrollTo().assertIsDisplayed()
            ui.runOnUiThread{ui.activity.onBackPressedDispatcher.onBackPressed()}
            ui.onNodeWithText("Legal & support").performScrollTo().assertIsDisplayed()
            ui.onAllNodesWithText("Updated 29 October 2026").assertCountEquals(0)
        }
        ui.runOnUiThread{ui.activity.onBackPressedDispatcher.onBackPressed()}
        ui.onNodeWithContentDescription("Settings").assertIsSelected()
        assertEquals(before.filterKeys{it.startsWith("count:") || it.startsWith("usage:")},store.prefs.all.filterKeys{it.startsWith("count:") || it.startsWith("usage:")})
        for(key in listOf("goal","dailyMinutes","platforms","themeMode","language","restartDay"))assertEquals(key,before[key],store.prefs.all[key])
    }
}
