package com.rafi.antireelai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/** Navigates an existing profile. Never toggles a setting or clears user data. */
class SettingsRoutingTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun allSettingsDestinationsAndManagePlatformsReturnWithAndroidBack() {
        val store=TrackerStore(ui.activity)
        val before=store.prefs.all.toMap()
        ui.runOnUiThread {ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
        try {
            ui.onNodeWithContentDescription("Settings").performClick()
            for(section in SettingsSection.entries) {
                ui.onNode(hasText(section.title) and hasClickAction()).performScrollTo().performClick()
                try{ui.onNodeWithContentDescription("Back to Settings").assertIsDisplayed()}catch(error:AssertionError){throw AssertionError("Page ${section.name}: back bounds ${runCatching{ui.onNodeWithContentDescription("Back to Settings").fetchSemanticsNode().boundsInRoot}.getOrNull()}",error)}
                if(section==SettingsSection.ACTIVITY) {
                    ui.onNodeWithText("Delete activity history").performScrollTo().performClick()
                    ui.onNodeWithText("Delete activity?").assertIsDisplayed()
                    ui.onNodeWithText("Keep it").performClick()
                }
                ui.runOnUiThread{ui.activity.onBackPressedDispatcher.onBackPressed()}
                ui.onNodeWithContentDescription("Settings").assertIsSelected()
                ui.onAllNodesWithContentDescription("Back to Settings").assertCountEquals(0)
            }
            ui.onNodeWithText("Manage platforms").performScrollTo().performClick()
            ui.onNodeWithContentDescription("Back to Settings").assertIsDisplayed()
            ui.onNodeWithContentDescription("Platforms").assertIsSelected()
            ui.runOnUiThread{ui.activity.onBackPressedDispatcher.onBackPressed()}
            ui.onNodeWithContentDescription("Settings").assertIsSelected()
            // Dispatch consecutive navigation taps before another rendered frame.
            val taps=listOf("Today","Platforms","Miso","Activity","Settings").map {title->ui.onNodeWithContentDescription(title).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action!!}
            ui.runOnIdle {repeat(3){taps.forEach{it()}}}
            ui.onNodeWithContentDescription("Settings").assertIsSelected()
            ui.onAllNodesWithContentDescription("Back to Settings").assertCountEquals(0)
            assertEquals(before.filterKeys{it.startsWith("count:") || it.startsWith("usage:")},store.prefs.all.filterKeys{it.startsWith("count:") || it.startsWith("usage:")})
            for(key in listOf("goal","dailyMinutes","platforms","themeMode","language","restartDay"))assertEquals(key,before[key],store.prefs.all[key])
        } finally {ui.runOnUiThread{ui.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}}
    }
}
