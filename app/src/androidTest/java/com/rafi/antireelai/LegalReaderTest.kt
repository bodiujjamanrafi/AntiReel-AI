package com.rafi.antireelai

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LegalReaderTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun allBundledDocumentsLoadWithBackAndActionsInBothThemesAtLargeFontScale() {
        var document by mutableStateOf(LegalDocument.PRIVACY)
        var dark by mutableStateOf(false)
        var backs=0
        val original=AppStyle.dark
        try {
            ui.setContent {
                AppStyle.dark=dark
                val density=LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                    SettingsTheme {key(document,dark) {
                        Column(Modifier.width(320.dp).fillMaxHeight().background(AppStyle.Background).verticalScroll(rememberScrollState())) {
                            LegalReader(document){backs++}
                        }
                    }}
                }
            }
            for(theme in listOf(false,true))for(page in LegalDocument.entries) {
                ui.runOnIdle{dark=theme;document=page}
                val context=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
                val updated=org.json.JSONObject(context.assets.open(page.asset).bufferedReader().use{it.readText()}).getString("updated")
                ui.waitUntil(5000){ui.onAllNodesWithText("Updated $updated").fetchSemanticsNodes().isNotEmpty()}
                if(page==LegalDocument.PRIVACY)ui.onNodeWithText("YouTube Shorts & Custom Platform Tracking").assertExists()
                ui.onNodeWithText(page.title).performScrollTo().assertIsDisplayed()
                ui.onNodeWithText("Open public page").performScrollTo().assertIsDisplayed()
                if(page==LegalDocument.SUPPORT)ui.onNodeWithText("Email support").performScrollTo().assertIsDisplayed()
                ui.onNodeWithContentDescription("Back to Settings").performScrollTo().performClick()
            }
            ui.runOnIdle{assertEquals(8,backs)}
        } finally {ui.runOnUiThread{AppStyle.dark=original}}
    }
}
