package com.rafi.antireelai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Existing completed profile only; navigation does not modify answers or history. */
class MainRoutingTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 @Test fun editorBackReturnsToPersonalSettingsAndActivityRecreationStartsAtHome() {
  ui.waitUntil(10000){ui.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithContentDescription("Settings").performClick()
  ui.onNodeWithText("Language & personal preferences").performScrollTo().performClick()
  ui.onNodeWithText("Edit my answers").performScrollTo().performClick()
  ui.waitUntil(10000){ui.onAllNodesWithText("Finish").fetchSemanticsNodes().isNotEmpty()}
  ui.runOnUiThread{ui.activity.onBackPressedDispatcher.onBackPressed()}
  ui.onNodeWithText("Edit my answers").assertIsDisplayed()
  ui.activityRule.scenario.recreate()
  ui.waitUntil(10000){ui.onAllNodesWithText("Reels today").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithContentDescription("Today").assertIsSelected()
 }
}
